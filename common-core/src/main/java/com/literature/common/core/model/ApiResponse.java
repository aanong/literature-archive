package com.literature.common.core.model;

import org.slf4j.MDC;

public record ApiResponse<T>(String code, String message, T data, String traceId) {

  private static final String TRACE_ID_KEY = "X-Trace-Id";

  /**
   * 成功响应 — 自动从 MDC 获取 traceId
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("0000", "success", data, MDC.get(TRACE_ID_KEY));
  }

  /**
   * 成功响应 — 手动传入 traceId（向后兼容）
   */
  public static <T> ApiResponse<T> success(T data, String traceId) {
    return new ApiResponse<>("0000", "success", data, traceId);
  }

  /**
   * 错误响应 — 自动从 MDC 获取 traceId
   */
  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(code, message, null, MDC.get(TRACE_ID_KEY));
  }

  /**
   * 错误响应 — 手动传入 traceId（向后兼容）
   */
  public static <T> ApiResponse<T> error(String code, String message, String traceId) {
    return new ApiResponse<>(code, message, null, traceId);
  }
}
