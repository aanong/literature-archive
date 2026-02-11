package com.literature.knowledge.controller;

import com.literature.knowledge.entity.QAMessage;
import com.literature.knowledge.entity.QASession;
import com.literature.knowledge.service.QAService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能问答控制器
 */
@RestController
@RequestMapping("/api/qa/sessions")
@RequiredArgsConstructor
public class QAController {
    
    private final QAService qaService;
    
    /**
     * 创建会话
     */
    @PostMapping
    public ResponseEntity<QASession> createSession(@RequestBody CreateSessionRequest request) {
        return ResponseEntity.ok(qaService.createSession(request.getUserId(), request.getTitle()));
    }
    
    /**
     * 获取用户会话列表
     */
    @GetMapping
    public ResponseEntity<List<QASession>> getUserSessions(@RequestParam Long userId) {
        return ResponseEntity.ok(qaService.getUserSessions(userId));
    }
    
    /**
     * 获取会话消息历史
     */
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<QAMessage>> getSessionMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(qaService.getSessionMessages(sessionId));
    }
    
    /**
     * 提问
     */
    @PostMapping("/{sessionId}/ask")
    public ResponseEntity<String> ask(@PathVariable Long sessionId, @RequestBody AskRequest request) {
        try {
            String answer = qaService.ask(sessionId, request.getQuestion());
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("提问失败: " + e.getMessage());
        }
    }
    
    @Data
    public static class CreateSessionRequest {
        private Long userId;
        private String title;
    }
    
    @Data
    public static class AskRequest {
        private String question;
    }
}
