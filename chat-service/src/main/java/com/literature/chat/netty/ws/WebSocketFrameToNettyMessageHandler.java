package com.literature.chat.netty.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.literature.chat.netty.protocol.AuthPayload;
import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import com.literature.chat.netty.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketFrameToNettyMessageHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            log.warn("Unsupported WebSocket frame: {}", frame.getClass().getSimpleName());
            return;
        }

        String text = ((TextWebSocketFrame) frame).text();
        WsMessage wsMessage = objectMapper.readValue(text, WsMessage.class);
        CmdType cmdType = parseCmd(wsMessage.cmd);
        if (cmdType == null) {
            log.warn("Unknown cmd: {}", wsMessage.cmd);
            return;
        }

        NettyMessage nettyMessage = new NettyMessage();
        NettyMessage.Header header = new NettyMessage.Header();
        header.setMagic((short) 0xCAFE);
        header.setVersion((byte) 1);
        header.setSerial((byte) 1);
        header.setCmdType((byte) cmdType.getNumber());
        header.setReqId(System.currentTimeMillis());
        nettyMessage.setHeader(header);

        if (cmdType == CmdType.AUTH) {
            AuthPayload payload = AuthPayload.newBuilder()
                    .setToken(wsMessage.token == null ? "" : wsMessage.token)
                    .build();
            nettyMessage.setBody(payload);
            ctx.fireChannelRead(nettyMessage);
            return;
        }

        if (cmdType == CmdType.HEARTBEAT) {
            nettyMessage.setBody(null);
            ctx.fireChannelRead(nettyMessage);
            return;
        }

        // Chat messages
        long now = System.currentTimeMillis();
        long senderId = wsMessage.senderId != null ? wsMessage.senderId : -1L;
        if (senderId <= 0) {
            Long currentUserId = sessionManager.getUserId(ctx.channel());
            if (currentUserId != null) {
                senderId = currentUserId;
            }
        }

        String extra = wsMessage.extra == null ? "" : wsMessage.extra;
        if ((wsMessage.senderType != null && !wsMessage.senderType.isBlank())
                || (wsMessage.targetUserType != null && !wsMessage.targetUserType.isBlank())) {
            try {
                java.util.Map<String, Object> meta = new java.util.HashMap<>();
                if (wsMessage.senderType != null) {
                    meta.put("senderType", wsMessage.senderType);
                }
                if (wsMessage.targetUserType != null) {
                    meta.put("targetUserType", wsMessage.targetUserType);
                }
                extra = objectMapper.writeValueAsString(meta);
            } catch (Exception e) {
                log.warn("Failed to encode ws meta", e);
            }
        }

        ChatPayload payload = ChatPayload.newBuilder()
                .setCmd(cmdType)
                .setSessionId(wsMessage.sessionId == null ? 0L : wsMessage.sessionId)
                .setSenderId(senderId)
                .setTargetId(wsMessage.targetId == null ? 0L : wsMessage.targetId)
                .setContent(wsMessage.content == null ? "" : wsMessage.content)
                .setContentType(wsMessage.contentType == null ? "text" : wsMessage.contentType)
                .setTimestamp(wsMessage.timestamp == null ? now : wsMessage.timestamp)
                .setExtra(extra)
                .build();

        nettyMessage.setBody(payload);
        ctx.fireChannelRead(nettyMessage);
    }

    private CmdType parseCmd(String cmd) {
        if (cmd == null) return null;
        try {
            return CmdType.valueOf(cmd.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
