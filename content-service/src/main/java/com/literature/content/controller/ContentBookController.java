package com.literature.content.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.common.core.model.ApiResponse;
import com.literature.content.entity.Book;
import com.literature.content.entity.Chapter;
import com.literature.content.entity.Volume;
import com.literature.content.service.BookService;
import com.literature.content.service.ChapterService;
import com.literature.content.service.VolumeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content/books")
public class ContentBookController {

    private final BookService bookService;
    private final VolumeService volumeService;
    private final ChapterService chapterService;

    public ContentBookController(BookService bookService,
                                  VolumeService volumeService,
                                  ChapterService chapterService) {
        this.bookService = bookService;
        this.volumeService = volumeService;
        this.chapterService = chapterService;
    }

    @GetMapping
    public ApiResponse<List<Book>> listPublishedBooks() {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, "published");
        wrapper.orderByDesc(Book::getCreatedAt);
        return ApiResponse.success(bookService.list(wrapper), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> getPublishedBook(@PathVariable Long id) {
        Book book = bookService.getById(id);
        if (book == null || !"published".equals(book.getStatus())) {
            return ApiResponse.error("404", "Book not found", null);
        }

        LambdaQueryWrapper<Volume> volumeWrapper = new LambdaQueryWrapper<>();
        volumeWrapper.eq(Volume::getBookId, id);
        volumeWrapper.orderByAsc(Volume::getOrderNo);
        List<Volume> volumes = volumeService.list(volumeWrapper);

        if (!volumes.isEmpty()) {
            List<Long> volumeIds = volumes.stream().map(Volume::getId).toList();

            LambdaQueryWrapper<Chapter> chapterWrapper = new LambdaQueryWrapper<>();
            chapterWrapper.in(Chapter::getVolumeId, volumeIds);
            chapterWrapper.select(Chapter.class, info -> !info.getProperty().equals("content"));
            chapterWrapper.orderByAsc(Chapter::getOrderNo);
            List<Chapter> chapters = chapterService.list(chapterWrapper);

            Map<Long, List<Chapter>> chapterMap = chapters.stream()
                    .collect(Collectors.groupingBy(Chapter::getVolumeId));

            volumes.forEach(v -> v.setChapters(chapterMap.getOrDefault(v.getId(), Collections.emptyList())));
        }

        book.setVolumes(volumes);
        return ApiResponse.success(book, null);
    }
}
