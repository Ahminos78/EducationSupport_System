<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HeaderBar from '../views/Home/components/HeaderBar.vue'
import ChatWidget from '../views/Home/components/ChatWidget.vue'

const router = useRouter()
const authStore = useAuthStore()

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
    <ChatWidget />
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
  /* 去掉 el-main 默认 padding，用自定义替换 */
  box-sizing: border-box;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}
</style>
