package com.literature.content.util;

import com.literature.content.entity.Chapter;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 章节解析工具类
 */
public class ChapterParser {

    // 匹配 "第xxx章"、"Chapter xxx" 等常见章节标题模式
    // 允许前面有少量空白字符，后面有标题内容
    private static final Pattern CHAPTER_PATTERN = Pattern
            .compile("^\\s*(第[0-9零一二三四五六七八九十百千]+[章节卷集部篇回]|Chapter\\s+\\d+).*");

    /**
     * 解析文本内容为章节列表
     *
     * @param content 书籍完整文本
     * @return 章节列表
     */
    public static List<Chapter> parse(String content) {
        List<Chapter> chapters = new ArrayList<>();
        if (!StringUtils.hasText(content)) {
            return chapters;
        }

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            Chapter currentChapter = null;
            StringBuilder currentContent = new StringBuilder();
            int order = 1;

            // 处理开头的序言或前言（在第一个章节标题之前的内容）
            Chapter preamble = new Chapter();
            preamble.setTitle("序言/前言");
            preamble.setOrderNo(0);
            StringBuilder preambleContent = new StringBuilder();

            boolean firstChapterFound = false;

            while ((line = reader.readLine()) != null) {
                // 判断是否为新章节标题
                if (isChapterTitle(line)) {
                    // 保存上一个章节
                    if (firstChapterFound) {
                        if (currentChapter != null) {
                            currentChapter.setContent(currentContent.toString().trim());
                            chapters.add(currentChapter);
                        }
                    } else {
                        // 保存序言（如果有内容）
                        if (preambleContent.length() > 0) {
                            preamble.setContent(preambleContent.toString().trim());
                            // 只有当序言有实际内容时才添加
                            if (StringUtils.hasText(preamble.getContent())) {
                                chapters.add(preamble);
                            }
                        }
                        firstChapterFound = true;
                    }

                    // 开始新章节
                    currentChapter = new Chapter();
                    currentChapter.setTitle(line.trim());
                    currentChapter.setOrderNo(order++);
                    currentChapter.setStatus("PUBLISHED"); // 默认状态
                    currentContent = new StringBuilder();
                } else {
                    // 累加内容
                    if (firstChapterFound) {
                        currentContent.append(line).append("\n");
                    } else {
                        preambleContent.append(line).append("\n");
                    }
                }
            }

            // 保存最后一个章节
            if (currentChapter != null) {
                currentChapter.setContent(currentContent.toString().trim());
                chapters.add(currentChapter);
            } else if (!firstChapterFound && preambleContent.length() > 0) {
                // 如果整本书没有章节标题，则全部视为一个章节（或正文）
                Chapter fullContent = new Chapter();
                fullContent.setTitle("正文");
                fullContent.setOrderNo(1);
                fullContent.setStatus("PUBLISHED");
                fullContent.setContent(preambleContent.toString().trim());
                chapters.add(fullContent);
            }

        } catch (IOException e) {
            // StringReader 一般不会抛出 IOException，但为了规范处理
            throw new RuntimeException("解析书籍内容出错", e);
        }

        return chapters;
    }

    private static boolean isChapterTitle(String line) {
        if (!StringUtils.hasText(line)) {
            return false;
        }
        // 限制标题长度，避免误判长句子
        if (line.length() > 100) {
            return false;
        }
        Matcher matcher = CHAPTER_PATTERN.matcher(line);
        return matcher.matches();
    }
}
