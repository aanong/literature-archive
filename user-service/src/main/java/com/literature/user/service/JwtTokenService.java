package com.literature.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
  private final SecretKey secretKey;
  private final String issuer;
  private final long expireMinutes;

  public JwtTokenService(@Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.expire-minutes}") long expireMinutes) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.expireMinutes = expireMinutes;
  }

  public String issueToken(String subject, List<String> permissions) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(subject)
        .claim("permissions", permissions)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public long getExpireMinutes() {
    return expireMinutes;
  }
}
