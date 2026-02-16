package com.literature.user.service;

import com.literature.user.mapper.CUserAccountMapper;
import com.literature.user.model.CUserAccount;
import com.literature.user.model.LoginRequest;
import com.literature.user.model.TokenResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CUserAuthService {

  private final CUserAccountMapper cUserAccountMapper;
  private final PasswordService passwordService;
  private final JwtTokenService jwtTokenService;

  public CUserAuthService(CUserAccountMapper cUserAccountMapper,
      PasswordService passwordService,
      JwtTokenService jwtTokenService) {
    this.cUserAccountMapper = cUserAccountMapper;
    this.passwordService = passwordService;
    this.jwtTokenService = jwtTokenService;
  }

  public TokenResponse login(LoginRequest request) {
    CUserAccount account = cUserAccountMapper.findByUsername(request.username());
    if (account == null) {
      throw new IllegalArgumentException("User not found");
    }

    if (!passwordService.matches(request.password(), account.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtTokenService.issueToken(
        account.getUsername(),
        List.of("chat:send", "chat:read"),
        "C_USER",
        account.getId());
    return new TokenResponse(token, jwtTokenService.getExpireMinutes() * 60);
  }
}
