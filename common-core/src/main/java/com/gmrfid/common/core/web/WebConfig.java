package com.gmrfid.common.core.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

  @Bean
  public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
    FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new TraceIdFilter());
    registration.setOrder(1);
    return registration;
  }
}
