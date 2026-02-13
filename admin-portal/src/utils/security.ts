import JSEncrypt from 'jsencrypt'
import CryptoJS from 'crypto-js'
import axios from 'axios'

/**
 * 安全配置接口
 */
export interface SecurityConfig {
    encryptionEnabled: boolean
    publicKey?: string
}

/**
 * 安全管理器 (单例)
 *
 * 负责：
 * 1. 从网关获取安全配置 (加密开关 + RSA 公钥)
 * 2. 缓存配置，避免重复请求
 * 3. 提供 RSA 加密和 AES 加解密的工具方法
 */
class SecurityManager {
    private static instance: SecurityManager
    private config: SecurityConfig | null = null
    private configPromise: Promise<SecurityConfig> | null = null

    /** 配置缓存过期时间 (毫秒)，默认 5 分钟 */
    private readonly CACHE_TTL_MS = 5 * 60 * 1000
    private lastFetchTime: number = 0

    private constructor() { }

    /**
     * 获取单例实例
     */
    static getInstance(): SecurityManager {
        if (!SecurityManager.instance) {
            SecurityManager.instance = new SecurityManager()
        }
        return SecurityManager.instance
    }

    /**
     * 获取安全配置
     * - 如果缓存有效，直接返回缓存的配置
     * - 否则从网关获取并缓存
     */
    async getConfig(): Promise<SecurityConfig> {
        const now = Date.now()
        if (this.config && (now - this.lastFetchTime) < this.CACHE_TTL_MS) {
            return this.config
        }

        // 防止并发请求
        if (this.configPromise) {
            return this.configPromise
        }

        this.configPromise = this.fetchConfig()
        try {
            const config = await this.configPromise
            this.config = config
            this.lastFetchTime = Date.now()
            return config
        } finally {
            this.configPromise = null
        }
    }

    /**
     * 从网关获取安全配置
     */
    private async fetchConfig(): Promise<SecurityConfig> {
        try {
            const response = await axios.get('/gateway/security/config')
            const data = response.data?.data || response.data
            return {
                encryptionEnabled: data.encryptionEnabled ?? false,
                publicKey: data.publicKey ?? undefined
            }
        } catch (error) {
            console.warn('[SecurityManager] 获取安全配置失败，默认禁用加密:', error)
            return { encryptionEnabled: false }
        }
    }

    /**
     * 检查加密是否启用
     */
    async isEncryptionEnabled(): Promise<boolean> {
        const config = await this.getConfig()
        return config.encryptionEnabled
    }

    /**
     * 生成随机 AES 密钥 (256 位)
     * @returns Base64 编码的 AES 密钥
     */
    generateAesKey(): string {
        const key = CryptoJS.lib.WordArray.random(32) // 256 位
        return CryptoJS.enc.Base64.stringify(key)
    }

    /**
     * 使用 AES-CBC 加密数据
     * @param data 待加密的明文数据
     * @param aesKeyBase64 Base64 编码的 AES 密钥
     * @returns Base64 编码的密文 (IV + 密文拼接)
     */
    aesEncrypt(data: string, aesKeyBase64: string): string {
        const key = CryptoJS.enc.Base64.parse(aesKeyBase64)
        const iv = CryptoJS.lib.WordArray.random(16)
        const encrypted = CryptoJS.AES.encrypt(data, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        })
        // 将 IV 和密文拼接后 Base64 编码
        const ivAndCiphertext = iv.concat(encrypted.ciphertext)
        return CryptoJS.enc.Base64.stringify(ivAndCiphertext)
    }

    /**
     * 使用 AES-CBC 解密数据
     * @param encryptedBase64 Base64 编码的密文 (IV + 密文拼接)
     * @param aesKeyBase64 Base64 编码的 AES 密钥
     * @returns 解密后的明文
     */
    aesDecrypt(encryptedBase64: string, aesKeyBase64: string): string {
        const key = CryptoJS.enc.Base64.parse(aesKeyBase64)
        const raw = CryptoJS.enc.Base64.parse(encryptedBase64)

        // 提取前16字节作为 IV
        const iv = CryptoJS.lib.WordArray.create(raw.words.slice(0, 4), 16)
        const ciphertext = CryptoJS.lib.WordArray.create(raw.words.slice(4), raw.sigBytes - 16)

        const decrypted = CryptoJS.AES.decrypt(
            // @ts-ignore - CryptoJS 需要 CipherParams 对象
            { ciphertext: ciphertext },
            key,
            {
                iv: iv,
                mode: CryptoJS.mode.CBC,
                padding: CryptoJS.pad.Pkcs7
            }
        )
        return decrypted.toString(CryptoJS.enc.Utf8)
    }

    /**
     * 使用后端 RSA 公钥加密 AES 会话密钥
     * @param aesKeyBase64 Base64 编码的 AES 密钥
     * @returns Base64 编码的加密后 AES 密钥
     */
    async rsaEncryptSessionKey(aesKeyBase64: string): Promise<string> {
        const config = await this.getConfig()
        if (!config.publicKey) {
            throw new Error('RSA 公钥不可用')
        }

        const encryptor = new JSEncrypt()
        encryptor.setPublicKey(config.publicKey)
        const encrypted = encryptor.encrypt(aesKeyBase64)
        if (!encrypted) {
            throw new Error('RSA 加密失败')
        }
        return encrypted
    }

    /**
     * 清除缓存的配置 (如需手动刷新)
     */
    clearCache(): void {
        this.config = null
        this.lastFetchTime = 0
    }
}

export default SecurityManager
