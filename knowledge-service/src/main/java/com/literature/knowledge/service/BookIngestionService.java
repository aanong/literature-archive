package com.literature.knowledge.service;

import com.literature.knowledge.config.BookIngestionProperties;
import com.literature.knowledge.model.BatchImportRequest;
import com.literature.knowledge.model.BatchImportResult;
import com.literature.knowledge.model.BookIngestionRequest;
import com.literature.knowledge.model.BookIngestionResult;
import com.literature.knowledge.model.TextChunk;
import com.literature.knowledge.service.splitter.TextSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 书籍自动入库服务
 * <p>
 * 将一本书的文本内容自动拆分为知识条目，并批量导入到知识库中。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookIngestionService {

    private final TextSplitter textSplitter;
    private final KnowledgeService knowledgeService;
    private final BookIngestionProperties properties;
    private final AutoClassificationService autoClassificationService;
    private final com.literature.common.core.feign.ContentServiceClient contentServiceClient;

    /**
     * 基于纯文本拆分并导入知识库
     *
     * @param request 入库请求（包含文本、拆分策略等配置）
     * @return 入库结果
     */
    public BookIngestionResult ingestFromText(BookIngestionRequest request) {
        // 1. 参数校验
        validateRequest(request);

        // 1.5 用配置默认值填充空参数
        applyDefaults(request);

        // 1.8 自动分类
        if (request.isAutoClassify() && !StringUtils.hasText(request.getCategory())) {
            try {
                AutoClassificationService.ClassificationResult result = autoClassificationService
                        .classify(request.getContent());
                request.setCategory(result.getCategory());
                request.setTags(result.getTags());
                log.info("AI 自动分类完成: bookId={}, category={}, tags={}", request.getBookId(), result.getCategory(),
                        result.getTags());

                // 异步更新 content-service
                try {
                    com.literature.common.core.dto.BookDTO bookDTO = new com.literature.common.core.dto.BookDTO(
                            request.getBookId(),
                            null, null, null, null,
                            result.getCategory(),
                            com.literature.common.core.utils.JacksonUtils.toJsonString(result.getTags()));
                    contentServiceClient.updateBookMetadata(request.getBookId(), bookDTO);
                } catch (Exception e) {
                    log.error("更新书籍元数据失败", e);
                }

            } catch (Exception e) {
                log.warn("AI 自动分类失败，将跳过分类步骤", e);
            }
        }

        // 2. 拆分文本
        List<TextChunk> chunks = splitText(request);
        log.info("书籍拆分完成: bookId={}, 策略={}, 拆分块数={}",
                request.getBookId(), request.getSplitStrategy(), chunks.size());

        if (chunks.isEmpty()) {
            return BookIngestionResult.builder()
                    .bookId(request.getBookId())
                    .totalChunks(0)
                    .successCount(0)
                    .failedCount(0)
                    .vectorizedCount(0)
                    .build();
        }

        // 3. 转换为批量导入请求
        BatchImportRequest batchRequest = convertToBatchRequest(request, chunks);

        // 4. 调用已有的批量导入逻辑
        BatchImportResult batchResult = knowledgeService.batchImport(batchRequest);

        // 5. 构建返回结果
        return BookIngestionResult.builder()
                .bookId(request.getBookId())
                .totalChunks(chunks.size())
                .successCount(batchResult.getSuccessCount())
                .failedCount(batchResult.getFailedCount())
                .vectorizedCount(batchResult.getVectorizedCount())
                .createdIds(batchResult.getCreatedIds())
                .build();
    }

    /**
     * 预览拆分结果（不入库）
     *
     * @param request 入库请求
     * @return 仅包含拆分预览的结果
     */
    public BookIngestionResult preview(BookIngestionRequest request) {
        validateRequest(request);
        applyDefaults(request);

        List<TextChunk> chunks = splitText(request);
        log.info("预览拆分完成: bookId={}, 策略={}, 拆分块数={}",
                request.getBookId(), request.getSplitStrategy(), chunks.size());

        return BookIngestionResult.builder()
                .bookId(request.getBookId())
                .totalChunks(chunks.size())
                .successCount(0)
                .failedCount(0)
                .vectorizedCount(0)
                .previewChunks(chunks)
                .build();
    }

    /**
     * 根据策略拆分文本
     */
    private List<TextChunk> splitText(BookIngestionRequest request) {
        String bookTitle = StringUtils.hasText(request.getBookTitle())
                ? request.getBookTitle()
                : "未命名";

        return switch (request.getSplitStrategy()) {
            case PARAGRAPH -> textSplitter.splitByParagraph(request.getContent(), bookTitle);
            case FIXED_SIZE -> textSplitter.splitByFixedSize(
                    request.getContent(), bookTitle,
                    request.getChunkSize(), request.getChunkOverlap());
            case CHAPTER_MARKER -> textSplitter.splitByChapterMarker(request.getContent(), bookTitle);
        };
    }

    /**
     * 将拆分结果转换为批量导入请求
     */
    private BatchImportRequest convertToBatchRequest(BookIngestionRequest request, List<TextChunk> chunks) {
        List<BatchImportRequest.KnowledgeItemDTO> items = chunks.stream()
                .map(chunk -> {
                    BatchImportRequest.KnowledgeItemDTO dto = new BatchImportRequest.KnowledgeItemDTO();
                    dto.setBookId(request.getBookId());
                    dto.setTitle(chunk.getTitle());
                    dto.setContent(chunk.getContent());
                    dto.setSourceText(chunk.getSourceText());
                    dto.setCategory(request.getCategory());
                    dto.setTags(request.getTags());
                    return dto;
                })
                .collect(Collectors.toList());

        BatchImportRequest batchRequest = new BatchImportRequest();
        batchRequest.setItems(items);
        batchRequest.setAutoVectorize(request.isAutoVectorize());
        return batchRequest;
    }

    /**
     * 参数校验
     */
    private void validateRequest(BookIngestionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("入库请求不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("文本内容不能为空");
        }
        // 校验文本长度限制
        if (properties.getMaxContentLength() > 0
                && request.getContent().length() > properties.getMaxContentLength()) {
            throw new IllegalArgumentException(
                    String.format("文本内容超过最大长度限制: %d > %d",
                            request.getContent().length(), properties.getMaxContentLength()));
        }
    }

    /**
     * 用配置默认值填充请求中为空的参数
     */
    private void applyDefaults(BookIngestionRequest request) {
        if (request.getSplitStrategy() == null) {
            try {
                request.setSplitStrategy(
                        BookIngestionRequest.SplitStrategy.valueOf(properties.getDefaultSplitStrategy()));
            } catch (IllegalArgumentException e) {
                request.setSplitStrategy(BookIngestionRequest.SplitStrategy.PARAGRAPH);
            }
        }
        if (request.getChunkSize() == null || request.getChunkSize() <= 0) {
            request.setChunkSize(properties.getDefaultChunkSize());
        }
        if (request.getChunkOverlap() == null || request.getChunkOverlap() < 0) {
            request.setChunkOverlap(properties.getDefaultChunkOverlap());
        }
    }
}
