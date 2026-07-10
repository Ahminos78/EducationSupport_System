<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../../stores/auth'

const authStore = useAuthStore()

const initial = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || ''
  return name.slice(0, 1).toUpperCase() || 'U'
})
</script>

<template>
  <section class="dashboard-banner">
    <div class="banner-bg" />
    <div class="banner-content">
      <div class="banner-left">
        <el-avatar :size="64" class="banner-avatar">{{ initial }}</el-avatar>
        <div class="banner-user-info">
          <div class="banner-name-row">
            <span class="banner-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-tag
              :type="authStore.user?.role === 1 ? '' : authStore.user?.role === 2 ? 'warning' : 'danger'"
              size="small"
              effect="plain"
              class="role-tag"
            >
              {{ authStore.roleText }}
            </el-tag>
          </div>
          <div class="banner-detail">
            <span>用户名：{{ authStore.user?.username }}</span>
            <span class="detail-sep">|</span>
            <span>ID：{{ authStore.user?.id }}</span>
            <span class="detail-sep">|</span>
            <span>注册时间：{{ authStore.user?.createdAt?.slice(0, 10) || '--' }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.dashboard-banner {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #e6f4ff 0%, #f0f5ff 100%);
  min-height: 120px;
}

.banner-bg {
  position: absolute;
  inset: 0;
  opacity: 0.3;
  background:
    radial-gradient(circle at 10% 30%, rgba(22, 119, 255, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 90% 70%, rgba(22, 119, 255, 0.06) 0%, transparent 50%);
}

.banner-content {
  position: relative;
  padding: 28px 32px;
  display: flex;
  align-items: center;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.banner-avatar {
  background: #1677ff;
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  flex-shrink: 0;
}

.banner-user-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.banner-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.banner-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.role-tag {
  font-size: 12px;
}

.banner-detail {
  font-size: 13px;
  color: #777;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-sep {
  color: #ddd;
}
</style>
