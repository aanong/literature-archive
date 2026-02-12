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
  private final com.literature.content.service.VolumeService volumeService;
  private final com.literature.content.service.ChapterService chapterService;

  public BookController(BookService bookService,
      com.literature.content.service.VolumeService volumeService,
      com.literature.content.service.ChapterService chapterService) {
    this.bookService = bookService;
    this.volumeService = volumeService;
    this.chapterService = chapterService;
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
    wrapper.orderByDesc(Book::getCreateTime);

    bookService.page(bookPage, wrapper);
    return ApiResponse.success(new PageResponse<>(bookPage.getTotal(), bookPage.getRecords()), null);
  }

  @GetMapping("/{id}")
  public ApiResponse<Book> getBook(@PathVariable Long id) {
    Book book = bookService.getById(id);
    if (book == null) {
      return ApiResponse.error("404", "Book not found", null);
    }

    // 1. 获取卷列表
    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.literature.content.entity.Volume> volumeWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
    volumeWrapper.eq(com.literature.content.entity.Volume::getBookId, id);
    volumeWrapper.orderByAsc(com.literature.content.entity.Volume::getOrderNo);
    java.util.List<com.literature.content.entity.Volume> volumes = volumeService.list(volumeWrapper);

    if (!volumes.isEmpty()) {
      java.util.List<Long> volumeIds = volumes.stream().map(com.literature.content.entity.Volume::getId).toList();

      // 2. 获取章节列表 (排除 content 字段)
      com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.literature.content.entity.Chapter> chapterWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
      chapterWrapper.in(com.literature.content.entity.Chapter::getVolumeId, volumeIds);
      chapterWrapper.select(com.literature.content.entity.Chapter.class, info -> !info.getProperty().equals("content"));
      chapterWrapper.orderByAsc(com.literature.content.entity.Chapter::getOrderNo);
      java.util.List<com.literature.content.entity.Chapter> chapters = chapterService.list(chapterWrapper);

      // 3. 聚合
      java.util.Map<Long, java.util.List<com.literature.content.entity.Chapter>> chapterMap = chapters.stream()
          .collect(java.util.stream.Collectors.groupingBy(com.literature.content.entity.Chapter::getVolumeId));

      volumes.forEach(v -> v.setChapters(chapterMap.getOrDefault(v.getId(), java.util.Collections.emptyList())));
    }

    book.setVolumes(volumes);
    return ApiResponse.success(book, null);
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
