package com.literature.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 聊天消息文档 (MongoDB)
 * 用于存储完整聊天历史
 */
@Data
@Document(collection = "chat_messages")
@CompoundIndex(def = "{'sessionId': 1, 'timestamp': -1}", name = "idx_session_timestamp")
public class ChatMessageDoc {

    @Id
    private String id;

    @Indexed
    private Long sessionId;

    @Indexed
    private Long senderId;

    /**
     * 接收者ID (仅单聊有效，群聊为null或0)
     */
    @Indexed
    private Long targetUserId;

    /**
     * 消息类型: 1-单聊, 2-群聊
     */
    private Integer type;

    private String content;

    private Long timestamp;

    private LocalDateTime createdAt;
}
