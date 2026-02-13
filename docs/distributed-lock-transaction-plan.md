# 分布式锁 & 分布式事务 Starter 技术方案

项目现有 4 个 Starter（crypto / oss / sharding / logging），现需新增 **分布式锁** 和 **分布式事务** 两个 Starter，统一封装并对业务服务开箱即用。

---

## 一、分布式锁 Starter：`lock-spring-boot-starter`

### 1.1 技术选型

| 方案 | 优势 | 劣势 | 推荐度 |
|------|------|------|--------|
| **Redisson** | 功能最全、可重入/公平/联锁/红锁、看门狗自动续期 | 依赖较重 | ⭐⭐⭐⭐⭐ |
| Spring Integration Redis Lock | 轻量、Spring 原生 | 功能单一、无自动续期 | ⭐⭐⭐ |
| Curator (ZooKeeper) | 强一致 | 需额外部署 ZK 集群 | ⭐⭐ |

> **推荐使用 Redisson**。项目已有 Redis 7.0 实例，无需额外基础设施，且 Redisson 的看门狗（Watchdog）机制可自动续期，避免业务未完成就释放锁的问题。

### 1.2 核心功能

| 功能 | 说明 |
|------|------|
| `@DistributedLock` 注解 | AOP 方式加锁，支持 SpEL 表达式动态生成 key |
| `DistributedLockTemplate` | 编程式 API，适合复杂场景 |
| 看门狗自动续期 | 默认 30s 租约，10s 续期 |
| 多种锁类型 | 可重入锁、公平锁、读写锁 |
| 锁获取失败策略 | 抛异常 / 快速失败 / 阻塞等待 |

### 1.3 配置项设计

```yaml
literature:
  lock:
    enabled: true                    # 总开关
    type: REDIS                      # 锁实现类型（预留扩展）
    redis:
      key-prefix: "lit:lock:"        # 锁 key 前缀
      lease-time: 30s                # 默认租约时长
      wait-time: 10s                 # 默认等待时长
      watchdog-timeout: 30s          # 看门狗超时
```

### 1.4 使用示例

```java
// 注解式
@DistributedLock(key = "'order:' + #orderId", waitTime = 5, leaseTime = 30)
public void processOrder(String orderId) {
    // 业务逻辑
}

// 编程式
lockTemplate.executeWithLock("order:" + orderId, Duration.ofSeconds(30), () -> {
    // 业务逻辑
});
```

### 1.5 模块结构

```
lock-spring-boot-starter/
├── pom.xml
└── src/main/java/com/literature/lock/
    ├── autoconfigure/
    │   ├── LockAutoConfiguration.java      # 自动配置
    │   └── LockProperties.java             # 配置属性
    ├── annotation/
    │   ├── DistributedLock.java             # 锁注解
    │   └── LockType.java                   # 锁类型枚举
    ├── core/
    │   ├── DistributedLockTemplate.java     # 编程式 API
    │   ├── LockExecutor.java               # 锁执行器接口
    │   └── RedissonLockExecutor.java        # Redisson 实现
    ├── aspect/
    │   └── DistributedLockAspect.java       # AOP 切面
    ├── exception/
    │   └── LockAcquireException.java        # 获取锁失败异常
    └── strategy/
        ├── LockFailureStrategy.java         # 失败策略接口
        └── ThrowExceptionStrategy.java      # 默认：抛出异常
```

### 1.6 关键依赖

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

---

## 二、分布式事务 Starter：`transaction-spring-boot-starter`

### 2.1 技术选型

| 方案 | 模式 | 优势 | 劣势 | 推荐度 |
|------|------|------|------|--------|
| **Seata** | AT/TCC/Saga | 功能最全、阿里生态 | 需部署 Server | ⭐⭐⭐⭐⭐ |
| RocketMQ 事务消息 | 最终一致性 | 高性能、解耦 | 需 RocketMQ | ⭐⭐⭐⭐ |
| 本地消息表 | 最终一致性 | 无外部依赖 | 实现复杂 | ⭐⭐⭐ |
| LCN | 伪 XA | 社区不活跃 | 侵入性强 | ⭐⭐ |

> **推荐使用 Seata（AT 模式为主 + TCC 模式可选）**。项目已有 Spring Cloud Alibaba 依赖（2023.0.1.0），Seata 与其天然集成。AT 模式对业务零侵入，只需 `@GlobalTransactional` 注解即可。

### 2.2 核心功能

| 功能 | 说明 |
|------|------|
| `@GlobalTransactional` | Seata 全局事务注解（AT 模式） |
| AT 自动补偿 | 自动生成 undo_log，失败自动回滚 |
| TCC 模式支持 | 高性能场景的 Try-Confirm-Cancel |
| Nacos 配置集成 | Seata 配置存储在 Nacos |
| 多数据源支持 | 代理 DataSource 实现透明事务 |

### 2.3 Seata 部署架构

```
业务服务 A --[注册分支事务]--> Seata Server (TC) <--[注册分支事务]-- 业务服务 B
                                  |
                            Nacos (配置/注册)
```

### 2.4 配置项设计

