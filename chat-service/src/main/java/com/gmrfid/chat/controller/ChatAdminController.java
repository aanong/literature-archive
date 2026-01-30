package com.gmrfid.chat.controller;

import com.gmrfid.common.core.model.ApiResponse;
import com.gmrfid.common.core.model.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/chat")
public class ChatAdminController {

  @GetMapping("/sessions")
  public ApiResponse<PageResponse<Map<String, Object>>> sessions(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    return ApiResponse.success(new PageResponse<>(0, List.of()), request.getHeader("X-Trace-Id"));
  }

  @GetMapping("/sessions/{sessionId}/messages")
  public ApiResponse<PageResponse<Map<String, Object>>> messages(
      @PathVariable String sessionId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    return ApiResponse.success(new PageResponse<>(0, List.of()), request.getHeader("X-Trace-Id"));
  }

  @PostMapping("/sessions/{sessionId}/mute")
  public ApiResponse<Map<String, Object>> mute(@PathVariable String sessionId,
      HttpServletRequest request) {
    return ApiResponse.success(Map.of("sessionId", sessionId), request.getHeader("X-Trace-Id"));
  }

  @PostMapping("/sessions/{sessionId}/ban")
  public ApiResponse<Map<String, Object>> ban(@PathVariable String sessionId,
      HttpServletRequest request) {
    return ApiResponse.success(Map.of("sessionId", sessionId), request.getHeader("X-Trace-Id"));
  }
}
