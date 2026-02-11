# LangChain4j版本兼容性说明

## 问题背景

在集成LangChain4j时遇到了版本兼容性问题:

1. **1.0.0-alpha1**: Alpha版本,API不稳定,某些类的包路径可能变化
2. **0.35.0**: 版本不存在或未发布到Maven中央仓库
3. **最新稳定版**: 功能可能不完整或API变化较大

## 解决方案

### 方案1: 使用0.27.1稳定版 + 简化配置(当前采用)

```xml
<langchain4j.version>0.27.1</langchain4j.version>
```

**优点**:
- 稳定可靠
- Maven中央仓库可用
- API相对成熟

**缺点**:
- 可能不支持最新的Ollama或Milvus特性
- 需要在Service层手动创建模型实例

**实施方式**:
- 配置类只读取配置参数
- 在Service层按需创建Ollama客户端
- 使用HTTP客户端直接调用Ollama API

### 方案2: 不使用LangChain4j,直接集成

如果LangChain4j持续有兼容性问题,可以考虑:

#### Ollama集成
```java
// 使用OkHttp或RestTemplate直接调用Ollama API
POST http://localhost:11434/api/generate
{
  "model": "qwen2.5:14b",
  "prompt": "你的问题",
  "stream": false
}
```

#### Milvus集成
```java
// 使用Milvus官方SDK
<dependency>
    <groupId>io.milvus</groupId>
    <artifactId>milvus-sdk-java</artifactId>
    <version>2.3.4</version>
</dependency>
```

### 方案3: 等待LangChain4j 1.x正式版

LangChain4j正在向1.x迁移,可以:
- 关注官方发布动态
- 使用最新的beta/rc版本
- 准备好在正式版发布后快速升级

## 当前实施

已采用**方案1**:
- 使用LangChain4j 0.27.1
- 简化配置类,只读取配置参数
- 将在Service层实现具体的AI功能

## 下一步

1. 创建Service层,实现Ollama调用逻辑
2. 如果0.27.1版本仍有问题,切换到方案2(直接HTTP调用)
3. 持续关注LangChain4j更新,适时升级
