# Literature Archive (古籍/诗书阅读系统)

## 项目概述
本项目是一个基于 **Spring Cloud Alibaba** 微服务架构的古籍/诗书阅读后台管理系统。旨在支撑书目内容的生产、发布、审核、图文映射、质量校验与运营管理。

## 核心功能
- **书目管理**：维护古籍的元数据、卷篇结构及版本。
- **资源管理**：图片批量上传、文本（原文/译文/注释）导入与分段。
- **图文映射**：左图右文的交互式映射编辑，确保古籍影像与文字精准匹配。
- **知识库 (RAG)**：基于 Milvus + LangChain4j 的古籍/科普知识检索增强生成。
- **AI 伴读**：沉浸式阅读体验，支持上下文感知的 AI 问答助手。
- **多端与运营**：Admin Portal (管理端) 与 User Web (读者端) 分离架构。

## 技术栈
- **后端框架**：Java 17 / Spring Boot 3.2 / Spring Cloud 2023
- **AI/LLM**：LangChain4j / Ollama / OpenAI / Milvus (向量数据库)
- **前端框架**：Vue 3 + Element Plus (Admin) / Next.js 14 + Tailwind (User)
- **微服务治理**：Nacos (注册中心/配置中心) / Gateway (网关) / Sentinel (限流)
- **存储方案**：MySQL 8.0 / Redis 7.0 / MongoDB 6.0 / MinIO
- **中间件**：Kafka 3.7 (消息队列) / Zookeeper

## 项目结构
```text
literature-archive
├── api-gateway           # 统一网关与鉴权中心
├── common-core           # 核心公共模块 (响应封装、错误码、基础类)
├── starters              # 自定义 Spring Boot Starters
├── user-service          # 用户与 RBAC 权限服务 (JWT/RSA)
├── content-service       # 核心内容服务 (书目、章节、文本)
├── knowledge-service     # 知识库与 AI 服务 (RAG, 向量检索)
├── asset-service         # 资源管理服务 (图片上传、素材库)
├── admin-portal          # 管理后台前端 (Vue 3)
├── user-web              # 读者端前端 (Next.js 14)
└── ...(其他服务)
```

## 快速开始

### 1. 环境准备
使用 Docker Compose 启动基础架构：
```bash
docker-compose up -d
```
内置服务包含：Nacos, MySQL, Redis, MongoDB, Kafka, Zookeeper。

### 2. 编译项目
在根目录下执行 Maven 安装：
```bash
mvn clean install
```

### 3. 本地配置
在各模块的 `bootstrap.yml` 中或通过环境变量设置以下参数（参考 `.env` 文件）：
- `NACOS_ADDR`: Nacos 访问地址
- `MYSQL_URL`: 数据库连接
- `REDIS_PASSWORD`: Redis 密码

## 开发规范
遵循项目核心原则（详见 `.cursorrules`）：
1. **统一响应**：API 必须封装在 `ApiResponse<T>` 中。
2. **错误处理**：使用 `ErrorCode` 常量，成功码为 `0000`。
3. **依赖注入**：优先使用构造函数注入。
4. **语言要求**：代码注释、文档及反馈必须使用 **中文**。

## 里程碑
- [x] 项目基础骨架搭建
- [x] 统一响应与错误码规范
- [x] 对象存储与加解密 Starter 实现
- [x] 知识库服务 (RAG) 与向量检索
- [x] 核心内容服务 (Book/Chapter)
- [x] Admin Portal 书籍摄入与管理
- [x] User Web 沉浸式阅读器与 AI 伴读
- [ ] 图文映射系统联调
- [ ] 发布与索引自动化流程
