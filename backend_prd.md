# 古籍/诗书阅读后台技术文档（PRD）

## 1. 概述
- 产品：古籍/诗书阅读后台管理（CMS + 运营后台）
- 目标：支撑书目内容生产、发布、审核、质量校验与运营管理
- 技术约束：Java（Spring Cloud Alibaba 微服务体系）

## 2. 目标与指标
- 内容发布周期缩短、图文映射准确率提升、运营效率提升
- 指标：内容导入成功率、发布成功率、审核通过率、回滚次数

## 3. 用户角色与权限
- 管理员：系统配置、权限管理、全局发布
- 内容编辑：书目/章节/文本维护
- 审核员：版本审核、发布审批
- 运营：书库上架、推荐、标签管理

## 4. 后台页面结构（导航与原型说明）

### 顶栏
- 产品名、书库入口、全局搜索
- 用户信息与权限切换、通知中心

### 左侧导航
1. 书目管理
2. 资源管理
3. 图文映射
4. 审核与发布
5. 索引与搜索
6. 运营管理
7. 聊天管理
8. 用户与权限
9. 系统设置
10. 日志与监控

### 页面原型说明
**书目管理**
- 列表页：书名/作者/版本/状态/更新时间/操作（编辑、下架、发布）
- 详情页：元数据、卷/篇结构树、版本记录

**资源管理**
- 图片资源：批量上传、分页预览、缺页提示
- 文本资源：导入原文/译文/注释、段落预览

**图文映射**
- 映射列表：章节级映射状态、缺失/冲突提示
- 映射编辑：左图片页列表、右段落列表、拖拽或批量映射

**审核与发布**
- 审核队列：提交人、变更范围、差异对比
- 发布记录：版本号、发布范围、回滚

**索引与搜索**
- 索引状态：构建进度、失败原因
- 词库管理：同义词、禁词

**运营管理**
- 书库上架、推荐位、标签管理

**聊天管理**
- 会话列表、消息审阅、禁言与封禁

**用户与权限**
- 角色管理、权限矩阵、用户列表

**系统设置**
- 存储配置、CDN、审核策略

**日志与监控**
- 操作日志、链路追踪、告警与监控面板

## 5. REST API 清单（示例）

### 书目与章节
- POST /api/admin/books  新建书目
- PUT /api/admin/books/{bookId}  更新书目信息
- GET /api/admin/books  书目列表
- GET /api/admin/books/{bookId}  书目详情
- POST /api/admin/books/{bookId}/volumes  新建卷
- POST /api/admin/volumes/{volumeId}/chapters  新建章/篇
- PUT /api/admin/chapters/{chapterId}  更新章节
- PUT /api/admin/chapters/{chapterId}/order  调整顺序

### 资源管理
- POST /api/admin/assets/images  上传图片
- GET /api/admin/assets/images  图片列表
- POST /api/admin/assets/texts/import  导入文本
- GET /api/admin/assets/texts  文本列表
- GET /api/admin/assets/texts/{textId}  文本详情

### 图文映射
- GET /api/admin/mappings  映射列表
- POST /api/admin/mappings  创建映射
- PUT /api/admin/mappings/{mappingId}  更新映射
- POST /api/admin/mappings/validate  校验映射
- GET /api/admin/mappings/{chapterId}/preview  预览映射

### 审核与发布
- POST /api/admin/reviews  提交审核
- POST /api/admin/reviews/{reviewId}/approve  审核通过
- POST /api/admin/reviews/{reviewId}/reject  审核驳回
- POST /api/admin/publish  发布版本
- POST /api/admin/publish/rollback  版本回滚
- GET /api/admin/publish/history  发布记录

### 索引与搜索
- POST /api/admin/search/reindex  重建索引
- GET /api/admin/search/status  索引状态
- POST /api/admin/search/dictionary  词库新增
- DELETE /api/admin/search/dictionary/{id}  删除词条

