package com.literature.gateway.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.crypto.autoconfigure.CryptoProperties;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

/**
 * 安全配置控制器
 * <p>
 * 提供动态加密配置。
 * </p>
 */
@RestController
@RequestMapping("/gateway/security")
public class SecurityConfigController {

    private final CryptoProperties cryptoProperties;
    private KeyPair rsaKeyPair;

    public SecurityConfigController(CryptoProperties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.rsaKeyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("初始化 RSA 密钥对失败", e);
        }
    }

    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getSecurityConfig() {
        Map<String, Object> config = new HashMap<>();
        boolean enabled = cryptoProperties.getHttp().isEnabled();
        config.put("encryptionEnabled", enabled);

        if (enabled) {
            String publicKey = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
            config.put("publicKey", publicKey);
        }

        return ApiResponse.success(config, UUID.randomUUID().toString());
    }

    public RSAPrivateKey getPrivateKey() {
        return (RSAPrivateKey) rsaKeyPair.getPrivate();
    }
}
