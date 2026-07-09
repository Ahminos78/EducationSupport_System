import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AssignmentManagementView from '../views/AssignmentManagementView.vue'
import CourseManagementView from '../views/CourseManagementView.vue'
import MainLayout from '../layouts/MainLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import EnrollmentManagementView from '../views/EnrollmentManagementView.vue'
import ForumView from '../views/ForumView.vue'
import LoginView from '../views/LoginView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'
import UserManagementView from '../views/UserManagementView.vue'

export const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      public: true,
    },
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: {
          title: '工作台',
        },
      },
      {
        path: 'users',
        name: 'users',
        component: UserManagementView,
        meta: {
          title: '用户管理',
          roles: [3],
        },
      },
      {
        path: 'courses',
        name: 'courses',
        component: CourseManagementView,
        meta: {
          title: '课程管理',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'enrollments',
        name: 'enrollments',
        component: EnrollmentManagementView,
        meta: {
          title: '选课管理',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'forum',
        name: 'forum',
        component: ForumView,
        meta: {
          title: '论坛讨论',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'assignments',
        name: 'assignments',
        component: AssignmentManagementView,
        meta: {
          title: '作业批改',
          roles: [1, 2, 3],
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    if (authStore.isLoggedIn && to.name === 'login') {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!authStore.token) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  try {
    await authStore.ensureUser()
  } catch {
    authStore.logout()
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (!authStore.hasRole(to.meta.roles)) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
