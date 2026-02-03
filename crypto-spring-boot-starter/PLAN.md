# 加密解密 Spring Boot Starter 实现计划

本文档描述为 Literature Archive 项目创建 `crypto-spring-boot-starter` 模块的技术方案，支持 **HTTP API 加密** 和 **Netty 聊天消息加密**。

---

## 目录

1. [技术选型](#技术选型)
2. [模块设计](#模块设计)
3. [实现方案](#实现方案)
4. [验证计划](#验证计划)

---

## 技术选型

| 场景 | 算法 | 说明 |
|------|------|------|
| **对称加密** | AES-256-GCM | 高性能，带认证加密，防篡改 |
| **非对称加密** | RSA-2048 / ECDH | 密钥交换场景 |
| **签名验签** | HMAC-SHA256 | API 请求签名防重放 |
| **密钥派生** | PBKDF2 / HKDF | 从密码或主密钥派生会话密钥 |

> **提示**: Java 17 内置 `javax.crypto` 支持 AES-GCM，无需引入第三方库。

---

## 模块设计

```
literature-archive/
├── crypto-spring-boot-starter/     # [NEW] 新模块
│   ├── pom.xml
│   └── src/main/java/com/literature/crypto/
│       ├── autoconfigure/
│       │   ├── CryptoAutoConfiguration.java
│       │   └── CryptoProperties.java
│       ├── core/
│       │   ├── AesGcmCrypto.java         # AES-GCM 实现
│       │   ├── RsaCrypto.java            # RSA 实现
│       │   ├── SignatureUtils.java       # HMAC 签名
│       │   └── KeyGenerator.java         # 密钥生成工具
│       ├── http/
│       │   ├── CryptoRequestFilter.java  # HTTP 请求解密
│       │   ├── CryptoResponseAdvice.java # HTTP 响应加密
│       │   └── @EncryptResponse 注解
│       ├── netty/
│       │   ├── CryptoCodec.java          # Netty 加解密 Handler
│       │   └── E2ECryptoHandler.java     # 端到端加密处理
│       └── spring.factories              # 自动配置入口
└── pom.xml                               # [MODIFY] 添加新模块
```

---

## 实现方案

### 1. 核心加密工具

#### AesGcmCrypto.java

```java
public class AesGcmCrypto {
    // AES-256-GCM 加密，自动生成 IV 并附加到密文头部
    public byte[] encrypt(byte[] plaintext, SecretKey key);
    
    // 从密文头部提取 IV 后解密
    public byte[] decrypt(byte[] ciphertext, SecretKey key);
}
```

#### SignatureUtils.java

```java
public class SignatureUtils {
    // 请求签名：sign = HMAC-SHA256(timestamp + nonce + body, secretKey)
    public String sign(String timestamp, String nonce, String body, String secretKey);
    
    // 验签 + 防重放（检查 timestamp 是否在 5 分钟内）
    public boolean verify(...);
}
```

---

### 2. HTTP API 加密

两种模式供业务选择：

| 模式 | 使用场景 | 实现方式 |
|------|----------|----------|
| **全局加密** | 对外开放 API | Gateway Filter 拦截所有请求 |
| **注解加密** | 部分敏感接口 | `@EncryptResponse` 注解 + ResponseBodyAdvice |

#### CryptoRequestFilter.java

```java
@Component
public class CryptoRequestFilter implements Filter {
    // 1. 验证 X-Signature 请求头
    // 2. 解密 Request Body（如果加密）
    // 3. 注入解密后的明文 Body
}
```

#### CryptoResponseAdvice.java

```java
@ControllerAdvice
public class CryptoResponseAdvice implements ResponseBodyAdvice<Object> {
    // 针对标注 @EncryptResponse 的接口，加密响应体
}
```

---

### 3. 聊天消息加密 (Netty)

#### 协议扩展

现有 Protobuf 协议新增加密标识：

```diff
// ChatProtocol.proto
message ChatPayload {
    CmdType cmd = 1;
    ...
    string content = 5;     // 消息内容
+   bool encrypted = 9;     // 是否加密
+   bytes enc_content = 10; // 加密后的内容（替代 content）
}
```

#### CryptoCodec.java

作为 Netty ChannelHandler 插入 Pipeline：

```
Pipeline: Decoder → CryptoCodec(解密) → BusinessHandler → CryptoCodec(加密) → Encoder
```

```java
public class CryptoCodec extends MessageToMessageCodec<ChatPayload, ChatPayload> {
    // encode: 加密 content → enc_content
    // decode: 解密 enc_content → content
}
```

---

### 4. 密钥管理

| 密钥类型 | 存储位置 | 说明 |
|----------|----------|------|
| **服务端主密钥** | 环境变量 / Vault | 用于派生会话密钥 |
| **API 签名密钥** | Nacos 配置中心 | 客户端与服务端共享 |
| **聊天会话密钥** | Redis（加密存储） | 每个会话独立密钥 |

---

### 5. 配置属性

```yaml
# application.yml
literature:
  crypto:
    enabled: true
    algorithm: AES-256-GCM
    # API 加密
    http:
      enabled: true
      sign-key: ${CRYPTO_SIGN_KEY}
      encrypt-key: ${CRYPTO_ENCRYPT_KEY}
      replay-window-seconds: 300
    # 聊天加密
    netty:
      enabled: true
      key-derive-salt: ${CRYPTO_SALT}
```

---

## 待确认事项

1. **密钥来源**：是否使用 HashiCorp Vault 或直接使用环境变量？
2. **聊天 E2E**：是否需要完整的端到端加密（客户端生成密钥对）？当前方案为服务端加密。
3. **兼容性**：是否需要支持老版本客户端（非加密请求）的过渡期？

---

## 验证计划

### 自动化测试

```bash
cd d:\code\gmrfid\literature-archive\crypto-spring-boot-starter
mvn test
```

测试用例包括：

| 测试类 | 覆盖范围 |
|--------|----------|
| `AesGcmCryptoTest` | AES 加解密正确性、IV 随机性、大数据分块 |
| `SignatureUtilsTest` | 签名验签、重放攻击检测 |
| `CryptoFilterTest` | HTTP 请求解密、响应加密 |
| `CryptoCodecTest` | Netty Handler 集成测试 |

### 手动验证

1. **启动服务**：运行 `chat-service`，观察 Netty 日志确认 `CryptoCodec` 已加载。
2. **发送加密请求**：使用 Postman 发送带 `X-Signature` 头的加密请求，验证响应正确解密。
3. **聊天测试**：使用现有 Python 脚本 `scripts/benchmark.py` 修改后发送加密消息。

---

## 变更总结

| 文件 | 操作 |
|------|------|
| `pom.xml` | 添加 `crypto-spring-boot-starter` 模块 |
| `crypto-spring-boot-starter/` | 新模块目录 |
| `ChatProtocol.proto` | 添加加密字段 |
| `chat-service` Pipeline 配置 | 插入 CryptoCodec Handler |
