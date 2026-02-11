package com.literature.knowledge.model;

import lombok.Data;

import java.util.List;

/**
 * 批量导入请求
 */
@Data
public class BatchImportRequest {

    /**
     * 知识条目列表
     */
    private List<KnowledgeItemDTO> items;

    /**
     * 是否自动向量化（默认false，大批量导入时建议先导入再异步向量化）
     */
    private boolean autoVectorize = false;

    @Data
    public static class KnowledgeItemDTO {
        private Long bookId;
        private Long chapterId;
        private String title;
        private String content;
        private String sourceText;
        private String category;
        private List<String> tags;
    }
}
