package com.literature.crypto.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class CryptoResponseAdvice implements ResponseBodyAdvice<Object> {
  private final CryptoProperties properties;
  private final AesGcmCrypto aesGcmCrypto;
  private final KeyGenerator keyGenerator;
  private final EncryptResponseAdvice encryptResponseAdvice;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public CryptoResponseAdvice(CryptoProperties properties, AesGcmCrypto aesGcmCrypto, KeyGenerator keyGenerator,
                              EncryptResponseAdvice encryptResponseAdvice) {
    this.properties = properties;
    this.aesGcmCrypto = aesGcmCrypto;
    this.keyGenerator = keyGenerator;
    this.encryptResponseAdvice = encryptResponseAdvice;
  }

  public CryptoResponseAdvice(CryptoProperties properties, AesGcmCrypto aesGcmCrypto) {
    this(properties, aesGcmCrypto, new KeyGenerator(), new EncryptResponseAdvice());
  }

  @Override
  public boolean supports(org.springframework.core.MethodParameter returnType, Class converterType) {
    return properties.getHttp().isEnabled();
  }

  @Override
  public Object beforeBodyWrite(Object body, org.springframework.core.MethodParameter returnType,
                               MediaType selectedContentType, Class selectedConverterType,
                               ServerHttpRequest request, ServerHttpResponse response) {
    if (!(response instanceof ServletServerHttpResponse)) {
      return body;
    }
    if (body == null) {
      return null;
    }
    if (!encryptResponseAdvice.shouldEncrypt(returnType)) {
      return body;
    }
    try {
      byte[] payload = objectMapper.writeValueAsBytes(body);
      SecretKey key = keyGenerator.keyFromBase64(properties.getHttp().getEncryptKey());
      byte[] encrypted = aesGcmCrypto.encrypt(payload, key);
      response.getHeaders().set("X-Encrypted", "true");
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception ex) {
      throw new IllegalStateException("HTTP response encryption failed", ex);
    }
  }
}
