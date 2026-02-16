package com.literature.common.core.feign;

import com.literature.common.core.dto.UserDTO;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * UserServiceClient 熔断降级工厂
 */
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallbackFactory.class);

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("UserServiceClient 调用失败，触发熔断降级", cause);
        
        return new UserServiceClient() {
            @Override
            public ApiResponse<UserDTO> getUserById(Long userId, String userType) {
                log.warn("getUserById 降级处理, userId: {}, userType: {}", userId, userType);
                return ApiResponse.error(
                    ErrorCode.DOWNSTREAM_ERROR,
                    "用户服务暂时不可用",
                    null
                );
            }

            @Override
            public ApiResponse<UserDTO> getUserByUsername(String username, String userType) {
                log.warn("getUserByUsername 降级处理, username: {}, userType: {}", username, userType);
                return ApiResponse.error(
                    ErrorCode.DOWNSTREAM_ERROR,
                    "用户服务暂时不可用",
                    null
                );
            }
        };
    }
}
