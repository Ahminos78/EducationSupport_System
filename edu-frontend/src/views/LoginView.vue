<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authStore.login({
      username: form.username.trim(),
      password: form.password,
    })
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-brand">
        <div class="brand-mark large">EDU</div>
        <div>
          <h1>在线教育辅助教学系统</h1>
          <p>通过网关完成登录认证与用户状态验证。</p>
        </div>
      </div>

      <el-form ref="formRef" class="login-form" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" autocomplete="username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            autocomplete="current-password"
            placeholder="密码"
            show-password
            type="password"
            :prefix-icon="Lock"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button class="login-button" :loading="loading" type="primary" @click="submit">
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>
