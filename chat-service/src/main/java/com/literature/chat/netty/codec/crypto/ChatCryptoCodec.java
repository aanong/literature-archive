package com.literature.chat.netty.codec.crypto;

import com.literature.chat.netty.protocol.ChatPayload;
import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class ChatCryptoCodec extends MessageToMessageCodec<ChatPayload, ChatPayload> {
  private final CryptoProperties properties;
  private final AesGcmCrypto aesGcmCrypto;
  private final KeyGenerator keyGenerator;

  public ChatCryptoCodec(CryptoProperties properties, AesGcmCrypto aesGcmCrypto, KeyGenerator keyGenerator) {
    this.properties = properties;
    this.aesGcmCrypto = aesGcmCrypto;
    this.keyGenerator = keyGenerator;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, ChatPayload msg, List<Object> out) {
    if (!properties.getNetty().isEnabled()) {
      out.add(msg);
      return;
    }
    if (msg.getContent().isEmpty()) {
      out.add(msg);
      return;
    }
    SecretKey key = deriveKey();
    byte[] encrypted = aesGcmCrypto.encrypt(msg.getContent().getBytes(StandardCharsets.UTF_8), key);
    ChatPayload encryptedPayload = ChatPayload.newBuilder(msg)
        .setEncrypted(true)
        .setEncContent(com.google.protobuf.ByteString.copyFrom(encrypted))
        .setContent("")
        .build();
    out.add(encryptedPayload);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ChatPayload msg, List<Object> out) {
    if (!properties.getNetty().isEnabled()) {
      out.add(msg);
      return;
    }
    if (!msg.getEncrypted()) {
      out.add(msg);
      return;
    }
    SecretKey key = deriveKey();
    byte[] decrypted = aesGcmCrypto.decrypt(msg.getEncContent().toByteArray(), key);
    ChatPayload decryptedPayload = ChatPayload.newBuilder(msg)
        .setContent(new String(decrypted, StandardCharsets.UTF_8))
        .setEncrypted(false)
        .clearEncContent()
        .build();
    out.add(decryptedPayload);
  }

  private SecretKey deriveKey() {
    String salt = properties.getNetty().getKeyDeriveSalt();
    if (salt == null || salt.isBlank()) {
      throw new IllegalStateException("Netty key derive salt is missing");
    }
    String baseKey = Base64.getEncoder().encodeToString(salt.getBytes(StandardCharsets.UTF_8));
    return keyGenerator.deriveKey(baseKey, salt);
  }
}
