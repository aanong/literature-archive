package com.gmrfid.monitor.controller;

import com.gmrfid.common.core.model.ApiResponse;
import com.gmrfid.common.core.model.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/monitor")
public class MonitorController {

  @GetMapping("/metrics")
  public ApiResponse<Map<String, Object>> metrics(HttpServletRequest request) {
    return ApiResponse.success(Map.of("items", List.of()), request.getHeader("X-Trace-Id"));
  }

  @GetMapping("/traces")
  public ApiResponse<PageResponse<Map<String, Object>>> traces(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    return ApiResponse.success(new PageResponse<>(0, List.of()), request.getHeader("X-Trace-Id"));
  }

  @GetMapping("/alerts")
  public ApiResponse<PageResponse<Map<String, Object>>> alerts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    return ApiResponse.success(new PageResponse<>(0, List.of()), request.getHeader("X-Trace-Id"));
  }
}
