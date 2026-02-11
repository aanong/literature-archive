# Maven依赖修复说明

## 问题

LangChain4j的某些依赖模块在Maven中央仓库中找不到:
- `langchain4j-embeddings` - 不存在此模块
- 版本0.35.0可能不是稳定版本

## 解决方案

### 方案1: 使用LangChain4j 1.0.0-alpha1 (推荐)

这是一个较新的alpha版本,支持Ollama和Milvus集成:

```xml
<properties>
    <langchain4j.version>1.0.0-alpha1</langchain4j.version>
</properties>

<dependencies>
    <!-- LangChain4j 核心 -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    
    <!-- Ollama集成 -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-ollama</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    
    <!-- Milvus向量存储 -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-milvus</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
</dependencies>
```

**注意**: 嵌入功能已包含在核心模块和Ollama模块中,无需单独依赖。

### 方案2: 使用稳定版本(如果alpha版有问题)

如果alpha版本有兼容性问题,可以使用更早的稳定版本:

```xml
<properties>
    <langchain4j.version>0.27.1</langchain4j.version>
</properties>
```

但需要注意:早期版本可能不支持最新的Ollama或Milvus功能。

### 方案3: 手动实现向量存储(如果LangChain4j集成有问题)

如果LangChain4j的Milvus集成有问题,可以直接使用Milvus SDK:

```xml
<dependency>
    <groupId>io.milvus</groupId>
    <artifactId>milvus-sdk-java</artifactId>
    <version>2.3.4</version>
</dependency>
```

然后自己实现向量存储逻辑。

## 当前配置

我已将pom.xml更新为使用 `langchain4j 1.0.0-alpha1`,并移除了不存在的`langchain4j-embeddings`依赖。

## 验证步骤

```bash
cd knowledge-service
mvn clean compile
```

如果仍有问题,请查看具体错误信息,我们可以进一步调整。
