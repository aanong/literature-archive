package com.literature.user.controller;

import com.literature.common.core.dto.UserDTO;
import com.literature.common.core.model.ApiResponse;
import com.literature.user.model.LoginRequest;
import com.literature.user.model.RegisterRequest;
import com.literature.user.model.TokenResponse;
import com.literature.user.service.CUserAuthService;
import com.literature.user.service.CUserRegisterService;
import com.literature.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

  private final CUserAuthService cUserAuthService;
  private final CUserRegisterService cUserRegisterService;
  private final UserService userService;

  public UserAuthController(CUserAuthService cUserAuthService,
      CUserRegisterService cUserRegisterService,
      UserService userService) {
    this.cUserAuthService = cUserAuthService;
    this.cUserRegisterService = cUserRegisterService;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest requestBody,
      HttpServletRequest request) {
    return ApiResponse.success(cUserAuthService.login(requestBody), request.getHeader("X-Trace-Id"));
  }

  @PostMapping("/register")
  public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest requestBody,
      HttpServletRequest request) {
    cUserRegisterService.register(requestBody);
    return ApiResponse.success(null, request.getHeader("X-Trace-Id"));
  }

  @GetMapping("/me")
  public ApiResponse<UserDTO> me(Authentication authentication, HttpServletRequest request) {
    String username = authentication.getName();
    String userType = "C_USER";
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Map<String, Object> claims = jwtAuth.getToken().getClaims();
      Object userTypeClaim = claims.get("userType");
      if (userTypeClaim != null) {
        userType = String.valueOf(userTypeClaim);
      }
    }
    UserDTO user = userService.getUserByUsername(username, userType);
    return ApiResponse.success(user, request.getHeader("X-Trace-Id"));
  }
}
