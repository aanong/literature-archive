package com.literature.chat.netty.ws;

import com.literature.chat.netty.handler.AuthHandler;
import com.literature.chat.netty.handler.ChatMessageHandler;
import com.literature.chat.netty.handler.ExceptionHandler;
import com.literature.chat.netty.handler.HeartbeatHandler;
import com.literature.chat.netty.handler.SessionCleanupHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private ChatMessageHandler chatMessageHandler;

    @Autowired
    private ExceptionHandler exceptionHandler;

    @Autowired
    private SessionCleanupHandler sessionCleanupHandler;

    @Autowired
    private WebSocketFrameToNettyMessageHandler wsInboundHandler;

    @Autowired
    private NettyMessageToWebSocketFrameEncoder wsOutboundEncoder;

    private EventExecutorGroup businessGroup;

    private final HeartbeatHandler sharedHeartbeatHandler = new HeartbeatHandler();

    @Value("${netty.ws.path:/ws}")
    private String wsPath;

    public void setBusinessGroup(EventExecutorGroup businessGroup) {
        this.businessGroup = businessGroup;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(sessionCleanupHandler);
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new WebSocketServerProtocolHandler(wsPath, null, true));

        pipeline.addLast(wsInboundHandler);

        pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(sharedHeartbeatHandler);

        if (businessGroup != null) {
            pipeline.addLast(businessGroup, authHandler);
            pipeline.addLast(businessGroup, chatMessageHandler);
        } else {
            pipeline.addLast(authHandler);
            pipeline.addLast(chatMessageHandler);
        }

        pipeline.addLast(wsOutboundEncoder);
        pipeline.addLast(exceptionHandler);
    }
}
