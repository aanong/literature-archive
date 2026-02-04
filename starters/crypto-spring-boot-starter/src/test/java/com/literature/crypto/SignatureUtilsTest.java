package com.literature.crypto;

import com.literature.crypto.core.SignatureUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SignatureUtilsTest {

  @Test
  void verifySignatureWithinWindow() {
    SignatureUtils utils = new SignatureUtils(300);
    String timestamp = String.valueOf(java.time.Instant.now().getEpochSecond());
    String nonce = "abc";
    String body = "{}";
    String secret = "secret";

    String signature = utils.sign(timestamp, nonce, body, secret);
    Assertions.assertTrue(utils.verify(timestamp, nonce, body, secret, signature));
  }

  @Test
  void rejectExpiredTimestamp() {
    SignatureUtils utils = new SignatureUtils(1);
    String timestamp = String.valueOf(java.time.Instant.now().minusSeconds(10).getEpochSecond());
    String nonce = "abc";
    String body = "{}";
    String secret = "secret";

    String signature = utils.sign(timestamp, nonce, body, secret);
    Assertions.assertFalse(utils.verify(timestamp, nonce, body, secret, signature));
  }
}
