package com.literature.crypto.http;

import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import com.literature.crypto.core.SignatureUtils;
import jakarta.servlet.FilterChain;
import java.util.Base64;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class CryptoRequestFilter extends OncePerRequestFilter {
  public static final String SIGNATURE_HEADER = "X-Signature";
  public static final String TIMESTAMP_HEADER = "X-Timestamp";
  public static final String NONCE_HEADER = "X-Nonce";
  public static final String ENCRYPTED_HEADER = "X-Encrypted";

  private final CryptoProperties properties;
  private final SignatureUtils signatureUtils;
  private final AesGcmCrypto aesGcmCrypto;
  private final KeyGenerator keyGenerator;

  public CryptoRequestFilter(CryptoProperties properties, SignatureUtils signatureUtils, AesGcmCrypto aesGcmCrypto, KeyGenerator keyGenerator) {
    this.properties = properties;
    this.signatureUtils = signatureUtils;
    this.aesGcmCrypto = aesGcmCrypto;
    this.keyGenerator = keyGenerator;
  }

  public CryptoRequestFilter(CryptoProperties properties, SignatureUtils signatureUtils, AesGcmCrypto aesGcmCrypto) {
    this(properties, signatureUtils, aesGcmCrypto, new KeyGenerator());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!properties.getHttp().isEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    String signature = request.getHeader(SIGNATURE_HEADER);
    String timestamp = request.getHeader(TIMESTAMP_HEADER);
    String nonce = request.getHeader(NONCE_HEADER);

    byte[] rawBody = StreamUtils.copyToByteArray(request.getInputStream());
    String bodyString = new String(rawBody, StandardCharsets.UTF_8);

    boolean encrypted = Boolean.parseBoolean(request.getHeader(ENCRYPTED_HEADER));
    if (encrypted && rawBody.length > 0) {
      SecretKey key = keyGenerator.keyFromBase64(properties.getHttp().getEncryptKey());
      byte[] decrypted = aesGcmCrypto.decrypt(Base64.getDecoder().decode(rawBody), key);
      rawBody = decrypted;
      bodyString = new String(rawBody, StandardCharsets.UTF_8);
    }

    if (signature != null && timestamp != null && nonce != null) {
      boolean valid = signatureUtils.verify(timestamp, nonce, bodyString, properties.getHttp().getSignKey(), signature);
      if (!valid) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid signature");
        return;
      }
    }

    HttpServletRequest wrapped = new CachedBodyRequestWrapper(request, rawBody);
    filterChain.doFilter(wrapped, response);
  }

  private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    private CachedBodyRequestWrapper(HttpServletRequest request, byte[] cachedBody) {
      super(request);
      this.cachedBody = cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
      return new ServletInputStream() {
        @Override
        public boolean isFinished() {
          return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() {
          return inputStream.read();
        }
      };
    }

    @Override
    public String getCharacterEncoding() {
      return StandardCharsets.UTF_8.name();
    }

    @Override
    public int getContentLength() {
      return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
      return cachedBody.length;
    }

    @Override
    public String getContentType() {
      return MediaType.APPLICATION_JSON_VALUE;
    }
  }
}
