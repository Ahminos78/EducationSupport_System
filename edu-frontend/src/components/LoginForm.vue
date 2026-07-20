<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Key, Lock, RefreshRight, User, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { registerUser, forgotPasswordVerify, forgotPasswordReset } from '../api/user'
import { useAuthStore } from '../stores/auth'
import { listQuickLoginAccounts } from '../api/user'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginMode = ref('user')
const pageMode = ref('login')
const formRef = ref()
const loading = ref(false)
const registerLoading = ref(false)
const rememberMe = ref(true)

onMounted(async () => {
  try {
    const data = await listQuickLoginAccounts()
    if (data && data.length) {
      quickAccounts.value = data
    }
  } catch {
    quickAccounts.value = []
  }
})

const quickAccounts = ref([])
const quickAccount = ref('')

const roleLabel = { 1: '学生', 2: '教师', 3: '管理员' }

const quickOptions = computed(() => quickAccounts.value.map(a => ({
  label: `${roleLabel[a.role] || '用户'} ${a.username} · ${a.nickname}`,
  username: a.username,
  password: '123456',
  role: a.role,
})))

function onQuickSelect(val) {
  const acct = quickOptions.value.find(a => a.username === val)
  if (!acct) return
  form.username = acct.username
  form.password = acct.password
  form.captcha = captchaCode.value
  if (acct.role === 3 && loginMode.value !== 'admin') {
    switchMode('admin')
  } else if (acct.role !== 3 && loginMode.value === 'admin') {
    switchMode('user')
  }
}

const form = reactive({
  username: '',
  password: '',
  captcha: '',
})

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  role: 1,
})

const forgotForm = reactive({
  username: '',
  email: '',
  phone: '',
})

const resetForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const forgotToken = ref('')
const forgotStep = ref(1)

const captchaCode = ref(generateCaptcha())

const isAdminMode = computed(() => loginMode.value === 'admin')
const isLogin = computed(() => pageMode.value === 'login')
const isRegister = computed(() => pageMode.value === 'register')
const isForgot = computed(() => pageMode.value === 'forgot')
const loginTitle = computed(() => {
  if (isRegister.value) return '注册账号'
  if (isForgot.value) return '忘记密码'
  return isAdminMode.value ? '管理员登录' : '欢迎登录'
})
const loginSubtitle = computed(() => {
  if (isRegister.value) return 'Create New Account'
  if (isForgot.value) return 'Reset Your Password'
  return isAdminMode.value ? 'Administrator Portal' : 'Welcome Back'
})

const rules = computed(() => ({
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  captcha: isAdminMode.value ? [] : [{ required: true, message: '验证码不能为空', trigger: 'blur' }],
}))

const registerRules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
  nickname: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
}

const forgotVerifyRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
}

const forgotResetRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }],
}

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let result = ''
  for (let index = 0; index < 4; index += 1) {
    result += chars[Math.floor(Math.random() * chars.length)]
  }
  return result
}

function refreshCaptcha() {
  captchaCode.value = generateCaptcha()
  form.captcha = ''
}

function switchMode(mode) {
  loginMode.value = mode
  form.username = ''
  form.password = ''
  form.captcha = ''
  formRef.value?.clearValidate()
  if (mode === 'user') {
    refreshCaptcha()
  }
}

function goPage(page) {
  pageMode.value = page
  forgotStep.value = 1
  forgotToken.value = ''
  Object.assign(forgotForm, { username: '', email: '', phone: '' })
  Object.assign(resetForm, { newPassword: '', confirmPassword: '' })
  formRef.value?.clearValidate()
  if (page === 'login' && !isAdminMode.value) {
    refreshCaptcha()
  }
}

