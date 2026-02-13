package com.literature.common.core.feign;

import com.literature.common.core.dto.BookDTO;
import com.literature.common.core.model.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 内容服务 Feign 客户端
 * 提供书籍元数据查询接口
 */
@FeignClient(
    name = "content-service",
    fallbackFactory = ContentServiceClientFallbackFactory.class
)
public interface ContentServiceClient {

    /**
     * 根据书籍ID获取书籍信息
     *
     * @param bookId 书籍ID
     * @return 书籍信息
     */
    @GetMapping("/api/internal/books/{bookId}")
    ApiResponse<BookDTO> getBookById(@PathVariable("bookId") Long bookId);

    /**
     * 批量获取书籍信息
     *
     * @param bookIds 书籍ID列表，逗号分隔
     * @return 书籍信息列表
     */
    @GetMapping("/api/internal/books/batch/{bookIds}")
    ApiResponse<List<BookDTO>> getBooksByIds(@PathVariable("bookIds") String bookIds);
}
