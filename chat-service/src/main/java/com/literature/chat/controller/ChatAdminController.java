package com.literature.chat.controller;

import com.literature.chat.dto.BanRequestDTO;
import com.literature.chat.dto.MuteRequestDTO;
import com.literature.chat.dto.SessionQueryDTO;
import com.literature.chat.service.ChatSessionService;
import com.literature.chat.vo.BanVO;
import com.literature.chat.vo.MessageVO;
import com.literature.chat.vo.MuteVO;
import com.literature.chat.vo.SessionVO;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天后台管理控制器
 * 
 * 对应 backend_prd.md 中的聊天管理 API
 */
@RestController
@RequestMapping("/api/admin/chat")
public class ChatAdminController {

  @Autowired
  private ChatSessionService chatSessionService;

  /**
   * 获取会话列表
   * GET /api/admin/chat/sessions
   */
  @GetMapping("/sessions")
  public ApiResponse<PageResponse<SessionVO>> sessions(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {

    SessionQueryDTO query = new SessionQueryDTO();
    query.setKeyword(keyword);
    query.setStatus(status);
    query.setPage(page);
    query.setPageSize(pageSize);

    PageResponse<SessionVO> result = chatSessionService.listSessions(query);
    return ApiResponse.success(result, request.getHeader("X-Trace-Id"));
  }

  /**
   * 获取会话详情
   * GET /api/admin/chat/sessions/{sessionId}
   */
  @GetMapping("/sessions/{sessionId}")
  public ApiResponse<SessionVO> getSession(
      @PathVariable Long sessionId,
      HttpServletRequest request) {

    SessionVO session = chatSessionService.getSession(sessionId);
    return ApiResponse.success(session, request.getHeader("X-Trace-Id"));
  }

  /**
   * 获取会话消息列表
   * GET /api/admin/chat/sessions/{sessionId}/messages
   */
  @GetMapping("/sessions/{sessionId}/messages")
  public ApiResponse<PageResponse<MessageVO>> messages(
      @PathVariable Long sessionId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {

    PageResponse<MessageVO> result = chatSessionService.listMessages(sessionId, page, pageSize);
    return ApiResponse.success(result, request.getHeader("X-Trace-Id"));
  }

  /**
   * 禁言用户
   * POST /api/admin/chat/sessions/{sessionId}/mute
   */
  @PostMapping("/sessions/{sessionId}/mute")
  public ApiResponse<MuteVO> mute(
      @PathVariable Long sessionId,
      @Valid @RequestBody MuteRequestDTO request,
      HttpServletRequest httpRequest) {

    // TODO: 从JWT中获取操作者ID
    Long operatorId = 1L;

    MuteVO result = chatSessionService.muteUser(sessionId, request, operatorId);
    return ApiResponse.success(result, httpRequest.getHeader("X-Trace-Id"));
  }

  /**
   * 解除禁言
   * POST /api/admin/chat/sessions/{sessionId}/unmute
   */
  @PostMapping("/sessions/{sessionId}/unmute")
  public ApiResponse<Void> unmute(
      @PathVariable Long sessionId,
      @RequestParam String userId,
      HttpServletRequest request) {

    Long operatorId = 1L;
    chatSessionService.unmuteUser(sessionId, Long.parseLong(userId), operatorId);
    return ApiResponse.success(null, request.getHeader("X-Trace-Id"));
  }

  /**
   * 封禁用户
   * POST /api/admin/chat/sessions/{sessionId}/ban
   */
  @PostMapping("/sessions/{sessionId}/ban")
  public ApiResponse<BanVO> ban(
      @PathVariable Long sessionId,
      @Valid @RequestBody BanRequestDTO request,
      HttpServletRequest httpRequest) {

    Long operatorId = 1L;

    BanVO result = chatSessionService.banUser(sessionId, request, operatorId);
    return ApiResponse.success(result, httpRequest.getHeader("X-Trace-Id"));
  }

  /**
   * 解除封禁
   * POST /api/admin/chat/sessions/{sessionId}/unban
   */
  @PostMapping("/sessions/{sessionId}/unban")
  public ApiResponse<Void> unban(
      @PathVariable Long sessionId,
      @RequestParam String userId,
      HttpServletRequest request) {

    Long operatorId = 1L;
    chatSessionService.unbanUser(sessionId, Long.parseLong(userId), operatorId);
    return ApiResponse.success(null, request.getHeader("X-Trace-Id"));
  }

  /**
   * 检查用户状态
   * GET /api/admin/chat/sessions/{sessionId}/user/{userId}/status
   */
  @GetMapping("/sessions/{sessionId}/user/{userId}/status")
  public ApiResponse<UserStatusVO> getUserStatus(
      @PathVariable Long sessionId,
      @PathVariable Long userId,
      HttpServletRequest request) {

    boolean muted = chatSessionService.isUserMuted(sessionId, userId);
    boolean banned = chatSessionService.isUserBanned(sessionId, userId);

    UserStatusVO status = new UserStatusVO();
    status.setUserId(String.valueOf(userId));
    status.setMuted(muted);
    status.setBanned(banned);

    return ApiResponse.success(status, request.getHeader("X-Trace-Id"));
  }

  /**
   * 用户状态内部类
   */
  public static class UserStatusVO {
    private String userId;
    private boolean muted;
    private boolean banned;

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public boolean isMuted() {
      return muted;
    }

    public void setMuted(boolean muted) {
      this.muted = muted;
    }

    public boolean isBanned() {
      return banned;
    }

    public void setBanned(boolean banned) {
      this.banned = banned;
    }
  }
}
