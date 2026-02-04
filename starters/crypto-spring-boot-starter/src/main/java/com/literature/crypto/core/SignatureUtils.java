package com.literature.crypto.core;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignatureUtils {
  private final int replayWindowSeconds;

  public SignatureUtils(int replayWindowSeconds) {
    this.replayWindowSeconds = replayWindowSeconds;
  }

  public String sign(String timestamp, String nonce, String body, String secretKey) {
    String message = timestamp + nonce + body;
    return hmacSha256(message, secretKey);
  }

  public boolean verify(String timestamp, String nonce, String body, String secretKey, String providedSignature) {
    if (!withinWindow(timestamp)) {
      return false;
    }
    String expected = sign(timestamp, nonce, body, secretKey);
    return constantTimeEquals(expected, providedSignature);
  }

  private boolean withinWindow(String timestamp) {
    try {
      long timestampSeconds = Long.parseLong(timestamp);
      long nowSeconds = Instant.now().getEpochSecond();
      long diff = Math.abs(nowSeconds - timestampSeconds);
      return diff <= replayWindowSeconds;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  private String hmacSha256(String message, String secretKey) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec spec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(spec);
      byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(digest);
    } catch (Exception ex) {
      throw new IllegalStateException("HMAC SHA256 failed", ex);
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }

  private boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) {
      return false;
    }
    byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
    byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
    if (aBytes.length != bBytes.length) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < aBytes.length; i++) {
      result |= aBytes[i] ^ bBytes[i];
    }
    return result == 0;
  }
}
