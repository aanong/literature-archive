package com.literature.crypto;

import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import com.literature.crypto.core.SignatureUtils;
import com.literature.crypto.http.CryptoRequestFilter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class CryptoFilterTest {

  @Test
  void decryptsEncryptedRequestBody() throws Exception {
    CryptoProperties properties = new CryptoProperties();
    KeyGenerator keyGenerator = new KeyGenerator();
    AesGcmCrypto crypto = new AesGcmCrypto();
    properties.getHttp().setEncryptKey(keyGenerator.generateBase64Key());
    CryptoRequestFilter filter = new CryptoRequestFilter(properties, new SignatureUtils(300), crypto, keyGenerator);

    SecretKey key = keyGenerator.keyFromBase64(properties.getHttp().getEncryptKey());
    byte[] encrypted = crypto.encrypt("hello".getBytes(StandardCharsets.UTF_8), key);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Encrypted", "true");
    request.setContent(Base64.getEncoder().encode(encrypted));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (req, res) -> {
      byte[] body = req.getInputStream().readAllBytes();
      Assertions.assertEquals("hello", new String(body, StandardCharsets.UTF_8));
    });
  }
}
