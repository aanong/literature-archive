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

    @org.springframework.beans.factory.annotation.Value("${netty.port:9090}")
    private int port;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessage msg) throws Exception {
        if (msg.getHeader().getCmdType() == CmdType.AUTH_VALUE) {
            AuthPayload payload = (AuthPayload) msg.getBody();
            String token = payload.getToken();
            // Real JWT validation
            if (token != null && !token.isEmpty()) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String subject = jwt.getSubject();
                    // 假设 subject 就是 userId
                    Long userId = Long.parseLong(subject);

                    sessionManager.addSession(userId, ctx.channel());

                    // Register route
                    String serverAddress = java.net.InetAddress.getLocalHost().getHostAddress() + ":" + port;
                    sessionRouteService.registerUserRoute(userId, serverAddress);

                    // Pull offline messages
                    offlineMessageService.pullAndPushOfflineMessages(userId);

                    ctx.pipeline().remove(this); // Remove self
                    log.info("User {} authenticated and route registered at {}", userId, serverAddress);
                } catch (JwtException | NumberFormatException e) {
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

    // 注意：channelInactive 已由 SessionCleanupHandler 统一处理，
    // AuthHandler 认证成功后会 remove(this)，所以不能在这里做清理。
}
