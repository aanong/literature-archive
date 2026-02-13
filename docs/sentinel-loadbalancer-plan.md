# Sentinel + Spring Cloud LoadBalancer 集成方案

## 背景

项目基于 **Spring Boot 3.2.3 + Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0**，共 11 个微服务模块，均已接入 Nacos 注册中心。当前现状：

| 能力 | 现状 |
|------|------|
| 服务注册发现 | ✅ 全部接入 Nacos |
| 负载均衡 | ⚠️ 仅 `api-gateway` 引入了 `spring-cloud-starter-loadbalancer` |
| 服务间调用 | ❌ 无 Feign/RestTemplate/WebClient，各服务独立 |
| 流控/熔断/降级 | ❌ 未集成 Sentinel |
| 可观测 | ✅ SkyWalking 已接入 |

---

## 提出变更

### 组件一：Spring Cloud LoadBalancer — 网关与服务间调用

> [!IMPORTANT]
> 当前各业务服务间没有远程调用代码。引入 OpenFeign + LoadBalancer 后需要**同时设计 Feign 接口**，否则仅是依赖引入而无实际作用。请确认是否需要在此次方案中包含典型的 Feign 接口设计（如 `user-service` ↔ `content-service`）。

#### 依赖变更

**根 `pom.xml`** — `<dependencyManagement>` 中声明 OpenFeign：

```xml
<!-- 已由 spring-cloud-dependencies BOM 管理，无需额外添加版本 -->
```

**需要发起远程调用的服务** (如 `content-service`, `knowledge-service`, `chat-service`)：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

#### 配置示例 (`application.yml`)

```yaml
spring:
  cloud:
    loadbalancer:
      retry:
        enabled: true        # 开启重试
        max-retries-on-same-service-instance: 1
        max-retries-on-next-service-instance: 2
      cache:
        enabled: true         # 缓存服务列表
        ttl: 30s
```

#### 代码变更

| 模块 | 变更 |
|------|------|
| **需要 Feign 的业务服务** | 启动类添加 `@EnableFeignClients`，创建 `feign/` 包放置接口定义 |
| **api-gateway** | 已有 LoadBalancer，确认 `lb://` 路由模式正常即可 |

---

### 组件二：Sentinel — 流控、熔断与降级

#### 1. 依赖引入

**方案**: 在 `common-core` 中引入 Sentinel 核心依赖，所有服务自动继承。

##### [MODIFY] [common-core/pom.xml](file:///d:/code/gmrfid/literature-archive/common-core/pom.xml)

```xml
<!-- Sentinel 核心 -->
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

##### [MODIFY] [api-gateway/pom.xml](file:///d:/code/gmrfid/literature-archive/api-gateway/pom.xml)

```xml
<!-- Gateway 专属适配器 -->
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```

#### 2. Sentinel Dashboard

##### [MODIFY] [docker-compose.yaml](file:///d:/code/gmrfid/literature-archive/docker-compose.yaml)

```yaml
sentinel-dashboard:
  image: bladex/sentinel-dashboard:1.8.8
  container_name: sentinel-dashboard
  ports:
    - "8858:8858"
  environment:
    - JAVA_OPTS=-Dserver.port=8858
  networks:
    - literature-network
```

#### 3. 各服务 Sentinel 配置

##### 通用配置 (`literature-common.yaml` — Nacos 共享配置)

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: ${SENTINEL_DASHBOARD:127.0.0.1:8858}
        port: 8719           # 与 Dashboard 通信的端口
      eager: true             # 启动即注册
      datasource:             # 规则持久化到 Nacos
        flow:
          nacos:
            server-addr: ${NACOS_ADDR:127.0.0.1:8848}
            data-id: ${spring.application.name}-sentinel-flow
            group-id: SENTINEL_GROUP
            rule-type: flow
        degrade:
          nacos:
            server-addr: ${NACOS_ADDR:127.0.0.1:8848}
            data-id: ${spring.application.name}-sentinel-degrade
            group-id: SENTINEL_GROUP
            rule-type: degrade
```

#### 4. Gateway Sentinel 集成

