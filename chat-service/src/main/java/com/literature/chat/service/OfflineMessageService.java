package com.literature.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.literature.chat.dto.ChatMessageDTO;
import com.literature.chat.entity.ChatMessageDoc;
import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import com.literature.chat.netty.session.SessionManager;
import com.literature.chat.repository.ChatMessageMongoRepository;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class OfflineMessageService {

    @Autowired
    private ChatMessageMongoRepository messageRepository;

    @Autowired
    private SessionManager sessionManager;

    // Circular dependency risk with SessionManager? No, SessionManager is low
    // level.
    // Circular with ChatMessageConsumer? No, Consumer uses this.

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String OFFLINE_QUEUE_PREFIX = "offline:queue:";

    /**
     * 保存离线消息 (异步)
     */
    @Async
    public void saveOfflineMessage(ChatMessageDTO message) {
        try {
            // 1. Save to MongoDB (History)
            ChatMessageDoc doc = new ChatMessageDoc();
            doc.setSessionId(message.getSessionId());
            doc.setSenderId(message.getSenderId());
            doc.setTargetUserId(message.getTargetUserId());
            doc.setType(message.getType());
            doc.setContent(message.getContent());
            doc.setTimestamp(message.getTimestamp());
            doc.setCreatedAt(LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(message.getTimestamp()),
                    ZoneId.systemDefault()));

            messageRepository.save(doc);
            log.debug("Saved offline message to MongoDB: {}", doc);

            // 2. If Single Chat and user offline, save to Redis Queue (Push mechanism)
            // Note: This method is called by ChatMessageHandler.
            // But ChatMessageHandler calls it *always*.
            // We need a separate method for "User is Offline, please queue".
            // OR we pass a flag.
            // But wait, ChatMessageHandler knows if user is offline.
            // Let's add a separate method `pushToOfflineQueue`.
        } catch (Exception e) {
            log.error("Failed to save offline message", e);
        }
    }

    /**
     * 推送消息到 Redis 离线队列 (仅单聊)
     */
    public void pushToOfflineQueue(Long targetUserId, ChatMessageDTO message) {
        if (message.getType() != 1) {
            return; // Group chat relies on history pull
        }
        try {
            String key = OFFLINE_QUEUE_PREFIX + targetUserId;
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, json);
            // Limit queue size (e.g., 50)
            redisTemplate.opsForList().trim(key, -50, -1);
            log.debug("Pushed offline message to Redis queue for user {}", targetUserId);
        } catch (Exception e) {
            log.error("Failed to push to offline queue", e);
        }
    }

    /**
     * 拉取并推送离线消息 (用户上线时调用)
     */
    public void pullAndPushOfflineMessages(Long userId) {
        try {
            String key = OFFLINE_QUEUE_PREFIX + userId;
            List<String> list = redisTemplate.opsForList().range(key, 0, -1);
            if (list == null || list.isEmpty()) {
                return;
            }

            Channel channel = sessionManager.getChannel(userId);
            if (channel == null || !channel.isActive()) {
                return;
            }

            for (String json : list) {
                ChatMessageDTO dto = objectMapper.readValue(json, ChatMessageDTO.class);

                ChatPayload payload = ChatPayload.newBuilder()
                        .setSenderId(dto.getSenderId())
                        .setTargetId(dto.getTargetUserId())
                        .setContent(dto.getContent())
                        .setTimestamp(dto.getTimestamp())
                        .build();

                NettyMessage nettyMessage = new NettyMessage();
                NettyMessage.Header header = new NettyMessage.Header();
                header.setMagic((short) 0xCAFE);
                header.setVersion((byte) 1);
                header.setCmdType((byte) CmdType.SINGLE_CHAT_VALUE);
                nettyMessage.setHeader(header);
                nettyMessage.setBody(payload);

                channel.writeAndFlush(nettyMessage);
            }

            // Clear queue after pushing
            redisTemplate.delete(key);
            log.info("Pushed {} offline messages to user {}", list.size(), userId);
        } catch (Exception e) {
            log.error("Failed to pull offline messages", e);
        }
    }
}
