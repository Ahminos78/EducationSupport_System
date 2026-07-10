<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../../stores/auth'

defineEmits(['logout'])

const authStore = useAuthStore()

const initial = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || ''
  return name.slice(0, 1).toUpperCase() || 'U'
})
</script>

<template>
  <el-dropdown trigger="click">
    <button class="user-trigger" type="button">
      <el-avatar :size="34" class="user-avatar">
        {{ initial }}
      </el-avatar>
      <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
      <el-icon class="dropdown-arrow">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </el-icon>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled class="user-info-item">
          <div class="dropdown-user-info">
            <span class="dropdown-role">{{ authStore.roleText }}</span>
            <span class="dropdown-name">{{ authStore.user?.username }}</span>
          </div>
        </el-dropdown-item>
        <el-dropdown-item divided>
          <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></el-icon>
          个人中心
        </el-dropdown-item>
        <el-dropdown-item>
          <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></el-icon>
          修改密码
        </el-dropdown-item>
        <el-dropdown-item divided @click="$emit('logout')">
          <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg></el-icon>
          退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped lang="scss">
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f0f5ff;
  }
}

.user-avatar {
  background: #1677ff;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.dropdown-arrow {
  color: #999;
}

.user-info-item {
  cursor: default !important;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dropdown-role {
  font-size: 12px;
  color: #999;
}

.dropdown-name {
  font-size: 13px;
  color: #333;
}
</style>
