package com.literature.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 会话路由服务 (基于 Redis)
 */
@Slf4j
@Service
public class SessionRouteService {

    private static final String USER_ROUTE_KEY = "chat:route:user:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 用户登录时注册路由
     *
     * @param userId        用户ID
     * @param serverAddress 当前服务器地址 (IP:Port)
     */
    public void registerUserRoute(Long userId, String serverAddress) {
        redisTemplate.opsForValue().set(
                USER_ROUTE_KEY + userId,
                serverAddress,
                Duration.ofHours(24) // 24小时过期，心跳保活时可续期
        );
        log.info("Registered route for user {}: {}", userId, serverAddress);
    }

    /**
     * 查询用户所在服务器
     *
     * @param userId 用户ID
     * @return 服务器地址，如果离线则返回 null
     */
    public String getUserRoute(Long userId) {
        return redisTemplate.opsForValue().get(USER_ROUTE_KEY + userId);
    }

    /**
     * 用户下线时移除路由
     *
     * @param userId 用户ID
     */
    public void removeUserRoute(Long userId) {
        redisTemplate.delete(USER_ROUTE_KEY + userId);
        log.info("Removed route for user {}", userId);
    }
}
