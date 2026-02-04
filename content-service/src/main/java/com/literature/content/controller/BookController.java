package com.literature.content.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.PageResponse;
import com.literature.content.entity.Book;
import com.literature.content.service.BookService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @PostMapping
  public ApiResponse<Book> createBook(@RequestBody Book book) {
    bookService.save(book);
    return ApiResponse.success(book, null);
  }

  @GetMapping
  public ApiResponse<PageResponse<Book>> listBooks(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {

    Page<Book> bookPage = new Page<>(page, pageSize);
    LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.like(Book::getTitle, keyword);
    }
    if (StringUtils.hasText(status)) {
      wrapper.eq(Book::getStatus, status);
    }

    bookService.page(bookPage, wrapper);
    return ApiResponse.success(new PageResponse<>(bookPage.getTotal(), bookPage.getRecords()), null);
  }

  @GetMapping("/{id}")
  public ApiResponse<Book> getBook(@PathVariable Long id) {
    return ApiResponse.success(bookService.getById(id), null);
  }

  @PutMapping("/{id}")
  public ApiResponse<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
    book.setId(id);
    bookService.updateById(book);
    return ApiResponse.success(book, null);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> deleteBook(@PathVariable Long id) {
    return ApiResponse.success(bookService.removeById(id), null);
  }
}
