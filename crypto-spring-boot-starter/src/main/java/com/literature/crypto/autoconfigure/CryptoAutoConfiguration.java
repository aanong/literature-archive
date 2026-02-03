package com.literature.crypto.autoconfigure;

import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import com.literature.crypto.core.SignatureUtils;
import com.literature.crypto.core.VaultKeyProvider;
import com.literature.crypto.http.CryptoRequestFilter;
import com.literature.crypto.http.CryptoResponseAdvice;
import com.literature.crypto.http.EncryptResponseAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CryptoProperties.class)
@ConditionalOnProperty(prefix = "literature.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CryptoAutoConfiguration {

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

  @Bean
  @ConditionalOnProperty(prefix = "literature.crypto.http", name = "enabled", havingValue = "true", matchIfMissing = true)
  public CryptoRequestFilter cryptoRequestFilter(CryptoProperties properties, SignatureUtils signatureUtils, AesGcmCrypto aesGcmCrypto, KeyGenerator keyGenerator) {
    return new CryptoRequestFilter(properties, signatureUtils, aesGcmCrypto, keyGenerator);
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.crypto.http", name = "enabled", havingValue = "true", matchIfMissing = true)
  public CryptoResponseAdvice cryptoResponseAdvice(CryptoProperties properties, AesGcmCrypto aesGcmCrypto, KeyGenerator keyGenerator,
                                                   EncryptResponseAdvice encryptResponseAdvice) {
    return new CryptoResponseAdvice(properties, aesGcmCrypto, keyGenerator, encryptResponseAdvice);
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.crypto.http", name = "enabled", havingValue = "true", matchIfMissing = true)
  public EncryptResponseAdvice encryptResponseAdvice() {
    return new EncryptResponseAdvice();
  }

  @Bean
  @ConditionalOnMissingBean
  public VaultKeyProvider vaultKeyProvider() {
    return new VaultKeyProvider();
  }

}
