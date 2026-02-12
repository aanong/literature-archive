package com.literature.knowledge.service.splitter;

import com.literature.knowledge.model.TextChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本智能拆分器
 * <p>
 * 支持多种拆分策略，将长文本切分为适合向量化存储的知识片段。
 * </p>
 */
@Slf4j
@Component
public class TextSplitter {

    /**
     * 中文章节标记正则：匹配 "第一章"、"第1章"、"卷一"、"卷1"、"篇一" 等
     */
    private static final Pattern CHAPTER_MARKER_PATTERN = Pattern.compile(
            "(?m)^\\s*(第[零一二三四五六七八九十百千万\\d]+[章节篇回卷]|卷[零一二三四五六七八九十百千万\\d]+)\\s*[、：:\\s]?(.*)$");

    /**
     * 按段落拆分
     * <p>
     * 以连续空行（\n\n）为分隔符，适合古文逐段入库。
     * </p>
     *
     * @param text      原始文本
     * @param bookTitle 书名前缀（用于生成标题）
     * @return 拆分结果列表
     */
    public List<TextChunk> splitByParagraph(String text, String bookTitle) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        // 按连续空行拆分
        String[] paragraphs = text.split("\\n\\s*\\n");
        List<TextChunk> chunks = new ArrayList<>();

        int index = 0;
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            chunks.add(TextChunk.builder()
                    .index(index)
                    .title(generateParagraphTitle(bookTitle, index))
                    .content(trimmed)
                    .sourceText(trimmed)
                    .build());
            index++;
        }

        log.debug("按段落拆分完成: 原始长度={}, 拆分块数={}", text.length(), chunks.size());
        return chunks;
    }

    /**
     * 按固定长度拆分（带重叠窗口）
     * <p>
     * 适合长文本，保证每个块有上下文重叠以提升检索质量。
     * </p>
     *
     * @param text      原始文本
     * @param bookTitle 书名前缀
     * @param chunkSize 块大小（字符数）
     * @param overlap   重叠字符数
     * @return 拆分结果列表
     */
    public List<TextChunk> splitByFixedSize(String text, String bookTitle, int chunkSize, int overlap) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        if (chunkSize <= 0) {
            chunkSize = 500;
        }
        if (overlap < 0 || overlap >= chunkSize) {
            overlap = 0;
        }

        List<TextChunk> chunks = new ArrayList<>();
        int textLength = text.length();
        int step = chunkSize - overlap;
        int index = 0;

        for (int start = 0; start < textLength; start += step) {
            int end = Math.min(start + chunkSize, textLength);
            String chunkContent = text.substring(start, end).trim();
            if (chunkContent.isEmpty()) {
                continue;
            }
            chunks.add(TextChunk.builder()
                    .index(index)
                    .title(generateFixedSizeTitle(bookTitle, index))
                    .content(chunkContent)
                    .sourceText(chunkContent)
                    .build());
            index++;

            // 如果已到文末，停止
            if (end >= textLength) {
                break;
            }
        }

        log.debug("按固定长度拆分完成: 原始长度={}, 块大小={}, 重叠={}, 拆分块数={}",
                textLength, chunkSize, overlap, chunks.size());
        return chunks;
    }

    /**
     * 按中文章节标记拆分
     * <p>
     * 自动识别 "第X章"、"卷X"、"篇X"、"回X" 等中文章节标题进行切分。
     * </p>
     *
     * @param text      原始文本
     * @param bookTitle 书名前缀
     * @return 拆分结果列表
     */
    public List<TextChunk> splitByChapterMarker(String text, String bookTitle) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        Matcher matcher = CHAPTER_MARKER_PATTERN.matcher(text);
        List<TextChunk> chunks = new ArrayList<>();

        // 收集所有章节标记的位置
        List<int[]> markerPositions = new ArrayList<>();
        List<String> markerTitles = new ArrayList<>();

        while (matcher.find()) {
            markerPositions.add(new int[] { matcher.start(), matcher.end() });
            String markerTitle = matcher.group(1).trim();
            String subtitle = matcher.group(2) != null ? matcher.group(2).trim() : "";
            markerTitles.add(subtitle.isEmpty() ? markerTitle : markerTitle + " " + subtitle);
        }

        // 如果没有找到章节标记，回退到段落拆分
        if (markerPositions.isEmpty()) {
            log.info("未检测到章节标记，回退到段落拆分模式");
            return splitByParagraph(text, bookTitle);
        }

        // 处理第一个章节标记之前的内容（如有）
        int firstMarkerStart = markerPositions.get(0)[0];
        if (firstMarkerStart > 0) {
            String preContent = text.substring(0, firstMarkerStart).trim();
            if (!preContent.isEmpty()) {
                chunks.add(TextChunk.builder()
                        .index(0)
                        .title(bookTitle + " - 序言")
                        .content(preContent)
                        .sourceText(preContent)
                        .build());
            }
        }

        // 按章节切分
        for (int i = 0; i < markerPositions.size(); i++) {
            int contentStart = markerPositions.get(i)[1];
            int contentEnd = (i + 1 < markerPositions.size())
                    ? markerPositions.get(i + 1)[0]
                    : text.length();

            String chapterContent = text.substring(contentStart, contentEnd).trim();
            if (chapterContent.isEmpty()) {
                continue;
            }

            String chapterTitle = bookTitle + " - " + markerTitles.get(i);
            chunks.add(TextChunk.builder()
                    .index(chunks.size())
                    .title(chapterTitle)
                    .content(chapterContent)
                    .sourceText(chapterContent)
                    .build());
        }

        log.debug("按章节标记拆分完成: 检测到{}个章节标记, 拆分块数={}", markerPositions.size(), chunks.size());
        return chunks;
    }

    /**
     * 生成段落拆分标题
     */
    private String generateParagraphTitle(String bookTitle, int index) {
        String prefix = StringUtils.hasText(bookTitle) ? bookTitle + " - " : "";
        return prefix + "段落" + (index + 1);
    }

    /**
     * 生成固定长度拆分标题
     */
    private String generateFixedSizeTitle(String bookTitle, int index) {
        String prefix = StringUtils.hasText(bookTitle) ? bookTitle + " - " : "";
        return prefix + "片段" + (index + 1);
    }
}
