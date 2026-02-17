<template>
  <div class="chat-page">
    <div class="session-panel">
      <div class="panel-header">
        <span class="panel-title">用户会话</span>
      </div>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="peerUsername" placeholder="输入 C 端用户名（如 reader01）" />
        </el-form-item>
        <el-button type="primary" class="full-width" @click="openPrivateSession">
          新建/进入私聊
        </el-button>
      </el-form>

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          :class="['session-item', { active: currentSessionId === session.id }]"
          @click="selectSession(session.id)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-preview">{{ session.lastMessage?.content || '暂无消息' }}</div>
        </div>
      </div>
    </div>

    <div class="message-panel">
      <div v-if="!currentSessionId" class="empty-state">请选择会话或先创建私聊</div>
      <template v-else>
        <div ref="messageContainerRef" class="message-list">
          <div
            v-for="message in messages"
            :key="message.id"
            :class="['message-row', { mine: message.senderName === currentUsername }]"
          >
            <div class="message-bubble">
              <div v-if="message.senderName !== currentUsername" class="sender">
                {{ message.senderName || message.senderId }}
              </div>
              <div>{{ message.content }}</div>
            </div>
          </div>
        </div>
        <div class="input-area">
          <el-input
            v-model="inputContent"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessageAction"
          />
          <el-button type="primary" :loading="sending" @click="sendMessageAction">发送</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createPrivateSession,
  getMySessions,
  getSessionMessages,
  type ChatMessage,
  type ChatSession
} from '@/api/im-chat'

const sessions = ref<ChatSession[]>([])
const currentSessionId = ref('')
const messages = ref<ChatMessage[]>([])
const peerUsername = ref('')
const inputContent = ref('')
const sending = ref(false)
const messageContainerRef = ref<HTMLElement | null>(null)
const currentUsername = ref(parseCurrentUsername())
const wsRef = ref<WebSocket | null>(null)
const wsStatus = ref<'disconnected' | 'connecting' | 'connected'>('disconnected')
let reconnectTimer: ReturnType<typeof setTimeout> | null = null

onMounted(async () => {
  await loadSessions()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

async function loadSessions() {
  const data = await getMySessions()
  sessions.value = data || []
  if (!currentSessionId.value && sessions.value.length > 0) {
    await selectSession(sessions.value[0].id)
  }
}

async function openPrivateSession() {
  const username = peerUsername.value.trim()
  if (!username) {
    ElMessage.warning('请输入用户名')
    return
  }
  const session = await createPrivateSession(username)
  peerUsername.value = ''
  await loadSessions()
  await selectSession(session.id)
  ElMessage.success('会话已就绪')
}

async function selectSession(sessionId: string) {
  currentSessionId.value = sessionId
  await loadMessages(sessionId)
}

async function loadMessages(sessionId: string) {
  const data = await getSessionMessages(sessionId)
  messages.value = data || []
  await nextTick()
  scrollToBottom()
}

async function sendMessageAction() {
  const content = inputContent.value.trim()
  if (!content || !currentSessionId.value || sending.value) return
  sending.value = true
  try {
    const session = sessions.value.find((s) => s.id === currentSessionId.value)
    const targetId = session?.peerUserId ? Number(session.peerUserId) : null
    const targetUserType = session?.peerUserType || null
    const ws = wsRef.value
    if (!targetId) {
      throw new Error('无法确定对方用户')
    }
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      const connected = await connectWebSocket(true)
      if (!connected) {
        throw new Error('WebSocket 未连接')
      }
    }
    ws.send(
      JSON.stringify({
        cmd: 'SINGLE_CHAT',
        sessionId: Number(currentSessionId.value),
        targetId,
        targetUserType,
        content,
        contentType: 'text',
        timestamp: Date.now()
      })
    )
    inputContent.value = ''
    await loadMessages(currentSessionId.value)
    await loadSessions()
  } finally {
    sending.value = false
  }
}

function connectWebSocket(waitForOpen = false): Promise<boolean> | void {
  if (wsStatus.value === 'connected' || wsStatus.value === 'connecting') {
    return waitForOpen ? Promise.resolve(wsStatus.value === 'connected') : undefined
  }
  const token = localStorage.getItem('token') || ''
  if (!token) {
    console.warn('[chat] token missing, skip websocket connect')
    return
  }
  const wsUrl =
    import.meta.env.VITE_CHAT_WS_URL ||
    `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.hostname}:18092/ws`
  const ws = new WebSocket(wsUrl)
  wsRef.value = ws
  wsStatus.value = 'connecting'

  ws.onopen = () => {
    console.info('[chat] websocket connected:', wsUrl)
    ws.send(JSON.stringify({ cmd: 'AUTH', token }))
    wsStatus.value = 'connected'
  }

  ws.onmessage = () => {
    loadSessions()
    if (currentSessionId.value) {
      loadMessages(currentSessionId.value)
    }
  }

  ws.onerror = () => {
    console.warn('[chat] websocket error:', wsUrl)
    wsStatus.value = 'disconnected'
  }

  ws.onclose = () => {
    console.warn('[chat] websocket closed:', wsUrl)
    wsRef.value = null
    wsStatus.value = 'disconnected'
    scheduleReconnect()
  }

  if (!waitForOpen) return
  return new Promise((resolve) => {
    const start = Date.now()
    const timer = setInterval(() => {
      if (wsStatus.value === 'connected') {
        clearInterval(timer)
        resolve(true)
        return
      }
      if (Date.now() - start > 2000) {
        clearInterval(timer)
        resolve(false)
      }
    }, 50)
  })
}

function disconnectWebSocket() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (wsRef.value) {
    wsRef.value.close()
    wsRef.value = null
  }
  wsStatus.value = 'disconnected'
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectWebSocket()
  }, 1000)
}

function scrollToBottom() {
  if (!messageContainerRef.value) return
  messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight
}

function parseCurrentUsername(): string {
  const token = localStorage.getItem('token') || ''
  if (!token) return ''
  try {
    const raw = token.split('.')[1] || ''
    const base64 = raw.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(base64))
    return payload?.sub || ''
  } catch {
    return ''
  }
}
</script>

<style scoped>
.chat-page {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  height: calc(100vh - 140px);
}

.session-panel,
.message-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 16px;
}

.panel-header {
  margin-bottom: 12px;
}

.panel-title {
  font-weight: 600;
}

.full-width {
  width: 100%;
}

.session-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: calc(100vh - 280px);
  overflow: auto;
}

.session-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
}

.session-item.active {
  border-color: #111827;
  background: #111827;
  color: #fff;
}

.session-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.session-preview {
  font-size: 12px;
  opacity: 0.8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.message-panel {
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 12px;
}

.message-row {
  display: flex;
  justify-content: flex-start;
}

.message-row.mine {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f3f4f6;
}

.message-row.mine .message-bubble {
  background: #111827;
  color: #fff;
}

.sender {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.input-area {
  border-top: 1px solid #e5e7eb;
  padding-top: 12px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}
</style>
