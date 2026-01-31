package com.literature.chat.mq;

import com.literature.chat.dto.ChatMessageDTO;
import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import com.literature.chat.netty.session.SessionManager;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Service
public class ChatMessageConsumer {

    @Autowired
    private SessionManager sessionManager;

    @Value("${netty.port:9090}")
    private int port;

    // Listen to the topic specific to this server instance
    // Limitation: SpEL for dynamic topic names in @KafkaListener is tricky if
    // server address changes.
    // Fixed topic + filtering is easier but less efficient.
    // Dynamic topic listening requires manual container setup.
    // For this implementation, let's assume a fixed topic "chat-messages" and all
    // servers listen,
    // checking if the target user is local. This works for small clusters.
    // But the Plan said "sendToServer".
    // Let's refine: Use "chat-messages" globally. Consumer checks if user is local.

    @Autowired
    private com.literature.chat.service.ChatSessionService chatSessionService;

    @KafkaListener(topics = "chat-messages", groupId = "chat-service-group")
    public void onMessage(ChatMessageDTO message) {
        if (message.getType() == 2) { // Group Chat
            handleGroupMessage(message);
        } else { // Single Chat
            handleSingleMessage(message);
        }
    }

    private void handleSingleMessage(ChatMessageDTO message) {
        Long targetUserId = message.getTargetUserId();
        pushToLocalUser(targetUserId, message);
    }

    private void handleGroupMessage(ChatMessageDTO message) {
        Long sessionId = message.getSessionId();
        // Option A: Query DB for members (Heavy for large groups if every server does
        // it)
        // Optimization: Cache members in Redis (Planned for Phase 5 or 6).
        // For now, query DB.
        java.util.List<Long> memberIds = chatSessionService.getSessionMemberIds(sessionId);
        for (Long memberId : memberIds) {
            // Push to local user if online
            pushToLocalUser(memberId, message);
        }
    }

    private void pushToLocalUser(Long userId, ChatMessageDTO message) {
        Channel channel = sessionManager.getChannel(userId);
        if (channel != null && channel.isActive()) {
            ChatPayload.Builder payloadBuilder = ChatPayload.newBuilder()
                    .setSenderId(message.getSenderId())
                    .setTargetId(message.getType() == 2 ? message.getSessionId() : message.getTargetUserId())
                    .setContent(message.getContent())
                    .setTimestamp(message.getTimestamp());

            NettyMessage nettyMessage = new NettyMessage();
            NettyMessage.Header header = new NettyMessage.Header();
            header.setMagic((short) 0xCAFE);
            header.setVersion((byte) 1);
            header.setCmdType((byte) (message.getType() == 2 ? CmdType.GROUP_CHAT_VALUE : CmdType.SINGLE_CHAT_VALUE));
            nettyMessage.setHeader(header);
            nettyMessage.setBody(payloadBuilder.build());

            channel.writeAndFlush(nettyMessage);
            log.debug("Relayed message to user {}", userId);
        }
    }
}
