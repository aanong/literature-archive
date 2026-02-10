package com.literature.chat.netty.handler;

import com.literature.chat.netty.session.SessionManager;
import com.literature.chat.service.SessionRouteService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 会话清理处理器（永久存在于 Pipeline 中）
 * <p>
 * 负责在 Channel 断开时清理 SessionManager 中的映射和 Redis 中的路由信息。
 * 独立于 AuthHandler，不会因 AuthHandler 被移除而失效。
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class SessionCleanupHandler extends ChannelInboundHandlerAdapter {

    private final SessionManager sessionManager;
    private final SessionRouteService sessionRouteService;

    public SessionCleanupHandler(SessionManager sessionManager, SessionRouteService sessionRouteService) {
        this.sessionManager = sessionManager;
        this.sessionRouteService = sessionRouteService;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = sessionManager.removeSession(ctx.channel());
        if (userId != null) {
            sessionRouteService.removeUserRoute(userId);
            log.info("用户 {} 断开连接，已清理会话和路由", userId);
        }
        super.channelInactive(ctx);
    }
}
