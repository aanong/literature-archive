package com.literature.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本拆分结果块
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextChunk {

    /**
     * 块序号（从0开始）
     */
    private int index;

    /**
     * 自动生成的标题
     */
    private String title;

    /**
     * 拆分后的内容
     */
    private String content;

    /**
     * 原文（可选）
     */
    private String sourceText;
}
