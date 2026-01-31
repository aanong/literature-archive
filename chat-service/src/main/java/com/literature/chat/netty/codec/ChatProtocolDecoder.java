package com.literature.chat.netty.codec;

import com.literature.chat.netty.protocol.AuthPayload;
import com.literature.chat.netty.protocol.AuthResponse;
import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 协议解码器
 */
public class ChatProtocolDecoder extends ByteToMessageDecoder {

    private static final int HEADER_LENGTH = 17;
    private static final short MAGIC = (short) 0xCAFE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }

        in.markReaderIndex();

        short magic = in.readShort();
        if (magic != MAGIC) {
            in.resetReaderIndex();
            throw new RuntimeException("Magic number mismatch");
        }

        byte version = in.readByte();
        byte serial = in.readByte();
        byte cmdType = in.readByte();
        long reqId = in.readLong();
        int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        NettyMessage message = new NettyMessage();
        NettyMessage.Header header = new NettyMessage.Header();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerial(serial);
        header.setCmdType(cmdType);
        header.setReqId(reqId);
        header.setLength(length);
        message.setHeader(header);

        // 根据 CmdType 反序列化 Body
        if (length > 0) {
            if (cmdType == CmdType.AUTH_VALUE) {
                message.setBody(AuthPayload.parseFrom(bytes));
            } else if (cmdType == CmdType.SINGLE_CHAT_VALUE || cmdType == CmdType.GROUP_CHAT_VALUE
                    || cmdType == CmdType.ACK_VALUE || cmdType == CmdType.ERROR_VALUE) {
                message.setBody(ChatPayload.parseFrom(bytes));
            } else if (cmdType == CmdType.HEARTBEAT_VALUE) {
                // 心跳无 Body
            }
        }

        out.add(message);
    }
}
