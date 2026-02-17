package com.literature.chat.netty.handler;

import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import com.literature.chat.netty.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ChatMessageHandler extends SimpleChannelInboundHandler<NettyMessage> {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private com.literature.chat.service.SessionRouteService sessionRouteService;

    @Autowired
    private com.literature.chat.service.OfflineMessageService offlineMessageService;

    @Autowired
    private com.literature.chat.service.ChatSessionService chatSessionService;

    @Autowired
    private com.literature.chat.mapper.ChatMessageMapper chatMessageMapper;

    @Autowired
    private com.literature.chat.mapper.ChatSessionMapper chatSessionMapper;

    @Autowired
    private com.literature.chat.mapper.ChatSessionMemberMapper memberMapper;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private com.literature.chat.mq.ChatMessageProducer chatMessageProducer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessage msg) throws Exception {
        if (msg.getHeader().getCmdType() == CmdType.SINGLE_CHAT_VALUE) {
            handleSingleChat(ctx, (ChatPayload) msg.getBody());
        } else if (msg.getHeader().getCmdType() == CmdType.GROUP_CHAT_VALUE) {
            handleGroupChat(ctx, (ChatPayload) msg.getBody());
        } else if (msg.getHeader().getCmdType() == CmdType.HEARTBEAT_VALUE) {
            // 心跳响应
        }
    }

    private void handleGroupChat(ChannelHandlerContext ctx, ChatPayload payload) {
        Long sessionId = payload.getSessionId() > 0 ? payload.getSessionId() : payload.getTargetId();
        Long senderId = payload.getSenderId();
        if (senderId <= 0) {
            senderId = sessionManager.getUserId(ctx.channel());
        }
        String senderType = extractSenderType(ctx, payload);

        if (sessionId == null || sessionId <= 0) {
            log.warn("Group chat missing sessionId, channel={}", ctx.channel().id());
            return;
        }

        if (senderId != null) {
            if (chatSessionService.isUserBanned(sessionId, senderId)) {
                log.warn("User {} is banned in session {}, dropping message", senderId, sessionId);
                return;
            }
            if (chatSessionService.isUserMuted(sessionId, senderId)) {
                log.warn("User {} is muted in session {}, dropping message", senderId, sessionId);
                return;
            }
        }

        // 持久化消息（历史记录）
        com.literature.chat.dto.ChatMessageDTO dto = new com.literature.chat.dto.ChatMessageDTO();
        dto.setMessageId(payload.getTimestamp());
        dto.setSenderId(senderId);
        dto.setSenderType(senderType);
        dto.setSessionId(sessionId);
        dto.setType(2); // 群聊
        dto.setContent(payload.getContent());
        dto.setTimestamp(payload.getTimestamp());
        offlineMessageService.saveOfflineMessage(dto);

        persistMessage(sessionId, senderId, senderType, payload.getContent(), payload.getContentType());

        // 广播到所有服务器（包括自身）以处理群组消息分发
        // "BROADCAST" 只是一个占位符，因为我们已将 Producer 修改为忽略
        // targetServer 并直接广播到 Topic。
        chatMessageProducer.sendToServer("BROADCAST", dto);
        log.info("Group message persisted and broadcasted for session {}", sessionId);

        // 我们不需要在此处进行本地推送，因为部署在同一服务器上的 Consumer 会
        // 接收消息并执行推送！
        // 这样可以确保一致性。
    }

    private void handleSingleChat(ChannelHandlerContext ctx, ChatPayload payload) {
        Long targetId = payload.getTargetId();
        Long sessionId = payload.getSessionId();
        Long senderId = payload.getSenderId();
        if (senderId <= 0) {
            senderId = sessionManager.getUserId(ctx.channel());
        }
        String senderType = extractSenderType(ctx, payload);
        String targetUserType = extractTargetUserType(payload);
        if ((targetUserType == null || targetUserType.isBlank()) && sessionId != null && sessionId > 0) {
            targetUserType = resolveTargetUserType(sessionId, targetId);
        }

        if (sessionId == null || sessionId <= 0) {
            log.warn("Single chat missing sessionId, channel={}", ctx.channel().id());
        } else if (senderId != null) {
            if (chatSessionService.isUserBanned(sessionId, senderId)) {
                log.warn("User {} is banned in session {}, dropping message", senderId, sessionId);
                return;
            }
            if (chatSessionService.isUserMuted(sessionId, senderId)) {
                log.warn("User {} is muted in session {}, dropping message", senderId, sessionId);
                return;
            }
        }

        // 持久化消息（历史记录和离线）
        com.literature.chat.dto.ChatMessageDTO dto = new com.literature.chat.dto.ChatMessageDTO();
        dto.setMessageId(payload.getTimestamp()); // 暂时使用时间戳作为 ID
        dto.setSenderId(senderId);
        dto.setSenderType(senderType);
        dto.setTargetUserId(targetId);
        dto.setTargetUserType(targetUserType);
        dto.setSessionId(sessionId);
        dto.setType(1); // 单聊
        dto.setContent(payload.getContent());
        dto.setTimestamp(payload.getTimestamp());
        offlineMessageService.saveOfflineMessage(dto);

        if (sessionId != null && sessionId > 0) {
            persistMessage(sessionId, senderId, senderType, payload.getContent(), payload.getContentType());
        }

        Channel targetChannel = sessionManager.getChannel(targetId, targetUserType);

        if (targetChannel != null && targetChannel.isActive()) {
            // 本地推送
            forwardMessage(targetChannel, payload);
            log.info("Message forwarded locally from {} to {}", payload.getSenderId(), targetId);
        } else {
            // 检查远程路由
            String targetServer = sessionRouteService.getUserRoute(targetId, targetUserType);
            if (targetServer != null) {
                // 通过 Kafka 转发
                chatMessageProducer.sendToServer(targetServer, dto);
                log.info("Message routed to server {} for user {}", targetServer, targetId);
            } else {
                log.info("User {} is offline, saving to Redis Offline Queue", targetId);
                offlineMessageService.pushToOfflineQueue(targetId, targetUserType, dto);
            }
        }
    }

    private void forwardMessage(Channel targetChannel, ChatPayload payload) {
        NettyMessage message = new NettyMessage();
        NettyMessage.Header header = new NettyMessage.Header();
        header.setMagic((short) 0xCAFE);
        header.setVersion((byte) 1);
        header.setSerial((byte) 1);
        header.setCmdType((byte) CmdType.SINGLE_CHAT_VALUE);
        header.setReqId(System.currentTimeMillis());
        message.setHeader(header);
        message.setBody(payload);

        targetChannel.writeAndFlush(message).addListener((io.netty.channel.ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("消息写入 Channel {} 失败", targetChannel.id(), future.cause());
            }
        });
    }

    private void persistMessage(Long sessionId, Long senderId, String senderType,
                                String content, String contentType) {
        if (sessionId == null || sessionId <= 0 || senderId == null || senderId <= 0) {
            return;
        }
        com.literature.chat.entity.ChatMessage message = new com.literature.chat.entity.ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setSenderType(senderType == null ? "C_USER" : senderType);
        message.setContent(content == null ? "" : content.trim());
        message.setContentType(contentType == null ? com.literature.chat.entity.ChatMessage.CONTENT_TYPE_TEXT : contentType);
        message.setStatus(com.literature.chat.entity.ChatMessage.STATUS_SENT);
        chatMessageMapper.insert(message);

        com.literature.chat.entity.ChatSession patch = new com.literature.chat.entity.ChatSession();
        patch.setId(sessionId);
        patch.setUpdatedAt(java.time.LocalDateTime.now());
        chatSessionMapper.updateById(patch);
    }

    private String extractSenderType(ChannelHandlerContext ctx, ChatPayload payload) {
        String fromExtra = extractExtraField(payload, "senderType");
        if (fromExtra != null && !fromExtra.isBlank()) {
            return fromExtra;
        }
        return sessionManager.getUserType(ctx.channel());
    }

    private String extractTargetUserType(ChatPayload payload) {
        return extractExtraField(payload, "targetUserType");
    }

    private String extractExtraField(ChatPayload payload, String field) {
        String extra = payload.getExtra();
        if (extra == null || extra.isBlank()) {
            return null;
        }
        try {
            java.util.Map<?, ?> map = objectMapper.readValue(extra, java.util.Map.class);
            Object val = map.get(field);
            return val == null ? null : String.valueOf(val);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveTargetUserType(Long sessionId, Long targetId) {
        if (sessionId == null || targetId == null) return null;
        java.util.List<com.literature.chat.entity.ChatSessionMember> members = memberMapper.selectBySessionId(sessionId);
        for (com.literature.chat.entity.ChatSessionMember member : members) {
            if (targetId.equals(member.getUserId())) {
                return member.getMemberType();
            }
        }
        return null;
    }
}
