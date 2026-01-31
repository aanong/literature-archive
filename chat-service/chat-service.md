

---
name: chat-service
description: 开发基于 **Java + Spring Cloud + Netty** 的高并发 IM（即时通讯）系统，是一项综合性的工程。这不仅涉及网络编程，还包括分布式架构设计和极致的性能优化。
---

#  核心架构设计 (The Distributed Blueprint)
##  高并发 IM 不能依赖单机，必须是分布式集群架构。
-  **接入层 (Netty Gateway):** 负责维持数百万个 TCP/WebSocket 长连接。使用 Netty 构建，不承载复杂业务逻辑，仅负责鉴权、编解码和消息转发。
-  **路由层 (Service Route):** 基于 Spring Cloud (Nacos/Eureka) 的注册中心。当用户登录时，分配一个负载均衡后的 Netty 节点 IP。
-   **消息中转层 (MQ):** 跨服务器通信的核心。若用户 A 在 Server1，用户 B 在 Server2，Server1 需要通过 Kafka 或 RocketMQ 将消息投递给 Server2。
-  **状态存储 (Redis Cluster):** 维护“用户-服务器”的映射关系（Session 路由表）以及在线状态。

# Netty 进阶实战技能
##  自定义协议与 Protobuf 编解码
- 设计一个包含“魔数(Magic Number) + 版本号 + 序列化算法 + 指令类型 + 请求ID + 数据长度 + 消息体”的 Header
## 线程模型优化
- 不要在 Netty 的 `IO 线程`（workerGroup）中执行耗时的数据库或 API 调用。
- **Skill:** 使用自定义线程池处理业务逻辑。或者利用 Netty 的 `EventExecutorGroup` 来异步化业务 Handler，防止 IO 线程阻塞。
## 内存管理
- **Skill:** 尽量使用 `PooledByteBufAllocator` 和堆外内存（DirectBuffer），减少 GC 压力。
- **注意:** 手动管理 `ReferenceCounted` 引用计数，防止内存泄漏。
# 分布式消息路由逻辑 
## Session 管理
-  用户登录 Netty 后，在 Redis 存储 `{userId: Server_IP:Port}`
## 消息投递逻辑
- **单聊:** Server A 收到 A 发给 B 的消息 $\rightarrow$ 查 Redis 发现 B 在 Server B $\rightarrow$ Server A 将消息发给 MQ $\rightarrow$ Server B 订阅 MQ 消费消息 $\rightarrow$ Server B 找到本地 Channel 推送
- **群聊:** 采用“推拉结合”。对于活跃群，Server 将消息写入 MQ，各节点消费并推给本地在线成员；对于超大群，客户端可定时轮询(Pull)历史消息。
# 高并发性能调优
## 应用层优化
- **心跳检测 (IdleStateHandler):** 及时清理假死连接（僵尸连接），释放内存
- **离线消息缓存:** 消息先落库（MySQL/MongoDB），再写缓存（Redis），最后异步推送# 技术栈建议清单
## **微服务框架:** Spring Cloud Alibaba (Nacos 为主)
## **网络框架:** Netty 4.1.x
## **序列化:** Protobuf 3
## **数据库:** MySQL (用户) + MongoDB(聊天历史)
## **缓存:** Redis (Session & 状态)
## **消息队列:** Kafka (高吞吐消息转发)
## **分布式唯一 ID:** 雪花算法 (Snowflake) 或 

