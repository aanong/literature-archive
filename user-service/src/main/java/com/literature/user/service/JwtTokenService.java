package com.literature.user.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  private final String issuer;
  private final long expireMinutes;

  private KeyPair keyPair;
  private RSAKey rsaJwk;

  public JwtTokenService(
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.expire-minutes}") long expireMinutes) {
    this.issuer = issuer;
    this.expireMinutes = expireMinutes;
  }

  @PostConstruct
  public void init() {
    try {
      // 1. 生成 RSA 密钥对
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      this.keyPair = generator.generateKeyPair();

      // 2. 构建 JWK (用于暴露给 Gateway)
      RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
      RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

      this.rsaJwk = new RSAKey.Builder(publicKey)
          .privateKey(privateKey)
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.RS256)
          .keyID(UUID.randomUUID().toString())
          .build();

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("初始化 RSA 密钥失败", e);
    }
  }

  public String issueToken(String subject, List<String> permissions) {
    Instant now = Instant.now();
    return Jwts.builder()
        .header().add("kid", rsaJwk.getKeyID()).and()
        .issuer(issuer)
        .subject(subject)
        .claim("permissions", permissions)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
        .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
        .compact();
  }

  /**
   * 获取 JWK Set JSON (供 Gateway 使用)
   */
  public String getJwkSetJson() {
    return new JWKSet(rsaJwk).toJSONObject().toString();
  }

  public long getExpireMinutes() {
    return expireMinutes;
  }
}
