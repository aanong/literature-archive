package com.literature.knowledge.service.splitter;

import com.literature.knowledge.model.TextChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TextSplitter 单元测试
 */
class TextSplitterTest {

    private TextSplitter textSplitter;

    @BeforeEach
    void setUp() {
        textSplitter = new TextSplitter();
    }

    // ===== 段落拆分测试 =====

    @Test
    void splitByParagraph_应正确按空行拆分() {
        String text = "道可道，非常道。\n名可名，非常名。\n\n无名天地之始。\n有名万物之母。\n\n故常无欲以观其妙。";
        List<TextChunk> chunks = textSplitter.splitByParagraph(text, "道德经");

        assertEquals(3, chunks.size());
        assertEquals("道德经 - 段落1", chunks.get(0).getTitle());
        assertTrue(chunks.get(0).getContent().contains("道可道"));
        assertTrue(chunks.get(1).getContent().contains("无名天地之始"));
        assertTrue(chunks.get(2).getContent().contains("故常无欲"));
    }

    @Test
    void splitByParagraph_空文本应返回空列表() {
        assertEquals(0, textSplitter.splitByParagraph("", "测试").size());
        assertEquals(0, textSplitter.splitByParagraph(null, "测试").size());
        assertEquals(0, textSplitter.splitByParagraph("   ", "测试").size());
    }

    @Test
    void splitByParagraph_单段文本应返回一块() {
        String text = "这是一段没有空行分隔的连续文本。";
        List<TextChunk> chunks = textSplitter.splitByParagraph(text, "测试书");

        assertEquals(1, chunks.size());
        assertEquals("测试书 - 段落1", chunks.get(0).getTitle());
    }

    // ===== 固定长度拆分测试 =====

    @Test
    void splitByFixedSize_应正确按长度拆分() {
        String text = "一二三四五六七八九十" + "壹贰叁肆伍陆柒捌玖拾";
        List<TextChunk> chunks = textSplitter.splitByFixedSize(text, "测试", 10, 0);

        assertEquals(2, chunks.size());
        assertEquals(10, chunks.get(0).getContent().length());
        assertEquals(10, chunks.get(1).getContent().length());
    }

    @Test
    void splitByFixedSize_应正确处理重叠窗口() {
        // 20个字符，块大小10，重叠3，步长7
        String text = "一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾";
        List<TextChunk> chunks = textSplitter.splitByFixedSize(text, "测试", 10, 3);

        // 0-9, 7-16, 14-19 → 3 块
        assertEquals(3, chunks.size());
        // 第一块和第二块应有重叠
        String first = chunks.get(0).getContent();
        String second = chunks.get(1).getContent();
        // 第一块末尾3个字和第二块开头3个字应相同
        assertEquals(first.substring(7), second.substring(0, 3));
    }

    @Test
    void splitByFixedSize_文本短于块大小应返回一块() {
        String text = "短文本";
        List<TextChunk> chunks = textSplitter.splitByFixedSize(text, "测试", 500, 50);

        assertEquals(1, chunks.size());
        assertEquals("短文本", chunks.get(0).getContent());
    }

    @Test
    void splitByFixedSize_空文本应返回空列表() {
        assertEquals(0, textSplitter.splitByFixedSize("", "测试", 500, 50).size());
    }

    // ===== 章节标记拆分测试 =====

    @Test
    void splitByChapterMarker_应正确识别第X章() {
        String text = """
                第一章 道可道
                道可道，非常道。名可名，非常名。

                第二章 天下皆知
                天下皆知美之为美，斯恶已。

                第三章 不尚贤
                不尚贤，使民不争。
                """;
        List<TextChunk> chunks = textSplitter.splitByChapterMarker(text, "道德经");

        assertEquals(3, chunks.size());
        assertTrue(chunks.get(0).getTitle().contains("第一章"));
        assertTrue(chunks.get(0).getContent().contains("道可道"));
        assertTrue(chunks.get(1).getTitle().contains("第二章"));
        assertTrue(chunks.get(2).getTitle().contains("第三章"));
    }

    @Test
    void splitByChapterMarker_应正确识别卷X() {
        String text = """
                序言内容
                卷一 创世
                盘古开天辟地。
                卷二 洪荒
                女娲补天。
                """;
        List<TextChunk> chunks = textSplitter.splitByChapterMarker(text, "神话故事");

        // 序言 + 卷一 + 卷二 = 3块
        assertEquals(3, chunks.size());
        assertTrue(chunks.get(0).getTitle().contains("序言"));
        assertTrue(chunks.get(1).getTitle().contains("卷一"));
        assertTrue(chunks.get(2).getTitle().contains("卷二"));
    }

    @Test
    void splitByChapterMarker_无标记应回退到段落拆分() {
        String text = "这是一段没有章节标记的普通文本。\n\n这是第二段。";
        List<TextChunk> chunks = textSplitter.splitByChapterMarker(text, "测试");

        // 应回退为段落拆分
        assertEquals(2, chunks.size());
    }

    @Test
    void splitByChapterMarker_应识别数字章节号() {
        String text = """
                第1章 开篇
                这是第一章的内容。
                第2章 续篇
                这是第二章的内容。
                """;
        List<TextChunk> chunks = textSplitter.splitByChapterMarker(text, "测试书");

        assertEquals(2, chunks.size());
        assertTrue(chunks.get(0).getTitle().contains("第1章"));
        assertTrue(chunks.get(1).getTitle().contains("第2章"));
    }
}
