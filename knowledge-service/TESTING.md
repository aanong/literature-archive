# 知识库服务测试指南

## 1. 单元测试 (Unit Tests)
我们已经建立了一套 Service 层的单元测试，使用 Mockito 模拟了数据库和外部 AI 服务的交互。这些测试不需要任何外部环境，可以直接运行。

### 包含的测试类
- `KnowledgeServiceTest`: 测试知识条目的 CRUD 逻辑和向量化调用。
- `QAServiceTest`: 测试问答流程，包括 RAG 检索调用、LangChain 交互和会话/消息保存。

### 运行方式
```bash
mvn test
```

## 2. 集成测试 (Integration Tests)
由于本项目依赖多个外部服务 (MySQL, Milvus, Ollama, Redis, Nacos)，自动化集成测试配置较为复杂。建议在本地开发环境启动所有 Docker 容器后，进行手动集成测试或 API 测试。

### 环境准备
确保以下服务已启动:
```bash
# 在项目根目录运行
docker-compose up -d mysql redis milvus ollama nacos
```

### 手动测试流程

#### A. 知识向量化测试
1. 启动应用: `mvn spring-boot:run`
2. 创建知识:
   ```bash
   curl -X POST http://localhost:8083/api/knowledge/items \
     -H "Content-Type: application/json" \
     -d '{"title":"测试知识","content":"这是测试内容"}'
   ```
3. 触发向量化:
   ```bash
   # ID为上一步返回的ID
   curl -X POST http://localhost:8083/api/knowledge/items/1/vectorize
   ```
4. 验证: 查看日志是否输出 `知识条目已向量化`，并在 Milvus 中是否存在数据。

#### B. 智能问答测试
1. 创建会话:
   ```bash
   curl -X POST http://localhost:8083/api/qa/sessions \
     -H "Content-Type: application/json" \
     -d '{"userId":1}'
   ```
2. 提问:
   ```bash
   curl -X POST http://localhost:8083/api/qa/sessions/1/ask \
     -H "Content-Type: application/json" \
     -d '{"question":"测试内容是什么?"}'
   ```
3. 验证: 系统应能检索到刚才创建的知识，并返回相关回答。

## 3. 常见问题
- **Unit Test 失败**: 检查 Mockito 模拟的返回值类型是否匹配。
- **集成测试连接失败**: 检查 `application.yml` 中的服务地址配置是否正确，特别是 Docker 容器内的连接。
