package com.literature.chat.vo;

import com.literature.chat.entity.ChatSession;
import java.time.LocalDateTime;

/**
 * 会话视图对象
 */
public class SessionVO {

    private String id;
    private String title;
    private String type;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long memberCount;
    private Long messageCount;
    private MessageVO lastMessage;

    /**
     * 从实体转换
     */
    public static SessionVO fromEntity(ChatSession session) {
        SessionVO vo = new SessionVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setTitle(session.getTitle());
        vo.setType(session.getType());
        vo.setStatus(session.getStatus());
        vo.setCreatedBy(String.valueOf(session.getCreatedBy()));
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public MessageVO getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageVO lastMessage) {
        this.lastMessage = lastMessage;
    }
}
