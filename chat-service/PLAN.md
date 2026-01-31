# Chat Service 开发计划

## Phase 1: 基础设施搭建 (✅ 已完成)
- [x] **MyBatis-Plus 集成**: 移除 JPA，全线迁移至 MyBatis-Plus。
- [x] **数据库设计**: 完成 `chat_sessions`, `chat_messages`, `chat_session_members` 等核心表设计。
- [x] **数据迁移**: 使用 Flyway 管理数据库版本 (`V1__chat_tables.sql`)。

## Phase 2: 后台管理 API (✅ 已完成)
- [x] **API 实现**: 完成会话管理、消息查询、封禁/禁言等管理接口。
- [x] **代码重构**: 统一响应结构 `ApiResponse`，提取公共配置到 `common-core`。

## Phase 3: Netty 长连接层 (✅ 已完成)
- [x] **协议设计**: 自定义二进制协议 (Magic + Version + Cmd + ReqId + Length + Protobuf Body)。
- [x] **核心组件**:
  - `NettyServer`: 基于 Netty 4.1 构建高性能 TCP 服务。
  - `ChatProtocolCodec`: 实现 Protobuf 编解码。
  - `AuthHandler`: 基于 Token 的连接认证。
  - `HeartbeatHandler`: 空闲检测与心跳保活。
- [x] **Session 管理**: 实现本地 UserID <-> Channel 映射管理。

## Phase 4: 分布式消息路由 (✅ 已完成)
- [x] **用户路由 (Redis)**: 维护用户在线状态与所在服务器地址 (`user:route:{userId}`).
- [x] **消息分发 (Kafka)**:
  - **单聊**: 本地直接推送，跨服通过 Kafka 转发。
  - **群聊**: 采用“读扩散”优化模式，消息写入 Kafka 广播至所有节点，各节点过滤本地成员进行推送。
- [x] **基础设施**: 集成 Redis (Lettuce) 与 Kafka。

## Phase 5: 离线消息与持久化 (✅ 已完成)
- [x] **历史存储 (MongoDB)**:
  - 集成 MongoDB 存储海量聊天记录 (`chat_messages` collection)。
  - 实现 `ChatMessageMongoRepository` 提供即时历史查询。
- [x] **离线消息 (Redis)**:
  - 实现离线消息队列 (`offline:queue:{userId}`)。
  - 用户登录后自动拉取并推送离线消息。
- [x] **服务层**: `OfflineMessageService` 异步处理持久化与队列推送。

## Phase 6: 性能调优 (✅ 已完成)
- [x] **Netty 调优**:
  - 配置 `PooledByteBufAllocator` 减少 GC churn。
  - 优化 TCP 参数 (`SO_BACKLOG=10240`, `TCP_NODELAY`, `SO_REUSEADDR`)。
  - 设置 `WRITE_BUFFER_WATER_MARK` 防止 OOM。
- [x] **连接池优化**: 调整 HikariCP (MySQL)、Lettuce (Redis)、MongoDB 连接池参数。
- [x] **群聊优化**: 引入 Redis 缓存群成员列表 (`chat:session:members:{id}`)，减少数据库 IO。
- [x] **基准测试**: 编写 Python 脚本 (`scripts/benchmark.py`) 模拟 100+ 客户端并发 Protobuf 消息。

---

## 下一步建议 (Next Steps)
1. **部署验证**: 在测试环境部署 Kafka, Redis, MongoDB, MySQL 集群。
2. **监控告警**: 接入 Prometheus + Grafana 监控 Netty 连接数和消息吞吐量。
3. **前端对接**: 开发 WebSocket/TCP 前端 SDK 对接自定义协议。
