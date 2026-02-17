package com.literature.chat.netty.ws;

/**
 * WebSocket 消息载体（JSON）
 */
public class WsMessage {
    public String cmd;
    public String token;
    public Long sessionId;
    public Long senderId;
    public String senderType;
    public Long targetId;
    public String targetUserType;
    public String content;
    public String contentType;
    public Long timestamp;
    public String extra;
}
