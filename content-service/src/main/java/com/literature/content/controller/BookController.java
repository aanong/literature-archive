package com.literature.content.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.security.PermissionGuard;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/books")
public class BookController {

  @GetMapping("/health")
  @PreAuthorize("@permissionGuard.hasPermission(authentication, 'BOOK_READ')")
  public ApiResponse<String> health(HttpServletRequest request) {
    return ApiResponse.success("ok", request.getHeader("X-Trace-Id"));
  }
}
