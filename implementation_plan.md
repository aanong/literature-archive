# 实施计划 - Literature Archive

## 概述
本文档概述了 Literature Archive 项目当前的实施状态和接下来的步骤，重点关注用户服务、聊天服务和知识服务。

## 1. 用户服务 (Authentication)
**状态**: ✅ 核心 JWT 基础设施与 Spring Security 已全部实施。
- [x] **JWT 服务**: `JwtTokenService` 已实现，支持 RSA-2048 密钥对生成。
- [x] **JWK 暴露**: `JwkSetController` 通过 `/.well-known/jwks.json` 暴露 JWK Set JSON。
- [x] **Spring Security 集成**:
  - `AuthServerConfig` 已配置 `SecurityFilterChain`，保护 API 端点。
  - 使用 `oauth2ResourceServer` + JWT 验证，会话策略为 STATELESS。
  - 公开路径: `/actuator/**`, `/api/admin/auth/**`, `/.well-known/jwks.json`。
- [x] **JWK Set 消费验证**: `api-gateway` 的 `SecurityConfig` 已配置 `ReactiveJwtDecoder`，可正确消费 JWT。

## 2. 聊天服务 (Real-time Communication)
**状态**: ✅ Netty 服务端管道、认证握手和消息处理已全部完成。
- [x] **Netty 管道**: 已在 `NettyServerInitializer` 中配置：
  - `LengthFieldBasedFrameDecoder` (粘包/半包处理)
  - `ChatProtocolDecoder` / `ChatProtocolEncoder`
  - `ChatCryptoCodec` (加密层)
  - `IdleStateHandler` (心跳检测)
  - `AuthHandler` (JWT 认证)
- [x] **认证**: `AuthHandler` 验证 JWT 令牌并注册用户会话。
- [x] **消息处理**: `ChatMessageHandler` 已完成，支持单聊 (`handleSingleChat`) 和群聊 (`handleGroupChat`)，集成 Kafka 消息转发。
- [x] **加密**: `ChatCryptoCodec` 已实现完整的 AES-GCM 加解密，支持配置开关。
- [x] **离线消息**: `OfflineMessageService` 已实现 MongoDB 持久化、Redis 离线队列推送及上线拉取。
- [x] **会话路由**: `SessionRouteService` 已实现基于 Redis 的路由注册/查询/移除。

## 3. 知识服务 (Semantic Search)
**状态**: ✅ 嵌入引擎、向量存储、内容入库和混合搜索已全部完成。
- [x] **嵌入服务**: `EmbeddingService` 已实现，支持：
  - 动态模型提供商切换 (Ollama/OpenAI)。
  - Milvus 向量存储集成。
  - 文本转向量方法 (`embed`, `store`, `search`)。
- [x] **内容入库**: `BookIngestionService` 和 `TextSplitter` 已实现，支持按段落、固定大小、章节标记三种策略拆分文本并批量导入。
- [x] **搜索 API**: 已暴露 REST 端点：
  - `KnowledgeController` 提供 `/search` 关键词搜索。
  - `QAController` 提供 `/ask` 智能问答。
  - `RAGService` 实现检索增强生成 (RAG)。
- [x] **混合搜索**: `HybridSearchService` 已实现，结合向量语义搜索 (Milvus) 与关键词搜索 (MySQL)：
  - 使用 RRF (Reciprocal Rank Fusion) 算法合并排序。
  - `HybridSearchController` 暴露 `/api/knowledge/hybrid-search` 端点。
  - 支持可调节的语义/关键词权重参数 (`semanticWeight`)。

## 4. API 安全 (加密与动态配置)
**状态**: ✅ 后端混合加密 (RSA+AES) 和前端加密集成已全部完成。
**目标**: 实现可配置的混合加密 (RSA+AES)，可通过管理后台管理。

### 后端 (网关与配置)
- [x] **密钥管理**: `SecurityConfigController` 已在 `@PostConstruct` 中实现 RSA-2048 密钥对生成。
- [x] **配置 API**: `/gateway/security/config` 端点已实现，暴露：
    - `encryptionEnabled` (布尔值)
    - `publicKey` (字符串, RSA 公钥)
- [x] **网关过滤器升级**: `CryptoGatewayFilter` 已支持混合加密：
    - 头部 `X-Session-Key`: 使用后端 RSA 公钥加密的 AES 密钥。
    - 使用后端 RSA 私钥解密 `X-Session-Key`。
    - 使用解密后的 AES 会话密钥解密请求体。
- [x] **管理 API**: `SecurityAdminController` 已实现，提供：
    - `GET /gateway/security/admin/status` - 查看当前加密配置
    - `PUT /gateway/security/admin/http-encryption` - 切换 HTTP 加密
    - `PUT /gateway/security/admin/netty-encryption` - 切换 Netty 加密
    - `PUT /gateway/security/admin/global-encryption` - 切换全局加密

### 前端 (Admin Portal)
- [x] **加密库**: 已集成 `jsencrypt` (RSA) 和 `crypto-js` (AES)，通过 npm 安装。
- [x] **安全管理器**: `SecurityManager` 单例已实现 (`src/utils/security.ts`)：
    - 从网关获取安全配置 (加密开关 + RSA 公钥)。
    - 5 分钟缓存策略，防止重复请求。
    - 提供 AES 加解密、RSA 公钥加密 AES 密钥等工具方法。
- [x] **拦截器** (`src/utils/request.ts` 已升级)：
    - **请求**: 启用加密时，生成一次性 AES 密钥 -> 加密请求体 -> RSA 加密 AES 密钥 -> 附加 `X-Session-Key`、`X-Encrypted` 头部。
    - **响应**: 检测 `X-Encrypted` 头部，使用缓存的 AES 密钥解密响应体。
    - 加密失败时优雅降级为明文传输。

## 5. 立即行动项
1.  ~~**聊天服务**: 使用真实客户端调试/测试 Netty 连接流程，验证 JWT 认证握手。~~  (集成测试待执行)
2.  ~~**知识服务**: 编写单元测试或简单控制器以验证 Milvus 连接和嵌入生成。~~  (集成测试待执行)
3.  ~~**安全**: 在网关中构建 `SecurityConfigController` 原型。~~ ✅ 已完成
4.  **文档**: 保持 `docs/prd/product_requirement_document.md` 与任何 API 变更同步。
