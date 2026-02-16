package com.literature.knowledge.controller;

import com.literature.knowledge.entity.SourceConflict;
import com.literature.knowledge.service.ConflictResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 史观冲突管理控制器
 */
@RestController
@RequestMapping("/api/knowledge/conflicts")
@RequiredArgsConstructor
public class ConflictController {

    private final ConflictResolutionService conflictService;

    /**
     * 创建冲突记录
     */
    @PostMapping
    public ResponseEntity<SourceConflict> create(@RequestBody SourceConflict conflict) {
        return ResponseEntity.ok(conflictService.create(conflict));
    }

    /**
     * 查询知识条目的冲突
     */
    @GetMapping("/item/{knowledgeItemId}")
    public ResponseEntity<List<SourceConflict>> getByKnowledgeItem(
            @PathVariable("knowledgeItemId") Long knowledgeItemId) {
        return ResponseEntity.ok(conflictService.findConflicts(knowledgeItemId));
    }

    /**
     * 添加编者按/学术共识
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<SourceConflict> resolve(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String resolution = body.get("resolution");
        SourceConflict.ResolutionType type = SourceConflict.ResolutionType.valueOf(
                body.getOrDefault("resolutionType", "CONSENSUS"));
        return ResponseEntity.ok(conflictService.resolve(id, resolution, type));
    }

    /**
     * 获取格式化后的冲突展示文本
     */
    @GetMapping("/item/{knowledgeItemId}/formatted")
    public ResponseEntity<String> getFormatted(@PathVariable("knowledgeItemId") Long knowledgeItemId) {
        return ResponseEntity.ok(conflictService.formatAllConflicts(knowledgeItemId));
    }
}
