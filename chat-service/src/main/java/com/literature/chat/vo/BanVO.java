package com.literature.chat.vo;

import com.literature.chat.entity.ChatBan;
import java.time.LocalDateTime;

/**
 * 封禁视图对象
 */
public class BanVO {

    private String id;
    private String sessionId;
    private String userId;
    private String status;
    private String reason;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime liftedAt;
    private String liftedBy;

    /**
     * 从实体转换
     */
    public static BanVO fromEntity(ChatBan ban) {
        BanVO vo = new BanVO();
        vo.setId(String.valueOf(ban.getId()));
        vo.setSessionId(String.valueOf(ban.getSessionId()));
        vo.setUserId(String.valueOf(ban.getUserId()));
        vo.setStatus(ban.getStatus());
        vo.setReason(ban.getReason());
        vo.setCreatedBy(String.valueOf(ban.getCreatedBy()));
        vo.setCreatedAt(ban.getCreatedAt());
        vo.setLiftedAt(ban.getLiftedAt());
        if (ban.getLiftedBy() != null) {
            vo.setLiftedBy(String.valueOf(ban.getLiftedBy()));
        }
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getLiftedAt() {
        return liftedAt;
    }

    public void setLiftedAt(LocalDateTime liftedAt) {
        this.liftedAt = liftedAt;
    }

    public String getLiftedBy() {
        return liftedBy;
    }

    public void setLiftedBy(String liftedBy) {
        this.liftedBy = liftedBy;
    }
}
