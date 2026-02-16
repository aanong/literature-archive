package com.literature.common.core.dto;

/**
 * 用户信息 DTO，用于服务间调用
 */
public record UserDTO(
    Long id,
    String username,
    String nickname,
    String status,
    String userType
) {
    /**
     * 创建默认的降级用户信息
     */
    public static UserDTO defaultUser(Long userId) {
        return new UserDTO(userId, "unknown", "未知用户", "UNKNOWN", "UNKNOWN");
    }
}
