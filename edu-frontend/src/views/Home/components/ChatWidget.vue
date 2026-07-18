<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'

const visible = ref(false)
const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const chatBody = ref(null)

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  })
}

async function sendMessage() {
  const question = inputText.value.trim()
  if (!question || loading.value) return

  messages.value.push({ role: 'user', content: question })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const { data } = await axios.post('/api/ai/chat', { question, topK: 4 })
    if (data.success) {
      messages.value.push({
        role: 'assistant',
        content: data.answer,
        sources: data.sources || [],
        model: data.model,
      })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，回答失败：' + (data.error || '未知错误') })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '网络异常，请确认 AI 服务已启动（端口 8060）' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="chat-widget">
    <el-tooltip content="AI 教学助手" placement="left">
      <div class="chat-fab" @click="visible = !visible">
        <svg v-if="!visible" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
        </svg>
        <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>
    </el-tooltip>

    <transition name="chat-slide">
      <div v-if="visible" class="chat-panel">
        <div class="chat-header">
          <div class="chat-header-info">
            <div class="chat-avatar">AI</div>
            <div>
              <div class="chat-title">教学助手</div>
              <div class="chat-subtitle">Powered by DeepSeek</div>
            </div>
          </div>
          <el-icon class="chat-close" @click="visible = false"><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></el-icon>
        </div>

        <div ref="chatBody" class="chat-body">
          <div v-if="messages.length === 0" class="chat-welcome">
            <div class="welcome-icon">🤖</div>
            <div class="welcome-title">你好，我是教学助手</div>
            <div class="welcome-desc">可以问我关于课程、作业、考试等问题</div>
            <div class="welcome-hints">
              <div class="hint-item" @click="inputText = '平台有哪些功能？'; sendMessage()">平台有哪些功能？</div>
              <div class="hint-item" @click="inputText = '如何提交作业？'; sendMessage()">如何提交作业？</div>
              <div class="hint-item" @click="inputText = '考试有什么注意事项？'; sendMessage()">考试有什么注意事项？</div>
            </div>
          </div>

          <div v-for="(msg, i) in messages" :key="i" class="chat-msg" :class="msg.role">
            <div v-if="msg.role === 'assistant'" class="msg-avatar">AI</div>
            <div class="msg-bubble">
              <div class="msg-text" v-html="msg.content"></div>
              <div v-if="msg.sources && msg.sources.length" class="msg-sources">
                <div class="sources-title">参考来源</div>
                <div v-for="(src, j) in msg.sources" :key="j" class="source-item">
                  <span class="source-doc">{{ src.document }}</span>
                  <span class="source-score">相关度 {{ (src.score * 100).toFixed(0) }}%</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="chat-msg assistant">
            <div class="msg-avatar">AI</div>
            <div class="msg-bubble">
              <div class="typing-indicator">
                <span /><span /><span />
              </div>
            </div>
          </div>
        </div>

        <div class="chat-footer">
          <div class="chat-input-wrap">
            <textarea
              v-model="inputText"
              placeholder="输入你的问题..."
              rows="1"
              :disabled="loading"
              @keydown="handleKeydown"
            />
            <el-button
              class="send-btn"
              type="primary"
              circle
              :disabled="!inputText.trim() || loading"
              @click="sendMessage"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.chat-widget {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
}

.chat-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(22, 119, 255, 0.4);
  transition: all 0.3s;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 24px rgba(22, 119, 255, 0.5);
  }
}

.chat-panel {
  position: absolute;
  bottom: 72px;
  right: 0;
  width: 400px;
  height: 560px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}

.chat-title {
  font-size: 15px;
  font-weight: 600;
}

.chat-subtitle {
  font-size: 11px;
  opacity: 0.8;
}

.chat-close {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f9fafb;
}

.chat-welcome {
  text-align: center;
  padding: 32px 16px;
}

.welcome-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.welcome-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.welcome-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
}

.welcome-hints {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hint-item {
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #1677ff;
    color: #1677ff;
    background: #f0f5ff;
  }
}

.chat-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;

  &.user {
    justify-content: flex-end;

    .msg-bubble {
      background: #1677ff;
      color: #fff;
      border-radius: 12px 12px 4px 12px;
    }
  }

  &.assistant {
    .msg-bubble {
      background: #fff;
      border-radius: 12px 12px 12px 4px;
      border: 1px solid #e8e8e8;
    }
  }
}

.msg-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.msg-sources {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.sources-title {
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.source-item {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #666;
  padding: 2px 0;
}

.source-doc {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-score {
  color: #1677ff;
  flex-shrink: 0;
  margin-left: 8px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #ccc;
    animation: typing 1.2s infinite;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.chat-footer {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.chat-input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 8px 8px 8px 14px;

  textarea {
    flex: 1;
    border: none;
    background: transparent;
    resize: none;
    outline: none;
    font-size: 13px;
    line-height: 1.5;
    font-family: inherit;
    max-height: 80px;
    padding: 0;
  }
}

.send-btn {
  flex-shrink: 0;
}

.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.95);
}
</style>
