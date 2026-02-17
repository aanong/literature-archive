package com.literature.knowledge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 自动分类服务
 * 利用 AI 对书籍内容进行分析，生成分类和标签
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoClassificationService {

    private final LangChainService langChainService;
    private final ObjectMapper objectMapper;

    /**
     * 分析文本内容并生成分类和标签
     *
     * @param content 书籍文本片段（建议截取前几千字）
     * @return 分类结果
     */
    public ClassificationResult classify(String content) {
        if (!StringUtils.hasText(content)) {
            return new ClassificationResult("未分类", Collections.emptyList());
        }

        // 截取适量长度，避免超过 token 限制
        String input = content.length() > 3000 ? content.substring(0, 3000) : content;

        String prompt = """
                你是一个专业的图书分类专家。请阅读以下书籍内容片段，并提取出最精确的1个分类和5个标签。

                要求：
                1. 返回格式必须是严格的 JSON。
                2. JSON 结构如下：{"category": "分类名称", "tags": ["标签1", "标签2", ...]}
                3. 分类应尽量使用通用的图书分类（如：科幻小说、历史传记、计算机技术、古典文学等）。
                4. 标签应具体反映书中的主题、风格或关键元素。
                5. 不要返回任何 JSON 以外的解释性文字。

                书籍内容片段：
                %s
                """.formatted(input);

        try {
            String response = langChainService.chat(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("AI 自动分类失败", e);
            return new ClassificationResult("未分类", Collections.emptyList());
        }
    }

    private ClassificationResult parseResponse(String response) {
        try {
            // 尝试清理可能的 markdown 代码块标记
            String json = response.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode root = objectMapper.readTree(json);

            String category = root.path("category").asText("未分类");
            List<String> tags = Collections.emptyList();

            JsonNode tagsNode = root.path("tags");
            if (tagsNode.isArray()) {
                tags = objectMapper.convertValue(tagsNode, List.class);
            }

            return new ClassificationResult(category, tags);
        } catch (JsonProcessingException e) {
            log.error("解析 AI 响应 JSON 失败: {}", response, e);
            // 简单的回退策略：如果解析失败，尝试从文本中直接提取（这里暂略，直接返回失败）
            return new ClassificationResult("未分类", Collections.emptyList());
        }
    }

    @Data
    public static class ClassificationResult {
        private final String category;
        private final List<String> tags;
    }
}
