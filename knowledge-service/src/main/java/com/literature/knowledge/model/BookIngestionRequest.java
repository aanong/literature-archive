package com.literature.knowledge.model;

import lombok.Data;

import java.util.List;

/**
 * 书籍入库请求
 */
@Data
public class BookIngestionRequest {

    /**
     * 关联的书籍ID（来自 content-service）
     */
    private Long bookId;

    /**
     * 书名（用于生成知识点标题前缀）
     */
    private String bookTitle;

    /**
     * 原始文本内容（纯文本模式使用）
     */
    private String content;

    /**
     * 分类（如 philosophy、history、literature）
     */
    private String category;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 拆分策略，为 null 时使用 application.yml 中配置的默认值
     */
    private SplitStrategy splitStrategy;

    /**
     * FIXED_SIZE 模式的块大小（字符数），为 null 时使用配置默认值
     */
    private Integer chunkSize;

    /**
     * FIXED_SIZE 模式的重叠字符数，为 null 时使用配置默认值
     */
    private Integer chunkOverlap;

    /**
     * 是否自动向量化（默认false，大批量时建议先导入再异步向量化）
     */
    private boolean autoVectorize = false;

    /**
     * 拆分策略枚举
     */
    public enum SplitStrategy {
        /**
         * 按段落拆分（以空行分隔）
         */
        PARAGRAPH,

        /**
         * 按固定长度拆分（可配置重叠窗口）
         */
        FIXED_SIZE,

        /**
         * 按章节标记拆分（自动识别 "第X章"、"卷X" 等）
         */
        CHAPTER_MARKER
    }
}
