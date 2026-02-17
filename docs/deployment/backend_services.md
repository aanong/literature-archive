# 后端服务部署指南 (Spring Cloud Alibaba)

本文档旨在说明如何部署 `literature-archive` 后端微服务集群。

---

## 🏗️ 架构概览

本项目采用微服务架构，基于 Spring Cloud Alibaba 体系构建。
- **注册/配置中心**: Nacos
- **网关**: Spring Cloud Gateway
- **数据库**: MySQL 8.0, Redis 6.0, MongoDB 5.0
- **向量检索**: Milvus 2.4+
- **消息队列**: Kafka (可选，用于日志收集)

---

## 🚀 部署步骤

### 1. 基础设施准备 (Docker)

使用项目根目录下的 `docker-compose.yaml` 启动基础服务：

```bash
cd literature-archive
docker-compose up -d
```

这将启动 MySQL, Redis, Nacos, MinIO 等容器。

### 2. 配置中心初始化 (Nacos)

1.  访问 Nacos 控制台: `http://localhost:8848/nacos` (默认账号: `nacos/nacos`)。
2.  创建命名空间 `literature-dev` (开发环境) 或 `literature-prod` (生产环境)。
3.  参考 `docs/nacos_config_plan.md` 文档：
    -   导入 `literature-common.yaml` 到相应命名空间。
    -   根据各服务需求，导入 `user-service.yaml`, `content-service.yaml` 等配置。
    -   **注意**: 修改配置中的 `MySQL`、`Redis` 地址为实际 Docker 或宿主机 IP。

### 3. 构建 JAR 包

确保已安装 Maven 3.8+ 和 JDK 17。

```bash
# 在根目录执行
mvn clean package -DskipTests
```

构建完成后，各模块 `target/` 目录下将生成可执行 jar 包，例如 `user-service-0.0.1-SNAPSHOT.jar`。

### 4. 启动服务

建议按依赖顺序启动服务：

1.  **api-gateway**: 网关服务 (Port: 18080)
    ```bash
    java -jar api-gateway/target/api-gateway-*.jar
    ```

2.  **user-service**: 用户认证服务 (Port: 18081)
    ```bash
    java -jar user-service/target/user-service-*.jar
    ```

3.  **content-service**: 书目内容服务 (Port: 18083)
    ```bash
    java -jar content-service/target/content-service-*.jar
    ```

4.  **knowledge-service**: 知识图谱服务 (Port: 18084)
    *   **依赖**: 需确保 Milvus 服务正常运行。
    ```bash
    java -jar knowledge-service/target/knowledge-service-*.jar
    ```

5.  **chat-service**: 实时通讯服务 (Port: 18090 / Netty: 18091 / WS: 18092)
    ```bash
    java -jar chat-service/target/chat-service-*.jar
    ```

### 5. 验证服务

- 访问网关 Swagger 文档 (需自行集成 Knife4j): `http://localhost:18080/doc.html`
- 或直接 curl 测试 API:
  ```bash
  curl http://localhost:18080/api/user/health
  ```

---

## 🛠️ 常见问题

### Q1: Nacos 连接失败？
- 检查 `bootstrap.yml` 中的 Nacos 地址是否正确。
- 确保本地防火墙已开放 8848 端口。

### Q2: 数据库连接超时？
- 检查 Docker 容器网络，确保服务能够访问到宿主机的 MySQL (IP 不要是 localhost，建议用宿主机局域网 IP)。

### Q3: Milvus 无法连接？
- 确认 Milvus 的 gRPC 端口 (默认 19530) 是否暴露。
- 检查 `knowledge-service` 配置中的 `langchain4j.milvus.host`。
