package com.literature.gateway.web;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {
  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  public Mono<Void> filter(org.springframework.cloud.gateway.filter.ServerWebExchange exchange,
      org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
    String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
      ServerHttpRequest request = exchange.getRequest().mutate()
          .header(TRACE_ID_HEADER, traceId)
          .build();
      return chain.filter(exchange.mutate().request(request).build());
    }
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
