package com.literature.chat.netty;

import com.literature.chat.netty.codec.ChatProtocolDecoder;
import com.literature.chat.netty.codec.ChatProtocolEncoder;
import com.literature.chat.netty.handler.AuthHandler;
import com.literature.chat.netty.handler.ChatMessageHandler;
import com.literature.chat.netty.handler.ExceptionHandler;
import com.literature.chat.netty.handler.HeartbeatHandler;
import com.literature.chat.netty.handler.SessionCleanupHandler;
import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
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

    @Autowired
    private SessionCleanupHandler sessionCleanupHandler;

    @Autowired
    private CryptoProperties cryptoProperties;

    @Autowired
    private AesGcmCrypto aesGcmCrypto;

    @Autowired
    private KeyGenerator keyGenerator;

    private EventExecutorGroup businessGroup;

    // HeartbeatHandler 无状态，可安全共享
    private final HeartbeatHandler sharedHeartbeatHandler = new HeartbeatHandler();

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

        // 0. 会话清理处理器（最早添加，确保 channelInactive 时能清理资源）
        pipeline.addLast(sessionCleanupHandler);

        // 1. 解决粘包半包（maxFrameLength: 256KB，聊天消息不需要 10MB）
        pipeline.addLast(new LengthFieldBasedFrameDecoder(256 * 1024, 13, 4, 0, 0));

        // 2. 编解码器
        pipeline.addLast(new ChatProtocolDecoder());
        pipeline.addLast(new ChatProtocolEncoder());
        // ChatCryptoCodec 是有状态的 MessageToMessageCodec，每个 Channel 必须独立实例
        pipeline.addLast(new com.literature.chat.netty.codec.crypto.ChatCryptoCodec(
                cryptoProperties, aesGcmCrypto, keyGenerator));

        // 3. 心跳检测（读空闲 60秒）
        pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(sharedHeartbeatHandler);

        // 4. 业务处理器（使用业务线程池）
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
