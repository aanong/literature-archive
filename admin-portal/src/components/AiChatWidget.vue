<template>
  <div class="ai-chat-widget">
    <button class="ai-toggle" @click="toggleOpen">
      {{ open ? '关闭 AI' : 'AI资料库' }}
    </button>
    <div v-if="open" class="ai-panel">
      <div class="ai-header">AI 资料库对话</div>
      <div class="ai-body">
        <div v-if="!userId" class="ai-error">未登录，无法开启 AI 对话。</div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['ai-message', msg.role === 'USER' ? 'me' : 'bot']"
        >
          {{ msg.content }}
        </div>
        <div v-if="error" class="ai-error">{{ error }}</div>
      </div>
      <form class="ai-input" @submit.prevent="send">
        <input v-model="input" placeholder="请输入问题..." />
        <button type="submit" :disabled="loading || !userId">发送</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { createSession, getMessages, getSessions, askQuestion } from '@/api/chat'

interface QAMessage {
  id: number
  role: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
}

const open = ref(false)
const sessionId = ref<number | null>(null)
const messages = ref<QAMessage[]>([])
const input = ref('')
const loading = ref(false)
const error = ref('')

const userId = computed(() => {
  const token = localStorage.getItem('token') || ''
  if (!token) return null
  try {
    const raw = token.split('.')[1] || ''
    const base64 = raw.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(base64))
    const id = payload?.userId
    if (typeof id === 'number') return id
    if (typeof id === 'string' && id.trim()) return Number(id)
  } catch {
    return null
  }
  return null
})

onMounted(() => {
  if (open.value) {
    initSession()
  }
})

watch(open, (value) => {
  if (value) {
    initSession()
  }
})

function toggleOpen() {
  open.value = !open.value
}

async function initSession() {
  if (!userId.value) return
  error.value = ''
  try {
    const sessions = await getSessions(userId.value)
    if (sessions && sessions.length > 0) {
      sessionId.value = sessions[0].id
      await loadMessages(sessions[0].id)
      return
    }
    const created = await createSession(userId.value, 'AI资料库对话')
    sessionId.value = created.id
    messages.value = []
  } catch (err: any) {
    error.value = err?.message || '初始化会话失败'
  }
}

async function loadMessages(id: number) {
  try {
    const data = await getMessages(id)
    messages.value = data || []
  } catch (err: any) {
    error.value = err?.message || '加载消息失败'
  }
}

async function send() {
  if (!sessionId.value || !input.value.trim() || loading.value) return
  loading.value = true
  error.value = ''
  const question = input.value.trim()
  input.value = ''
  messages.value.push({ id: Date.now(), role: 'USER', content: question })
  try {
    const answer = await askQuestion(sessionId.value, { question })
    messages.value.push({
      id: Date.now() + 1,
      role: 'ASSISTANT',
      content: answer || '（无回答）'
    })
  } catch (err: any) {
    error.value = err?.message || '提问失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-chat-widget {
  position: fixed;
  right: 24px;
  top: 24px;
  z-index: 999;
}

.ai-toggle {
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
}

.ai-panel {
  margin-top: 12px;
  width: 360px;
  height: 420px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.18);
}

.ai-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  font-size: 13px;
}

.ai-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
}

.ai-message {
  max-width: 85%;
  padding: 8px 10px;
  border-radius: 8px;
  line-height: 1.4;
}

.ai-message.me {
  align-self: flex-end;
  background: #111827;
  color: #fff;
}

.ai-message.bot {
  background: #f3f4f6;
  color: #111827;
}

.ai-error {
  color: #dc2626;
  font-size: 12px;
}

.ai-input {
  border-top: 1px solid #e5e7eb;
  padding: 10px;
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .ai-chat-widget {
    right: 12px;
    top: 12px;
  }

  .ai-panel {
    width: calc(100vw - 24px);
    height: min(70vh, 520px);
  }

  .ai-toggle {
    padding: 6px 12px;
    font-size: 12px;
  }
}

.ai-input input {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 6px 8px;
  font-size: 13px;
}

.ai-input button {
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
}

.ai-input button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
