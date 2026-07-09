<script setup>
import { computed, onMounted, ref } from 'vue'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const TOKEN_KEY = 'edu_token'
const USER_KEY = 'edu_user'

const username = ref('admin')
const password = ref('admin123')
const token = ref(localStorage.getItem(TOKEN_KEY) || '')
const user = ref(readStoredUser())
const loading = ref(false)
const message = ref('')

const isLoggedIn = computed(() => Boolean(token.value && user.value))
const roleText = computed(() => {
  const roleMap = {
    1: '学生',
    2: '教师',
    3: '管理员',
  }
  return user.value ? roleMap[user.value.role] || '未知角色' : ''
})

onMounted(() => {
  if (token.value) {
    fetchCurrentUser()
  }
})

async function login() {
  message.value = ''
  if (!username.value.trim() || !password.value.trim()) {
    message.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const result = await request('/users/login', {
      method: 'POST',
      body: JSON.stringify({
        username: username.value.trim(),
        password: password.value,
      }),
    })
    token.value = result.token
    user.value = result.user
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem(USER_KEY, JSON.stringify(result.user))
    message.value = '登录成功'
  } catch (error) {
    clearAuth()
    message.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}

async function fetchCurrentUser() {
  message.value = ''
  loading.value = true
  try {
    const currentUser = await request('/users/me')
    user.value = currentUser
    localStorage.setItem(USER_KEY, JSON.stringify(currentUser))
  } catch (error) {
    clearAuth()
    message.value = error.message || '登录状态已失效'
  } finally {
    loading.value = false
  }
}

function logout() {
  clearAuth()
  message.value = '已退出登录'
}

async function request(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }
  if (token.value) {
    headers.Authorization = `Bearer ${token.value}`
  }
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })
  const payload = await response.json().catch(() => null)
  if (!response.ok || !payload || payload.code !== 200) {
    if (payload?.code === 401) {
      clearAuth()
    }
    throw new Error(payload?.message || `请求失败：${response.status}`)
  }
  return payload.data
}

function clearAuth() {
  token.value = ''
  user.value = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

function readStoredUser() {
  const stored = localStorage.getItem(USER_KEY)
  if (!stored) {
    return null
  }
  try {
    return JSON.parse(stored)
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-panel">
      <div class="brand">
        <div class="brand-mark">EDU</div>
        <div>
          <h1>在线教育辅助教学系统</h1>
          <p>登录后验证网关、Token 与当前用户接口是否正常。</p>
        </div>
      </div>

      <form v-if="!isLoggedIn" class="login-form" @submit.prevent="login">
        <label>
          <span>用户名</span>
          <input v-model="username" autocomplete="username" placeholder="请输入用户名" />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="password"
            autocomplete="current-password"
            placeholder="请输入密码"
            type="password"
          />
        </label>

        <button class="primary-button" :disabled="loading" type="submit">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <section v-else class="user-card">
        <div class="user-heading">
          <div class="avatar">{{ user.nickname?.slice(0, 1) || user.username?.slice(0, 1) }}</div>
          <div>
            <h2>{{ user.nickname }}</h2>
            <p>{{ user.username }} · {{ roleText }}</p>
          </div>
        </div>

        <dl class="user-meta">
          <div>
            <dt>用户ID</dt>
            <dd>{{ user.id }}</dd>
          </div>
          <div>
            <dt>角色编码</dt>
            <dd>{{ user.role }}</dd>
          </div>
          <div>
            <dt>创建时间</dt>
            <dd>{{ user.createdAt || '-' }}</dd>
          </div>
        </dl>

        <div class="actions">
          <button class="secondary-button" :disabled="loading" type="button" @click="fetchCurrentUser">
            重新验证
          </button>
          <button class="text-button" type="button" @click="logout">退出登录</button>
        </div>
      </section>

      <p v-if="message" class="message" :class="{ success: isLoggedIn && message === '登录成功' }">
        {{ message }}
      </p>
    </section>
  </main>
</template>