### 运营管理
- POST /api/admin/ops/shelves  上架
- POST /api/admin/ops/recommend  推荐位配置
- POST /api/admin/ops/tags  标签管理

### 聊天管理
- GET /api/admin/chat/sessions  会话列表
- GET /api/admin/chat/sessions/{sessionId}/messages  会话消息
- POST /api/admin/chat/sessions/{sessionId}/mute  禁言
- POST /api/admin/chat/sessions/{sessionId}/ban  封禁

### 用户与权限
- POST /api/admin/users  新建用户
- GET /api/admin/users  用户列表
- POST /api/admin/roles  新建角色
- PUT /api/admin/roles/{roleId}  更新角色
- GET /api/admin/permissions  权限列表

### 系统与日志
- GET /api/admin/logs  操作日志
- GET /api/admin/monitor/metrics  监控指标
- GET /api/admin/monitor/traces  链路追踪
- GET /api/admin/monitor/alerts  告警列表
- GET /api/admin/config  系统配置
- PUT /api/admin/config  更新配置

## 6. 数据库表设计（MySQL）

### 核心表
- books
  - id, title, author, edition, status, created_at, updated_at
- volumes
  - id, book_id, title, order_no
- chapters
  - id, volume_id, title, order_no, status
- image_assets
  - id, book_id, volume_id, chapter_id, page_no, url, width, height
- text_paragraphs
  - id, chapter_id, para_no, content, type(original/translation/annotation)
- mappings
  - id, chapter_id, image_id, para_start_id, para_end_id
- publish_versions
  - id, book_id, version, status, created_by, created_at
- reviews
  - id, target_type, target_id, status, reviewer_id, comment

### 权限与审计
- users
  - id, username, password_hash, status
- roles
  - id, name, description
- user_roles
  - user_id, role_id
- permissions
  - id, code, name
- role_permissions
  - role_id, permission_id
- audit_logs
  - id, user_id, action, target_type, target_id, created_at
- chat_sessions
  - id, title, status, created_by, created_at
- chat_messages
  - id, session_id, sender_id, content, status, created_at
- chat_mutes
  - id, session_id, user_id, muted_until, reason
- chat_bans
  - id, session_id, user_id, status, reason
- monitor_alerts
  - id, level, title, status, created_at

## 7. 搜索与缓存方案

### Elasticsearch（全文检索）
- 索引对象：书目、章节、段落（原文/译文/注释）
- 关键字段：book_id, chapter_id, para_no, content, type
- 支持：高亮、分词、同义词

### Redis（缓存）
- 缓存对象：目录树、章节内容、映射关系、搜索热词
- 过期策略：内容更新后主动失效，常用数据滑动过期

### MySQL Binlog 同步缓存
- 使用 Binlog 订阅（Canal/Debezium）
- 当 books/chapters/text_paragraphs/mappings 更新时触发缓存失效/重建
- 保证缓存与ES索引的一致性

## 8. 微服务架构（Spring Cloud Alibaba）

### 组件建议
- 服务注册与发现：Nacos
- 配置中心：Nacos Config
- 网关：Spring Cloud Gateway
- 负载均衡：Spring Cloud LoadBalancer
- 认证与鉴权：Spring Security + JWT
- 任务调度：Spring Scheduler/XXL-Job
- 分布式事务（按需）：Seata
- 限流熔断：Sentinel
- 日志与监控：SkyWalking/Zipkin + Prometheus + Grafana

### 服务拆分（建议）
- api-gateway：统一网关与鉴权（Spring Cloud Gateway）
- content-service：书目/章节/文本
- asset-service：图片与资源管理
- mapping-service：图文映射
- publish-service：审核与发布
- search-service：检索与索引
- user-service：权限与用户
- ops-service：运营管理
- chat-service：聊天与消息管理
- monitor-service：日志检索与监控视图

## 9. 里程碑
- T+1周：PRD/原型评审
- T+2–3周：数据与权限模块
- T+4–5周：资源与映射管理
- T+6周：审核/发布/索引
- T+7周：统计与运维配置
- T+8周：联调与上线

