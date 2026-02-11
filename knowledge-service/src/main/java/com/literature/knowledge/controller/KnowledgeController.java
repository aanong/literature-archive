package com.literature.knowledge.controller;

import com.literature.knowledge.entity.KnowledgeItem;
import com.literature.knowledge.model.BatchImportRequest;
import com.literature.knowledge.model.BatchImportResult;
import com.literature.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 典籍知识管理控制器
 */
@RestController
@RequestMapping("/api/knowledge/items")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 创建知识条目
     */
    @PostMapping
    public ResponseEntity<KnowledgeItem> create(@RequestBody KnowledgeItem item) {
        return ResponseEntity.ok(knowledgeService.createKnowledgeItem(item));
    }

    /**
     * 批量导入知识条目
     */
    @PostMapping("/batch-import")
    public ResponseEntity<BatchImportResult> batchImport(@RequestBody BatchImportRequest request) {
        return ResponseEntity.ok(knowledgeService.batchImport(request));
    }

    /**
     * 批量向量化知识条目
     */
    @PostMapping("/batch-vectorize")
    public ResponseEntity<Integer> batchVectorize(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(knowledgeService.batchVectorize(ids));
    }

    /**
     * 获取知识条目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeItem> get(@PathVariable Long id) {
        return knowledgeService.getKnowledgeItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新知识条目
     */
    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeItem> update(@PathVariable Long id, @RequestBody KnowledgeItem item) {
        try {
            return ResponseEntity.ok(knowledgeService.updateKnowledgeItem(id, item));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除知识条目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteKnowledgeItem(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据书目查询
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<KnowledgeItem>> getByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(knowledgeService.findByBookId(bookId));
    }

    /**
     * 向量化知识条目
     */
    @PostMapping("/{id}/vectorize")
    public ResponseEntity<Void> vectorize(@PathVariable Long id) {
        try {
            knowledgeService.vectorizeKnowledgeItem(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 关键词搜索
     */
    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeItem>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(knowledgeService.search(keyword));
    }
}
