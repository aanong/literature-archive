package com.literature.user.controller;

import com.literature.common.core.dto.UserDTO;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.ErrorCode;
import com.literature.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部用户 API 控制器
 * 供其他微服务通过 Feign 调用
 */
@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserDTO> getUserById(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "userType", required = false) String userType,
            HttpServletRequest request) {
        
        UserDTO user = userService.getUserById(userId, userType);
        if (user == null) {
            return ApiResponse.error(
                ErrorCode.NOT_FOUND,
                "用户不存在",
                request.getHeader("X-Trace-Id")
            );
        }
        return ApiResponse.success(user, request.getHeader("X-Trace-Id"));
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public ApiResponse<UserDTO> getUserByUsername(
            @PathVariable("username") String username,
            @RequestParam(value = "userType", required = false) String userType,
            HttpServletRequest request) {
        
        UserDTO user = userService.getUserByUsername(username, userType);
        if (user == null) {
            return ApiResponse.error(
                ErrorCode.NOT_FOUND,
                "用户不存在",
                request.getHeader("X-Trace-Id")
            );
        }
        return ApiResponse.success(user, request.getHeader("X-Trace-Id"));
    }
}
