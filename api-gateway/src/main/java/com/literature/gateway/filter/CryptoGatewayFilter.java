package com.literature.gateway.filter;

import com.literature.crypto.autoconfigure.CryptoProperties;
import com.literature.crypto.core.AesGcmCrypto;
import com.literature.crypto.core.KeyGenerator;
import com.literature.crypto.core.SignatureUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

  public CryptoGatewayFilter(CryptoProperties properties, SignatureUtils signatureUtils, AesGcmCrypto aesGcmCrypto, KeyGenerator keyGenerator) {
    this.properties = properties;
    this.signatureUtils = signatureUtils;
    this.aesGcmCrypto = aesGcmCrypto;
    this.keyGenerator = keyGenerator;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!properties.getHttp().isEnabled()) {
      return chain.filter(exchange);
    }

    String signature = exchange.getRequest().getHeaders().getFirst("X-Signature");
    String timestamp = exchange.getRequest().getHeaders().getFirst("X-Timestamp");
    String nonce = exchange.getRequest().getHeaders().getFirst("X-Nonce");
    boolean encrypted = Boolean.parseBoolean(exchange.getRequest().getHeaders().getFirst("X-Encrypted"));

    if (signature == null || timestamp == null || nonce == null) {
      return chain.filter(exchange);
    }

    return DataBufferUtils.join(exchange.getRequest().getBody())
        .flatMap(buffer -> {
          byte[] bodyBytes = new byte[buffer.readableByteCount()];
          buffer.read(bodyBytes);
          DataBufferUtils.release(buffer);

          byte[] payloadBytes = bodyBytes;
          if (encrypted && bodyBytes.length > 0) {
            byte[] decoded = Base64.getDecoder().decode(bodyBytes);
            payloadBytes = aesGcmCrypto.decrypt(decoded, keyGenerator.keyFromBase64(properties.getHttp().getEncryptKey()));
          }

          String body = new String(payloadBytes, StandardCharsets.UTF_8);
          boolean valid = signatureUtils.verify(timestamp, nonce, body, properties.getHttp().getSignKey(), signature);
          if (!valid) {
            return unauthorized(exchange.getResponse());
          }

          byte[] finalBytes = payloadBytes;
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
              if (!encrypted) {
                return super.writeWith(body);
              }
              return DataBufferUtils.join(body)
                  .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    byte[] encryptedBody = aesGcmCrypto.encrypt(bytes,
                        keyGenerator.keyFromBase64(properties.getHttp().getEncryptKey()));
                    String encoded = Base64.getEncoder().encodeToString(encryptedBody);
                    DataBuffer wrapped = exchange.getResponse().bufferFactory()
                        .wrap(encoded.getBytes(StandardCharsets.UTF_8));
                    exchange.getResponse().getHeaders().set("X-Encrypted", "true");
                    return super.writeWith(Mono.just(wrapped));
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
