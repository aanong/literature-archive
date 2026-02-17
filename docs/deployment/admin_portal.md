# 管理后台部署指南 (Vue 3 + Vite)

本文档说明如何部署 `admin-portal`，这是古籍与知识图谱的运营管理后台。

---

## 🛠️ 项目依赖

- Node.js 16+
- pnpm (或 npm 7+)
- Vite 4+

## 🚀 部署步骤

### 1. 环境变量配置

在 `admin-portal/.env` 文件中设置环境变量：

```ini
# 后端 API Gateway 地址
VITE_API_URL=https://api.literature-archive.com/api/admin

# 资源 CDN 前缀 (可选)
VITE_ASSETS_CDN=https://cdn.literature-archive.com
```

### 2. 构建生产版本

```bash
cd admin-portal
pnpm install
pnpm build
```

构建完成后，会在根目录生成 `dist/` 文件夹。该目录下包含 `index.html` 以及 `assets/` 静态资源文件。

### 3. Nginx 静态文件托管

管理后台是纯 SPA 应用，需使用 Nginx 或类似 Web 服务器进行托管。

#### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name admin.literature-archive.com;

    root /var/www/admin-portal/dist;
    index index.html;

    # 处理 Vue Router 的 History Mode
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 代理 API 请求到后端网关
    location /api/ {
        proxy_pass http://literature-api-gateway:18080/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # 静态资源缓存控制
    location ~* \.(?:css|js|map|jpe?g|gif|png|ico|svg|woff2?|ttf)$ {
        expires 30d;
        add_header Cache-Control "public";
    }
}
```

### 4. 验证部署

访问 `http://admin.literature-archive.com` 即可看到登录页面。

---

## 🔒 常见问题 (FAQ)

### Q1: 刷新页面 404？
- 确 认 Nginx 中已配置 `try_files $uri $uri/ /index.html;`。这是 SPA 应用路由必须的配置。

### Q2: API 跨域问题？
- 建议在 Nginx `location /api/` 中配置反向代理，避免浏览器直接请求后端 API 导致的 CORS 问题。

### Q3: 登录后跳转失败？
- 检查 `/api/auth/login` 接口是否返回了正确的 JWT 令牌格式。
- 确认本地 Session Storage 是否启用了 HTTPS 安全策略 (Secure Cookie)，如果在 HTTP 环境下测试，请临时放宽浏览器安全策略。
