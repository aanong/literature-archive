package com.literature.content.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.content.entity.Chapter;
import com.literature.content.service.ChapterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/{id}")
    public ApiResponse<Chapter> getChapter(@PathVariable Long id) {
        return ApiResponse.success(chapterService.getById(id), null);
    }
}
