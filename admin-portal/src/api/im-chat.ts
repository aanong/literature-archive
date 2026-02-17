import request from '@/utils/request'

export interface ChatMessage {
  id: string
  sessionId: string
  senderId: string
  senderName?: string
  content: string
  createdAt: string
}

export interface ChatSession {
  id: string
  title: string
  updatedAt: string
  peerUserId?: string
  peerUserType?: string
  peerUsername?: string
  messageCount?: number
  lastMessage?: ChatMessage
}

export function createPrivateSession(peerUsername: string) {
  return request<any, ChatSession>({
    url: '/api/chat/private-sessions',
    method: 'post',
    data: { peerUsername }
  })
}

export function getMySessions() {
  return request<any, ChatSession[]>({
    url: '/api/chat/sessions/mine',
    method: 'get'
  })
}

export function getSessionMessages(sessionId: string) {
  return request<any, ChatMessage[]>({
    url: `/api/chat/sessions/${sessionId}/messages`,
    method: 'get',
    params: { page: 1, pageSize: 100 }
  })
}

export function sendSessionMessage(sessionId: string, content: string) {
  return request<any, ChatMessage>({
    url: `/api/chat/sessions/${sessionId}/messages`,
    method: 'post',
    data: { content }
  })
}
