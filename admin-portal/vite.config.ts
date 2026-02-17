import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:18080', // API Gateway
                changeOrigin: true,
                // rewrite: (path) => path.replace(/^\/api/, '') // Gateway通常需要/api前缀，视具体路由配置而定
                // 根据 Gateway 配置，路由带有 /api 前缀，所以不需要 rewrite
            }
        }
    }
})
