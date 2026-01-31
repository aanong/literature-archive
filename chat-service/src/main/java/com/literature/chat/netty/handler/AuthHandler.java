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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessage msg) throws Exception {
        if (msg.getHeader().getCmdType() == CmdType.AUTH_VALUE) {
            AuthPayload payload = (AuthPayload) msg.getBody();
            String token = payload.getToken();
            // TODO: Real JWT validation
            if (token != null && token.startsWith("user:")) {
                Long userId = Long.parseLong(token.split(":")[1]);
                sessionManager.addSession(userId, ctx.channel());

                // Register route
                String serverAddress = java.net.InetAddress.getLocalHost().getHostAddress() + ":" + port;
                sessionRouteService.registerUserRoute(userId, serverAddress);

                // Pull offline messages
                offlineMessageService.pullAndPushOfflineMessages(userId);

                ctx.pipeline().remove(this); // Remove self
                log.info("User {} authenticated and route registered at {}", userId, serverAddress);
            } else {
                log.warn("Authentication failed for channel {}", ctx.channel().id());
                ctx.close();
            }
        } else {
            // Unauthenticated message
            log.warn("Unauthenticated message received, closing channel {}", ctx.channel().id());
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.removeSession(ctx.channel());
        // Remove route? Ideally yes, but we need userId.
        // SessionManager maps channel -> userId.
        Long userId = sessionManager.getUserId(ctx.channel());
        if (userId != null) {
            sessionRouteService.removeUserRoute(userId);
        }
        super.channelInactive(ctx);
    }
}
