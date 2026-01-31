package com.literature.chat.vo;

import com.literature.chat.entity.ChatMessage;
import java.time.LocalDateTime;

/**
 * 消息视图对象
 */
public class MessageVO {

    private String id;
    private String sessionId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String contentType;
    private String status;
    private String replyToId;
    private Object extra;
    private LocalDateTime createdAt;

    /**
     * 从实体转换
     */
    public static MessageVO fromEntity(ChatMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(String.valueOf(message.getId()));
        vo.setSessionId(String.valueOf(message.getSessionId()));
        vo.setSenderId(String.valueOf(message.getSenderId()));
        vo.setContent(message.getContent());
        vo.setContentType(message.getContentType());
        vo.setStatus(message.getStatus());
        if (message.getReplyToId() != null) {
            vo.setReplyToId(String.valueOf(message.getReplyToId()));
        }
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
