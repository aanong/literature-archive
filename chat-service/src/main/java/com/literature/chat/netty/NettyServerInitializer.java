package com.literature.chat.netty;

import com.literature.chat.netty.codec.ChatProtocolDecoder;
import com.literature.chat.netty.codec.ChatProtocolEncoder;
import com.literature.chat.netty.handler.AuthHandler;
import com.literature.chat.netty.handler.ChatMessageHandler;
import com.literature.chat.netty.handler.ExceptionHandler;
import com.literature.chat.netty.handler.HeartbeatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Netty Channel 初始化器
 */
@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private ChatMessageHandler chatMessageHandler;

    @Autowired
    private ExceptionHandler exceptionHandler;

    private EventExecutorGroup businessGroup;

    public void setBusinessGroup(EventExecutorGroup businessGroup) {
        this.businessGroup = businessGroup;
    }

    @Autowired
    public NettyServerInitializer() {
        // Spring 自动注入其他依赖，businessGroup 将通过 setter 方法设置
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 1. 解决粘包半包 (maxFrameLength: 10MB)
        pipeline.addLast(new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 13, 4, 0, 0));

        // 2. 编解码器
        pipeline.addLast(new ChatProtocolDecoder());
        pipeline.addLast(new ChatProtocolEncoder());

        // 3. 心跳检测 (读空闲 60秒)
        pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new HeartbeatHandler()); // 每次 new 一个，或者 HeartbeatHandler 加 @Sharable

        // 4. 业务处理器 (使用业务线程池)
        if (businessGroup != null) {
            pipeline.addLast(businessGroup, authHandler);
            pipeline.addLast(businessGroup, chatMessageHandler);
        } else {
            pipeline.addLast(authHandler);
            pipeline.addLast(chatMessageHandler);
        }

        pipeline.addLast(exceptionHandler);
    }
}
