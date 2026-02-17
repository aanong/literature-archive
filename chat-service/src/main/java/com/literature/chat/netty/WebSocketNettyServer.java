package com.literature.chat.netty;

import com.literature.chat.netty.ws.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketNettyServer {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(16);

    @Value("${netty.ws.port:18092}")
    private int port;

    @Autowired
    private WebSocketServerInitializer webSocketServerInitializer;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                webSocketServerInitializer.setBusinessGroup(businessGroup);

                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 10240)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                                new io.netty.channel.WriteBufferWaterMark(32 * 1024, 64 * 1024))
                        .childHandler(webSocketServerInitializer);

                ChannelFuture future = bootstrap.bind(port).sync();
                log.info("WebSocket Netty server started on port {} (path=/ws)", port);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("WebSocket Netty server interrupted", e);
                Thread.currentThread().interrupt();
            } finally {
                stop();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping WebSocket Netty server...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        businessGroup.shutdownGracefully();
        log.info("WebSocket Netty server stopped");
    }
}
