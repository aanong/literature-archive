package com.literature.knowledge.controller;

import com.literature.knowledge.entity.PopularScienceArticle;
import com.literature.knowledge.service.PopularScienceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科普文章控制器
 */
@RestController
@RequestMapping("/api/popular-science")
@RequiredArgsConstructor
public class PopularScienceController {

    private final PopularScienceService popularScienceService;

    /**
     * 生成文章
     */
    @PostMapping("/generate")
    public ResponseEntity<PopularScienceArticle> generate(@RequestBody GenerateRequest request) {
        return ResponseEntity.ok(popularScienceService.generateArticle(request.getTopic(), request.getRequirement()));
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<PopularScienceArticle> getArticle(@PathVariable("id") Long id) {
        return popularScienceService.getArticle(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取文章列表
     */
    @GetMapping("/articles")
    public ResponseEntity<List<PopularScienceArticle>> listArticles() {
        return ResponseEntity.ok(popularScienceService.getAllArticles());
    }

    /**
     * 发布文章
     */
    @PostMapping("/articles/{id}/publish")
    public ResponseEntity<PopularScienceArticle> publish(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(popularScienceService.publishArticle(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Data
    public static class GenerateRequest {
        private String topic;
        private String requirement;
    }
}
