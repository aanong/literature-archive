package com.literature.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 封禁请求参数
 */
public class BanRequestDTO {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    private String reason;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getUserIdLong() {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
