package com.literature.user.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.user.model.LoginRequest;
import com.literature.user.model.TokenResponse;
import com.literature.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest requestBody,
      HttpServletRequest request) {
    return ApiResponse.success(authService.login(requestBody), request.getHeader("X-Trace-Id"));
  }
}
