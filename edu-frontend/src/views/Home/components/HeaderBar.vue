<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../../stores/auth'
import LanguageSwitch from './LanguageSwitch.vue'
import UserDropdown from './UserDropdown.vue'

defineEmits(['logout'])

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { path: '/dashboard', label: '工作台', roles: [1, 2, 3] },
  { path: '/courses', label: '我的课程', roles: [1, 2, 3] },
  { path: '/enrollments', label: '我的选课', roles: [1, 2] },
  { path: '/forum', label: '课程广场', roles: [1, 2, 3] },
  { path: '/assignments', label: '作业批改', roles: [1, 2, 3] },
]

const visibleNav = computed(() => navItems.filter((item) => authStore.hasRole(item.roles)))

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<template>
  <header class="header-bar">
    <div class="header-left">
      <div class="logo-area" @click="router.push('/dashboard')">
        <div class="logo-icon">EDU</div>
        <div class="logo-text">
          <strong>在线教育辅助教学系统</strong>
          <span>Online Education System</span>
        </div>
      </div>

      <nav class="nav-area">
        <router-link
          v-for="item in visibleNav"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          {{ item.label }}
        </router-link>
      </nav>
    </div>

    <div class="header-right">
      <LanguageSwitch />
      <el-badge :value="3" :hidden="false" class="notification-badge">
        <el-tooltip content="消息通知" placement="bottom">
          <el-icon :size="20" class="notification-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 01-3.46 0" />
            </svg>
          </el-icon>
        </el-tooltip>
      </el-badge>
      <UserDropdown @logout="$emit('logout')" />
    </div>
  </header>
</template>

<style scoped lang="scss">
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  padding: 0 32px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: #1677ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
}

.logo-text {
  line-height: 1.3;
}

.logo-text strong {
  display: block;
  font-size: 16px;
  color: #1a1a1a;
}

.logo-text span {
  display: block;
  font-size: 11px;
  color: #999;
}

.nav-area {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  padding: 6px 16px;
  font-size: 14px;
  color: #555;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s ease;

  &:hover {
    color: #1677ff;
    background: #f0f5ff;
  }

  &.active {
    color: #1677ff;
    font-weight: 600;
    background: #e6f4ff;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  cursor: pointer;
}

.notification-icon {
  color: #555;
  transition: color 0.2s;

  &:hover {
    color: #1677ff;
  }
}
</style>
