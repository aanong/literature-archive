package com.literature.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
public class JwtDecoderConfig {

  @Bean
  public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer}") String issuer) {
    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return token -> {
      Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
      if (!issuer.equals(claims.getIssuer())) {
        throw new IllegalArgumentException("Invalid issuer");
      }
      return Jwt.withTokenValue(token)
          .issuedAt(claims.getIssuedAt().toInstant())
          .expiresAt(claims.getExpiration().toInstant())
          .issuer(claims.getIssuer())
          .subject(claims.getSubject())
          .claim("permissions", claims.get("permissions"))
          .build();
    };
  }
}
