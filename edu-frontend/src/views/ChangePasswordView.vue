<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changeMyPassword } from '../api/user'

const router = useRouter()
const saving = ref(false)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

function validateForm() {
  if (!form.value.oldPassword) {
    ElMessage.warning('请输入旧密码')
    return false
  }
  if (!form.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return false
  }
  if (form.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return false
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return false
  }
  if (form.value.oldPassword === form.value.newPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!validateForm()) return
  saving.value = true
  try {
    await changeMyPassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    router.push('/login')
  } catch (err) {
    const msg = err?.response?.data?.message || err?.message || '修改失败'
    ElMessage.error(msg.includes('旧密码') ? '旧密码错误' : msg)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="password-page">
    <el-card class="password-card" shadow="hover">
      <div class="card-header">
        <div class="icon-circle">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#1677ff" stroke-width="1.8">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0110 0v4" />
          </svg>
        </div>
        <h2 class="card-title">修改密码</h2>
        <span class="card-subtitle">请先验证当前密码，再设置新密码</span>
      </div>

      <el-divider />

      <el-form label-width="80px" class="password-form">
        <el-form-item label="旧密码">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            size="large"
          />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button size="large" @click="router.back()">取消</el-button>
        <el-button type="primary" size="large" :loading="saving" @click="handleSubmit">确认修改</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.password-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 60px;
}

.password-card {
  width: 440px;
  border-radius: 12px;
  padding: 12px 0;

  :deep(.el-card__body) {
    padding: 24px 32px 32px;
  }
}

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8f3ff, #d6e8ff);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.card-subtitle {
  font-size: 13px;
  color: #86909c;
}

.password-form {
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}
</style>
