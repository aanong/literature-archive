# Knowledge Service 快速启动指南

## 前置条件

### 1. 启动基础设施服务

```bash
# 进入项目根目录
cd /Users/caoliangqiang/code/literature-archive

# 启动Docker服务
docker-compose up -d mysql redis mongodb nacos ollama milvus
```

### 2. 下载Ollama模型

```bash
# 下载Qwen2.5模型(14B参数,中文能力强)
docker exec -it ollama ollama pull qwen2.5:14b

# 下载bge-m3嵌入模型
docker exec -it ollama ollama pull bge-m3

# 验证模型已下载
docker exec -it ollama ollama list
```

### 3. 创建数据库

```bash
# 创建knowledge数据库
docker exec -it mysql mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS literature_knowledge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 验证数据库
docker exec -it mysql mysql -uroot -p -e "SHOW DATABASES;"
```

## 编译项目

```bash
cd knowledge-service

                                                                                                                                                      

# 清理并编译
mvn clean compile

# 打包(可选)
mvn clean package -DskipTests
```
OLLAMA_HOST=0.0.0.0 ollama serve
## 启动服务

### 方式1: Maven启动(开发环境)

```bash
cd knowledge-service
mvn spring-boot:run
```

### 方式2: JAR包启动

```bash
cd knowledge-service
mvn clean package -DskipTests
java -jar target/knowledge-service-0.1.0-SNAPSHOT.jar
```

### 方式3: IDE启动

在IDE中运行 `KnowledgeServiceApplication.java`

## 验证服务

### 1. 检查服务状态

```bash
# 健康检查
curl http://localhost:18084/actuator/health

# 预期输出
{"status":"UP"}
```

### 2. 检查Nacos注册

访问Nacos控制台: http://localhost:8848/nacos

- 用户名: nacos
- 密码: nacos

查看服务列表,应该能看到 `knowledge-service`

### 3. 测试Ollama连接

```bash
# 测试Ollama服务
curl http://localhost:11434/api/tags

# 测试模型生成
curl http://localhost:11434/api/generate -d '{
  "model": "qwen2.5:14b",
  "prompt": "你好,请简单介绍一下道德经",
  "stream": false
}'
```

### 4. 测试Milvus连接

```bash
# 检查Milvus状态
curl http://localhost:18092/healthz
```

## 环境变量配置(可选)

如果需要自定义配置,可以设置以下环境变量:

```bash
# MySQL配置
export MYSQL_URL=jdbc:mysql://localhost:3306/literature_knowledge?useSSL=false&serverTimezone=UTC
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=redis123

# MongoDB配置
export MONGODB_URI=mongodb://admin:mongo123@localhost:27017/literature_qa?authSource=admin

# Ollama配置
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2.5:14b
export OLLAMA_EMBEDDING_MODEL=bge-m3

# Milvus配置
export MILVUS_HOST=localhost
export MILVUS_PORT=19530

# Nacos配置
export NACOS_ADDR=localhost:8848
```

## 常见问题

### 1. Maven依赖下载失败

```bash
# 清理Maven缓存
rm -rf ~/.m2/repository/dev/langchain4j

# 重新下载
mvn clean compile
```

### 2. Ollama模型未下载

```bash
# 检查Ollama容器状态
docker ps | grep ollama

# 进入容器下载模型
docker exec -it ollama bash
ollama pull qwen2.5:14b
ollama pull bge-m3
exit
```

### 3. 数据库连接失败

```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 检查数据库是否创建
docker exec -it mysql mysql -uroot -p -e "SHOW DATABASES;"

# 查看日志
docker logs mysql
```

### 4. Milvus连接失败

```bash
# 检查Milvus及其依赖容器
docker ps | grep milvus

# 重启Milvus
docker-compose restart milvus milvus-etcd milvus-minio
```

## 下一步

服务启动成功后,可以:

1. 使用API导入典籍知识条目
2. 测试知识向量化功能
3. 测试智能问答功能
4. 生成科普文章

详细API文档请参考: [api_design.md](file:///Users/caoliangqiang/.gemini/antigravity/brain/f8b07dfc-b089-4b46-9c5a-984f7514334d/api_design.md)
