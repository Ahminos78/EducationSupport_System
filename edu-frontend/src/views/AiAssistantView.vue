<script setup>
import { ref, nextTick, onMounted, watch, computed } from 'vue'
import { chatSend, clearSession, getAiHealth } from '../api/ai'

const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是 **学伴**，你的 AI 学习助手。有什么可以帮助你的吗？',
  },
])
const inputText = ref('')
const loading = ref(false)
const sessionId = ref(null)
const ragEnabled = ref(false)
const healthChecked = ref(false)
const aiAvailable = ref(true)
const messagesContainer = ref(null)

onMounted(async () => {
  try {
    const health = await getAiHealth()
    ragEnabled.value = health.ragEnabled
    aiAvailable.value = true
  } catch {
    aiAvailable.value = false
    messages.value.push({
      role: 'system',
      content: 'AI 服务暂时不可用，请确认后端 edu-ai-service 已启动。',
    })
  } finally {
    healthChecked.value = true
  }
})

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value || !aiAvailable.value) return

  inputText.value = ''
  messages.value.push({ role: 'user', content: text })
  loading.value = true

  try {
    const res = await chatSend({
      message: text,
      sessionId: sessionId.value,
      useRag: ragEnabled.value,
    })
    sessionId.value = res.sessionId
    messages.value.push({ role: 'assistant', content: res.content })
  } catch {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我暂时无法回答这个问题。请稍后重试。',
    })
  } finally {
    loading.value = false
  }
}

async function handleClear() {
  if (sessionId.value) {
    try {
      await clearSession(sessionId.value)
    } catch {
      /* ignore */
    }
  }
  sessionId.value = null
  messages.value = [
    {
      role: 'assistant',
      content: '你好！我是 **学伴**，你的 AI 学习助手。有什么可以帮助你的吗？',
    },
  ]
}

function handleNewChat() {
  if (sessionId.value) {
    clearSession(sessionId.value).catch(() => {})
  }
  sessionId.value = null
  messages.value = [
    {
      role: 'assistant',
      content: '你好！我是 **学伴**，你的 AI 学习助手。有什么可以帮助你的吗？',
    },
  ]
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

watch(messages, scrollToBottom, { deep: true })
</script>

<template>
  <div class="ai-page">
    <!-- Page Header -->
    <div class="ai-page-header">
      <div class="ai-page-title">
        <h1>AI 助手</h1>
        <span class="ai-subtitle">智能学伴 · 随时为你解答学习问题</span>
      </div>
      <div class="ai-page-status">
        <span v-if="healthChecked && aiAvailable" class="status-indicator online" />
        <span v-else-if="healthChecked" class="status-indicator offline" />
        <span class="status-indicator checking" />
        <span class="status-text">
          {{ healthChecked ? (aiAvailable ? '服务在线' : '服务离线') : '检测中...' }}
        </span>
        <span v-if="ragEnabled && aiAvailable" class="rag-tag">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
          知识库增强
        </span>
      </div>
      <div class="ai-page-actions">
        <el-tooltip content="新建对话" placement="bottom">
          <button class="page-action-btn" @click="handleNewChat" :disabled="loading">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14" />
            </svg>
          </button>
        </el-tooltip>
        <el-tooltip content="清除当前对话" placement="bottom">
          <button class="page-action-btn" @click="handleClear" :disabled="loading || messages.length <= 1">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
            </svg>
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- Chat Area -->
    <div class="ai-chat-area">
      <div ref="messagesContainer" class="ai-messages">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="msg-row"
          :class="'msg-' + msg.role"
        >
          <div v-if="msg.role === 'assistant'" class="msg-avatar bot">🤖</div>
          <div class="msg-bubble" v-html="msg.content" />
          <div v-if="msg.role === 'user'" class="msg-avatar user">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
        </div>

        <div v-if="loading" class="msg-row msg-assistant">
          <div class="msg-avatar bot">🤖</div>
          <div class="msg-bubble typing">
            <span class="dot" />
            <span class="dot" />
            <span class="dot" />
          </div>
        </div>

        <div v-if="!healthChecked && messages.length <= 1" class="msg-loading">
          <div class="loading-spinner" />
          <span>连接 AI 服务中...</span>
        </div>
      </div>

      <!-- Input -->
      <div class="ai-input-area">
        <div class="input-container">
          <input
            v-model="inputText"
            type="text"
            class="chat-input"
            placeholder="输入你的问题，按 Enter 发送..."
            :disabled="loading || !aiAvailable"
            @keydown="handleKeydown"
          />
          <button
            class="send-button"
            :class="{ active: inputText.trim() && !loading && aiAvailable }"
            :disabled="!inputText.trim() || loading || !aiAvailable"
            @click="sendMessage"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ai-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(31, 45, 61, 0.035);
  overflow: hidden;
}

