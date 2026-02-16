import request from '@/utils/request'

// ===== 类型定义 =====

export interface QASession {
    id: number
    userId: number
    title: string
    status: string
    messageCount: number
    createdAt: string
    lastMessageAt: string
}

export interface QAMessage {
    id: number
    sessionId: number
    role: 'USER' | 'ASSISTANT' | 'SYSTEM'
    content: string
    modelName?: string
    createdAt: string
}

export interface AskRequest {
    question: string
    context?: string
}

// ===== API 调用 =====

/** 获取用户会话列表 */
export function getSessions(userId: number) {
    return request<any, QASession[]>({
        url: '/api/knowledge/qa/sessions',
        method: 'get',
        params: { userId }
    })
}

/** 创建新会话 */
export function createSession(userId: number, title: string) {
    return request<any, QASession>({
        url: '/api/knowledge/qa/sessions',
        method: 'post',
        data: { userId, title }
    })
}

/** 获取会话消息历史 */
export function getMessages(sessionId: number) {
    return request<any, QAMessage[]>({
        url: `/api/knowledge/qa/sessions/${sessionId}/messages`,
        method: 'get'
    })
}

/** 提问 */
export function askQuestion(sessionId: number, data: AskRequest) {
    return request<any, string>({
        url: `/api/knowledge/qa/sessions/${sessionId}/ask`,
        method: 'post',
        data
    })
}
