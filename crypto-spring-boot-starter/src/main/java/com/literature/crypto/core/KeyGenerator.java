package com.literature.crypto.core;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyGenerator {
  private static final int KEY_LENGTH_BITS = 256;
  private static final int ITERATIONS = 65536;
  private final SecureRandom secureRandom = new SecureRandom();

  public SecretKey deriveKey(String password, String salt) {
    return deriveKey(password, salt.getBytes(StandardCharsets.UTF_8));
  }

  public SecretKey deriveKey(String password, byte[] salt) {
    try {
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      byte[] keyBytes = factory.generateSecret(spec).getEncoded();
      return new SecretKeySpec(keyBytes, "AES");
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Key derivation failed", ex);
    }
  }

  public SecretKey keyFromBase64(String base64) {
    byte[] keyBytes = Base64.getDecoder().decode(base64);
    return new SecretKeySpec(keyBytes, "AES");
  }

  public String generateBase64Key() {
    byte[] keyBytes = new byte[KEY_LENGTH_BITS / 8];
    secureRandom.nextBytes(keyBytes);
    return Base64.getEncoder().encodeToString(keyBytes);
  }
}
