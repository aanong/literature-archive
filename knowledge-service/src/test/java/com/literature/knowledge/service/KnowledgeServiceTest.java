package com.literature.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.knowledge.entity.KnowledgeItem;
import com.literature.knowledge.mapper.KnowledgeItemMapper;
import com.literature.knowledge.model.BatchImportRequest;
import com.literature.knowledge.model.BatchImportResult;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock
    private KnowledgeItemMapper knowledgeItemMapper;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private KnowledgeService knowledgeService;

    private KnowledgeItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new KnowledgeItem();
        testItem.setId(1L);
        testItem.setTitle("Test Title");
        testItem.setContent("Test Content");
        testItem.setTags(Arrays.asList("tag1", "tag2"));
        testItem.setStatus(KnowledgeItem.Status.DRAFT);
    }

    @Test
    void createKnowledgeItem_ShouldInsertAndReturn() {
        when(knowledgeItemMapper.insert(any(KnowledgeItem.class))).thenReturn(1);

        KnowledgeItem created = knowledgeService.createKnowledgeItem(testItem);

        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertEquals(KnowledgeItem.Status.DRAFT, created.getStatus());
        verify(knowledgeItemMapper).insert(testItem);
    }

    @Test
    void getKnowledgeItem_ShouldReturnItem_WhenFound() {
        when(knowledgeItemMapper.selectById(1L)).thenReturn(testItem);

        Optional<KnowledgeItem> result = knowledgeService.getKnowledgeItem(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Title", result.get().getTitle());
    }

    @Test
    void updateKnowledgeItem_ShouldUpdateFieldsAndReturn() {
        KnowledgeItem updatedInfo = new KnowledgeItem();
        updatedInfo.setTitle("Updated Title");
        updatedInfo.setContent("Updated Content");

        when(knowledgeItemMapper.selectById(1L)).thenReturn(testItem);
        when(knowledgeItemMapper.updateById(any(KnowledgeItem.class))).thenReturn(1);

        KnowledgeItem result = knowledgeService.updateKnowledgeItem(1L, updatedInfo);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        assertNotNull(result.getUpdatedAt());
        verify(knowledgeItemMapper).updateById(testItem);
    }

    @Test
    void deleteKnowledgeItem_ShouldDeleteById() {
        when(knowledgeItemMapper.deleteById(1L)).thenReturn(1);

        knowledgeService.deleteKnowledgeItem(1L);

        verify(knowledgeItemMapper).deleteById(1L);
    }

    @Test
    void vectorizeKnowledgeItem_ShouldStoreEmbeddingAndUpdateItem() {
        when(knowledgeItemMapper.selectById(1L)).thenReturn(testItem);
        when(embeddingService.store(any(TextSegment.class))).thenReturn("embedding-123");
        when(knowledgeItemMapper.updateById(any(KnowledgeItem.class))).thenReturn(1);

        knowledgeService.vectorizeKnowledgeItem(1L);

        assertEquals("embedding-123", testItem.getEmbeddingId());
        verify(embeddingService).store(any(TextSegment.class));
        verify(knowledgeItemMapper).updateById(testItem);
    }

    @Test
    void batchImport_ShouldInsertAllItems() {
        BatchImportRequest request = new BatchImportRequest();
        BatchImportRequest.KnowledgeItemDTO dto1 = new BatchImportRequest.KnowledgeItemDTO();
        dto1.setTitle("赤壁之战");
        dto1.setContent("建安十三年，曹操率军南下...");
        dto1.setCategory("history");
        dto1.setTags(Arrays.asList("三国", "曹操", "赤壁"));

        BatchImportRequest.KnowledgeItemDTO dto2 = new BatchImportRequest.KnowledgeItemDTO();
        dto2.setTitle("玄武门之变");
        dto2.setContent("武德九年六月初四...");
        dto2.setCategory("history");
        dto2.setTags(Arrays.asList("唐朝", "李世民"));

        request.setItems(Arrays.asList(dto1, dto2));
        request.setAutoVectorize(false);

        when(knowledgeItemMapper.insert(any(KnowledgeItem.class))).thenReturn(1);

        BatchImportResult result = knowledgeService.batchImport(request);

        assertEquals(2, result.getTotalSubmitted());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(0, result.getVectorizedCount());
        verify(knowledgeItemMapper, times(2)).insert(any(KnowledgeItem.class));
    }

    @Test
    void batchImport_WithAutoVectorize_ShouldVectorizeAfterImport() {
        BatchImportRequest request = new BatchImportRequest();
        BatchImportRequest.KnowledgeItemDTO dto = new BatchImportRequest.KnowledgeItemDTO();
        dto.setTitle("司马迁与《史记》");
        dto.setContent("司马迁，字子长...");
        dto.setCategory("history");
        request.setItems(List.of(dto));
        request.setAutoVectorize(true);

        KnowledgeItem insertedItem = new KnowledgeItem();
        insertedItem.setId(10L);
        insertedItem.setTitle("司马迁与《史记》");
        insertedItem.setContent("司马迁，字子长...");
        insertedItem.setCategory("history");

        when(knowledgeItemMapper.insert(any(KnowledgeItem.class))).thenAnswer(invocation -> {
            KnowledgeItem item = invocation.getArgument(0);
            item.setId(10L);
            return 1;
        });
        when(knowledgeItemMapper.selectById(10L)).thenReturn(insertedItem);
        when(embeddingService.store(any(TextSegment.class))).thenReturn("vec-456");
        when(knowledgeItemMapper.updateById(any(KnowledgeItem.class))).thenReturn(1);

        BatchImportResult result = knowledgeService.batchImport(request);

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getVectorizedCount());
        verify(embeddingService).store(any(TextSegment.class));
    }
}
