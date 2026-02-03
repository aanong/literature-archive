package com.literature.crypto;

import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CryptoCodecTest {

  @Test
  void encryptsAndDecryptsPayload() {
    KeyGenerator keyGenerator = new KeyGenerator();
    AesGcmCrypto crypto = new AesGcmCrypto();

    byte[] plaintext = "hello".getBytes(StandardCharsets.UTF_8);
    SecretKey key = keyGenerator.deriveKey("secret", "salt");
    byte[] encrypted = crypto.encrypt(plaintext, key);
    byte[] decrypted = crypto.decrypt(encrypted, key);

    Assertions.assertEquals("hello", new String(decrypted, StandardCharsets.UTF_8));
  }
}
