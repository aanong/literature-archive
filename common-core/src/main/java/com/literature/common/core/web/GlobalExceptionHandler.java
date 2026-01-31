package com.literature.common.core.web;

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

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleGeneric(Exception ex, HttpServletRequest request) {
    return ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.getMessage(), request.getHeader("X-Trace-Id"));
  }
}
