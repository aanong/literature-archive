import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, type LoginRequest } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const username = ref('')
    const roles = ref<string[]>([])

    function setToken(newToken: string) {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    function removeToken() {
        token.value = ''
        localStorage.removeItem('token')
    }

    async function loginAction(loginForm: LoginRequest) {
        try {
            const res = await login(loginForm)
            setToken(res.token)
            return true
        } catch (error) {
            return false
        }
    }

    async function logoutAction() {
        removeToken()
        // location.reload() // 或者 router.push('/login')
    }

    return {
        token,
        username,
        roles,
        loginAction,
        logoutAction,
        removeToken
    }
})
