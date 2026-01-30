package com.gmrfid.common.core.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class TraceIdFilter extends OncePerRequestFilter {
  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
        .filter(value -> !value.isBlank())
        .orElse(UUID.randomUUID().toString());
    MDC.put(TRACE_ID_HEADER, traceId);
    response.setHeader(TRACE_ID_HEADER, traceId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(TRACE_ID_HEADER);
    }
  }
}
