package com.literature.common.core.web;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    return ApiResponse.error(ErrorCode.INVALID_PARAM, ex.getMessage(), request.getHeader("X-Trace-Id"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
    return ApiResponse.error(ErrorCode.INVALID_PARAM, ex.getMessage(), request.getHeader("X-Trace-Id"));
  }

  /**
   * Sentinel 限流/熔断/降级异常统一处理
   */
  @ExceptionHandler(BlockException.class)
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  public ApiResponse<Void> handleBlockException(BlockException ex, HttpServletRequest request) {
    String message = getBlockExceptionMessage(ex);
    return ApiResponse.error(ErrorCode.TOO_MANY_REQUESTS, message, request.getHeader("X-Trace-Id"));
  }

  /**
   * 根据不同的 BlockException 类型返回对应的错误信息
   */
  private String getBlockExceptionMessage(BlockException ex) {
    if (ex instanceof FlowException) {
      return "请求过于频繁，请稍后再试";
    } else if (ex instanceof DegradeException) {
      return "服务降级中，请稍后再试";
    } else if (ex instanceof ParamFlowException) {
      return "热点参数限流，请稍后再试";
    } else if (ex instanceof SystemBlockException) {
      return "系统负载过高，请稍后再试";
    } else if (ex instanceof AuthorityException) {
      return "授权规则不通过";
    }
    return "服务限流中，请稍后再试";
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleGeneric(Exception ex, HttpServletRequest request) {
    return ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.getMessage(), request.getHeader("X-Trace-Id"));
  }
}
