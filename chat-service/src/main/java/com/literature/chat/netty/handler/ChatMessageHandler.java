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
            // Heartbeat response
        }
    }

    private void handleGroupChat(ChannelHandlerContext ctx, ChatPayload payload) {
        Long sessionId = payload.getTargetId();

        // Persist message (History)
        com.literature.chat.dto.ChatMessageDTO dto = new com.literature.chat.dto.ChatMessageDTO();
        dto.setMessageId(payload.getTimestamp());
        dto.setSenderId(payload.getSenderId());
        dto.setSessionId(sessionId);
        dto.setType(2); // Group chat
        dto.setContent(payload.getContent());
        dto.setTimestamp(payload.getTimestamp());
        offlineMessageService.saveOfflineMessage(dto);

        // Broadcast to all servers (including self) to handle group fan-out
        // "BROADCAST" is just a placeholder here since we changed Producer to ignore
        // targetServer and broadcast to topic.
        chatMessageProducer.sendToServer("BROADCAST", dto);
        log.info("Group message persisted and broadcasted for session {}", sessionId);

        // We do NOT need to push locally here because Consumer on this same server will
        // pick it up and do the push!
        // This ensures consistency.
    }

    private void handleSingleChat(ChannelHandlerContext ctx, ChatPayload payload) {
        Long targetId = payload.getTargetId();

        // Persist message (History & Offline)
        com.literature.chat.dto.ChatMessageDTO dto = new com.literature.chat.dto.ChatMessageDTO();
        dto.setMessageId(payload.getTimestamp()); // Use timestamp as ID for now
        dto.setSenderId(payload.getSenderId());
        dto.setTargetUserId(targetId);
        dto.setType(1); // Single chat
        dto.setContent(payload.getContent());
        dto.setTimestamp(payload.getTimestamp());
        offlineMessageService.saveOfflineMessage(dto);

        Channel targetChannel = sessionManager.getChannel(targetId);

        if (targetChannel != null && targetChannel.isActive()) {
            // Local push
            forwardMessage(targetChannel, payload);
            log.info("Message forwarded locally from {} to {}", payload.getSenderId(), targetId);
        } else {
            // Check remote route
            String targetServer = sessionRouteService.getUserRoute(targetId);
            if (targetServer != null) {
                // Forward via Kafka
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

        targetChannel.writeAndFlush(message);
    }
}
