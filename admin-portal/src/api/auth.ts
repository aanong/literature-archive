import request from '@/utils/request'

export interface LoginRequest {
    username: string
    password: string
}

export interface TokenResponse {
    token: string
    expireSeconds: number
}

// 登录接口
export function login(data: LoginRequest) {
    return request<any, TokenResponse>({
        url: '/admin/auth/login',
        method: 'post',
        data
    })
}

export function getUserInfo() {
    return request<any, any>({
        url: '/admin/users/me', // 假设有此接口
        method: 'get'
    })
}

export function logout() {
    return request<any, any>({
        url: '/admin/auth/logout',
        method: 'post'
    })
}
