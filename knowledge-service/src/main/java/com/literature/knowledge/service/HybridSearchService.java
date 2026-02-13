package com.literature.knowledge.service;

import com.literature.knowledge.entity.KnowledgeItem;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合搜索服务
 * <p>
 * 结合向量语义搜索 (Milvus) 与关键词搜索 (MySQL) 的双路检索，
 * 使用 RRF (Reciprocal Rank Fusion) 算法合并排序，提供更高质量的搜索结果。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HybridSearchService {

    private final EmbeddingService embeddingService;
    private final KnowledgeService knowledgeService;

    /**
     * RRF 常量 k，用于平滑排名差异
     */
    private static final int RRF_K = 60;

    /**
     * 执行混合搜索
     *
     * @param query          搜索查询文本
     * @param maxResults     最大返回结果数
     * @param semanticWeight 语义搜索权重 (0.0~1.0)，关键词搜索权重为 1 - semanticWeight
     * @return 混合搜索结果列表
     */
    public List<HybridSearchResult> search(String query, int maxResults, double semanticWeight) {
        // 限制权重范围
        semanticWeight = Math.max(0.0, Math.min(1.0, semanticWeight));
        double keywordWeight = 1.0 - semanticWeight;

        // 1. 语义搜索 (Milvus)
        List<SemanticHit> semanticHits = performSemanticSearch(query, maxResults * 2);

        // 2. 关键词搜索 (MySQL)
        List<KeywordHit> keywordHits = performKeywordSearch(query, maxResults * 2);

        // 3. RRF 合并排序
        Map<Long, HybridSearchResult.HybridSearchResultBuilder> resultMap = new LinkedHashMap<>();

        // 处理语义搜索结果
        for (int rank = 0; rank < semanticHits.size(); rank++) {
            SemanticHit hit = semanticHits.get(rank);
            if (hit.knowledgeItemId == null) {
                continue;
            }
            double rrfScore = semanticWeight * (1.0 / (RRF_K + rank + 1));
            resultMap.computeIfAbsent(hit.knowledgeItemId, id -> HybridSearchResult.builder()
                    .knowledgeItemId(id)
                    .title(hit.title)
                    .contentSnippet(hit.textSnippet)
                    .semanticScore(hit.score)).semanticRrfScore(rrfScore);
        }

        // 处理关键词搜索结果
        for (int rank = 0; rank < keywordHits.size(); rank++) {
            KeywordHit hit = keywordHits.get(rank);
            double rrfScore = keywordWeight * (1.0 / (RRF_K + rank + 1));
            HybridSearchResult.HybridSearchResultBuilder builder = resultMap.computeIfAbsent(hit.id,
                    id -> HybridSearchResult.builder()
                            .knowledgeItemId(id)
                            .title(hit.title)
                            .contentSnippet(truncateContent(hit.content, 200)));
            builder.keywordRrfScore(rrfScore);
            // 如果语义搜索未覆盖到，补充字段
            if (builder.build().getTitle() == null) {
                builder.title(hit.title);
                builder.contentSnippet(truncateContent(hit.content, 200));
            }
        }

        // 4. 计算综合分数并排序
        return resultMap.values().stream()
                .map(builder -> {
                    HybridSearchResult result = builder.build();
                    double combinedScore = (result.getSemanticRrfScore() != null ? result.getSemanticRrfScore() : 0.0)
                            + (result.getKeywordRrfScore() != null ? result.getKeywordRrfScore() : 0.0);
                    result.setCombinedScore(combinedScore);
                    // 判断命中来源
                    boolean hitSemantic = result.getSemanticRrfScore() != null && result.getSemanticRrfScore() > 0;
                    boolean hitKeyword = result.getKeywordRrfScore() != null && result.getKeywordRrfScore() > 0;
                    if (hitSemantic && hitKeyword) {
                        result.setSource("hybrid");
                    } else if (hitSemantic) {
                        result.setSource("semantic");
                    } else {
                        result.setSource("keyword");
                    }
                    return result;
                })
                .sorted(Comparator.comparingDouble(HybridSearchResult::getCombinedScore).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * 使用默认权重 (0.7 语义 + 0.3 关键词) 执行混合搜索
     */
    public List<HybridSearchResult> search(String query, int maxResults) {
        return search(query, maxResults, 0.7);
    }

    /**
     * 执行语义搜索
     */
    private List<SemanticHit> performSemanticSearch(String query, int maxResults) {
        try {
            List<EmbeddingMatch<TextSegment>> matches = embeddingService.search(query, maxResults, 0.5);
            return matches.stream()
                    .map(match -> {
                        SemanticHit hit = new SemanticHit();
                        hit.score = match.score();
                        hit.textSnippet = match.embedded() != null ? truncateContent(match.embedded().text(), 200) : "";
                        // 从元数据中提取知识条目 ID
                        if (match.embedded() != null && match.embedded().metadata() != null) {
                            String idStr = match.embedded().metadata().get("id");
                            if (idStr != null) {
                                try {
                                    hit.knowledgeItemId = Long.parseLong(idStr);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            hit.title = match.embedded().metadata().get("title");
                        }
                        return hit;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("语义搜索失败，降级为仅关键词搜索: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 执行关键词搜索
     */
    private List<KeywordHit> performKeywordSearch(String query, int maxResults) {
        try {
            List<KnowledgeItem> items = knowledgeService.search(query);
            return items.stream()
                    .limit(maxResults)
                    .map(item -> {
                        KeywordHit hit = new KeywordHit();
                        hit.id = item.getId();
                        hit.title = item.getTitle();
                        hit.content = item.getContent();
                        return hit;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("关键词搜索失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 截断文本内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    // ==================== 内部数据结构 ====================

    private static class SemanticHit {
        Long knowledgeItemId;
        String title;
        String textSnippet;
        double score;
    }

    private static class KeywordHit {
        Long id;
        String title;
        String content;
    }

    /**
     * 混合搜索结果
     */
    @Data
    @Builder
    public static class HybridSearchResult {
        /** 知识条目 ID */
        private Long knowledgeItemId;
        /** 标题 */
        private String title;
        /** 内容摘要 */
        private String contentSnippet;
        /** 语义搜索原始分数 */
        private Double semanticScore;
        /** 语义搜索 RRF 分数 */
        private Double semanticRrfScore;
        /** 关键词搜索 RRF 分数 */
        private Double keywordRrfScore;
        /** 综合分数 */
        private Double combinedScore;
        /** 命中来源: semantic, keyword, hybrid */
        private String source;
    }
}