## 10. 接口契约（REST API）

### 通用约定
- Base URL: /api/admin
- Content-Type: application/json
- 分页参数：page, pageSize
- 响应统一结构：
  - code: string
  - message: string
  - data: object
  - traceId: string

### 通用错误码
- 0000: Success
- 4001: InvalidParam
- 4004: NotFound
- 4009: Conflict
- 4010: Unauthorized
- 4030: Forbidden
- 5000: InternalError
- 5001: DownstreamError

### 书目与章节

**POST /api/admin/books**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 是 | 书名 |
| author | string | 否 | 作者 |
| edition | string | 否 | 版本 |
| status | string | 否 | 状态：draft/published/offline |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 书目ID |
| title | string | 书名 |
| author | string | 作者 |
| edition | string | 版本 |
| status | string | 状态 |
| createdAt | string | 创建时间 |

**GET /api/admin/books**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 关键词 |
| status | string | 否 | 状态 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 书目列表 |

**PUT /api/admin/books/{bookId}**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 否 | 书名 |
| author | string | 否 | 作者 |
| edition | string | 否 | 版本 |
| status | string | 否 | 状态 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 书目ID |
| updatedAt | string | 更新时间 |

**GET /api/admin/books/{bookId}**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 书目ID |
| title | string | 书名 |
| author | string | 作者 |
| edition | string | 版本 |
| status | string | 状态 |
| volumes | array | 卷列表 |

**POST /api/admin/books/{bookId}/volumes**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 是 | 卷名 |
| orderNo | int | 否 | 排序 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 卷ID |
| bookId | string | 书目ID |
| title | string | 卷名 |
| orderNo | int | 排序 |

**POST /api/admin/volumes/{volumeId}/chapters**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 是 | 章/篇名 |
| orderNo | int | 否 | 排序 |
| status | string | 否 | 状态 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 章节ID |
| volumeId | string | 卷ID |
| title | string | 标题 |

**PUT /api/admin/chapters/{chapterId}**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 否 | 标题 |
| status | string | 否 | 状态 |
| orderNo | int | 否 | 排序 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 章节ID |
| updatedAt | string | 更新时间 |

**PUT /api/admin/chapters/{chapterId}/order**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orderNo | int | 是 | 排序 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 章节ID |
| orderNo | int | 排序 |

### 资源管理

**POST /api/admin/assets/images**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| bookId | string | 是 | 书目ID |
| volumeId | string | 否 | 卷ID |
| chapterId | string | 否 | 章节ID |
| pageNo | int | 是 | 页码 |
| url | string | 是 | 图片地址 |
| width | int | 否 | 宽 |
| height | int | 否 | 高 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 图片ID |
| pageNo | int | 页码 |
| url | string | 图片地址 |

**GET /api/admin/assets/images**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| bookId | string | 否 | 书目ID |
| chapterId | string | 否 | 章节ID |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 图片列表 |

**POST /api/admin/assets/texts/import**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| chapterId | string | 是 | 章节ID |
| type | string | 是 | original/translation/annotation |
| content | string | 是 | 文本内容（可分段解析） |
| source | string | 否 | 来源 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| imported | int | 导入段落数 |
| chapterId | string | 章节ID |

**GET /api/admin/assets/texts**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| chapterId | string | 否 | 章节ID |
| type | string | 否 | original/translation/annotation |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 段落列表 |

**GET /api/admin/assets/texts/{textId}**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 段落ID |
| content | string | 内容 |
| type | string | 类型 |
| chapterId | string | 章节ID |

### 图文映射

**GET /api/admin/mappings**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| chapterId | string | 否 | 章节ID |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 映射列表 |

**POST /api/admin/mappings**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| chapterId | string | 是 | 章节ID |
| imageId | string | 是 | 图片ID |
| paraStartId | string | 是 | 起始段落ID |
| paraEndId | string | 是 | 结束段落ID |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 映射ID |
| chapterId | string | 章节ID |

