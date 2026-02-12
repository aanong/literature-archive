package com.literature.knowledge.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.knowledge.model.BookIngestionRequest;
import com.literature.knowledge.model.BookIngestionResult;
import com.literature.knowledge.service.BookIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 书籍自动入库控制器
 * <p>
 * 提供文本自动拆分并导入知识库的接口。
 * </p>
 */
@RestController
@RequestMapping("/api/knowledge/ingest")
@RequiredArgsConstructor
public class BookIngestionController {

    private final BookIngestionService bookIngestionService;

    /**
     * 文本拆分并入库
     * <p>
     * 将一段文本按指定策略拆分为知识条目，并批量导入知识库。
     * </p>
     *
     * @param request 入库请求
     * @return 入库结果
     */
    @PostMapping
    public ApiResponse<BookIngestionResult> ingest(@RequestBody BookIngestionRequest request) {
        BookIngestionResult result = bookIngestionService.ingestFromText(request);
        return ApiResponse.success(result, null);
    }

    /**
     * 预览拆分结果（不入库）
     * <p>
     * 仅执行文本拆分并返回预览结果，不产生任何持久化操作。
     * 适合在正式入库前预览拆分效果。
     * </p>
     *
     * @param request 入库请求
     * @return 预览结果（包含拆分后的文本块列表）
     */
    @PostMapping("/preview")
    public ApiResponse<BookIngestionResult> preview(@RequestBody BookIngestionRequest request) {
        BookIngestionResult result = bookIngestionService.preview(request);
        return ApiResponse.success(result, null);
    }
}
