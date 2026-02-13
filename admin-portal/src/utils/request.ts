import axios from 'axios'
import { ElMessage } from 'element-plus'
import SecurityManager from './security'

// 创建 axios 实例
const service = axios.create({
    baseURL: '', // Vite Proxy configured with /api prefix
    timeout: 60000 // 请求超时时间
})

/**
 * 当前请求使用的 AES 密钥缓存
 * key: requestId, value: Base64 编码的 AES 密钥
 */
const pendingAesKeys = new Map<string, string>()

/** 生成唯一请求 ID */
let requestIdCounter = 0
function generateRequestId(): string {
    return `req_${Date.now()}_${++requestIdCounter}`
}

// request 拦截器
service.interceptors.request.use(
    async config => {
        // 1. 添加认证令牌
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }

        // 2. 加密处理
        try {
            const securityManager = SecurityManager.getInstance()
            const enabled = await securityManager.isEncryptionEnabled()

            if (enabled && config.data) {
                // 生成一次性 AES 密钥
                const aesKey = securityManager.generateAesKey()

                // 加密请求体
                const plainText = typeof config.data === 'string'
                    ? config.data
                    : JSON.stringify(config.data)
                const encryptedBody = securityManager.aesEncrypt(plainText, aesKey)

                // 使用后端 RSA 公钥加密 AES 密钥
                const encryptedSessionKey = await securityManager.rsaEncryptSessionKey(aesKey)

                // 附加加密头部
                config.headers['X-Session-Key'] = encryptedSessionKey
                config.headers['X-Encrypted'] = 'true'

                // 签名相关头部 (时间戳 + 随机数)
                const timestamp = Date.now().toString()
                const nonce = Math.random().toString(36).substring(2, 15)
                config.headers['X-Timestamp'] = timestamp
                config.headers['X-Nonce'] = nonce

                // 替换请求体为加密后的内容
                config.data = encryptedBody

                // 缓存 AES 密钥以供响应解密
                const requestId = generateRequestId()
                config.headers['X-Request-Id'] = requestId
                pendingAesKeys.set(requestId, aesKey)

                // 设置自动清理 (30秒过期)
                setTimeout(() => pendingAesKeys.delete(requestId), 30000)
            }
        } catch (error) {
            console.warn('[CryptoInterceptor] 加密处理失败，使用明文发送:', error)
        }

        return config
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)

// response 拦截器
service.interceptors.response.use(
    response => {
        // 1. 解密处理
        const isEncrypted = response.headers['x-encrypted'] === 'true'
        const requestId = response.config.headers?.['X-Request-Id'] as string

        if (isEncrypted && requestId) {
            const aesKey = pendingAesKeys.get(requestId)
            pendingAesKeys.delete(requestId) // 用完即删

            if (aesKey && typeof response.data === 'string') {
                try {
                    const securityManager = SecurityManager.getInstance()
                    const decryptedText = securityManager.aesDecrypt(response.data, aesKey)
                    response.data = JSON.parse(decryptedText)
                } catch (error) {
                    console.error('[CryptoInterceptor] 响应解密失败:', error)
                }
            }
        }

        // 2. 业务逻辑处理
        const res = response.data
        // 后端统一返回 ApiResponse { code: "0000", message: "success", data: ... }
        if (res.code && res.code !== '0000') {
            ElMessage({
                message: res.message || 'Error',
                type: 'error',
                duration: 5 * 1000
            })
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res.data
        }
    },
    error => {
        console.log('err' + error)
        ElMessage({
            message: error.message,
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    }
)

export default service
