<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { listCourses } from '../api/course'
import {
  approveEnrollment,
  rejectEnrollment,
  listCourseEnrollments,
} from '../api/enrollment'
import { ENROLLMENT_STATUS_OPTIONS, enrollmentStatusLabel } from '../utils/options'

const authStore = useAuthStore()
const loading = ref(false)
const enrollLoading = ref(false)
const courses = ref([])
const selectedCourseId = ref(null)
const enrollments = ref([])
const reviewDialogVisible = ref(false)
const reviewAction = ref('approve')
const reviewingRow = ref(null)
const reviewForm = ref({})

const activeSubNav = ref('my-courses')

const currentTeacherId = computed(() => authStore.user?.id)

const myCourses = computed(() =>
  courses.value.filter((c) => c.teacherId === currentTeacherId.value)
)

async function loadCourses() {
  loading.value = true
  try {
    courses.value = await listCourses({ page: 1, size: 999 })
    // 默认选中第一门本老师的课程
    if (myCourses.value.length && !selectedCourseId.value) {
      selectedCourseId.value = myCourses.value[0].id
      await loadEnrollments()
    }
  } catch {
    ElMessage.error('课程加载失败')
  } finally {
    loading.value = false
  }
}

async function loadEnrollments() {
  if (!selectedCourseId.value) {
    enrollments.value = []
    return
  }
  enrollLoading.value = true
  try {
    enrollments.value = await listCourseEnrollments(selectedCourseId.value)
  } catch {
    ElMessage.error('选课记录加载失败')
  } finally {
    enrollLoading.value = false
  }
}

function openReview(row, action) {
  reviewingRow.value = row
  reviewAction.value = action
  reviewForm.value = { reviewComment: action === 'approve' ? '通过' : '不通过' }
  reviewDialogVisible.value = true
}

async function submitReview() {
  try {
    if (reviewAction.value === 'approve') {
      await approveEnrollment(reviewingRow.value.id, { reviewComment: reviewForm.value.reviewComment || '通过' })
      ElMessage.success('已通过申请')
    } else {
      await rejectEnrollment(reviewingRow.value.id, { reviewComment: reviewForm.value.reviewComment || '不通过' })
      ElMessage.success('已拒绝申请')
    }
    reviewDialogVisible.value = false
    await loadEnrollments()
  } catch (e) {
    ElMessage.error(e.message || '审核失败')
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<template>
  <div class="course-selection-page">
    <!-- 顶部导航栏（学生端同款风格） -->
    <div class="sub-navbar">
      <div class="sub-navbar-inner">
        <div class="sub-navbar-left">
          <span class="school-logo-placeholder" />
          <span class="school-name">武汉理工大学</span>
          <span class="divider">|</span>
          <span class="system-name">教师选课系统</span>
        </div>
        <div class="sub-navbar-menu">
          <button
            class="sub-nav-item active"
          >开设课程</button>
        </div>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="table-container" style="margin-top: 20px;">
      <el-table
        v-loading="loading"
        :data="myCourses"
        stripe
        style="width: 100%"
        row-class-name="table-row"
        border
      >
        <el-table-column label="课程号" prop="code" width="100" align="center" />
        <el-table-column label="课程名称" prop="name" min-width="200" />
        <el-table-column label="教学班个数" width="110" align="center" prop="classCount" />
        <el-table-column label="课程性质" width="120" align="center" prop="category" />
        <el-table-column label="开课单位" prop="dept" min-width="160" />
        <el-table-column label="课程标签" width="140" align="center" prop="tags" />
        <el-table-column label="学分" width="80" align="center" prop="credit" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :type="selectedCourseId === row.id ? 'primary' : 'default'"
              @click="selectedCourseId = row.id; loadEnrollments()"
            >查看申请</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 选中课程的选课申请 -->
    <div v-if="selectedCourseId" class="table-container" style="margin-top: 20px;">
      <div style="padding: 16px 16px 0; font-size: 16px; font-weight: 600; color: #1a1a1a;">
        选课申请 — {{ myCourses.find(c => c.id === selectedCourseId)?.name || '' }}
      </div>
      <el-table
        v-loading="enrollLoading"
        :data="enrollments"
        stripe
        style="width: 100%"
        row-class-name="table-row"
        border
      >
        <el-table-column label="学生ID" prop="studentId" width="100" align="center" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag effect="plain">{{ enrollmentStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请说明" prop="applyReason" min-width="180" />
        <el-table-column label="审核意见" prop="reviewComment" min-width="160" />
        <el-table-column label="申请时间" prop="appliedAt" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="primary" @click="openReview(row, 'approve')">通过</el-button>
              <el-button link type="danger" @click="openReview(row, 'reject')">拒绝</el-button>
            </template>
            <el-tag v-else size="small" effect="plain" type="info">已处理</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewAction === 'approve' ? '通过申请' : '拒绝申请'" width="460">
      <el-form :model="reviewForm">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.reviewComment" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.course-selection-page {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 子导航栏（学生端同款） */
.sub-navbar {
  background: #1677ff;
  color: #fff;
  margin: -24px -24px 0;
  padding: 0;
}

.sub-navbar-inner {
  display: flex;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  gap: 32px;
}

.sub-navbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  font-size: 15px;
  font-weight: 600;
}

.school-logo-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.2);
  display: inline-block;
}

.school-name {
  white-space: nowrap;
}

.divider {
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
}

.system-name {
  font-weight: 400;
  opacity: 0.85;
}

.sub-navbar-menu {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.sub-nav-item {
  padding: 8px 18px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
    color: #fff;
  }

  &.active {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    font-weight: 600;
  }
}

/* 表格容器 */
.table-container {
  background: #fff;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>

<style lang="scss">
.el-table .table-row {
  &:hover td {
    background-color: #f0f5ff;
  }
}
</style>
