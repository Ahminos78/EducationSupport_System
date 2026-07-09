<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChatLineRound,
  Collection,
  DocumentChecked,
  Fold,
  House,
  Reading,
  SwitchButton,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menuItems = [
  {
    path: '/dashboard',
    label: '工作台',
    icon: House,
    roles: [1, 2, 3],
  },
  {
    path: '/users',
    label: '用户管理',
    icon: User,
    roles: [3],
  },
  {
    path: '/courses',
    label: '课程管理',
    icon: Reading,
    roles: [1, 2, 3],
  },
  {
    path: '/enrollments',
    label: '选课管理',
    icon: Collection,
    roles: [1, 2, 3],
  },
  {
    path: '/forum',
    label: '论坛讨论',
    icon: ChatLineRound,
    roles: [1, 2, 3],
  },
  {
    path: '/assignments',
    label: '作业批改',
    icon: DocumentChecked,
    roles: [1, 2, 3],
  },
]

const visibleMenus = computed(() => menuItems.filter((item) => authStore.hasRole(item.roles)))
const pageTitle = computed(() => route.meta.title || '工作台')

async function refreshCurrentUser() {
  try {
    await authStore.fetchCurrentUser()
    ElMessage.success('当前用户信息已更新')
  } catch (error) {
    ElMessage.error(error.message || '验证失败')
  }
}

function logout() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="228px">
      <div class="sidebar-brand">
        <div class="brand-mark">EDU</div>
        <div>
          <strong>辅助教学系统</strong>
          <span>Teaching Console</span>
        </div>
      </div>

      <el-menu class="app-menu" :default-active="route.path" router>
        <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="page-heading">
          <el-icon><Fold /></el-icon>
          <span>{{ pageTitle }}</span>
        </div>

        <div class="header-actions">
          <el-button plain @click="refreshCurrentUser">验证登录态</el-button>
          <el-dropdown trigger="click">
            <button class="user-trigger" type="button">
              <el-avatar :size="34">
                {{ authStore.user?.nickname?.slice(0, 1) || authStore.user?.username?.slice(0, 1) }}
              </el-avatar>
              <span>{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <small>{{ authStore.roleText }}</small>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-icon><UserFilled /></el-icon>
                  {{ authStore.user?.username }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