async function submit() {
  if (loading.value) return
  await formRef.value.validate()
  if (!isAdminMode.value && form.captcha.trim().toUpperCase() !== captchaCode.value) {
    ElMessage.error('验证码不正确')
    refreshCaptcha()
    return
  }
  loading.value = true
  try {
    const user = await authStore.login({
      username: form.username.trim(),
      password: form.password,
    })
    if (isAdminMode.value && user.role !== 3) {
      authStore.logout()
      ElMessage.error('请使用管理员账号登录')
      return
    }
    if (!isAdminMode.value && user.role === 3) {
      authStore.logout()
      ElMessage.error('管理员账号请切换至管理员登录')
      refreshCaptcha()
      return
    }
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
    if (!isAdminMode.value) refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  if (registerLoading.value) return
  await formRef.value.validate()
  registerLoading.value = true
  try {
    await registerUser({
      username: registerForm.username.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim(),
      role: registerForm.role,
    })
    ElMessage.success('注册成功，请登录')
    goPage('login')
    form.username = registerForm.username.trim()
    form.password = ''
    refreshCaptcha()
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}

async function submitForgotVerify() {
  if (loading.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await forgotPasswordVerify({
      username: forgotForm.username.trim(),
      email: forgotForm.email.trim() || undefined,
      phone: forgotForm.phone.trim() || undefined,
    })
    forgotToken.value = res.token
    forgotStep.value = 2
    ElMessage.success('验证通过')
  } catch (error) {
    ElMessage.error(error.message || '验证失败')
  } finally {
    loading.value = false
  }
}

async function submitForgotReset() {
  if (loading.value) return
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  loading.value = true
  try {
    await forgotPasswordReset({ token: forgotToken.value, newPassword: resetForm.newPassword })
    ElMessage.success('密码重置成功，请登录')
    goPage('login')
    form.username = forgotForm.username
    form.password = ''
    refreshCaptcha()
  } catch (error) {
    ElMessage.error(error.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="login-card" :class="{ 'admin-card': isAdminMode }">
    <div class="login-heading">
      <span class="login-eyebrow">{{ isAdminMode ? 'Admin Access' : 'Unified Campus Portal' }}</span>
      <h2>{{ loginTitle }}</h2>
      <p>{{ loginSubtitle }}</p>
    </div>

    <!-- 登录面板 -->
    <el-form
      v-if="isLogin"
      ref="formRef"
      class="modern-login-form"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      @keyup.enter="submit"
      @submit.prevent
    >
      <el-form-item v-if="!isAdminMode && quickOptions.length" label="快速登录">
        <el-select
          v-model="quickAccount"
          placeholder="选择测试账号（自动填写）"
          clearable
          style="width:100%"
          @change="onQuickSelect"
        >
          <el-option v-for="item in quickOptions" :key="item.username" :label="item.label" :value="item.username" />
        </el-select>
      </el-form-item>

      <el-form-item label="账号" prop="username">
        <el-input v-model="form.username" autocomplete="username" placeholder="请输入账号" :prefix-icon="User" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" autocomplete="current-password" placeholder="请输入密码" show-password type="password" :prefix-icon="Lock" />
      </el-form-item>

      <template v-if="!isAdminMode">
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" :prefix-icon="Key" />
            <button class="captcha-code" type="button" title="点击刷新验证码" @click="refreshCaptcha">
              <span>{{ captchaCode }}</span>
              <el-icon><RefreshRight /></el-icon>
            </button>
          </div>
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住登录</el-checkbox>
          <el-button link type="primary" @click="goPage('forgot')">忘记密码</el-button>
        </div>
      </template>

      <el-button class="login-submit" :loading="loading" type="primary" @click="submit">登录</el-button>

      <div v-if="!isAdminMode" class="register-line">
        <span>还没有账号？</span>
        <el-button link type="primary" @click="goPage('register')">注册账号</el-button>
      </div>

      <div class="mode-switch">
        <el-button v-if="!isAdminMode" plain type="primary" @click="switchMode('admin')">管理员身份登录</el-button>
        <el-button v-else plain type="primary" @click="switchMode('user')">返回普通用户登录</el-button>
      </div>
    </el-form>

    <!-- 注册面板 -->
    <el-form
      v-if="isRegister"
      ref="formRef"
      class="modern-login-form"
      :model="registerForm"
      :rules="registerRules"
      label-position="top"
      size="large"
      @keyup.enter="submitRegister"
      @submit.prevent
    >
      <el-form-item label="账号" prop="username">
        <el-input v-model="registerForm.username" placeholder="请输入账号" :prefix-icon="User" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="registerForm.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="registerForm.password" placeholder="请输入密码" show-password type="password" :prefix-icon="Lock" />
      </el-form-item>
      <el-form-item label="身份" prop="role">
        <el-radio-group v-model="registerForm.role" class="role-group">
          <el-radio-button :label="1">学生</el-radio-button>
          <el-radio-button :label="2">教师</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-button class="login-submit" :loading="registerLoading" type="primary" @click="submitRegister">注册</el-button>

      <div class="register-line">
        <span>已有账号？</span>
        <el-button link type="primary" @click="goPage('login')">返回登录</el-button>
      </div>
    </el-form>

    <!-- 忘记密码面板 -->
    <div v-if="isForgot" class="modern-login-form">
      <template v-if="forgotStep === 1">
        <el-form
          ref="formRef"
          :model="forgotForm"
          :rules="forgotVerifyRules"
          label-position="top"
          size="large"
          @keyup.enter="submitForgotVerify"
          @submit.prevent
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="forgotForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱（选填）" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="forgotForm.phone" placeholder="请输入注册手机号（选填）" :prefix-icon="Iphone" />
          </el-form-item>
          <div class="forgot-tip">邮箱和手机号至少填写一项</div>

          <el-button class="login-submit" :loading="loading" type="primary" @click="submitForgotVerify">验证身份</el-button>
        </el-form>
      </template>

      <template v-if="forgotStep === 2">
        <el-form
          ref="formRef"
          :model="resetForm"
          :rules="forgotResetRules"
          label-position="top"
          size="large"
          @keyup.enter="submitForgotReset"
          @submit.prevent
        >
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="resetForm.newPassword" placeholder="请输入新密码" show-password type="password" :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="resetForm.confirmPassword" placeholder="请再次输入新密码" show-password type="password" :prefix-icon="Lock" />
          </el-form-item>

          <el-button class="login-submit" :loading="loading" type="primary" @click="submitForgotReset">重置密码</el-button>
        </el-form>
      </template>

      <div class="register-line">
        <el-button link type="primary" @click="goPage('login')">返回登录</el-button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.login-card {
  width: min(100%, 420px);
  padding: 36px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(16px);
  box-shadow:
    0 24px 60px rgba(29, 74, 132, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  animation: card-in 620ms ease both;
}

.admin-card {
  padding-top: 42px;
  padding-bottom: 42px;
}

.login-heading {
  margin-bottom: 30px;
}

.login-eyebrow {
  display: block;
  margin-bottom: 12px;
  color: #1677ff;
  font-size: 13px;
  font-weight: 800;
}

.login-heading h2 {
  color: #162033;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 820;
}

.login-heading p {
  margin-top: 8px;
  color: #77869a;
  font-size: 15px;
}

.modern-login-form {
  display: grid;
  gap: 2px;
}

.modern-login-form :deep(.el-form-item__label) {
  color: #2d3b50;
  font-weight: 700;
}

.modern-login-form :deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dbe6f2 inset;
}

.modern-login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(22, 119, 255, 0.5) inset;
}

.modern-login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px #1677ff inset,
    0 0 0 4px rgba(22, 119, 255, 0.15);
}

.captcha-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 118px;
  gap: 10px;
}

.captcha-code {
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #dbe6f2;
  border-radius: 8px;
  background: linear-gradient(135deg, #f1f7ff, #ffffff);
  color: #1677ff;
  font: inherit;
  font-weight: 820;
  letter-spacing: 2px;
  cursor: pointer;
  transition:
    border-color 180ms ease,
    box-shadow 180ms ease,
    transform 180ms ease;
}

.captcha-code:hover {
  border-color: rgba(22, 119, 255, 0.58);
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.12);
  transform: translateY(-1px);
}

.captcha-code .el-icon {
  font-size: 15px;
  letter-spacing: 0;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 2px 0 16px;
}

.forgot-tip {
  font-size: 12px;
  color: #8a94a3;
  text-align: center;
  margin: -4px 0 12px;
}

.login-submit {
  width: 100%;
  height: 46px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  border: none;
  font-weight: 780;
  transition:
    box-shadow 180ms ease,
    transform 180ms ease,
    background 180ms ease;
}

.login-submit:hover {
  background: linear-gradient(135deg, #176bf0, #3b8aff);
  box-shadow: 0 12px 26px rgba(22, 119, 255, 0.32);
  transform: translateY(-1px);
}

.register-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #7a8798;
  font-size: 14px;
  margin-top: 18px;
}

.mode-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 14px;
}

.mode-switch .el-button {
  width: 100%;
  height: 42px;
  border-radius: 8px;
}

.role-group {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.role-group :deep(.el-radio-button__inner) {
  width: 100%;
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 520px) {
  .login-card {
    padding: 26px 18px;
    border-radius: 12px;
  }
  .captcha-row {
    grid-template-columns: 1fr;
  }
  .form-options {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
