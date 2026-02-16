package com.literature.knowledge.controller;

import com.literature.knowledge.entity.HistoricalTimeline;
import com.literature.knowledge.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 时间线管理控制器
 */
@RestController
@RequestMapping("/api/knowledge/timelines")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    /**
     * 创建时间线条目
     */
    @PostMapping
    public ResponseEntity<HistoricalTimeline> create(@RequestBody HistoricalTimeline timeline) {
        return ResponseEntity.ok(timelineService.create(timeline));
    }

    /**
     * 批量创建时间线条目
     */
    @PostMapping("/batch")
    public ResponseEntity<Void> batchCreate(@RequestBody List<HistoricalTimeline> timelines) {
        timelineService.batchCreate(timelines);
        return ResponseEntity.ok().build();
    }

    /**
     * 按知识条目查询时间线
     */
    @GetMapping("/item/{knowledgeItemId}")
    public ResponseEntity<List<HistoricalTimeline>> getByKnowledgeItem(
            @PathVariable("knowledgeItemId") Long knowledgeItemId) {
        return ResponseEntity.ok(timelineService.getTimeline(knowledgeItemId));
    }

    /**
     * 按朝代查询
     */
    @GetMapping("/dynasty/{dynasty}")
    public ResponseEntity<List<HistoricalTimeline>> getByDynasty(@PathVariable("dynasty") String dynasty) {
        return ResponseEntity.ok(timelineService.getTimelineByDynasty(dynasty));
    }

    /**
     * 按公元年范围查询
     */
    @GetMapping("/range")
    public ResponseEntity<List<HistoricalTimeline>> getByRange(
            @RequestParam("start") int start, @RequestParam("end") int end) {
        return ResponseEntity.ok(timelineService.getTimelineRange(start, end));
    }

    /**
     * 校验时间线逻辑一致性
     */
    @GetMapping("/validate/{knowledgeItemId}")
    public ResponseEntity<List<String>> validate(@PathVariable("knowledgeItemId") Long knowledgeItemId) {
        return ResponseEntity.ok(timelineService.validateChronology(knowledgeItemId));
    }

    /**
     * 格式化时间线条目显示
     */
    @PostMapping("/format")
    public ResponseEntity<String> formatDate(@RequestBody HistoricalTimeline timeline) {
        return ResponseEntity.ok(timelineService.formatDate(timeline));
    }
}
