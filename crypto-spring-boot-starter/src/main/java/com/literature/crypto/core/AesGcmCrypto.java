package com.literature.crypto.core;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AesGcmCrypto {
  private static final int IV_LENGTH_BYTES = 12;
  private static final int TAG_LENGTH_BITS = 128;
  private final SecureRandom secureRandom = new SecureRandom();

  public byte[] encrypt(byte[] plaintext, SecretKey key) {
    try {
      byte[] iv = new byte[IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      byte[] ciphertext = cipher.doFinal(plaintext);
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
      buffer.put(iv);
      buffer.put(ciphertext);
      return buffer.array();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("AES-GCM encryption failed", ex);
    }
  }

  public byte[] decrypt(byte[] ciphertextWithIv, SecretKey key) {
    try {
      if (ciphertextWithIv.length < IV_LENGTH_BYTES + 1) {
        throw new IllegalArgumentException("Ciphertext too short");
      }
      ByteBuffer buffer = ByteBuffer.wrap(ciphertextWithIv);
      byte[] iv = new byte[IV_LENGTH_BYTES];
      buffer.get(iv);
      byte[] ciphertext = new byte[buffer.remaining()];
      buffer.get(ciphertext);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      return cipher.doFinal(ciphertext);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("AES-GCM decryption failed", ex);
    }
  }
}
