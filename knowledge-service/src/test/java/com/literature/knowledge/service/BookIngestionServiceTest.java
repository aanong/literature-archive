package com.literature.knowledge.service;

import com.literature.knowledge.config.BookIngestionProperties;
import com.literature.knowledge.model.BatchImportRequest;
import com.literature.knowledge.model.BatchImportResult;
import com.literature.knowledge.model.BookIngestionRequest;
import com.literature.knowledge.model.BookIngestionResult;
import com.literature.knowledge.service.splitter.TextSplitter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BookIngestionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class BookIngestionServiceTest {

    @Spy
    private TextSplitter textSplitter = new TextSplitter();

    @Mock
    private KnowledgeService knowledgeService;

    @Mock
    private BookIngestionProperties properties;

    @InjectMocks
    private BookIngestionService bookIngestionService;

    private BookIngestionRequest createTestRequest() {
        BookIngestionRequest request = new BookIngestionRequest();
        request.setBookId(1L);
        request.setBookTitle("道德经");
        request.setContent("道可道，非常道。\n\n名可名，非常名。\n\n无名天地之始。");
        request.setCategory("philosophy");
        request.setTags(Arrays.asList("道家", "老子"));
        request.setSplitStrategy(BookIngestionRequest.SplitStrategy.PARAGRAPH);
        request.setAutoVectorize(false);
        return request;
    }

    @Test
    void ingestFromText_应成功拆分并导入() {
        BookIngestionRequest request = createTestRequest();

        // 模拟批量导入成功
        BatchImportResult mockBatchResult = BatchImportResult.builder()
                .totalSubmitted(3)
                .successCount(3)
                .failedCount(0)
                .vectorizedCount(0)
                .createdIds(List.of(1L, 2L, 3L))
                .build();
        when(knowledgeService.batchImport(any(BatchImportRequest.class))).thenReturn(mockBatchResult);

        BookIngestionResult result = bookIngestionService.ingestFromText(request);

        assertEquals(1L, result.getBookId());
        assertEquals(3, result.getTotalChunks());
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(3, result.getCreatedIds().size());

        // 验证确实调用了批量导入
        ArgumentCaptor<BatchImportRequest> captor = ArgumentCaptor.forClass(BatchImportRequest.class);
        verify(knowledgeService).batchImport(captor.capture());

        BatchImportRequest captured = captor.getValue();
        assertEquals(3, captured.getItems().size());
        assertEquals(1L, captured.getItems().get(0).getBookId());
        assertEquals("philosophy", captured.getItems().get(0).getCategory());
        assertFalse(captured.isAutoVectorize());
    }

    @Test
    void ingestFromText_开启自动向量化_应传递到批量导入() {
        BookIngestionRequest request = createTestRequest();
        request.setAutoVectorize(true);

        BatchImportResult mockBatchResult = BatchImportResult.builder()
                .totalSubmitted(3)
                .successCount(3)
                .failedCount(0)
                .vectorizedCount(3)
                .createdIds(List.of(1L, 2L, 3L))
                .build();
        when(knowledgeService.batchImport(any(BatchImportRequest.class))).thenReturn(mockBatchResult);

        BookIngestionResult result = bookIngestionService.ingestFromText(request);

        assertEquals(3, result.getVectorizedCount());

        ArgumentCaptor<BatchImportRequest> captor = ArgumentCaptor.forClass(BatchImportRequest.class);
        verify(knowledgeService).batchImport(captor.capture());
        assertTrue(captor.getValue().isAutoVectorize());
    }

    @Test
    void preview_应返回拆分结果而不调用批量导入() {
        BookIngestionRequest request = createTestRequest();

        BookIngestionResult result = bookIngestionService.preview(request);

        assertEquals(1L, result.getBookId());
        assertEquals(3, result.getTotalChunks());
        assertEquals(3, result.getPreviewChunks().size());
        assertEquals(0, result.getSuccessCount());

        // 验证没有调用批量导入
        verify(knowledgeService, never()).batchImport(any());
    }

    @Test
    void ingestFromText_空文本应抛出异常() {
        BookIngestionRequest request = createTestRequest();
        request.setContent("");

        assertThrows(IllegalArgumentException.class,
                () -> bookIngestionService.ingestFromText(request));
        verify(knowledgeService, never()).batchImport(any());
    }

    @Test
    void ingestFromText_null请求应抛出异常() {
        assertThrows(IllegalArgumentException.class,
                () -> bookIngestionService.ingestFromText(null));
    }

    @Test
    void ingestFromText_使用FIXED_SIZE策略应正确拆分() {
        BookIngestionRequest request = createTestRequest();
        request.setContent("这是一段很长的测试文本，需要按固定长度来拆分。" +
                "这里是第二部分内容。" +
                "第三部分也需要被拆分出来。");
        request.setSplitStrategy(BookIngestionRequest.SplitStrategy.FIXED_SIZE);
        request.setChunkSize(20);
        request.setChunkOverlap(5);

        BatchImportResult mockBatchResult = BatchImportResult.builder()
                .totalSubmitted(3)
                .successCount(3)
                .failedCount(0)
                .vectorizedCount(0)
                .createdIds(List.of(1L, 2L, 3L))
                .build();
        when(knowledgeService.batchImport(any(BatchImportRequest.class))).thenReturn(mockBatchResult);

        BookIngestionResult result = bookIngestionService.ingestFromText(request);

        assertTrue(result.getTotalChunks() > 1);
        verify(knowledgeService).batchImport(any());
    }

    @Test
    void ingestFromText_使用CHAPTER_MARKER策略应正确拆分() {
        BookIngestionRequest request = createTestRequest();
        request.setContent("第一章 道可道\n道可道，非常道。\n第二章 天下皆知\n天下皆知美之为美。");
        request.setSplitStrategy(BookIngestionRequest.SplitStrategy.CHAPTER_MARKER);

        BatchImportResult mockBatchResult = BatchImportResult.builder()
                .totalSubmitted(2)
                .successCount(2)
                .failedCount(0)
                .vectorizedCount(0)
                .createdIds(List.of(1L, 2L))
                .build();
        when(knowledgeService.batchImport(any(BatchImportRequest.class))).thenReturn(mockBatchResult);

        BookIngestionResult result = bookIngestionService.ingestFromText(request);

        assertEquals(2, result.getTotalChunks());
        verify(knowledgeService).batchImport(any());
    }
}
