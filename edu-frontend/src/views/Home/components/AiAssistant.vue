<script setup>
import { ref, nextTick, onMounted, watch, computed } from 'vue'
import { chatSend, clearSession, getAiHealth } from '../../../api/ai'

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
const collapsed = ref(false)

onMounted(async () => {
  try {
    const health = await getAiHealth()
    ragEnabled.value = health.ragEnabled
    aiAvailable.value = true
  } catch {
    aiAvailable.value = false
    ragEnabled.value = false
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

const hasMessages = computed(() => messages.value.length > 0)
</script>

<template>
  <aside class="ai-assistant" :class="{ collapsed }">
    <!-- Header -->
    <div class="ai-header">
      <div class="ai-brand">
        <span class="ai-icon">🤖</span>
        <div class="ai-title">
          <strong>AI 学伴</strong>
          <small v-if="healthChecked && aiAvailable" class="status-dot online">在线</small>
          <small v-else-if="healthChecked && !aiAvailable" class="status-dot offline">离线</small>
          <small v-else class="status-dot">检查中...</small>
        </div>
      </div>
      <div class="ai-header-actions">
        <el-tooltip content="清除对话" placement="bottom">
          <button class="icon-btn" @click="handleClear" :disabled="!hasMessages || loading">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
            </svg>
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- Messages -->
    <div ref="messagesContainer" class="ai-messages">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="msg-row"
        :class="'msg-' + msg.role"
      >
        <div v-if="msg.role === 'assistant'" class="msg-avatar">🤖</div>
        <div class="msg-bubble" v-html="msg.content" />
        <div v-if="msg.role === 'user'" class="msg-avatar user-avatar">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </div>
      </div>

      <!-- Typing indicator -->
      <div v-if="loading" class="msg-row msg-assistant">
        <div class="msg-avatar">🤖</div>
        <div class="msg-bubble typing-indicator">
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!healthChecked" class="msg-loading">
        <span>连接 AI 服务中...</span>
      </div>
    </div>

    <!-- Input -->
    <div class="ai-input">
      <div class="input-wrap">
        <input
          v-model="inputText"
          type="text"
          class="msg-input"
          placeholder="输入你的问题..."
          :disabled="loading || !aiAvailable"
          @keydown="handleKeydown"
        />
        <el-tooltip content="发送 (Enter)" placement="top">
          <button
            class="send-btn"
            :class="{ active: inputText.trim() && !loading && aiAvailable }"
            :disabled="!inputText.trim() || loading || !aiAvailable"
            @click="sendMessage"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
          </button>
        </el-tooltip>
      </div>
      <div v-if="ragEnabled" class="rag-badge">
        <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L2 7l10 5 10-5-10-5z" />
          <path d="M2 17l10 5 10-5" />
          <path d="M2 12l10 5 10-5" />
        </svg>
        知识库增强
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.ai-assistant {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  max-height: 620px;
}

/* ── Header ── */
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
  flex-shrink: 0;
}

.ai-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-icon {
  font-size: 22px;
  line-height: 1;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-title strong {
  font-size: 15px;
  color: #1a1a1a;
}

.status-dot {
  font-size: 11px;
  color: #bbb;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ddd;
  flex-shrink: 0;
}

.status-dot.online::before {
  background: #52c41a;
}

.status-dot.offline::before {
  background: #ff4d4f;
}

.ai-header-actions {
  display: flex;
  gap: 6px;
}

.icon-btn {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #999;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;

  &:hover:not(:disabled) {
    background: #f5f5f5;
    color: #555;
  }

  &:disabled {
    opacity: 0.35;
    cursor: not-allowed;
  }
}

/* ── Messages ── */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 200px;
  max-height: 400px;
  scroll-behavior: smooth;
}

.msg-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  max-width: 100%;
}

.msg-assistant {
  justify-content: flex-start;
}

.msg-user {
  justify-content: flex-end;
}

.msg-system {
  justify-content: center;
}

.msg-avatar {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 16px;
  background: #f0f5ff;
}

.user-avatar {
  background: #1677ff;
  color: #fff;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  max-width: calc(100% - 42px);
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
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  text-align: center;
  max-width: 100%;
}

.msg-loading {
  text-align: center;
  color: #bbb;
  font-size: 13px;
  padding: 20px 0;
}

/* ── Typing indicator ── */
.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 14px 18px !important;
}

.typing-indicator .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #bbb;
  animation: bounce 1.4s ease-in-out infinite;
}

.typing-indicator .dot:nth-child(1) { animation-delay: 0s; }
.typing-indicator .dot:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}

/* ── Input ── */
.ai-input {
  flex-shrink: 0;
  padding: 12px 16px 14px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-wrap {
  display: flex;
  gap: 8px;
  align-items: center;
}

.msg-input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #fafafa;
  font-size: 13px;
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

.send-btn {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: #e4e7ed;
  color: #999;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, transform 0.15s;

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

.rag-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #95a5b3;
  padding-left: 4px;
}
</style>
