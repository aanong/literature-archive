package com.literature.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 封禁记录实体
 */
@TableName("chat_bans")
public class ChatBan {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    private String status;

    private String reason;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("lifted_at")
    private LocalDateTime liftedAt;

    @TableField("lifted_by")
    private Long liftedBy;

    // 常量定义
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_LIFTED = "lifted";

    /**
     * 检查封禁是否仍然有效
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * 解除封禁
     */
    public void lift(Long operatorId) {
        this.status = STATUS_LIFTED;
        this.liftedAt = LocalDateTime.now();
        this.liftedBy = operatorId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
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

    public Long getLiftedBy() {
        return liftedBy;
    }

    public void setLiftedBy(Long liftedBy) {
        this.liftedBy = liftedBy;
    }
}
