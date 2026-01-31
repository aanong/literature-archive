package com.literature.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;

/**
 * 本地开发安全配置
 * 为了解决启动时找不到 JwtDecoder 的问题
 */
@Configuration
public class LocalSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        // 注意：这只是为了让服务在没有 auth-service 的情况下能启动
        // 正常的 JWT 校验逻辑在生产环境应由 issuer-uri 或 jwk-set-uri 自动配置
        String secret = "literature-archive-secret-key-local-dev-only-123456";
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
