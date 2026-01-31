package com.literature.chat.netty.protocol;

import lombok.Data;

/**
 * 自定义协议消息封装
 */
@Data
public class NettyMessage {
    private Header header;
    private Object body; // Protobuf 对象

    @Data
    public static class Header {
        private short magic; // 魔数
        private byte version; // 版本
        private byte serial; // 序列化方式
        private byte cmdType; // 指令类型
        private long reqId; // 请求ID
        private int length; // 消息体长度
    }
}
