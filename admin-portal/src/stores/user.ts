import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, type LoginRequest } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const username = ref('')
    const roles = ref<string[]>([])

    function setToken(newToken: string, expiresInSeconds?: number) {
        token.value = newToken
        localStorage.setItem('token', newToken)
        // 存储 token 过期的绝对时间戳（毫秒）
        if (expiresInSeconds) {
            const expireAt = Date.now() + expiresInSeconds * 1000
            localStorage.setItem('tokenExpireAt', String(expireAt))
        }
    }

    function removeToken() {
        token.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('tokenExpireAt')
    }

    /**
     * 检查 token 是否仍然有效（未过期）
     */
    function isTokenValid(): boolean {
        const storedToken = localStorage.getItem('token')
        if (!storedToken) return false
        const expireAt = localStorage.getItem('tokenExpireAt')
        if (expireAt && Date.now() > Number(expireAt)) {
            // token 已过期，主动清除
            removeToken()
            return false
        }
        return true
    }

    async function loginAction(loginForm: LoginRequest) {
        try {
            const res = await login(loginForm)
            // expiresIn 单位为秒（后端返回 expireMinutes * 60）
            setToken(res.token, res.expiresIn)
            return true
        } catch (error) {
            return false
        }
    }

    async function logoutAction() {
        removeToken()
        router.replace('/login')
    }

    return {
        token,
        username,
        roles,
        loginAction,
        logoutAction,
        removeToken,
        isTokenValid
    }
})
