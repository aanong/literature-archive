package com.literature.chat.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.chat.dto.BanRequestDTO;
import com.literature.chat.dto.MuteRequestDTO;
import com.literature.chat.dto.SessionQueryDTO;
import com.literature.chat.entity.ChatBan;
import com.literature.chat.entity.ChatMessage;
import com.literature.chat.entity.ChatMute;
import com.literature.chat.entity.ChatSession;
import com.literature.chat.entity.ChatSessionMember;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.chat.mapper.*;
import com.literature.chat.service.ChatSessionService;
import com.literature.chat.vo.BanVO;
import com.literature.chat.vo.MessageVO;
import com.literature.chat.vo.MuteVO;
import com.literature.chat.vo.SessionVO;
import com.literature.common.core.model.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天会话服务实现（MyBatis 版本）
 */
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper sessionMapper;

    @Autowired
    private ChatSessionMemberMapper memberMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private ChatMuteMapper muteMapper;

    @Autowired
    private ChatBanMapper banMapper;

    @Override
    public PageResponse<SessionVO> listSessions(SessionQueryDTO query) {
        Page<ChatSession> page = new Page<>(query.getPage(), query.getPageSize());

        IPage<ChatSession> result = sessionMapper.searchSessions(
                page,
                query.getKeyword(),
                query.getStatus());

        List<SessionVO> items = result.getRecords().stream()
                .map(this::enrichSessionVO)
                .collect(Collectors.toList());

        return new PageResponse<>(result.getTotal(), items);
    }

    @Override
    public SessionVO getSession(Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        return enrichSessionVO(session);
    }

    @Override
    public PageResponse<MessageVO> listMessages(Long sessionId, int page, int pageSize) {
        // 验证会话存在
        if (sessionMapper.selectById(sessionId) == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        Page<ChatMessage> pageParam = new Page<>(page, pageSize);
        IPage<ChatMessage> result = messageMapper.selectBySessionIdDesc(pageParam, sessionId);

        List<MessageVO> items = result.getRecords().stream()
                .map(MessageVO::fromEntity)
                .collect(Collectors.toList());

        return new PageResponse<>(result.getTotal(), items);
    }

    @Override
    @Transactional
    public MuteVO muteUser(Long sessionId, MuteRequestDTO request, Long operatorId) {
        Long userId = request.getUserIdLong();
        if (userId == null) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        // 验证会话存在
        if (sessionMapper.selectById(sessionId) == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        // 创建禁言记录
        ChatMute mute = new ChatMute();
        mute.setSessionId(sessionId);
        mute.setUserId(userId);
        mute.setMutedUntil(LocalDateTime.now().plusMinutes(request.getMinutes()));
        mute.setReason(request.getReason());
        mute.setCreatedBy(operatorId);
        mute.setCreatedAt(LocalDateTime.now());

        muteMapper.insert(mute);
        return MuteVO.fromEntity(mute);
    }

    @Override
    @Transactional
    public void unmuteUser(Long sessionId, Long userId, Long operatorId) {
        ChatMute mute = muteMapper.selectActiveMute(sessionId, userId, LocalDateTime.now());
        if (mute != null) {
            mute.setMutedUntil(LocalDateTime.now());
            muteMapper.updateById(mute);
        }
    }

    @Override
    @Transactional
    public BanVO banUser(Long sessionId, BanRequestDTO request, Long operatorId) {
        Long userId = request.getUserIdLong();
        if (userId == null) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        // 验证会话存在
        if (sessionMapper.selectById(sessionId) == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        // 检查是否已被封禁
        if (banMapper.isUserBanned(sessionId, userId)) {
            throw new IllegalStateException("用户已被封禁");
        }

        // 创建封禁记录
        ChatBan ban = new ChatBan();
        ban.setSessionId(sessionId);
        ban.setUserId(userId);
        ban.setStatus(ChatBan.STATUS_ACTIVE);
        ban.setReason(request.getReason());
        ban.setCreatedBy(operatorId);
        ban.setCreatedAt(LocalDateTime.now());

        banMapper.insert(ban);
        return BanVO.fromEntity(ban);
    }

    @Override
    @Transactional
    public void unbanUser(Long sessionId, Long userId, Long operatorId) {
        ChatBan ban = banMapper.selectActiveBan(sessionId, userId);
        if (ban != null) {
            ban.lift(operatorId);
            banMapper.updateById(ban);
        }
    }

    @Override
    public boolean isUserMuted(Long sessionId, Long userId) {
        return muteMapper.isUserMuted(sessionId, userId, LocalDateTime.now());
    }

    @Override
    public boolean isUserBanned(Long sessionId, Long userId) {
        return banMapper.isUserBanned(sessionId, userId);
    }

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final String SESSION_MEMBERS_KEY_PREFIX = "chat:session:members:";

    @Override
    public List<Long> getSessionMemberIds(Long sessionId) {
        String key = SESSION_MEMBERS_KEY_PREFIX + sessionId;

        // 1. Check Redis
        java.util.Set<String> members = redisTemplate.opsForSet().members(key);
        if (members != null && !members.isEmpty()) {
            return members.stream().map(Long::valueOf).collect(Collectors.toList());
        }

        // 2. Query DB
        List<Long> memberIds = memberMapper.selectList(
                new LambdaQueryWrapper<ChatSessionMember>()
                        .eq(ChatSessionMember::getSessionId, sessionId)
                        .select(ChatSessionMember::getUserId))
                .stream()
                .map(ChatSessionMember::getUserId)
                .collect(Collectors.toList());

        // 3. Cache to Redis (TTL 1 hour)
        if (!memberIds.isEmpty()) {
            String[] values = memberIds.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(key, values);
            redisTemplate.expire(key, 1, java.util.concurrent.TimeUnit.HOURS);
        }

        return memberIds;
    }

    /**
     * 丰富会话信息（添加统计数据）
     */
    private SessionVO enrichSessionVO(ChatSession session) {
        SessionVO vo = SessionVO.fromEntity(session);
        vo.setMemberCount(memberMapper.selectCount(
                new LambdaQueryWrapper<ChatSessionMember>()
                        .eq(ChatSessionMember::getSessionId, session.getId())));
        vo.setMessageCount(messageMapper
                .selectCount(new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, session.getId())));
        return vo;
    }
}
