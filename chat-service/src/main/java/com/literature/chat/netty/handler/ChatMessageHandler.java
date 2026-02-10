package com.literature.chat.netty.handler;

import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import com.literature.chat.netty.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ChatMessageHandler extends SimpleChannelInboundHandler<NettyMessage> {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private com.literature.chat.service.SessionRouteService sessionRouteService;

    @Autowired
    private com.literature.chat.service.OfflineMessageService offlineMessageService;

    @Autowired
    private com.literature.chat.mq.ChatMessageProducer chatMessageProducer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessage msg) throws Exception {
        if (msg.getHeader().getCmdType() == CmdType.SINGLE_CHAT_VALUE) {
            handleSingleChat(ctx, (ChatPayload) msg.getBody());
        } else if (msg.getHeader().getCmdType() == CmdType.GROUP_CHAT_VALUE) {
            handleGroupChat(ctx, (ChatPayload) msg.getBody());
        } else if (msg.getHeader().getCmdType() == CmdType.HEARTBEAT_VALUE) {
            // 心跳响应
        }
    }

    private void handleGroupChat(ChannelHandlerContext ctx, ChatPayload payload) {
        Long sessionId = payload.getTargetId();

        // 持久化消息（历史记录）
        com.literature.chat.dto.ChatMessageDTO dto = new com.literature.chat.dto.ChatMessageDTO();
        dto.setMessageId(payload.getTimestamp());
        dto.setSenderId(payload.getSenderId());
        dto.setSessionId(sessionId);
        dto.setType(2); // 群聊
        dto.setContent(payload.getContent());
        dto.setTimestamp(payload.getTimestamp());
        offlineMessageService.saveOfflineMessage(dto);

        // 广播到所有服务器（包括自身）以处理群组消息分发
        // "BROADCAST" 只是一个占位符，因为我们已将 Producer 修改为忽略
        // targetServer 并直接广播到 Topic。
        chatMessageProducer.sendToServer("BROADCAST", dto);
        log.info("Group message persisted and broadcasted for session {}", sessionId);

        // 我们不需要在此处进行本地推送，因为部署在同一服务器上的 Consumer 会
        // 接收消息并执行推送！
        // 这样可以确保一致性。
    }

    private void handleSingleChat(ChannelHandlerContext ctx, ChatPayload payload) {
        Long targetId = payload.getTargetId();

        // 持久化消息（历史记录和离线）
        com.literature.chat.dto.ChatMessageDTO dto = new com.literature.chat.dto.ChatMessageDTO();
        dto.setMessageId(payload.getTimestamp()); // 暂时使用时间戳作为 ID
        dto.setSenderId(payload.getSenderId());
        dto.setTargetUserId(targetId);
        dto.setType(1); // 单聊
        dto.setContent(payload.getContent());
        dto.setTimestamp(payload.getTimestamp());
        offlineMessageService.saveOfflineMessage(dto);

        Channel targetChannel = sessionManager.getChannel(targetId);

        if (targetChannel != null && targetChannel.isActive()) {
            // 本地推送
            forwardMessage(targetChannel, payload);
            log.info("Message forwarded locally from {} to {}", payload.getSenderId(), targetId);
        } else {
            // 检查远程路由
            String targetServer = sessionRouteService.getUserRoute(targetId);
            if (targetServer != null) {
                // 通过 Kafka 转发
                chatMessageProducer.sendToServer(targetServer, dto);
                log.info("Message routed to server {} for user {}", targetServer, targetId);
            } else {
                log.info("User {} is offline, saving to Redis Offline Queue", targetId);
                offlineMessageService.pushToOfflineQueue(targetId, dto);
            }
        }
    }

    private void forwardMessage(Channel targetChannel, ChatPayload payload) {
        NettyMessage message = new NettyMessage();
        NettyMessage.Header header = new NettyMessage.Header();
        header.setMagic((short) 0xCAFE);
        header.setVersion((byte) 1);
        header.setSerial((byte) 1);
        header.setCmdType((byte) CmdType.SINGLE_CHAT_VALUE);
        header.setReqId(System.currentTimeMillis());
        message.setHeader(header);
        message.setBody(payload);

        targetChannel.writeAndFlush(message).addListener((io.netty.channel.ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("消息写入 Channel {} 失败", targetChannel.id(), future.cause());
            }
        });
    }
}
