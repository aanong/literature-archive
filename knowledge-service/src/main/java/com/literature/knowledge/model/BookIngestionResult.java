package com.literature.knowledge.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 书籍入库结果
 */
@Data
@Builder
public class BookIngestionResult {

    /**
     * 关联的书籍ID
     */
    private Long bookId;

    /**
     * 拆分出的总块数
     */
    private int totalChunks;

    /**
     * 成功导入数
     */
    private int successCount;

    /**
     * 失败数
     */
    private int failedCount;

    /**
     * 已向量化数
     */
    private int vectorizedCount;

    /**
     * 创建的知识条目ID列表
     */
    @Builder.Default
    private List<Long> createdIds = new ArrayList<>();

    /**
     * 拆分预览（仅预览模式返回）
     */
    @Builder.Default
    private List<TextChunk> previewChunks = new ArrayList<>();
}
