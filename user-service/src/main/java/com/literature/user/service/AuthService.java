package com.literature.user.service;

import com.literature.user.mapper.PermissionMapper;
import com.literature.user.mapper.UserAccountMapper;
import com.literature.user.model.LoginRequest;
import com.literature.user.model.TokenResponse;
import com.literature.user.model.UserAccount;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserAccountMapper userAccountMapper;
  private final PermissionMapper permissionMapper;
  private final PasswordService passwordService;
  private final JwtTokenService jwtTokenService;

  public AuthService(UserAccountMapper userAccountMapper,
      PermissionMapper permissionMapper,
      PasswordService passwordService,
      JwtTokenService jwtTokenService) {
    this.userAccountMapper = userAccountMapper;
    this.permissionMapper = permissionMapper;
    this.passwordService = passwordService;
    this.jwtTokenService = jwtTokenService;
  }

  public TokenResponse login(LoginRequest request) {
    UserAccount account = userAccountMapper.findByUsername(request.username());
    if (account == null) {
      throw new IllegalArgumentException("User not found");
    }

    // 注意：这里需要确保 PasswordService 的 matches 方法签名匹配
    if (!passwordService.matches(request.password(), account.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    List<String> permissions = permissionMapper.findPermissionCodesByUserId(account.getId());
    String token = jwtTokenService.issueToken(account.getUsername(), permissions);
    return new TokenResponse(token, jwtTokenService.getExpireMinutes() * 60);
  }
}
