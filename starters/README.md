# Starters 模块聚合

本目录包含 Literature Archive 项目的所有 Spring Boot Starter 模块。

## 模块列表

| 模块 | 说明 | 状态 |
|------|------|------|
| [crypto-spring-boot-starter](./crypto-spring-boot-starter/) | 加密解密 Starter | ✅ 已实现 |
| [oss-spring-boot-starter](./oss-spring-boot-starter/) | 文件上传 Starter | ✅ 已实现 |

## 使用方式

在业务模块中引入所需的 Starter：

```xml
<dependency>
    <groupId>com.literature</groupId>
    <artifactId>crypto-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>

<dependency>
    <groupId>com.literature</groupId>
    <artifactId>oss-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```
