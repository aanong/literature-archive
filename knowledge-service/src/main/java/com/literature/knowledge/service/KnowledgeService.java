package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.entity.KnowledgeItem;
import com.literature.knowledge.mapper.KnowledgeItemMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.literature.knowledge.model.BatchImportRequest;
import com.literature.knowledge.model.BatchImportResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 典籍知识管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeItemMapper knowledgeItemMapper;
    private final EmbeddingService embeddingService;

    /**
     * 批量导入知识条目
     */
    @Transactional
    public BatchImportResult batchImport(BatchImportRequest request) {
        List<BatchImportRequest.KnowledgeItemDTO> items = request.getItems();
        List<Long> createdIds = new ArrayList<>();
        List<BatchImportResult.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            BatchImportRequest.KnowledgeItemDTO dto = items.get(i);
            try {
                KnowledgeItem item = new KnowledgeItem();
                item.setBookId(dto.getBookId());
                item.setChapterId(dto.getChapterId());
                item.setTitle(dto.getTitle());
                item.setContent(dto.getContent());
                item.setSourceText(dto.getSourceText());
                item.setCategory(dto.getCategory());
                item.setTags(dto.getTags());
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                item.setStatus(KnowledgeItem.Status.DRAFT);

                knowledgeItemMapper.insert(item);
                createdIds.add(item.getId());
            } catch (Exception e) {
                log.warn("批量导入第{}条失败: title={}, error={}", i, dto.getTitle(), e.getMessage());
                failures.add(BatchImportResult.FailureDetail.builder()
                        .index(i)
                        .title(dto.getTitle())
                        .reason(e.getMessage())
                        .build());
            }
        }

        // 如果开启了自动向量化，对成功导入的条目进行向量化
        int vectorizedCount = 0;
        if (request.isAutoVectorize()) {
            vectorizedCount = batchVectorize(createdIds);
        }

        log.info("批量导入完成: 总数={}, 成功={}, 失败={}, 向量化={}",
                items.size(), createdIds.size(), failures.size(), vectorizedCount);

        return BatchImportResult.builder()
                .totalSubmitted(items.size())
                .successCount(createdIds.size())
                .failedCount(failures.size())
                .vectorizedCount(vectorizedCount)
                .createdIds(createdIds)
                .failures(failures)
                .build();
    }

    /**
     * 批量向量化知识条目
     * 
     * @return 成功向量化的数量
     */
    public int batchVectorize(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            try {
                vectorizeKnowledgeItem(id);
                count++;
            } catch (Exception e) {
                log.warn("向量化失败: id={}, error={}", id, e.getMessage());
            }
        }
        log.info("批量向量化完成: 总数={}, 成功={}", ids.size(), count);
        return count;
    }

    /**
     * 创建知识条目
     */
    @Transactional
    public KnowledgeItem createKnowledgeItem(KnowledgeItem item) {
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        if (item.getStatus() == null) {
            item.setStatus(KnowledgeItem.Status.DRAFT);
        }
        knowledgeItemMapper.insert(item);
        return item;
    }

    /**
     * 获取知识条目详情
     */
    public Optional<KnowledgeItem> getKnowledgeItem(Long id) {
        return Optional.ofNullable(knowledgeItemMapper.selectById(id));
    }

    /**
     * 更新知识条目
     */
    @Transactional
    public KnowledgeItem updateKnowledgeItem(Long id, KnowledgeItem updatedItem) {
        KnowledgeItem item = knowledgeItemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("Knowledge item not found with id " + id);
        }

        item.setTitle(updatedItem.getTitle());
        item.setContent(updatedItem.getContent());
        item.setSourceText(updatedItem.getSourceText());
        item.setCategory(updatedItem.getCategory());
        item.setTags(updatedItem.getTags());
        item.setStatus(updatedItem.getStatus());
        item.setUpdatedAt(LocalDateTime.now());

        knowledgeItemMapper.updateById(item);
        return item;
    }

    /**
     * 删除知识条目
     */
    @Transactional
    public void deleteKnowledgeItem(Long id) {
        knowledgeItemMapper.deleteById(id);
    }

    /**
     * 根据书目查询
     */
    public List<KnowledgeItem> findByBookId(Long bookId) {
        return knowledgeItemMapper.selectList(
                new LambdaQueryWrapper<KnowledgeItem>().eq(KnowledgeItem::getBookId, bookId));
    }

    /**
     * 关键词搜索
     */
    public List<KnowledgeItem> search(String keyword) {
        // 先尝试全文搜索
        try {
            return knowledgeItemMapper.selectList(new LambdaQueryWrapper<KnowledgeItem>()
                    .apply("MATCH(title, content) AGAINST({0} IN NATURAL LANGUAGE MODE)", keyword));
        } catch (Exception e) {
            log.warn("全文搜索失败: {}", e.getMessage());
            // 降级为模糊查询
            return knowledgeItemMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeItem>()
                            .like(KnowledgeItem::getTitle, keyword)
                            .or()
                            .like(KnowledgeItem::getContent, keyword));
        }
    }

    /**
     * 向量化知识条目
     */
    @Transactional
    public void vectorizeKnowledgeItem(Long id) {
        KnowledgeItem item = knowledgeItemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("Knowledge item not found: " + id);
        }

        // 组合文本用于嵌入 (标题 + 内容)
        String textToEmbed = item.getTitle() + "\n\n" + item.getContent();

        // 创建元数据
        Metadata metadata = new Metadata();
        metadata.add("id", item.getId().toString());
        metadata.add("title", item.getTitle());
        metadata.add("category", item.getCategory());
        // 如果有标签
        if (item.getTags() != null && !item.getTags().isEmpty()) {
            metadata.add("tags", String.join(",", item.getTags()));
        }

        TextSegment segment = TextSegment.from(textToEmbed, metadata);

        // 存储并获取嵌入ID
        String embeddingId = embeddingService.store(segment);

        // 更新知识条目的嵌入ID
        item.setEmbeddingId(embeddingId);
        knowledgeItemMapper.updateById(item);
        log.info("知识条目已向量化: id={}, embeddingId={}", id, embeddingId);
    }

    public List<String> getAllTags() {
        return knowledgeItemMapper.findAllDistinctTags();
    }
}
