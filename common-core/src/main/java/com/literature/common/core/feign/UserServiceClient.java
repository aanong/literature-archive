package com.literature.common.core.feign;

import com.literature.common.core.dto.UserDTO;
import com.literature.common.core.model.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * 提供用户信息查询接口
 */
@FeignClient(
    name = "user-service",
    fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/api/internal/users/{userId}")
    ApiResponse<UserDTO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/internal/users/username/{username}")
    ApiResponse<UserDTO> getUserByUsername(@PathVariable("username") String username);
}
