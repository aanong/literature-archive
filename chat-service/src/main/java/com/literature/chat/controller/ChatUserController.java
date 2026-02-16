package com.literature.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.chat.dto.CreatePrivateSessionRequestDTO;
import com.literature.chat.dto.SendMessageRequestDTO;
import com.literature.chat.entity.ChatMessage;
import com.literature.chat.entity.ChatSession;
import com.literature.chat.entity.ChatSessionMember;
import com.literature.chat.mapper.ChatMessageMapper;
import com.literature.chat.mapper.ChatSessionMapper;
import com.literature.chat.mapper.ChatSessionMemberMapper;
import com.literature.chat.service.ChatSessionService;
import com.literature.chat.vo.MessageVO;
import com.literature.chat.vo.SessionVO;
import com.literature.common.core.dto.UserDTO;
import com.literature.common.core.feign.UserServiceClient;
import com.literature.common.core.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatUserController {

  @Autowired
  private ChatSessionMapper sessionMapper;

  @Autowired
  private ChatSessionMemberMapper memberMapper;

  @Autowired
  private ChatMessageMapper messageMapper;

  @Autowired
  private ChatSessionService chatSessionService;

  @Autowired
  private UserServiceClient userServiceClient;

  @PostMapping("/private-sessions")
  public ApiResponse<SessionVO> createOrGetPrivateSession(
      @Valid @RequestBody CreatePrivateSessionRequestDTO requestBody,
      Authentication authentication,
      HttpServletRequest request) {

    UserIdentity currentUser = resolveCurrentUser(authentication);
    String peerType = "ADMIN".equalsIgnoreCase(currentUser.userType()) ? "C_USER" : "ADMIN";
    UserDTO peerUser = resolveUserByUsername(requestBody.getPeerUsername().trim(), peerType);

    if (currentUser.userId().equals(peerUser.id())
        && currentUser.userType().equalsIgnoreCase(peerUser.userType())) {
      throw new IllegalArgumentException("不能与自己创建私聊会话");
    }

    ChatSession existing = sessionMapper.selectPrivateSessionByIdentity(
        currentUser.userId(),
        currentUser.userType(),
        peerUser.id(),
        peerUser.userType());
    if (existing != null) {
      return ApiResponse.success(toSessionVO(existing), request.getHeader("X-Trace-Id"));
    }

    ChatSession session = new ChatSession();
    session.setType(ChatSession.TYPE_PRIVATE);
    session.setStatus(ChatSession.STATUS_ACTIVE);
    session.setCreatedBy(currentUser.userId());
    session.setTitle(currentUser.username() + " / " + peerUser.username());
    sessionMapper.insert(session);

    ChatSessionMember selfMember = new ChatSessionMember();
    selfMember.setSessionId(session.getId());
    selfMember.setUserId(currentUser.userId());
    selfMember.setMemberType(currentUser.userType());
    selfMember.setRole(ChatSessionMember.ROLE_OWNER);
    selfMember.setNickname(currentUser.username());
    memberMapper.insert(selfMember);

    ChatSessionMember peerMember = new ChatSessionMember();
    peerMember.setSessionId(session.getId());
    peerMember.setUserId(peerUser.id());
    peerMember.setMemberType(peerUser.userType());
    peerMember.setRole(ChatSessionMember.ROLE_MEMBER);
    peerMember.setNickname(peerUser.username());
    memberMapper.insert(peerMember);

    ChatSession saved = sessionMapper.selectById(session.getId());
    return ApiResponse.success(toSessionVO(saved), request.getHeader("X-Trace-Id"));
  }

  @GetMapping("/sessions/mine")
  public ApiResponse<List<SessionVO>> mySessions(
      Authentication authentication,
      HttpServletRequest request) {
    UserIdentity currentUser = resolveCurrentUser(authentication);

    Page<ChatSession> page = new Page<>(1, 100);
    IPage<ChatSession> result = sessionMapper.selectByMemberUserId(page, currentUser.userId(),
        currentUser.userType(), ChatSession.STATUS_ACTIVE);

    List<SessionVO> sessions = result.getRecords().stream().map(this::toSessionVO).toList();
    return ApiResponse.success(sessions, request.getHeader("X-Trace-Id"));
  }

  @GetMapping("/sessions/{sessionId}/messages")
  public ApiResponse<List<MessageVO>> sessionMessages(
      @PathVariable Long sessionId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "50") int pageSize,
      Authentication authentication,
      HttpServletRequest request) {
    UserIdentity currentUser = resolveCurrentUser(authentication);
    ensureSessionMember(sessionId, currentUser.userId(), currentUser.userType());

    int safePage = Math.max(page, 1);
    int safePageSize = Math.min(Math.max(pageSize, 1), 200);
    Page<ChatMessage> pageParam = new Page<>(safePage, safePageSize);
    IPage<ChatMessage> result = messageMapper.selectBySessionIdDesc(pageParam, sessionId);

    List<ChatMessage> ordered = new ArrayList<>(result.getRecords());
    Collections.reverse(ordered);

    Map<String, String> senderNames = new HashMap<>();
    List<MessageVO> messages = ordered.stream().map(message -> {
      MessageVO vo = MessageVO.fromEntity(message);
      String senderType = vo.getSenderType() == null ? "ADMIN" : vo.getSenderType();
      String senderKey = message.getSenderId() + ":" + senderType;
      senderNames.computeIfAbsent(senderKey,
          key -> resolveUsernameByIdentity(message.getSenderId(), senderType));
      vo.setSenderName(senderNames.get(senderKey));
      return vo;
    }).toList();

    return ApiResponse.success(messages, request.getHeader("X-Trace-Id"));
  }

  @PostMapping("/sessions/{sessionId}/messages")
  public ApiResponse<MessageVO> sendMessage(
      @PathVariable Long sessionId,
      @Valid @RequestBody SendMessageRequestDTO requestBody,
      Authentication authentication,
      HttpServletRequest request) {
    UserIdentity currentUser = resolveCurrentUser(authentication);
    ensureSessionMember(sessionId, currentUser.userId(), currentUser.userType());

    if (chatSessionService.isUserBanned(sessionId, currentUser.userId())) {
      throw new IllegalArgumentException("您已被封禁，无法发言");
    }
    if (chatSessionService.isUserMuted(sessionId, currentUser.userId())) {
      throw new IllegalArgumentException("您已被禁言，暂时无法发言");
    }

    ChatMessage message = new ChatMessage();
    message.setSessionId(sessionId);
    message.setSenderId(currentUser.userId());
    message.setSenderType(currentUser.userType());
    message.setContent(requestBody.getContent().trim());
    message.setContentType(ChatMessage.CONTENT_TYPE_TEXT);
    message.setStatus(ChatMessage.STATUS_SENT);
    messageMapper.insert(message);

    ChatSession patch = new ChatSession();
    patch.setId(sessionId);
    patch.setUpdatedAt(LocalDateTime.now());
    sessionMapper.updateById(patch);

    MessageVO vo = MessageVO.fromEntity(message);
    vo.setSenderName(currentUser.username());
    return ApiResponse.success(vo, request.getHeader("X-Trace-Id"));
  }

  private void ensureSessionMember(Long sessionId, Long userId, String userType) {
    if (!memberMapper.existsBySessionIdAndIdentity(sessionId, userId, userType)) {
      throw new IllegalArgumentException("无权访问该会话");
    }
  }

  private SessionVO toSessionVO(ChatSession session) {
    SessionVO vo = SessionVO.fromEntity(session);

    Long memberCount = memberMapper.countBySessionId(session.getId());
    Long messageCount = messageMapper.countBySessionId(session.getId());
    vo.setMemberCount(memberCount == null ? 0L : memberCount);
    vo.setMessageCount(messageCount == null ? 0L : messageCount);

    Page<ChatMessage> lastMessagePage = new Page<>(1, 1);
    IPage<ChatMessage> messageResult = messageMapper.selectBySessionIdDesc(lastMessagePage,
        session.getId());
    if (!messageResult.getRecords().isEmpty()) {
      MessageVO lastMessage = MessageVO.fromEntity(messageResult.getRecords().get(0));
      String senderType = lastMessage.getSenderType() == null ? "ADMIN" : lastMessage.getSenderType();
      lastMessage.setSenderName(resolveUsernameByIdentity(
          Long.valueOf(lastMessage.getSenderId()),
          senderType));
      vo.setLastMessage(lastMessage);
    }

    return vo;
  }

  private UserIdentity resolveCurrentUser(Authentication authentication) {
    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
        || authentication.getName() == null
        || authentication.getName().isBlank()) {
      throw new IllegalArgumentException("用户未登录");
    }
    Map<String, Object> claims = jwtAuth.getToken().getClaims();
    Object userIdClaim = claims.get("userId");
    Object userTypeClaim = claims.get("userType");
    if (userIdClaim == null || userTypeClaim == null) {
      UserDTO fallback = resolveUserByUsername(authentication.getName(), null);
      return new UserIdentity(fallback.id(), fallback.userType(), fallback.username());
    }
    return new UserIdentity(
        Long.parseLong(String.valueOf(userIdClaim)),
        String.valueOf(userTypeClaim),
        authentication.getName());
  }

  private UserDTO resolveUserByUsername(String username, String userType) {
    ApiResponse<UserDTO> response = userServiceClient.getUserByUsername(username, userType);
    if (response == null || response.data() == null) {
      throw new IllegalArgumentException("用户不存在: " + username);
    }
    return response.data();
  }

  private String resolveUsernameByIdentity(Long userId, String userType) {
    ApiResponse<UserDTO> response = userServiceClient.getUserById(userId, userType);
    if (response == null || response.data() == null) {
      return "user-" + userId;
    }
    return response.data().username();
  }

  private record UserIdentity(Long userId, String userType, String username) {
  }
}