```yaml
literature:
  transaction:
    enabled: true                        # 总开关
    mode: AT                             # 事务模式：AT / TCC
    seata:
      application-id: ${spring.application.name}
      tx-service-group: literature_tx_group
      registry:
        type: nacos
        nacos:
          server-addr: ${spring.cloud.nacos.server-addr}
          namespace: ${spring.cloud.nacos.namespace:}
          group: SEATA_GROUP
      config:
        type: nacos
        nacos:
          server-addr: ${spring.cloud.nacos.server-addr}
          namespace: ${spring.cloud.nacos.namespace:}
          group: SEATA_GROUP
```

### 2.5 使用示例

```java
// AT 模式 —— 零侵入
@GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
public void createOrder(OrderDTO dto) {
    orderService.save(dto);              // 操作订单库
    inventoryClient.deduct(dto.getSku()); // Feign 调用扣减库存
    accountClient.debit(dto.getAmount()); // Feign 调用扣款
}

// TCC 模式 —— 高性能
@TwoPhaseBusinessAction(name = "deductInventory",
    commitMethod = "confirm", rollbackMethod = "cancel")
public boolean tryDeduct(BusinessActionContext ctx, String sku, int qty) {
    // 冻结库存
}
public boolean confirm(BusinessActionContext ctx) { /* 确认扣减 */ }
public boolean cancel(BusinessActionContext ctx) { /* 解冻回滚 */ }
```

### 2.6 模块结构

```
transaction-spring-boot-starter/
├── pom.xml
└── src/main/java/com/literature/transaction/
    ├── autoconfigure/
    │   ├── TransactionAutoConfiguration.java   # 自动配置
    │   └── TransactionProperties.java          # 配置属性
    ├── config/
    │   ├── SeataDataSourceConfig.java          # DataSource 代理
    │   └── SeataFeignInterceptor.java          # 传播 XID 到下游
    ├── handler/
    │   └── GlobalTransactionExceptionHandler.java  # 事务异常处理
    └── util/
        └── RootContext.java                    # XID 上下文工具
```

### 2.7 关键依赖

```xml
<!-- Seata 与 Spring Cloud Alibaba 集成 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
<!-- Seata 核心（版本由 BOM 管理） -->
<dependency>
    <groupId>io.seata</groupId>
    <artifactId>seata-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 2.8 基础设施变更

需在 `docker-compose.yaml` 中新增 Seata Server：

```yaml
seata-server:
  image: seataio/seata-server:2.0.0
  container_name: seata-server
  ports:
    - "8091:8091"
    - "7091:7091"
  environment:
    - SEATA_PORT=8091
    - STORE_MODE=db
    - SEATA_IP=seata-server
  depends_on:
    - mysql
    - nacos
  networks:
    - literature-network
```

同时需要在 MySQL 中创建 Seata 所需的表：
- `global_table` —— 全局事务表
- `branch_table` —— 分支事务表
- `lock_table` —— 全局锁表
- 各业务库的 `undo_log` 表（AT 模式）

---

## 三、需要变更的文件清单

### 分布式锁 Starter

| 操作 | 文件 |
|------|------|
| NEW | `starters/lock-spring-boot-starter/pom.xml` |
| NEW | `LockAutoConfiguration.java` |
| NEW | `LockProperties.java` |
| NEW | `DistributedLock.java`（注解） |
| NEW | `LockType.java`（枚举） |
| NEW | `DistributedLockTemplate.java` |
| NEW | `LockExecutor.java`（接口） |
| NEW | `RedissonLockExecutor.java` |
| NEW | `DistributedLockAspect.java` |
| NEW | `LockAcquireException.java` |
| NEW | `AutoConfiguration.imports` |
| NEW | `LockAutoConfigurationTest.java` |
| MODIFY | `starters/pom.xml` — 新增 module |

### 分布式事务 Starter

| 操作 | 文件 |
|------|------|
| NEW | `starters/transaction-spring-boot-starter/pom.xml` |
| NEW | `TransactionAutoConfiguration.java` |
| NEW | `TransactionProperties.java` |
| NEW | `SeataDataSourceConfig.java` |
| NEW | `SeataFeignInterceptor.java` |
| NEW | `GlobalTransactionExceptionHandler.java` |
| NEW | `AutoConfiguration.imports` |
| MODIFY | `starters/pom.xml` — 新增 module |
| MODIFY | `docker-compose.yaml` — 新增 Seata Server |

---

## 四、对比总结

| 维度 | lock-spring-boot-starter | transaction-spring-boot-starter |
|------|--------------------------|--------------------------------|
| **解决问题** | 并发控制（如防止重复下单） | 跨服务数据一致性 |
| **底层技术** | Redisson（Redis） | Seata（AT/TCC） |
| **使用方式** | `@DistributedLock` 注解 + 编程式 API | `@GlobalTransactional` 注解 |
| **基础设施** | 已有 Redis，零新增 | 需部署 Seata Server |
| **侵入性** | 极低 | AT 模式零侵入 |
| **性能影响** | 微秒级 | AT 模式有约 10-20% 开销 |

---

## 五、实施建议

> 分布式锁 Starter 可以**优先实现**，因为不需要额外基础设施（已有 Redis）。分布式事务 Starter 需要先部署 Seata Server，建议作为第二阶段。
