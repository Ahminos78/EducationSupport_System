import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { currentUserApi, loginApi } from '../api/user'
import { clearStoredAuth, getStoredToken, getStoredUser, setStoredToken, setStoredUser } from '../utils/authStorage'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getStoredToken())
  const user = ref(getStoredUser())
  const bootstrapped = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value && user.value))
  const roleText = computed(() => {
    const roleMap = {
      1: '学生',
      2: '教师',
      3: '管理员',
    }
    return user.value ? roleMap[user.value.role] || '未知角色' : ''
  })

  async function login(credentials) {
    const result = await loginApi(credentials)
    token.value = result.token
    user.value = result.user
    setStoredToken(result.token)
    setStoredUser(result.user)
    bootstrapped.value = true
    return result.user
  }

  async function fetchCurrentUser() {
    if (!token.value) {
      logout()
      return null
    }
    const currentUser = await currentUserApi()
    user.value = currentUser
    setStoredUser(currentUser)
    bootstrapped.value = true
    return currentUser
  }

  async function ensureUser() {
    if (user.value) {
      bootstrapped.value = true
      return user.value
    }
    return fetchCurrentUser()
  }

  function logout() {
    token.value = ''
    user.value = null
    bootstrapped.value = true
    clearStoredAuth()
  }

  function hasRole(roles = []) {
    if (!roles.length) {
      return true
    }
    return Boolean(user.value && roles.includes(user.value.role))
  }

  return {
    token,
    user,
    bootstrapped,
    isLoggedIn,
    roleText,
    login,
    fetchCurrentUser,
    ensureUser,
    logout,
    hasRole,
  }
})
