package com.literature.chat.netty.handler;

import com.literature.chat.netty.protocol.AuthPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import com.literature.chat.netty.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * 认证处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<NettyMessage> {

    @Autowired
    private com.literature.chat.netty.session.SessionManager sessionManager;

    @Autowired
    private com.literature.chat.service.SessionRouteService sessionRouteService;

    @Autowired
    private com.literature.chat.service.OfflineMessageService offlineMessageService;

    @Autowired
    private com.literature.common.core.feign.UserServiceClient userServiceClient;

    @org.springframework.beans.factory.annotation.Value("${netty.port:18091}")
    private int port;

    @Autowired
    private JwtDecoder jwtDecoder;

    @org.springframework.beans.factory.annotation.Value("${chat.security.allow-unsigned:true}")
    private boolean allowUnsigned;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessage msg) throws Exception {
        if (msg.getHeader().getCmdType() == CmdType.AUTH_VALUE) {
            AuthPayload payload = (AuthPayload) msg.getBody();
            String token = payload.getToken();
            // Real JWT validation
            if (token != null && !token.isEmpty()) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String userType = jwt.getClaim("userType");
                    Long userId = extractUserId(jwt);
                    if (userId == null) {
                        String username = jwt.getSubject();
                        com.literature.common.core.dto.UserDTO user = resolveUserByUsername(username, userType);
                        if (user != null) {
                            userId = user.id();
                            if (userType == null || userType.isBlank()) {
                                userType = user.userType();
                            }
                        }
                    }
                    if (userId == null) {
                        throw new IllegalArgumentException("userId is missing in JWT and lookup failed");
                    }
                    if (userType == null || userType.isBlank()) {
                        userType = "UNKNOWN";
                    }

                    sessionManager.addSession(userId, userType, ctx.channel());

                    // Register route
                    String serverAddress = java.net.InetAddress.getLocalHost().getHostAddress() + ":" + port;
                    sessionRouteService.registerUserRoute(userId, userType, serverAddress);

                    // Pull offline messages
                    offlineMessageService.pullAndPushOfflineMessages(userId, userType);

                    ctx.pipeline().remove(this); // Remove self
                    log.info("User {} authenticated and route registered at {}", userId, serverAddress);
                } catch (JwtException e) {
                    if (allowUnsigned) {
                        Jwt jwt = tryParseUnsignedJwt(token);
                        if (jwt != null) {
                            String userType = jwt.getClaim("userType");
                            Long userId = extractUserId(jwt);
                            if (userId == null) {
                                String username = jwt.getSubject();
                                com.literature.common.core.dto.UserDTO user = resolveUserByUsername(username, userType);
                                if (user != null) {
                                    userId = user.id();
                                    if (userType == null || userType.isBlank()) {
                                        userType = user.userType();
                                    }
                                }
                            }
                            if (userId != null) {
                                if (userType == null || userType.isBlank()) {
                                    userType = "UNKNOWN";
                                }
                                sessionManager.addSession(userId, userType, ctx.channel());
                                String serverAddress = java.net.InetAddress.getLocalHost().getHostAddress() + ":" + port;
                                sessionRouteService.registerUserRoute(userId, userType, serverAddress);
                                offlineMessageService.pullAndPushOfflineMessages(userId, userType);
                                ctx.pipeline().remove(this);
                                log.info("User {} authenticated (unsigned) and route registered at {}", userId, serverAddress);
                                return;
                            }
                        }
                    }
                    log.warn("Authentication failed for channel {}: {}", ctx.channel().id(), e.getMessage());
                    ctx.close();
                } catch (IllegalArgumentException e) {
                    log.warn("Authentication failed for channel {}: {}", ctx.channel().id(), e.getMessage());
                    ctx.close();
                } catch (Exception e) {
                    log.error("Unexpected authentication error", e);
                    ctx.close();
                }
            } else {
                log.warn("Authentication failed for channel {}: Token is missing", ctx.channel().id());
                ctx.close();
            }
        } else {
            // Unauthenticated message
            log.warn("Unauthenticated message received, closing channel {}", ctx.channel().id());
            ctx.close();
        }
    }

    private Long extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        if (userIdClaim instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        String subject = jwt.getSubject();
        if (subject != null) {
            try {
                return Long.parseLong(subject);
            } catch (NumberFormatException ignored) {
                // subject is not numeric
            }
        }
        return null;
    }

    private com.literature.common.core.dto.UserDTO resolveUserByUsername(String username, String userType) {
        if (username == null || username.isBlank()) {
            return null;
        }
        try {
            var response = userServiceClient.getUserByUsername(username, userType);
            if (response != null && response.data() != null) {
                return response.data();
            }
            if (userType != null) {
                response = userServiceClient.getUserByUsername(username, null);
                if (response != null && response.data() != null) {
                    return response.data();
                }
            }
        } catch (Exception e) {
            log.warn("User lookup failed for username {}", username, e);
        }
        return null;
    }

    private Jwt tryParseUnsignedJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = parts[1];
            String json = new String(java.util.Base64.getUrlDecoder().decode(payload));
            java.util.Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, java.util.Map.class);
            return Jwt.withTokenValue(token)
                    .headers(h -> h.put("alg", "none"))
                    .claims(c -> c.putAll(claims))
                    .subject((String) claims.getOrDefault("sub", ""))
                    .issuedAt(java.time.Instant.now())
                    .expiresAt(java.time.Instant.now().plusSeconds(3600))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    // 注意：channelInactive 已由 SessionCleanupHandler 统一处理，
    // AuthHandler 认证成功后会 remove(this)，所以不能在这里做清理。
}
