# 项目规范 (Project Rules)

## 核心准则
1. **回复语言**: 必须使用正体/简体中文进行回答。
2. **回复质量**: 逻辑严密，代码准确，符合 Spring Boot 微服务最佳实践。

## 接口规范 (API Standards)
所有接口的出参必须统一封装。

### 统一响应结构
使用 `ApiResponse` 类（位于 `com.literature.common.core.model`）：
```java
public record ApiResponse<T>(String code, String message, T data, String traceId)
```

- **成功情况**: `code` 固定为 `"0000"`，`message` 为 `"success"`。调用 `ApiResponse.success(data, traceId)`。
- **异常情况**: `code` 和 `message` 参考 `ErrorCode` 类。调用 `ApiResponse.error(code, message, traceId)`。

### 分页规范
分页接口返回实体必须使用 `PageResponse`：
```java
public record PageResponse<T>(long total, List<T> items)
```
最终封装形态为：`ApiResponse<PageResponse<T>>`.

## 开发提示
- 解释任何技术决策时，请引用 `common-core` 中的基类。
- 确保所有新增的微服务模块都依赖 `common-core` 以保证一致性。
