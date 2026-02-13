# 云章·经籍 (Literature Archive)

## 项目简介
`云章·经籍` 是一个专注于古籍数字化、知识图谱构建与智能化阅读的数字人文平台。

本项目致力于：
1. **古籍数字化**：支持 TEI/Markdown 格式的古籍录入与展示。
2. **语义增强**：利用大型语言模型 (LLM) 进行古文断句、翻译、实体识别。
3. **知识图谱**：构建人物、地名、职官等实体关系网络（RAG系统）。
4. **沉浸阅读**：提供宋朝古风审美的阅读体验。

---

## 文档索引

详细文档请参考 `docs` 目录：

### 📚 产品文档
- [产品需求文档 (PRD)](docs/prd/product_requirement_document.md): 包含功能规格、后台原型、数据结构设计。

### 🛠️ 部署运维
- [后端服务部署指南](docs/deployment/backend_services.md): Spring Boot 微服务、Nacos、MySQL/Redis 环境搭建。
- [管理后台部署指南](docs/deployment/admin_portal.md): Admin Portal (Vue3) 构建与 Nginx 部署。
- [阅读前端部署指南](docs/deployment/user_web.md): User Web (Next.js) 部署说明。

### 📖 用户手册
- [用户操作手册](docs/user_guide/user_manual.md): 读者与管理员的操作指引。

---

## 快速开始

### 前置要求
- JDK 17+
- Node.js 18+ (pnpm 推荐)
- Docker & Docker Compose
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x

### 本地开发

1. **启动基础设施**
   ```bash
   docker-compose up -d mysql redis nacos minio
   ```

2. **启动后端服务**
   - 导入 `docs/nacos_config_plan.md` 中的配置到 Nacos。
   - 依次启动 `user-service`, `content-service`, `api-gateway` 等核心服务。

3. **启动前端**
   ```bash
   # 管理后台
   cd admin-portal
   npm install && npm run dev

   # 阅读前台
   cd user-web
   npm install && npm run dev
   ```

## 技术栈

- **后端**: Spring Cloud Alibaba, Mybatis-Plus, LangChain4j
- **前端**: Next.js (Tailwind CSS), Vue 3 (Element Plus)
- **AI/数据**: Milvus (向量库), Ollama/OpenAI (大模型)

## 版权说明
© 2026 云章书院 · 承道以文
