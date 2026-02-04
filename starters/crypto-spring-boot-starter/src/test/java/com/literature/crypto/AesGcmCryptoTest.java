package com.literature.crypto;

import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AesGcmCryptoTest {

  @Test
  void encryptDecryptRoundTrip() {
    AesGcmCrypto crypto = new AesGcmCrypto();
    KeyGenerator keyGenerator = new KeyGenerator();
    SecretKey key = keyGenerator.deriveKey("password", "salt");

    byte[] payload = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    byte[] encrypted = crypto.encrypt(payload, key);
    byte[] decrypted = crypto.decrypt(encrypted, key);

    Assertions.assertArrayEquals(payload, decrypted);
  }
}
