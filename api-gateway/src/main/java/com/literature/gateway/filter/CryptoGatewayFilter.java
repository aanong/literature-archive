package com.literature.gateway.filter;

import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import com.literature.crypto.core.SignatureUtils;
import com.literature.gateway.controller.SecurityConfigController;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CryptoGatewayFilter implements GlobalFilter, Ordered {
  private final CryptoProperties properties;
  private final SignatureUtils signatureUtils;
  private final AesGcmCrypto aesGcmCrypto;
  private final KeyGenerator keyGenerator;
  private final SecurityConfigController securityConfigController;

  public CryptoGatewayFilter(CryptoProperties properties, SignatureUtils signatureUtils, AesGcmCrypto aesGcmCrypto,
      KeyGenerator keyGenerator, SecurityConfigController securityConfigController) {
    this.properties = properties;
    this.signatureUtils = signatureUtils;
    this.aesGcmCrypto = aesGcmCrypto;
    this.keyGenerator = keyGenerator;
    this.securityConfigController = securityConfigController;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!properties.getHttp().isEnabled()) {
      return chain.filter(exchange);
    }

    String signature = exchange.getRequest().getHeaders().getFirst("X-Signature");
    String timestamp = exchange.getRequest().getHeaders().getFirst("X-Timestamp");
    String nonce = exchange.getRequest().getHeaders().getFirst("X-Nonce");
    String sessionKeyHeader = exchange.getRequest().getHeaders().getFirst("X-Session-Key");
    String encryptedHeader = exchange.getRequest().getHeaders().getFirst("X-Encrypted");
    boolean encrypted = "true".equalsIgnoreCase(encryptedHeader);

    if (signature == null || timestamp == null || nonce == null) {
      return chain.filter(exchange);
    }

    return DataBufferUtils.join(exchange.getRequest().getBody())
        .flatMap(buffer -> {
          byte[] bodyBytes = new byte[buffer.readableByteCount()];
          buffer.read(bodyBytes);
          DataBufferUtils.release(buffer);

          byte[] payloadBytes = bodyBytes;
          SecretKey aesKey;

          try {
            if (sessionKeyHeader != null) {
              byte[] encryptedSessionKey = Base64.getDecoder().decode(sessionKeyHeader);
              Cipher cipher = Cipher.getInstance("RSA");
              cipher.init(Cipher.DECRYPT_MODE, securityConfigController.getPrivateKey());
              byte[] sessionKeyBytes = cipher.doFinal(encryptedSessionKey);
              aesKey = new SecretKeySpec(sessionKeyBytes, "AES");
            } else {
              aesKey = keyGenerator.keyFromBase64(properties.getHttp().getEncryptKey());
            }

            if (encrypted && bodyBytes.length > 0) {
              byte[] decoded = Base64.getDecoder().decode(bodyBytes);
              payloadBytes = aesGcmCrypto.decrypt(decoded, aesKey);
            }
          } catch (Exception e) {
            e.printStackTrace();
            return unauthorized(exchange.getResponse());
          }

          String body = new String(payloadBytes, StandardCharsets.UTF_8);
          boolean valid = signatureUtils.verify(timestamp, nonce, body, properties.getHttp().getSignKey(), signature);
          if (!valid) {
            return unauthorized(exchange.getResponse());
          }

          byte[] finalBytes = payloadBytes;
          SecretKey finalAesKey = aesKey;

          ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
              DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(finalBytes);
              return Flux.just(buffer);
            }
          };

          ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
              return DataBufferUtils.join(body)
                  .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    if (!encrypted) {
                      DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                      return super.writeWith(Mono.just(buffer));
                    }

                    try {
                      byte[] encryptedBody = aesGcmCrypto.encrypt(bytes, finalAesKey);
                      String encoded = Base64.getEncoder().encodeToString(encryptedBody);
                      DataBuffer wrapped = exchange.getResponse().bufferFactory()
                          .wrap(encoded.getBytes(StandardCharsets.UTF_8));
                      exchange.getResponse().getHeaders().set("X-Encrypted", "true");
                      return super.writeWith(Mono.just(wrapped));
                    } catch (Exception e) {
                      return Mono.error(e);
                    }
                  });
            }
          };

          return chain.filter(exchange.mutate().request(decorated).response(decoratedResponse).build());
        });
  }

  private Mono<Void> unauthorized(ServerHttpResponse response) {
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();
  }

  @Override
  public int getOrder() {
    return -2;
  }
}
