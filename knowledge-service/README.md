# Knowledge Service (AI科普模块)

基于Spring Boot和LangChain4j构建的AI科普知识服务,集成了Ollama本地大模型和Milvus向量数据库,提供智能问答、知识检索和科普文章生成功能。

## 核心功能

1. **典籍知识管理**
   - 知识条目增删改查
   - **知识向量化** (`POST /api/knowledge/items/{id}/vectorize`): 将知识点转换为向量存储到Milvus,用于后续检索。

2. **智能问答 (RAG)**
   - 基于检索增强生成(RAG)技术
   - 用户提问时自动检索相关知识条目
   - 将检索到的上下文注入到Prompt中
   - 支持多轮对话上下文管理

3. **科普文章生成**
   - 基于AI自动生成科普文章
   - 支持自定义主题和要求

## 技术栈

- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Plus 3.5.5
- **AI集成**: LangChain4j 0.27.1
- **LLM**: Ollama (Qwen2.5:14b)
- **Embedding**: Ollama (bge-m3)
- **Vector DB**: Milvus 2.3+
- **Database**: MySQL 8.0
- **Cache**: Redis 7.0

## 快速启动

### 1. 启动基础设施

```bash
docker-compose up -d mysql redis mongodb nacos ollama milvus
```

### 2. 准备模型

```bash
# 下载Qwen2.5模型
docker exec -it ollama ollama pull qwen2.5:14b

# 下载Embedding模型
docker exec -it ollama ollama pull bge-m3
```

### 3. 运行服务

```bash
mvn spring-boot:run
```

## API使用示例

### 1. 创建并向量化知识

```bash
# 1. 创建知识条目
curl -X POST http://localhost:8083/api/knowledge/items \
  -H "Content-Type: application/json" \
  -d '{
    "title": "道德经第一章",
    "content": "道可道,非常道。名可名,非常名。无名天地之始;有名万物之母。",
    "category": "philosophy",
    "tags": ["道家","老子"]
  }'

# 假设返回ID为1

# 2. 向量化知识 (关键步骤!)
curl -X POST http://localhost:8083/api/knowledge/items/1/vectorize
```

### 2. 智能问答

```bash
# 1. 创建会话
curl -X POST http://localhost:8083/api/qa/sessions \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "问答测试"}'

# 2. 提问 (会自动检索知识库)
curl -X POST http://localhost:8083/api/qa/sessions/1/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "道德经里怎么解释道?"}'
```

### 3. 生成科普文章

```bash
curl -X POST http://localhost:8083/api/popular-science/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "老子的哲学思想对现代生活的影响",
    "requirement": "通俗易懂,800字左右"
  }'
```

## 配置说明

在 `application.yml` 中配置:

```yaml
langchain4j:
  ollama:
    base-url: http://localhost:11434
    chat-model:
      model-name: qwen2.5:14b
    embedding-model:
      model-name: bge-m3
  milvus:
    host: localhost
    port: 19530
    collection-name: knowledge_embeddings
```