**PUT /api/admin/mappings/{mappingId}**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| imageId | string | 否 | 图片ID |
| paraStartId | string | 否 | 起始段落ID |
| paraEndId | string | 否 | 结束段落ID |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 映射ID |
| updatedAt | string | 更新时间 |

**POST /api/admin/mappings/validate**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| chapterId | string | 是 | 章节ID |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| valid | bool | 是否通过 |
| issues | array | 错误列表 |

**GET /api/admin/mappings/{chapterId}/preview**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| chapterId | string | 章节ID |
| items | array | 映射预览 |

### 审核与发布

**POST /api/admin/reviews**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| targetType | string | 是 | book/volume/chapter |
| targetId | string | 是 | 目标ID |
| comment | string | 否 | 说明 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 审核ID |
| status | string | 状态 |

**POST /api/admin/reviews/{reviewId}/approve**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 审核ID |
| status | string | 状态 |

**POST /api/admin/reviews/{reviewId}/reject**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| reason | string | 是 | 驳回原因 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 审核ID |
| status | string | 状态 |

**POST /api/admin/publish**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| targetType | string | 是 | book/volume/chapter |
| targetId | string | 是 | 目标ID |
| version | string | 是 | 版本号 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 发布ID |
| status | string | 状态 |

**POST /api/admin/publish/rollback**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| version | string | 是 | 版本号 |
| reason | string | 否 | 原因 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 发布ID |
| status | string | 状态 |

**GET /api/admin/publish/history**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| bookId | string | 否 | 书目ID |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 发布记录 |

### 索引与搜索

**POST /api/admin/search/reindex**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| scope | string | 否 | book/chapter/all |
| targetId | string | 否 | 目标ID |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| taskId | string | 任务ID |
| status | string | 状态 |

**GET /api/admin/search/status**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| status | string | 索引状态 |
| lastUpdatedAt | string | 最近更新时间 |

**POST /api/admin/search/dictionary**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| word | string | 是 | 词条 |
| type | string | 是 | synonym/stop |
| target | string | 否 | 目标词 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 词条ID |
| word | string | 词条 |

**DELETE /api/admin/search/dictionary/{id}**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 词条ID |

### 运营管理

**POST /api/admin/ops/shelves**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| bookId | string | 是 | 书目ID |
| status | string | 是 | on/off |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| bookId | string | 书目ID |
| status | string | 上架状态 |

**POST /api/admin/ops/recommend**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| bookId | string | 是 | 书目ID |
| position | int | 是 | 推荐位 |
| startAt | string | 否 | 开始时间 |
| endAt | string | 否 | 结束时间 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 推荐ID |
| bookId | string | 书目ID |

**POST /api/admin/ops/tags**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | string | 是 | 标签名 |
| bookIds | array | 否 | 书目列表 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 标签ID |
| name | string | 标签名 |

### 用户与权限

**POST /api/admin/users**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| status | string | 否 | 状态 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 用户ID |
| username | string | 用户名 |

**GET /api/admin/users**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 关键词 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 用户列表 |

**POST /api/admin/roles**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | string | 是 | 角色名 |
| description | string | 否 | 描述 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 角色ID |
| name | string | 角色名 |

**PUT /api/admin/roles/{roleId}**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | string | 否 | 角色名 |
| description | string | 否 | 描述 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 角色ID |
| name | string | 角色名 |

**GET /api/admin/permissions**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| items | array | 权限列表 |

### 聊天管理

**GET /api/admin/chat/sessions**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 关键词 |
| status | string | 否 | 状态 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 会话列表 |

**GET /api/admin/chat/sessions/{sessionId}/messages**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 消息列表 |

**POST /api/admin/chat/sessions/{sessionId}/mute**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| userId | string | 是 | 用户ID |
| minutes | int | 是 | 禁言时长（分钟） |
| reason | string | 否 | 原因 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| sessionId | string | 会话ID |
| mutedUntil | string | 禁言结束时间 |

