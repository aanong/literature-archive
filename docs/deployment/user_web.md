# 前端阅读应用部署指南 (Next.js)

本文档说明如何部署 `user-web`，这是用户进行古籍阅读和检索的主入口应用。

---

## 🛠️ 项目依赖

- Node.js 18.17+
- pnpm (或 npm 9+)
- Next.js 14+

## 🚀 部署步骤

### 1. 环境变量配置

在 `user-web/.env.production` 文件中设置环境变量：

```ini
# 后端 API Gateway 地址
NEXT_PUBLIC_API_URL=https://api.literature-archive.com

# 应用公共 URL
NEXT_PUBLIC_APP_URL=https://www.literature-archive.com

# 资源 CDN 前缀 (如有)
NEXT_PUBLIC_ASSETS_CDN=https://cdn.literature-archive.com
```

### 2. 构建生产版本

```bash
cd user-web
pnpm install
pnpm build
```

构建完成后，将生成 `.next` 文件夹。

### 3. 运行生产服务

#### 使用 Node.js 直接运行

```bash
pnpm start
```
默认监听于 `http://localhost:3000`。

#### 使用 PM2 进程管理 (推荐)

安装 PM2：
```bash
npm install -g pm2
```

启动应用：
```bash
pm2 start npm --name "user-web" -- start
```

### 4. 静态资源导出 (可选)

由于使用了服务端渲染 (SSR) 和动态路由，本项目**不推荐**使用 `output: 'export'` 进行静态导出，建议保持 Node.js 运行时环境。

---

## 🔒 Nginx 反向代理配置

推荐使用 Nginx 作为前置代理，处理 SSL 证书和静态文件缓存。

```nginx
server {
    listen 80;
    server_name www.literature-archive.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

## 🔄 常见问题 (FAQ)

### Q1: API 调用 404？
- 检查 `NEXT_PUBLIC_API_URL` 是否指向了正确的后端网关地址。
- 确认是否配置了跨域 (CORS) 头，或者 Nginx 是否做了正确的反向代理。

### Q2:字体加载太慢？
- 项目已移除 Google Font `Inter`，改用系统宋体。如果仍有个别自定义字体，请检查 CDN 加速。
