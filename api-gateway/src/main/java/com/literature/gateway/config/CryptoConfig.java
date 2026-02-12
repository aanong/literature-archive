package com.literature.gateway.config;

import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import com.literature.crypto.core.SignatureUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CryptoProperties.class)
@ConditionalOnProperty(prefix = "literature.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CryptoConfig {

    @Bean
    @ConditionalOnMissingBean
    public AesGcmCrypto aesGcmCrypto() {
        return new AesGcmCrypto();
    }

    @Bean
    @ConditionalOnMissingBean
    public SignatureUtils signatureUtils(CryptoProperties properties) {
        return new SignatureUtils(properties.getHttp().getReplayWindowSeconds());
    }

    @Bean
    @ConditionalOnMissingBean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator();
    }
}
