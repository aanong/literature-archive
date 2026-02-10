package com.literature.chat.netty.codec;

import com.literature.chat.netty.protocol.AuthPayload;
import com.literature.chat.netty.protocol.AuthResponse;
import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.chat.netty.protocol.CmdType;
import com.literature.chat.netty.protocol.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 协议解码器
 */
@Slf4j
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
            // 非法魔数，安全关闭 Channel 而非抛异常（避免 ByteBuf 泄漏）
            log.warn("Channel {} 收到非法魔数: 0x{}, 关闭连接", ctx.channel().id(), Integer.toHexString(magic & 0xFFFF));
            ctx.close();
            return;
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

        // 根据 CmdType 反序列化 Body，捕获 Protobuf 解析异常
        try {
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
        } catch (Exception e) {
            log.error("Channel {} Protobuf 反序列化失败, cmdType={}", ctx.channel().id(), cmdType, e);
            ctx.close();
            return;
        }

        out.add(message);
    }
}
