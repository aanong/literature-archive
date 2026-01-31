package com.literature.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 禁言请求参数
 */
public class MuteRequestDTO {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotNull(message = "禁言时长不能为空")
    @Min(value = 1, message = "禁言时长至少1分钟")
    private Integer minutes;

    private String reason;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
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
