package com.literature.knowledge.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.knowledge.entity.QAMessage;
import com.literature.knowledge.entity.QASession;
import com.literature.knowledge.service.QAService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能问答控制器
 */
@RestController
@RequestMapping("/api/knowledge/qa/sessions")
@RequiredArgsConstructor
public class QAController {

    private final QAService qaService;

    /**
     * 创建会话
     */
    @PostMapping
    public ApiResponse<QASession> createSession(@RequestBody CreateSessionRequest request) {
        return ApiResponse.success(qaService.createSession(request.getUserId(), request.getTitle()));
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping
    public ApiResponse<List<QASession>> getUserSessions(@RequestParam("userId") Long userId) {
        return ApiResponse.success(qaService.getUserSessions(userId));
    }

    /**
     * 获取会话消息历史
     */
    @GetMapping("/{sessionId}/messages")
    public ApiResponse<List<QAMessage>> getSessionMessages(@PathVariable("sessionId") Long sessionId) {
        return ApiResponse.success(qaService.getSessionMessages(sessionId));
    }

    /**
     * 提问
     */
    @PostMapping("/{sessionId}/ask")
    public ApiResponse<String> ask(@PathVariable("sessionId") Long sessionId, @RequestBody AskRequest request) {
        try {
            String answer = qaService.ask(sessionId, request.getQuestion(), request.getContext());
            return ApiResponse.success(answer);
        } catch (Exception e) {
            return ApiResponse.error("5000", "提问失败: " + e.getMessage());
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
        private String context; // Current chapter content or selection
    }
}