##### [NEW] [GatewaySentinelConfig.java](file:///d:/code/gmrfid/literature-archive/api-gateway/src/main/java/com/literature/gateway/config/GatewaySentinelConfig.java)

- 自定义 `BlockRequestHandler`，返回统一 `ApiResponse` 格式
- 按路由 ID 配置流控规则：如 `/api/knowledge/**` QPS=100, `/api/admin/auth/login` QPS=20

#### 5. 业务服务 Sentinel 集成

##### [NEW] [SentinelExceptionHandler.java](file:///d:/code/gmrfid/literature-archive/common-core/src/main/java/com/literature/common/core/handler/SentinelExceptionHandler.java)

- 在已有 `GlobalExceptionHandler` 中增加 `BlockException` 处理
- 返回 `ApiResponse.fail(ErrorCode.TOO_MANY_REQUESTS, ...)`

##### 注解式降级示例 (各 Service 层)

```java
@SentinelResource(value = "hybridSearch",
    blockHandler = "hybridSearchBlockHandler",
    fallback = "hybridSearchFallback")
public List<HybridSearchResult> search(String query, int maxResults) { ... }
```

#### 6. Feign + Sentinel 熔断整合

```yaml
feign:
  sentinel:
    enabled: true   # 自动为 Feign 接口开启熔断
```

每个 Feign 接口提供 `fallback` 或 `fallbackFactory` 实现。

---

## 涉及文件清单

| 类型 | 文件 | 说明 |
|------|------|------|
| MODIFY | `common-core/pom.xml` | 添加 Sentinel starter |
| MODIFY | `api-gateway/pom.xml` | 添加 sentinel-gateway 适配器 |
| MODIFY | `docker-compose.yaml` | 添加 Sentinel Dashboard 容器 |
| MODIFY | Nacos `literature-common.yaml` | 添加 Sentinel 通用配置 |
| NEW | `GatewaySentinelConfig.java` | Gateway 自定义限流响应 |
| NEW | `SentinelExceptionHandler.java` | 业务服务统一限流异常处理 |
| MODIFY | 需要 Feign 的服务 `pom.xml` | 添加 OpenFeign + LoadBalancer |
| NEW | 各 Feign 接口 + Fallback | 按需创建 |

---

## 实施阶段

```mermaid
graph LR
    A["阶段一<br/>基础设施"] --> B["阶段二<br/>网关层"]
    B --> C["阶段三<br/>业务服务"]
    C --> D["阶段四<br/>Feign + 熔断"]
```

| 阶段 | 内容 | 产出 |
|------|------|------|
| **一** | Docker 部署 Sentinel Dashboard + Nacos 共享配置 | Dashboard 可访问 |
| **二** | Gateway 引入 sentinel-gateway，配置路由级流控 + 自定义 BlockHandler | 网关级限流生效 |
| **三** | common-core 引入 Sentinel，全局异常处理 + 关键接口注解式降级 | 业务接口流控生效 |
| **四** | 按需引入 OpenFeign + LoadBalancer，创建 Feign 接口 + Fallback | 服务间调用有熔断保护 |

---

## 验证计划

### 自动化验证
- `mvn compile` 确认所有模块依赖正确
- 启动各服务，确认 Sentinel Dashboard 能发现所有客户端

### 手动验证
- 通过 Sentinel Dashboard 对 Gateway 路由配置 QPS=5，使用压测工具验证限流响应
- 模拟下游服务不可用，验证 Feign Fallback 触发

---

## User Review Required

> [!IMPORTANT]
> **服务间调用 (OpenFeign) 的范围**: 目前各服务间没有远程调用代码。请确认哪些服务之间需要建立 Feign 调用关系？常见场景包括：
> - `content-service` → `user-service` (获取作者信息)
> - `knowledge-service` → `content-service` (获取书籍元数据)
> - `chat-service` → `user-service` (查询用户昵称)

> [!NOTE]
> **Sentinel Dashboard 数据持久化**: 默认 Sentinel Dashboard 的规则是内存态的，重启会丢失。方案中已设计通过 **Nacos 持久化规则**，Dashboard 与 Nacos 双向同步，确保规则持久化。
