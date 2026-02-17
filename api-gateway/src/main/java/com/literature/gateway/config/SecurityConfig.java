package com.literature.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchange -> exchange
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/api/admin/auth/**").permitAll()
            .pathMatchers("/api/auth/login").permitAll()
            .pathMatchers("/api/auth/register").permitAll()
            .pathMatchers("/api/content/**").permitAll()
            .pathMatchers("/api/internal/**").permitAll()
            .pathMatchers("/gateway/security/**").permitAll()
            .pathMatchers("/.well-known/jwks.json").permitAll()
            .anyExchange().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
        .build();
  }
}
