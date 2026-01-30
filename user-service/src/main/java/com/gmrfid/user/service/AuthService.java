package com.gmrfid.user.service;

import com.gmrfid.user.model.LoginRequest;
import com.gmrfid.user.model.TokenResponse;
import com.gmrfid.user.model.UserAccount;
import com.gmrfid.user.repository.PermissionRepository;
import com.gmrfid.user.repository.UserAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserAccountRepository userAccountRepository;
  private final PermissionRepository permissionRepository;
  private final PasswordService passwordService;
  private final JwtTokenService jwtTokenService;

  public AuthService(UserAccountRepository userAccountRepository,
      PermissionRepository permissionRepository,
      PasswordService passwordService,
      JwtTokenService jwtTokenService) {
    this.userAccountRepository = userAccountRepository;
    this.permissionRepository = permissionRepository;
    this.passwordService = passwordService;
    this.jwtTokenService = jwtTokenService;
  }

  public TokenResponse login(LoginRequest request) {
    UserAccount account = userAccountRepository.findByUsername(request.username())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!passwordService.matches(request.password(), account.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    List<String> permissions = permissionRepository.findPermissionCodesByUserId(account.getId());
    String token = jwtTokenService.issueToken(account.getUsername(), permissions);
    return new TokenResponse(token, jwtTokenService.getExpireMinutes() * 60);
  }
}
