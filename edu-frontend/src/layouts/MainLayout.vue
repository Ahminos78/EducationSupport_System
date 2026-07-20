<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HeaderBar from '../views/Home/components/HeaderBar.vue'

const router = useRouter()
const authStore = useAuthStore()
const chatVisible = ref(false)

function logout() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="app-layout">
    <HeaderBar @logout="logout" />
    <main class="app-main">
      <div class="page-container">
        <router-view />
      </div>
    </main>

    <button class="ai-float-btn" @click="chatVisible = !chatVisible" :title="chatVisible ? '关闭AI助手' : '打开AI助手'">
      <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1.27A7.1 7.1 0 0112 22a7.1 7.1 0 01-6.73-4H4a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 012-2z"/>
        <path d="M9 15v1" stroke-linecap="round"/><path d="M15 15v1" stroke-linecap="round"/>
        <path d="M10 18a2 2 0 004 0" stroke-linecap="round"/>
      </svg>
    </button>

    <Teleport to="body">
      <div v-if="chatVisible" class="ai-chat-overlay" @click.self="chatVisible = false">
        <div class="ai-chat-panel" @click.stop>
          <div class="chat-panel-header">
            <span>AI 助手</span>
            <button class="chat-close-btn" @click="chatVisible = false">&times;</button>
          </div>
          <iframe src="/ai-chat-embed" class="chat-iframe" frameborder="0"></iframe>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.app-main {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.ai-float-btn {
  position: fixed;
  bottom: 28px;
  right: 28px;
  z-index: 999;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(22,119,255,0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}

.ai-float-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(22,119,255,0.5);
}
</style>

<style>
.ai-chat-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0,0,0,0.25);
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 24px;
}

.ai-chat-panel {
  width: 420px;
  height: 580px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slide-up 0.25s ease;
}

.chat-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  font-weight: 600;
  font-size: 15px;
}

.chat-close-btn {
  background: none;
  border: none;
  color: rgba(255,255,255,0.8);
  font-size: 22px;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}

.chat-close-btn:hover { color: #fff; }

.chat-iframe {
  flex: 1;
  width: 100%;
  border: none;
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
