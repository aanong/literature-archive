package com.literature.knowledge.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果
 */
@Data
@Builder
public class BatchImportResult {

    /**
     * 总提交数
     */
    private int totalSubmitted;

    /**
     * 成功导入数
     */
    private int successCount;

    /**
     * 失败数
     */
    private int failedCount;

    /**
     * 成功向量化数
     */
    private int vectorizedCount;

    /**
     * 成功创建的知识条目ID列表
     */
    @Builder.Default
    private List<Long> createdIds = new ArrayList<>();

    /**
     * 失败详情
     */
    @Builder.Default
    private List<FailureDetail> failures = new ArrayList<>();

    @Data
    @Builder
    public static class FailureDetail {
        private int index;
        private String title;
        private String reason;
    }
}
