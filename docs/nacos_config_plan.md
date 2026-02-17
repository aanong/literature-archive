# Nacos 配置中心整合方案

本方案旨在将 Spring Boot 配置文件 (`application.yml`) 迁移至 Nacos 配置中心，实现配置的统一管理与热更新。

## 1. 命名空间 (Namespace)

建议在 Nacos 中创建独立的命名空间，例如 `literature-dev` (开发环境) 或 `literature-prod` (生产环境)，以隔离不同环境的配置。

## 2. 公共配置 (Shared Config)

创建 Data ID: `literature-common.yaml` (Group: `DEFAULT_GROUP`)
包含所有服务共用的数据库连接、Redis、MyBatis Plus 及基础日志配置。

```yaml
spring:
  # 数据库公共配置 (URL 由各服务自行定义)
  datasource:
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      max-lifetime: 1800000
      connection-timeout: 30000

  # Redis 公共配置
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:root}
      database: 0 # 默认库，个别服务可覆盖
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

  # Nacos Discovery 公共配置
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:} # 读取本地 bootstrap.yml 或环境变量

# MyBatis Plus 公共配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志公共配置
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 3. 服务独立配置

为每个微服务创建独立的 Data ID (格式: `服务名.yaml`)，包含端口、数据库 URL 及特有业务配置。

### 3.1 用户服务 (`user-service.yaml`)

```yaml
server:
  port: 18081

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/literature_user?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

# JWT 配置 (对应 JwtTokenService)
security:
  jwt:
    issuer: literature-auth
    expire-minutes: 120
    # 注意: 当前实现是在启动时生成 RSA 密钥对，因此不需要配置静态私钥
```

### 3.2 内容服务 (`content-service.yaml`)

```yaml
server:
  port: 18083

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/literature_content?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.literature.content.entity
```

### 3.3 知识库服务 (`knowledge-service.yaml`)

```yaml
server:
  port: 18084

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/literature_knowledge?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  data:
    redis:
      database: 2

# LangChain4j & Milvus 配置 (对应 EmbeddingService)
langchain4j:
  milvus:
    host: ${MILVUS_HOST:localhost}
    port: ${MILVUS_PORT:19530}
    collection-name: literature_segment
    dimension: 1024 # 需与 embedding 模型输出维度一致 (如 bge-m3 为 1024)

# AI 模型策略配置 (对应 AiModelProperties)
ai-model:
  embedding:
    provider: ollama # 可选: ollama, openai
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      model-name: bge-m3
    openai:
      api-key: ${OPENAI_API_KEY:}
      model-name: text-embedding-3-small
  chat:
    provider: ollama
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      model-name: deepseek-r1:7b
```

### 3.4 聊天服务 (`chat-service.yaml`)

```yaml
server:
  port: 18090

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/literature_chat?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  data:
    redis:
      database: 1
    mongodb:
      uri: mongodb://${MONGO_HOST:localhost}:27017/literature_chat

# Netty 配置 (对应 NettyServerInitializer / AuthHandler)
netty:
  port: 18091

# 加密配置 (对应 CryptoProperties)
crypto:
  key: ${CHAT_CRYPTO_KEY:1234567890123456} # AES 128/256 key
  salt: ${CHAT_CRYPTO_SALT:somesalt}

# OAuth2 资源服务器配置 (用于校验 JWT)
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://user-service/.well-known/jwks.json # 指向用户服务的 JWK 端点
```

### 3.5 资源服务 (`asset-service.yaml`)

```yaml
server:
  port: 18082

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/literature_asset?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

# MinIO / OSS 配置
storage:
  type: minio
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minio}
  secret-key: ${MINIO_SECRET_KEY:minio123}
  bucket: literature-assets
```

### 3.6 API 网关 (`api-gateway.yaml`)

```yaml
server:
  port: 18080

spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1
        - id: content-service
          uri: lb://content-service
          predicates:
            - Path=/api/content/**
          filters:
            - StripPrefix=1
        - id: knowledge-service
          uri: lb://knowledge-service
          predicates:
            - Path=/api/knowledge/**
          filters:
            - StripPrefix=1
```

## 4. 本地 bootstrap.yml 模板

各服务本地仅需保留 `bootstrap.yml`，用于连接 Nacos 并加载配置。

```yaml
server:
  port: 808x # 本地开发如果不连接Nacos，需要保留端口，否则启动随机端口或报错

spring:
  application:
    name: service-name # 替换为具体服务名
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_ADDR:localhost:8848}
        file-extension: yaml
        namespace: ${NACOS_NAMESPACE:} # 命名空间ID，开发环境可留空
        shared-configs:
          - data-id: literature-common.yaml
            refresh: true
      discovery:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:}

# 环境变量默认值 (可选)
# MYSQL_HOST: localhost
# REDIS_HOST: localhost
```
