<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { currentUserApi, updateMyProfile } from '../api/user'

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const form = ref({
  nickname: '',
  email: '',
  phone: '',
})

onMounted(async () => {
  loading.value = true
  try {
    const user = await currentUserApi()
    form.value = {
      nickname: user.nickname || '',
      email: user.email || '',
      phone: user.phone || '',
    }
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  if (!form.value.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    await updateMyProfile(form.value)
    await authStore.ensureUser()
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <el-card v-loading="loading" class="profile-card" shadow="hover">
      <div class="card-header">
        <el-avatar :size="72" class="profile-avatar" :src="authStore.user?.avatarUrl || undefined">
          {{ (form.nickname || 'U').slice(0, 1) }}
        </el-avatar>
        <h2 class="card-title">个人中心</h2>
        <span class="card-role">{{ authStore.roleText }}</span>
      </div>

      <el-divider />

      <el-form label-width="80px" class="profile-form">
        <el-form-item label="用户名">
          <el-input :model-value="authStore.user?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="50" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存修改</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 48px;
}

.profile-card {
  width: 480px;
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

.profile-avatar {
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  font-size: 26px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.card-role {
  font-size: 13px;
  color: #86909c;
  background: #f5f7fa;
  padding: 2px 12px;
  border-radius: 10px;
}

.profile-form {
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
