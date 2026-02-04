# 文件上传 Spring Boot Starter 实现计划

本文档描述为 Literature Archive 项目创建 `oss-spring-boot-starter` 模块的技术方案，支持 **本地存储**、**MinIO**、**阿里云 OSS**、**七牛云** 四种存储方式。

---

## 目录

1. [技术选型](#技术选型)
2. [模块设计](#模块设计)
3. [实现方案](#实现方案)
4. [验证计划](#验证计划)

---

## 技术选型

| 存储类型 | SDK / 依赖 | 说明 |
|----------|------------|------|
| **本地存储** | JDK NIO | 适用于开发环境或小型系统 |
| **MinIO** | `io.minio:minio` | 兼容 S3 协议，私有化部署首选 |
| **阿里云 OSS** | `com.aliyun.oss:aliyun-sdk-oss` | 公有云存储 |
| **七牛云** | `com.qiniu:qiniu-java-sdk` | 国内 CDN 加速优势 |

> **设计目标**: 统一 API 接口，通过配置切换存储实现，业务代码无需修改。

---

## 模块设计

### 目录结构（整合所有 Starter）

```
literature-archive/
├── starters/                                # [NEW] Starter 模块统一目录
│   ├── crypto-spring-boot-starter/          # [MOVE] 加密解密 Starter
│   │   └── ...
│   └── oss-spring-boot-starter/             # [NEW] 文件上传 Starter
│       ├── pom.xml
│       └── src/main/java/com/literature/oss/
│           ├── autoconfigure/
│           │   ├── OssAutoConfiguration.java
│           │   └── OssProperties.java
│           ├── core/
│           │   ├── OssTemplate.java          # 统一操作模板
│           │   ├── OssClient.java            # 客户端接口
│           │   └── OssException.java         # 统一异常
│           ├── client/
│           │   ├── LocalOssClient.java       # 本地存储实现
│           │   ├── MinioOssClient.java       # MinIO 实现
│           │   ├── AliyunOssClient.java      # 阿里云 OSS 实现
│           │   └── QiniuOssClient.java       # 七牛云实现
│           ├── model/
│           │   ├── OssObject.java            # 存储对象元数据
│           │   └── UploadResult.java         # 上传结果
│           └── META-INF/
│               └── spring/
│                   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── pom.xml                                   # [MODIFY] 添加 starters 聚合模块
```

---

## 实现方案

### 1. 统一接口设计

#### OssClient.java（核心接口）

```java
public interface OssClient {
    
    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（含路径）
     * @param inputStream 文件流
     * @param contentType MIME 类型
     * @return 上传结果
     */
    UploadResult upload(String bucketName, String objectName, 
                        InputStream inputStream, String contentType);
    
    /**
     * 下载文件
     */
    InputStream download(String bucketName, String objectName);
    
    /**
     * 删除文件
     */
    void delete(String bucketName, String objectName);
    
    /**
     * 获取文件访问 URL
     * @param expireSeconds 过期时间（秒），0 表示永久
     */
    String getUrl(String bucketName, String objectName, int expireSeconds);
    
    /**
     * 检查文件是否存在
     */
    boolean exists(String bucketName, String objectName);
}
```

#### OssTemplate.java（模板类）

```java
@Component
public class OssTemplate {
    
    private final OssClient ossClient;
    private final OssProperties properties;
    
    // 简化上传方法，使用默认 bucket
    public UploadResult upload(MultipartFile file);
    
    // 生成唯一文件名（日期 + UUID）
    public String generateObjectName(String originalFilename);
    
    // 批量上传
    public List<UploadResult> uploadBatch(List<MultipartFile> files);
}
```

---

### 2. 各存储实现

#### LocalOssClient.java

```java
@ConditionalOnProperty(name = "literature.oss.type", havingValue = "local")
public class LocalOssClient implements OssClient {
    // 基于 NIO Files API 实现
    // 支持配置根目录和访问 URL 前缀
}
```

#### MinioOssClient.java

```java
@ConditionalOnProperty(name = "literature.oss.type", havingValue = "minio")
public class MinioOssClient implements OssClient {
    private final MinioClient minioClient;
    // 使用官方 SDK 实现
}
```

#### AliyunOssClient.java

```java
@ConditionalOnProperty(name = "literature.oss.type", havingValue = "aliyun")
public class AliyunOssClient implements OssClient {
    private final OSS ossClient;
    // 使用阿里云 SDK 实现
}
```

#### QiniuOssClient.java

```java
@ConditionalOnProperty(name = "literature.oss.type", havingValue = "qiniu")
public class QiniuOssClient implements OssClient {
    private final UploadManager uploadManager;
    private final Auth auth;
    // 使用七牛云 SDK 实现
}
```

---

### 3. 配置属性

```yaml
# application.yml
literature:
  oss:
    type: local  # 可选: local, minio, aliyun, qiniu
    default-bucket: literature-files
    
    # 本地存储配置
    local:
      root-path: /data/uploads
      url-prefix: http://localhost:8080/files
    
    # MinIO 配置
    minio:
      endpoint: http://minio.example.com:9000
      access-key: ${MINIO_ACCESS_KEY}
      secret-key: ${MINIO_SECRET_KEY}
    
    # 阿里云 OSS 配置
    aliyun:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: ${ALIYUN_ACCESS_KEY_ID}
      access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
    
    # 七牛云配置
    qiniu:
      access-key: ${QINIU_ACCESS_KEY}
      secret-key: ${QINIU_SECRET_KEY}
      domain: cdn.example.com
      region: z0  # 华东
```

---

### 4. 自动配置

#### OssAutoConfiguration.java

```java
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(name = "literature.oss.enabled", havingValue = "true", matchIfMissing = true)
public class OssAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public OssTemplate ossTemplate(OssClient ossClient, OssProperties properties) {
        return new OssTemplate(ossClient, properties);
    }
    
    @Configuration
    @ConditionalOnProperty(name = "literature.oss.type", havingValue = "minio")
    static class MinioConfiguration {
        @Bean
        public MinioClient minioClient(OssProperties properties) { ... }
        
        @Bean
        public OssClient ossClient(MinioClient minioClient) {
            return new MinioOssClient(minioClient);
        }
    }
    
    // 其他存储类型配置类...
}
```

---

### 5. 依赖管理

#### pom.xml

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- MinIO (optional) -->
    <dependency>
        <groupId>io.minio</groupId>
        <artifactId>minio</artifactId>
        <version>8.5.7</version>
        <optional>true</optional>
    </dependency>
    
    <!-- Aliyun OSS (optional) -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>3.17.4</version>
        <optional>true</optional>
    </dependency>
    
    <!-- Qiniu (optional) -->
    <dependency>
        <groupId>com.qiniu</groupId>
        <artifactId>qiniu-java-sdk</artifactId>
        <version>7.14.0</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## Starter 整合计划

将现有 `crypto-spring-boot-starter` 和新建 `oss-spring-boot-starter` 统一到 `starters` 目录：

| 操作 | 说明 |
|------|------|
| 创建 `starters/pom.xml` | 聚合父 POM |
| 移动 `crypto-spring-boot-starter` 到 `starters/` | 更新父 POM 路径 |
| 创建 `starters/oss-spring-boot-starter` | 新模块 |
| 修改根 `pom.xml` | 将 `<module>crypto-spring-boot-starter</module>` 改为 `<module>starters</module>` |

---

## 验证计划

### 自动化测试

```bash
cd d:\code\gmrfid\literature-archive\starters\oss-spring-boot-starter
mvn test
```

| 测试类 | 覆盖范围 |
|--------|----------|
| `LocalOssClientTest` | 本地文件上传下载删除 |
| `MinioOssClientTest` | MinIO 集成测试（Testcontainers） |
| `OssTemplateTest` | 模板方法、文件名生成 |

### 手动验证

1. **切换存储类型**：修改 `literature.oss.type` 配置，验证无缝切换。
2. **上传测试**：通过 API 上传文件，检查存储位置和返回 URL。
3. **性能测试**：大文件分片上传（MinIO/阿里云支持）。

---

## 变更总结

| 文件 | 操作 |
|------|------|
| `starters/pom.xml` | [NEW] Starter 聚合模块 |
| `starters/oss-spring-boot-starter/` | [NEW] 文件上传模块 |
| `starters/crypto-spring-boot-starter/` | [MOVE] 从根目录移动 |
| `pom.xml` | [MODIFY] 模块路径调整 |
