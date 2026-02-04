# 分库分表与日志统一处理 Starter 技术方案

本文档描述为 Literature Archive 项目创建两个新的 Spring Boot Starter 模块的技术方案。

---

## 目录

1. [分库分表 Starter (ShardingSphere-JDBC)](#分库分表-starter-shardingsphere-jdbc)
2. [日志统一处理 Starter (SkyWalking + ES + Kafka)](#日志统一处理-starter-skywalking--es--kafka)
3. [Docker Compose 服务补充](#docker-compose-服务补充)
4. [验证计划](#验证计划)

---

## 分库分表 Starter (ShardingSphere-JDBC)

### 设计目标
提供开箱即用的分库分表支持，通过配置实现：
- 数据分片（水平拆分）
- 读写分离
- 分布式事务
- 广播表/绑定表配置

### 技术选型

| 组件 | 版本 | 说明 |
|------|------|------|
| ShardingSphere-JDBC | 5.5.0+ | Apache 顶级项目，支持 Spring Boot 3 |
| MySQL | 8.0+ | 主数据库 |

### 模块结构

```
starters/
└── sharding-spring-boot-starter/
    ├── pom.xml
    └── src/main/java/com/literature/sharding/
        ├── autoconfigure/
        │   ├── ShardingAutoConfiguration.java
        │   └── ShardingProperties.java
        ├── config/
        │   └── ShardingRuleConfig.java
        └── META-INF/spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 配置属性设计

```yaml
literature:
  sharding:
    enabled: true
    # 数据源配置
    datasources:
      ds0:
        url: jdbc:mysql://localhost:3306/literature_0
        username: root
        password: ${MYSQL_ROOT_PASSWORD}
      ds1:
        url: jdbc:mysql://localhost:3306/literature_1
        username: root
        password: ${MYSQL_ROOT_PASSWORD}
    
    # 分片规则（按业务需求配置）
    rules:
      tables:
        # 书目表：按 ID 分片
        books:
          actual-data-nodes: ds$->{0..1}.books_$->{0..3}
          table-strategy:
            standard:
              sharding-column: id
              sharding-algorithm-name: books_mod
          key-generate-strategy:
            column: id
            key-generator-name: snowflake
        
        # 章节表：按 volume_id 分片（与卷表绑定）
        chapters:
          actual-data-nodes: ds$->{0..1}.chapters_$->{0..3}
          table-strategy:
            standard:
              sharding-column: volume_id
              sharding-algorithm-name: chapters_mod
        
        # 聊天消息表：按 session_id 分片
        chat_messages:
          actual-data-nodes: ds$->{0..1}.chat_messages_$->{0..7}
          table-strategy:
            standard:
              sharding-column: session_id
              sharding-algorithm-name: messages_mod
      
      # 绑定表（避免跨表 JOIN）
      binding-tables:
        - books, volumes, chapters
      
      # 广播表（全量同步）
      broadcast-tables:
        - sys_config
        - sys_dict
      
      # 分片算法
      sharding-algorithms:
        books_mod:
          type: MOD
          props:
            sharding-count: 4
        chapters_mod:
          type: MOD
          props:
            sharding-count: 4
        messages_mod:
          type: MOD
          props:
            sharding-count: 8
      
      # ID 生成器
      key-generators:
        snowflake:
          type: SNOWFLAKE
          props:
            worker-id: ${SW_WORKER_ID:1}
    
    # 读写分离
    read-write-splitting:
      enabled: false
      datasources:
        primary: ds0
        replicas: [ds1]
```

### 核心实现

#### ShardingProperties.java

```java
@Configuration
@ConfigurationProperties(prefix = "literature.sharding")
public class ShardingProperties {
    private boolean enabled = true;
    private Map<String, DataSourceProperties> datasources;
    private ShardingRuleProperties rules;
    private ReadWriteSplittingProperties readWriteSplitting;
    // getters/setters
}
```

#### ShardingAutoConfiguration.java

```java
@AutoConfiguration
@EnableConfigurationProperties(ShardingProperties.class)
@ConditionalOnProperty(name = "literature.sharding.enabled", havingValue = "true")
@ConditionalOnClass(ShardingSphereDataSource.class)
public class ShardingAutoConfiguration {
    
    @Bean
    @Primary
    public DataSource shardingDataSource(ShardingProperties properties) {
        // 构建 ShardingSphere 数据源
    }
}
```

---

## 日志统一处理 Starter (SkyWalking + ES + Kafka)

### 设计目标
提供统一的日志采集和链路追踪支持：
- 自动注入 TraceId 到日志
- 通过 Kafka 异步发送日志到 Elasticsearch
- 集成 SkyWalking Agent 做分布式追踪
- 支持自定义日志格式和索引策略
- **日志索引按小时滚动**

### 技术选型

| 组件 | 版本 | 说明 |
|------|------|------|
| SkyWalking Agent | 9.1.0+ | APM 探针，支持 Java 17+ |
| SkyWalking OAP | 9.7.0 | 可观测性分析平台 |
| Logback | 1.4+ | Spring Boot 默认日志框架 |
| Elasticsearch | 8.x | 日志存储后端 |
| logback-kafka-appender | 0.2.0-RC3 | 日志推送到 Kafka |
| Kafka | 3.7.0 | 日志消息队列 |

### 模块结构

```
starters/
└── logging-spring-boot-starter/
    ├── pom.xml
    └── src/main/java/com/literature/logging/
        ├── autoconfigure/
        │   ├── LoggingAutoConfiguration.java
        │   └── LoggingProperties.java
        ├── appender/
        │   └── KafkaLogAppender.java
        ├── consumer/
        │   └── LogToElasticsearchConsumer.java
        ├── filter/
        │   └── TraceIdMdcFilter.java
        └── META-INF/spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 配置属性设计

```yaml
literature:
  logging:
    enabled: true
    
    # TraceId 配置
    trace:
      header-name: X-Trace-Id
      mdc-key: traceId
      auto-generate: true
    
    # Kafka 日志通道
    kafka:
      enabled: true
      bootstrap-servers: localhost:9092
      topic: literature-logs
      security:
        protocol: SASL_PLAINTEXT
        sasl-mechanism: PLAIN
        username: user
        password: ${KAFKA_CLIENT_PASSWORD}
    
    # Elasticsearch 日志存储
    elasticsearch:
      enabled: true
      hosts: ["http://localhost:9200"]
      # 按小时滚动索引
      index-pattern: literature-logs-%{yyyy.MM.dd.HH}
      username: ${ES_USERNAME:elastic}
      password: ${ES_PASSWORD}
      batch:
        size: 500
        flush-interval: 3s
    
    # SkyWalking 集成
    skywalking:
      enabled: true
      service-name: ${spring.application.name}
      backend-service: skywalking-oap:11800
    
    # 日志格式
    pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n"
```

### 核心实现

#### TraceIdMdcFilter.java

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdMdcFilter implements Filter {
    
    private final LoggingProperties properties;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = httpRequest.getHeader(properties.getTrace().getHeaderName());
        
        // 优先使用 SkyWalking TraceId
        if (traceId == null) {
            traceId = TraceContext.traceId(); // SkyWalking API
        }
        if (traceId == null && properties.getTrace().isAutoGenerate()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        
        MDC.put(properties.getTrace().getMdcKey(), traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

#### KafkaLogAppender.java

```java
public class KafkaLogAppender extends AppenderBase<ILoggingEvent> {
    
    private KafkaProducer<String, String> producer;
    private String topic;
    
    @Override
    protected void append(ILoggingEvent event) {
        String message = formatLogEvent(event);
        producer.send(new ProducerRecord<>(topic, event.getLoggerName(), message));
    }
}
```

#### LogToElasticsearchConsumer.java

```java
@Component
@ConditionalOnProperty(name = "literature.logging.elasticsearch.enabled", havingValue = "true")
public class LogToElasticsearchConsumer {
    
    private final ElasticsearchClient esClient;
    private final LoggingProperties properties;
    
    @KafkaListener(topics = "#{loggingProperties.kafka.topic}")
    public void consumeLog(String logMessage) {
        // 按小时生成索引名
        String indexName = generateIndexName(); // e.g., literature-logs-2026.02.04.14
        esClient.index(i -> i.index(indexName).document(parseLog(logMessage)));
    }
    
    private String generateIndexName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");
        return "literature-logs-" + LocalDateTime.now().format(formatter);
    }
}
```

### SkyWalking Agent 集成方式

```bash
java -javaagent:/opt/skywalking-agent/skywalking-agent.jar \
  -Dskywalking.agent.service_name=content-service \
  -Dskywalking.collector.backend_service=skywalking-oap:11800 \
  -jar content-service.jar
```

---

## Docker Compose 服务补充

### 新增服务清单

| 服务 | 镜像 | 端口 | 用途 |
|------|------|------|------|
| elasticsearch | elasticsearch:8.12.0 | 9200, 9300 | 日志存储 |
| skywalking-oap | apache/skywalking-oap-server:9.7.0 | 11800, 12800 | 链路追踪后端 |
| skywalking-ui | apache/skywalking-ui:9.7.0 | 8088 | 链路追踪 UI |

### docker-compose.yaml 补充内容

```yaml
  elasticsearch:
    image: elasticsearch:8.12.0
    container_name: elasticsearch
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=true
      - ELASTIC_PASSWORD=${ES_PASSWORD:-elastic123}
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    volumes:
      - es_data:/usr/share/elasticsearch/data
    networks:
      - literature-network

  skywalking-oap:
    image: apache/skywalking-oap-server:9.7.0
    container_name: skywalking-oap
    ports:
      - "11800:11800"
      - "12800:12800"
    environment:
      - SW_STORAGE=elasticsearch
      - SW_STORAGE_ES_CLUSTER_NODES=elasticsearch:9200
      - SW_ES_USER=elastic
      - SW_ES_PASSWORD=${ES_PASSWORD:-elastic123}
    depends_on:
      - elasticsearch
    networks:
      - literature-network

  skywalking-ui:
    image: apache/skywalking-ui:9.7.0
    container_name: skywalking-ui
    ports:
      - "8088:8080"
    environment:
      - SW_OAP_ADDRESS=http://skywalking-oap:12800
    depends_on:
      - skywalking-oap
    networks:
      - literature-network
```

### volumes 补充

```yaml
volumes:
  mysql_data:
  redis_data:
  mongo_data:
  es_data:        # 新增
```

---

## 拟议变更（文件列表）

### 分库分表 Starter

| 文件 | 操作 |
|------|------|
| `starters/sharding-spring-boot-starter/pom.xml` | [NEW] |
| `starters/sharding-spring-boot-starter/.../ShardingAutoConfiguration.java` | [NEW] |
| `starters/sharding-spring-boot-starter/.../ShardingProperties.java` | [NEW] |
| `starters/pom.xml` | [MODIFY] 添加子模块 |

### 日志统一处理 Starter

| 文件 | 操作 |
|------|------|
| `starters/logging-spring-boot-starter/pom.xml` | [NEW] |
| `starters/logging-spring-boot-starter/.../LoggingAutoConfiguration.java` | [NEW] |
| `starters/logging-spring-boot-starter/.../LoggingProperties.java` | [NEW] |
| `starters/logging-spring-boot-starter/.../TraceIdMdcFilter.java` | [NEW] |
| `starters/logging-spring-boot-starter/.../KafkaLogAppender.java` | [NEW] |
| `starters/logging-spring-boot-starter/.../LogToElasticsearchConsumer.java` | [NEW] |
| `starters/pom.xml` | [MODIFY] 添加子模块 |

### 基础设施

| 文件 | 操作 |
|------|------|
| `docker-compose.yaml` | [MODIFY] 补充 ES、SkyWalking 服务 |
| `.env` | [MODIFY] 补充 ES_PASSWORD 等环境变量 |

---

## 验证计划

### 分库分表 Starter

```bash
# 单元测试
mvn test -f starters/sharding-spring-boot-starter/pom.xml

# 集成测试（使用 Testcontainers 启动多个 MySQL 实例）
```

| 测试项 | 验证内容 |
|--------|----------|
| 数据分片写入 | 根据分片键写入不同数据节点 |
| 跨分片查询 | 聚合多节点数据返回 |
| 绑定表 JOIN | books-volumes-chapters 关联查询 |
| 广播表同步 | 配置表全节点一致 |

### 日志统一处理 Starter

| 测试项 | 验证内容 |
|--------|----------|
| TraceId 注入 | HTTP 请求头自动注入 MDC |
| Kafka 日志发送 | 消费者端收到日志消息 |
| ES 按小时索引 | Kibana 查看 `literature-logs-2026.02.04.14` 索引 |
| SkyWalking 链路 | UI 查看服务拓扑和 TraceId 关联 |