**POST /api/admin/chat/sessions/{sessionId}/ban**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| userId | string | 是 | 用户ID |
| reason | string | 否 | 原因 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| sessionId | string | 会话ID |
| status | string | 封禁状态 |

### 系统与日志

**GET /api/admin/logs**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| userId | string | 否 | 用户ID |
| action | string | 否 | 操作类型 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 日志列表 |

**GET /api/admin/monitor/metrics**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| items | array | 监控指标 |

**GET /api/admin/monitor/traces**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| traceId | string | 否 | 追踪ID |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 链路列表 |

**GET /api/admin/monitor/alerts**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| status | string | 否 | 状态 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| total | int | 总数 |
| items | array | 告警列表 |

**GET /api/admin/config**

响应字段
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| storage | object | 存储配置 |
| cdn | object | CDN配置 |

**PUT /api/admin/config**

请求字段
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| storage | object | 存储配置 |
| cdn | object | CDN配置 |

## 11. MySQL 建表 SQL（含索引与约束）

```sql
CREATE TABLE books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  author VARCHAR(64),
  edition VARCHAR(64),
  status VARCHAR(16) NOT NULL DEFAULT 'draft',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_books_title_edition (title, edition),
  KEY idx_books_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE volumes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  order_no INT NOT NULL DEFAULT 0,
  KEY idx_volumes_book_id (book_id),
  KEY idx_volumes_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chapters (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  volume_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  order_no INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'draft',
  KEY idx_chapters_volume_id (volume_id),
  KEY idx_chapters_status (status),
  KEY idx_chapters_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE image_assets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  volume_id BIGINT,
  chapter_id BIGINT,
  page_no INT NOT NULL,
  url VARCHAR(512) NOT NULL,
  width INT,
  height INT,
  UNIQUE KEY uk_image_assets_chapter_page (chapter_id, page_no),
  KEY idx_image_assets_book_id (book_id),
  KEY idx_image_assets_volume_id (volume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE text_paragraphs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  chapter_id BIGINT NOT NULL,
  para_no INT NOT NULL,
  content TEXT NOT NULL,
  type VARCHAR(16) NOT NULL,
  KEY idx_text_paragraphs_chapter_id (chapter_id),
  KEY idx_text_paragraphs_type (type),
  KEY idx_text_paragraphs_para_no (para_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mappings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  chapter_id BIGINT NOT NULL,
  image_id BIGINT NOT NULL,
  para_start_id BIGINT NOT NULL,
  para_end_id BIGINT NOT NULL,
  KEY idx_mappings_chapter_id (chapter_id),
  KEY idx_mappings_image_id (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE publish_versions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  version VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_publish_versions_book_version (book_id, version),
  KEY idx_publish_versions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  target_type VARCHAR(16) NOT NULL,
  target_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL,
  reviewer_id BIGINT NOT NULL,
  comment VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_reviews_target (target_type, target_id),
  KEY idx_reviews_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_username (username),
  KEY idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255),
  UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  KEY idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  UNIQUE KEY uk_permissions_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  KEY idx_role_permissions_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(16),
  target_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_logs_user_id (user_id),
  KEY idx_audit_logs_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_chat_sessions_status (status),
  KEY idx_chat_sessions_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'normal',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_chat_messages_session_id (session_id),
  KEY idx_chat_messages_sender_id (sender_id),
  KEY idx_chat_messages_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_mutes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  muted_until DATETIME NOT NULL,
  reason VARCHAR(255),
  KEY idx_chat_mutes_session_id (session_id),
  KEY idx_chat_mutes_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_bans (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'active',
  reason VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_chat_bans_session_id (session_id),
  KEY idx_chat_bans_user_id (user_id),
  KEY idx_chat_bans_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE monitor_alerts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  level VARCHAR(16) NOT NULL,
  title VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'open',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_monitor_alerts_level (level),
  KEY idx_monitor_alerts_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
