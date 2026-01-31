package com.literature.chat.vo;

import com.literature.chat.entity.ChatMute;
import java.time.LocalDateTime;

/**
 * 禁言视图对象
 */
public class MuteVO {

    private String id;
    private String sessionId;
    private String userId;
    private LocalDateTime mutedUntil;
    private String reason;
    private String createdBy;
    private LocalDateTime createdAt;
    private boolean active;

    /**
     * 从实体转换
     */
    public static MuteVO fromEntity(ChatMute mute) {
        MuteVO vo = new MuteVO();
        vo.setId(String.valueOf(mute.getId()));
        vo.setSessionId(String.valueOf(mute.getSessionId()));
        vo.setUserId(String.valueOf(mute.getUserId()));
        vo.setMutedUntil(mute.getMutedUntil());
        vo.setReason(mute.getReason());
        vo.setCreatedBy(String.valueOf(mute.getCreatedBy()));
        vo.setCreatedAt(mute.getCreatedAt());
        vo.setActive(mute.isActive());
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getMutedUntil() {
        return mutedUntil;
    }

    public void setMutedUntil(LocalDateTime mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
