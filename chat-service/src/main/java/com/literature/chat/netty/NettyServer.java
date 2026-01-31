package com.literature.chat.netty;

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

/**
 * Netty 服务启动类
 */
@Slf4j
@Component
public class NettyServer {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    // 业务线程池，用于处理耗时业务逻辑
    private final EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(16);

    @Value("${netty.port:9090}")
    private int port;

    @Autowired
    private NettyServerInitializer nettyServerInitializer;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                // 注入业务线程池
                nettyServerInitializer.setBusinessGroup(businessGroup);

                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 10240) // Increased backlog for high burst
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                                new io.netty.channel.WriteBufferWaterMark(32 * 1024, 64 * 1024)) // Prevent OOM
                        .childHandler(nettyServerInitializer);

                ChannelFuture future = bootstrap.bind(port).sync();
                log.info("Netty server started on port {}", port);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty server interrupted", e);
                Thread.currentThread().interrupt();
            } finally {
                stop();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping Netty server...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        businessGroup.shutdownGracefully();
        log.info("Netty server stopped");
    }

    public EventExecutorGroup getBusinessGroup() {
        return businessGroup;
    }
}
