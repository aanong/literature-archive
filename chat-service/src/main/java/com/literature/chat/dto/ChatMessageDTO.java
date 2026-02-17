package com.literature.chat.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long messageId;
    private Long senderId;
    private String senderType;
    private Long targetUserId; // For single chat
    private String targetUserType;
    private Long sessionId; // For group chat
    private Integer type; // 1: Single, 2: Group
    private String content;
    private Long timestamp;
}