/* ── Header ── */
.ai-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 28px;
  border-bottom: 1px solid #f0f2f5;
  flex-shrink: 0;
}

.ai-page-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.ai-page-title h1 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.ai-subtitle {
  font-size: 13px;
  color: #98a2b3;
}

.ai-page-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &.online { background: #52c41a; }
  &.offline { background: #ff4d4f; }
  &.checking {
    background: #faad14;
    animation: pulse 1.2s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.status-text {
  font-size: 13px;
  color: #888;
}

.rag-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 20px;
  background: #e6f7ff;
  color: #1677ff;
  font-size: 12px;
  font-weight: 500;
}

.ai-page-actions {
  display: flex;
  gap: 6px;
}

.page-action-btn {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #fff;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    border-color: #1677ff;
    color: #1677ff;
    background: #f0f5ff;
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

/* ── Chat Area ── */
.ai-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scroll-behavior: smooth;
}

.msg-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  max-width: 100%;
}

.msg-assistant { justify-content: flex-start; }
.msg-user { justify-content: flex-end; }
.msg-system { justify-content: center; }

.msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 18px;

  &.bot {
    background: #f0f5ff;
  }

  &.user {
    background: #1677ff;
    color: #fff;
  }
}

.msg-bubble {
  padding: 12px 18px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  max-width: 70%;
  word-break: break-word;
  white-space: pre-wrap;
}

.msg-user .msg-bubble {
  background: #1677ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-assistant .msg-bubble {
  background: #f5f7fa;
  color: #333;
  border-bottom-left-radius: 4px;
}

.msg-system .msg-bubble {
  background: #fffbe6;
  color: #ad8b00;
  font-size: 13px;
  padding: 8px 16px;
  border-radius: 8px;
  text-align: center;
  max-width: 100%;
}

.msg-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex: 1;
  color: #bbb;
  font-size: 14px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Typing indicator */
.typing {
  display: flex;
  gap: 5px;
  align-items: center;
  padding: 16px 20px !important;
}

.typing .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #bbb;
  animation: bounce 1.4s ease-in-out infinite;
}

.typing .dot:nth-child(1) { animation-delay: 0s; }
.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-8px); }
}

/* ── Input ── */
.ai-input-area {
  flex-shrink: 0;
  padding: 16px 28px 24px;
  border-top: 1px solid #f0f2f5;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: center;
  max-width: 900px;
  margin: 0 auto;
}

.chat-input {
  flex: 1;
  height: 48px;
  padding: 0 18px;
  border: 1.5px solid #e4e7ed;
  border-radius: 12px;
  background: #fafafa;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: border-color 0.2s, background 0.2s;

  &:focus {
    border-color: #1677ff;
    background: #fff;
  }

  &:disabled {
    background: #f5f5f5;
    cursor: not-allowed;
  }

  &::placeholder {
    color: #bbb;
  }
}

.send-button {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: #e4e7ed;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;

  &.active {
    background: #1677ff;
    color: #fff;
  }

  &:hover:not(:disabled) {
    &.active {
      background: #4096ff;
      transform: scale(1.05);
    }
    &:not(.active) {
      background: #d0d5dd;
    }
  }

  &:disabled {
    cursor: not-allowed;
  }
}
</style>
