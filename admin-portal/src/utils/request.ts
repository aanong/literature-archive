import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import SecurityManager from './security'
import router from '@/router'

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

/**
 * Token 过期/失效时的统一处理
 * 使用防抖标志避免多个并发请求同时触发多次弹窗和跳转
 */
let isRedirectingToLogin = false
function handleTokenExpired(message: string = '登录凭证已过期，请重新登录') {
    if (isRedirectingToLogin) return
    isRedirectingToLogin = true

    // 清除本地存储的 token 和过期时间
    localStorage.removeItem('token')
    localStorage.removeItem('tokenExpireAt')

    ElMessageBox.confirm(message, '凭证失效', {
        confirmButtonText: '重新登录',
        showCancelButton: false,
        closeOnClickModal: false,
        closeOnPressEscape: false,
        type: 'warning',
    }).then(() => {
        router.replace('/login')
    }).finally(() => {
        // 延迟重置标志，避免跳转过程中再次触发
        setTimeout(() => {
            isRedirectingToLogin = false
        }, 1000)
    })
}

// request 拦截器
service.interceptors.request.use(
    async config => {
        // 1. 添加认证令牌
        const token = localStorage.getItem('token')
        if (token) {
            // 前端主动检查 token 是否已过期
            const expireAt = localStorage.getItem('tokenExpireAt')
            if (expireAt && Date.now() > Number(expireAt)) {
                handleTokenExpired('登录已超时，请重新登录')
                return Promise.reject(new Error('Token 已过期'))
            }
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
            // 业务层面的 token 过期码 (如后端自定义的过期码)
            if (res.code === '0401' || res.code === 'TOKEN_EXPIRED') {
                handleTokenExpired(res.message || '登录凭证已过期')
                return Promise.reject(new Error(res.message || 'Token expired'))
            }
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
        // HTTP 状态码层面的 token 过期/失效处理
        if (error.response) {
            const status = error.response.status
            switch (status) {
                case 401:
                    // 401 Unauthorized: token 过期或无效
                    handleTokenExpired('登录凭证已过期或无效，请重新登录')
                    break
                case 403:
                    // 403 Forbidden: 无权限（并非 token 过期，但可能 token 被吊销）
                    ElMessage({
                        message: '您没有该操作的权限',
                        type: 'error',
                        duration: 5 * 1000
                    })
                    break
                default:
                    ElMessage({
                        message: error.response.data?.message || error.message || '服务器异常',
                        type: 'error',
                        duration: 5 * 1000
                    })
            }
        } else {
            ElMessage({
                message: error.message || '网络连接异常',
                type: 'error',
                duration: 5 * 1000
            })
        }
        return Promise.reject(error)
    }
)

export default service
