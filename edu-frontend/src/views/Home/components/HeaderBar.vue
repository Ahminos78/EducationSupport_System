<script setup>
import { computed, ref } from 'vue'
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
  { path: '/course-selection', label: '学生选课', roles: [1] },
  { path: '/teacher-course-selection', label: '选课管理', roles: [2] },
]

const visibleNav = computed(() => navItems.filter((item) => authStore.hasRole(item.roles)))
const notificationPopover = ref(false)

const notifications = [
  { id: 1, title: '2026年秋季学期选课已开始', time: '2026-07-08 10:00', read: false },
  { id: 2, title: 'Java Web 开发课程作业即将截止', time: '2026-07-09 14:30', read: false },
  { id: 3, title: '系统维护通知：7月12日 02:00-06:00', time: '2026-07-10 09:00', read: false },
  { id: 4, title: '数据结构课程发布了新作业', time: '2026-07-09 16:00', read: true },
]

const unreadCount = computed(() => notifications.filter((n) => !n.read).length)

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

function markAsRead(id) {
  const item = notifications.find((n) => n.id === id)
  if (item) item.read = true
}
</script>

<template>
  <header class="header-bar">
    <div class="header-inner">
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

        <el-popover
          v-model:visible="notificationPopover"
          placement="bottom-end"
          :width="360"
          trigger="click"
          popper-class="notification-popover"
        >
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
              <el-tooltip content="消息通知" placement="bottom">
                <el-icon :size="20" class="notification-icon">
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
                    <path d="M13.73 21a2 2 0 01-3.46 0" />
                  </svg>
                </el-icon>
              </el-tooltip>
            </el-badge>
          </template>

          <div class="notification-list">
            <div class="notification-header">
              <span class="notification-title">消息通知</span>
              <el-button text size="small" @click="notificationPopover = false">关闭</el-button>
            </div>
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ unread: !item.read }"
              @click="markAsRead(item.id)"
            >
              <div class="notif-dot" :class="{ active: !item.read }" />
              <div class="notif-content">
                <span class="notif-text">{{ item.title }}</span>
                <span class="notif-time">{{ item.time }}</span>
              </div>
            </div>
            <div v-if="notifications.length === 0" class="notification-empty">
              暂无新通知
            </div>
          </div>
        </el-popover>

        <UserDropdown @logout="$emit('logout')" />
      </div>
    </div>
  </header>
</template>

<style scoped lang="scss">
.header-bar {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  user-select: none;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
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
  flex-shrink: 0;
}

.notification-badge {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.notification-icon {
  color: #555;
  transition: color 0.2s;

  &:hover {
    color: #1677ff;
  }
}
</style>

<style lang="scss">
/* Global styles for notification popover (not scoped) */
.notification-popover {
  padding: 0 !important;
  border-radius: 12px !important;
  overflow: hidden;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #f5f7fa;
  }

  &.unread {
    background: #f0f5ff;
  }
}

.notif-dot {
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: transparent;
  margin-top: 6px;

  &.active {
    background: #1677ff;
  }
}

.notif-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.notif-text {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
}

.notif-time {
  font-size: 12px;
  color: #bbb;
}

.notification-empty {
  padding: 40px 16px;
  text-align: center;
  color: #999;
  font-size: 13px;
}
</style>
