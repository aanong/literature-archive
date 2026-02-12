package com.literature.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 书籍入库配置
 * <p>
 * 通过 application.yml 中的 {@code book-ingestion} 前缀进行配置。
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "book-ingestion")
public class BookIngestionProperties {

    /**
     * 默认拆分策略: PARAGRAPH / FIXED_SIZE / CHAPTER_MARKER
     */
    private String defaultSplitStrategy = "PARAGRAPH";

    /**
     * FIXED_SIZE 模式的默认块大小（字符数）
     */
    private int defaultChunkSize = 500;

    /**
     * FIXED_SIZE 模式的默认重叠字符数
     */
    private int defaultChunkOverlap = 50;

    /**
     * 是否默认开启自动向量化
     */
    private boolean defaultAutoVectorize = false;

    /**
     * 单次入库最大允许文本长度（字符数，0表示不限制）
     */
    private int maxContentLength = 0;

    /**
     * 单次入库最大允许拆分块数（0表示不限制）
     */
    private int maxChunkCount = 0;
}
