package com.literature.user.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RSA 密钥对配置属性
 * 从配置文件读取持久化的 RSA 密钥，避免每次重启生成新密钥导致 JWT 验证失败
 */
@ConfigurationProperties(prefix = "security.jwt.rsa")
public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
}
