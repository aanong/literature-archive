package com.literature.chat.netty.codec;

import com.literature.chat.netty.protocol.NettyMessage;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 协议编码器
 */
public class ChatProtocolEncoder extends MessageToByteEncoder<NettyMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf out) throws Exception {
        NettyMessage.Header header = msg.getHeader();

        out.writeShort(header.getMagic());
        out.writeByte(header.getVersion());
        out.writeByte(header.getSerial());
        out.writeByte(header.getCmdType());
        out.writeLong(header.getReqId());

        byte[] bytes = null;
        if (msg.getBody() != null && msg.getBody() instanceof MessageLite) {
            bytes = ((MessageLite) msg.getBody()).toByteArray();
        }

        int length = bytes == null ? 0 : bytes.length;
        out.writeInt(length);

        if (bytes != null) {
            out.writeBytes(bytes);
        }
    }
}
