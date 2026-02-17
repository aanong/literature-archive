package com.literature.chat.netty.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.literature.chat.netty.protocol.AuthPayload;
import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyMessageToWebSocketFrameEncoder extends MessageToMessageEncoder<NettyMessage> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, List<Object> out) throws Exception {
        WsMessage ws = new WsMessage();
        CmdType cmdType = CmdType.forNumber(msg.getHeader().getCmdType());
        if (cmdType == null) {
            return;
        }

        ws.cmd = cmdType.name();
        Object body = msg.getBody();
        if (body instanceof ChatPayload payload) {
            ws.sessionId = payload.getSessionId();
            ws.senderId = payload.getSenderId();
            ws.targetId = payload.getTargetId();
            ws.content = payload.getContent();
            ws.contentType = payload.getContentType();
            ws.timestamp = payload.getTimestamp();
            ws.extra = payload.getExtra();
        } else if (body instanceof AuthPayload payload) {
            ws.token = payload.getToken();
        }

        String json = objectMapper.writeValueAsString(ws);
        out.add(new TextWebSocketFrame(json));
    }
}
