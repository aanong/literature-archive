package com.literature.content.controller;

import com.literature.common.core.dto.BookDTO;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.ErrorCode;
import com.literature.content.entity.Book;
import com.literature.content.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内部书籍 API 控制器
 * 供其他微服务通过 Feign 调用
 */
@RestController
@RequestMapping("/api/internal/books")
public class InternalBookController {

    private final BookService bookService;

    public InternalBookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 根据书籍ID获取书籍信息
     *
     * @param bookId 书籍ID
     * @return 书籍信息
     */
    @GetMapping("/{bookId}")
    public ApiResponse<BookDTO> getBookById(
            @PathVariable("bookId") Long bookId,
            HttpServletRequest request) {
        
        Book book = bookService.getById(bookId);
        if (book == null) {
            return ApiResponse.error(
                ErrorCode.NOT_FOUND,
                "书籍不存在",
                request.getHeader("X-Trace-Id")
            );
        }
        return ApiResponse.success(toDTO(book), request.getHeader("X-Trace-Id"));
    }

    /**
     * 批量获取书籍信息
     *
     * @param bookIds 书籍ID列表，逗号分隔
     * @return 书籍信息列表
     */
    @GetMapping("/batch/{bookIds}")
    public ApiResponse<List<BookDTO>> getBooksByIds(
            @PathVariable("bookIds") String bookIds,
            HttpServletRequest request) {
        
        if (bookIds == null || bookIds.isBlank()) {
            return ApiResponse.success(Collections.emptyList(), request.getHeader("X-Trace-Id"));
        }

        List<Long> ids = Arrays.stream(bookIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), request.getHeader("X-Trace-Id"));
        }

        List<Book> books = bookService.listByIds(ids);
        List<BookDTO> dtos = books.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(dtos, request.getHeader("X-Trace-Id"));
    }

    /**
     * 将 Book 实体转换为 BookDTO
     */
    private BookDTO toDTO(Book book) {
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getEdition(),
            book.getStatus()
        );
    }
}
