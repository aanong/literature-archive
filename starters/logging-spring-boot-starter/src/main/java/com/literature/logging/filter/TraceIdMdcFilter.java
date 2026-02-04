package com.literature.logging.filter;

import com.literature.logging.autoconfigure.LoggingProperties;
import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdMdcFilter implements Filter {

  private final LoggingProperties properties;

  public TraceIdMdcFilter(LoggingProperties properties) {
    this.properties = properties;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String traceId = httpRequest.getHeader(properties.getTrace().getHeaderName());

    if (traceId == null || traceId.isBlank()) {
      try {
        traceId = TraceContext.traceId();
      } catch (Exception ignored) {
        traceId = null;
      }
    }
    if ((traceId == null || traceId.isBlank()) && properties.getTrace().isAutoGenerate()) {
      traceId = UUID.randomUUID().toString().replace("-", "");
    }

    if (traceId != null) {
      MDC.put(properties.getTrace().getMdcKey(), traceId);
    }
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(properties.getTrace().getMdcKey());
    }
  }
}
