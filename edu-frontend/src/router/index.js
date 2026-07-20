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
import CourseSelectionView from '../views/CourseSelectionView.vue'
import TeacherCourseSelectionView from '../views/TeacherCourseSelectionView.vue'
import CourseDetailView from '../views/CourseDetailView.vue'
import HomeworkDetailView from '../views/HomeworkDetailView.vue'
import ExamDetailView from '../views/ExamDetailView.vue'
import ExamBuilderView from '../views/ExamBuilderView.vue'
import ExamReviewView from '../views/ExamReviewView.vue'
import ProfileView from '../views/ProfileView.vue'
import ChangePasswordView from '../views/ChangePasswordView.vue'
import MyScheduleView from '../views/MyScheduleView.vue'
import AiAssistantView from '../views/AiAssistantView.vue'

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
    path: '/courses/:courseId/exams/create',
    name: 'exam-create',
    component: ExamBuilderView,
    meta: {
      title: '发布考试',
      roles: [2, 3],
    },
  },
  {
    path: '/courses/:courseId/exams/:examId/edit',
    name: 'exam-edit',
    component: ExamBuilderView,
    meta: {
      title: '编辑考试',
      roles: [2, 3],
    },
  },
  {
    path: '/courses/:courseId/exams/:examId/review/:attemptId',
    name: 'exam-review',
    component: ExamReviewView,
    meta: {
      title: '试卷批阅',
      roles: [2, 3],
    },
  },
  {
    path: '/courses/:courseId/exams/:examId',
    name: 'exam-detail',
    component: ExamDetailView,
    meta: {
      title: '考试答题',
      roles: [1],
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
        path: 'courses/:id',
        name: 'course-detail',
        component: CourseDetailView,
        meta: {
          title: '课程详情',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'courses/:courseId/homework/:homeworkId',
        name: 'homework-detail',
        component: HomeworkDetailView,
        meta: {
          title: '作业详情',
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
          title: '课程论坛',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'course-market',
        name: 'course-market',
        component: PlaceholderView,
        meta: {
          title: '课程广场',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'course-selection',
        name: 'course-selection',
        component: CourseSelectionView,
        meta: {
          title: '学生选课',
          roles: [1],
        },
      },
      {
        path: 'teacher-course-selection',
        name: 'teacher-course-selection',
        component: TeacherCourseSelectionView,
        meta: {
          title: '选课管理',
          roles: [2, 3],
        },
      },
      {
        path: 'my-schedule',
        name: 'my-schedule',
        component: MyScheduleView,
        meta: {
          title: '我的课表',
          roles: [1],
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
      {
        path: 'profile',
        name: 'profile',
        component: ProfileView,
        meta: {
          title: '个人中心',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'change-password',
        name: 'change-password',
        component: ChangePasswordView,
        meta: {
          title: '修改密码',
          roles: [1, 2, 3],
        },
      },
      {
        path: 'ai-assistant',
        name: 'ai-assistant',
        component: AiAssistantView,
        meta: { title: 'AI 助手', roles: [1, 2, 3] },
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
