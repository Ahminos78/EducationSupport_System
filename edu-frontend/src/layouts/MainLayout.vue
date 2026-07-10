<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HeaderBar from '../views/Home/components/HeaderBar.vue'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="app-layout">
    <HeaderBar @logout="logout" />
    <el-main class="app-main">
      <div class="page-container">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<style scoped>
.app-main {
  background-color: #f5f7fa;
  min-height: calc(100vh - 70px);
  padding: 24px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}
</style>
