<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  approveEnrollment,
  dropEnrollment,
  listCourseEnrollments,
  listMyEnrollments,
  rejectEnrollment,
} from '../api/enrollment'
import { listCourses } from '../api/course'
import { ENROLLMENT_STATUS_OPTIONS, enrollmentStatusLabel } from '../utils/options'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const coursesLoading = ref(false)
const enrollments = ref([])
const courses = ref([])
const selectedCourseId = ref(null)
const statusFilter = ref()
const reviewDialogVisible = ref(false)
const reviewAction = ref('approve')
const reviewingRow = ref(null)
const reviewForm = reactive({
  reviewComment: '',
})

const isStudent = computed(() => authStore.user?.role === 1)
const canReview = computed(() => [2, 3].includes(authStore.user?.role))

onMounted(async () => {
  if (isStudent.value) {
    await loadMyEnrollments()
  } else {
    await loadCourses()
  }
})

async function loadCourses() {
  coursesLoading.value = true
  try {
    courses.value = await listCourses({ page: 1, size: 100 })
    if (courses.value.length && !selectedCourseId.value) {
      selectedCourseId.value = courses.value[0].id
      await loadCourseEnrollments()
    }
  } catch (error) {
    ElMessage.error(error.message || '课程加载失败')
  } finally {
    coursesLoading.value = false
  }
}

async function loadMyEnrollments() {
  loading.value = true
  try {
    enrollments.value = await listMyEnrollments()
  } catch (error) {
    ElMessage.error(error.message || '我的选课加载失败')
  } finally {
    loading.value = false
  }
}

async function loadCourseEnrollments() {
  if (!selectedCourseId.value) {
    enrollments.value = []
    return
  }
  loading.value = true
  try {
    enrollments.value = await listCourseEnrollments(selectedCourseId.value, {
      status: statusFilter.value,
    })
  } catch (error) {
    ElMessage.error(error.message || '选课记录加载失败')
  } finally {
    loading.value = false
  }
}

async function drop(row) {
  try {
    await dropEnrollment(row.id)
    ElMessage.success('已退选')
    await loadMyEnrollments()
  } catch (error) {
    ElMessage.error(error.message || '退选失败')
  }
}

function openReview(row, action) {
  reviewingRow.value = row
  reviewAction.value = action
  reviewForm.reviewComment = action === 'approve' ? '通过' : '不通过'
  reviewDialogVisible.value = true
}

async function submitReview() {
  try {
    if (reviewAction.value === 'approve') {
      await approveEnrollment(reviewingRow.value.id, reviewForm)
      ElMessage.success('已通过申请')
    } else {
      await rejectEnrollment(reviewingRow.value.id, reviewForm)
      ElMessage.success('已拒绝申请')
    }
    reviewDialogVisible.value = false
    await loadCourseEnrollments()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  }
}
</script>

<template>
  <section class="page-stack">
    <section class="surface page-toolbar">
      <div>
        <p class="eyebrow">选课流程</p>
        <h2>{{ isStudent ? '我的选课' : '选课审核' }}</h2>
      </div>
      <div class="toolbar-actions">
        <template v-if="canReview">
          <el-select
            v-model="selectedCourseId"
            :loading="coursesLoading"
            placeholder="选择课程"
            style="width: 220px"
            @change="loadCourseEnrollments"
          >
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
          <el-select
            v-model="statusFilter"
            clearable
            placeholder="全部状态"
            style="width: 150px"
            @change="loadCourseEnrollments"
          >
            <el-option
              v-for="item in ENROLLMENT_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </template>
        <el-button @click="isStudent ? loadMyEnrollments() : loadCourseEnrollments()">刷新</el-button>
      </div>
    </section>

    <section class="surface table-surface">
      <el-table v-loading="loading" :data="enrollments" stripe>
        <el-table-column label="ID" prop="id" width="90" />
        <el-table-column label="课程" min-width="180">
          <template #default="{ row }">
            <strong>{{ row.courseName || `课程 ${row.courseId}` }}</strong>
            <p class="table-subtitle">课程ID：{{ row.courseId }}</p>
          </template>
        </el-table-column>
        <el-table-column v-if="canReview" label="学生ID" prop="studentId" width="110" />
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
            <template v-if="canReview && row.status === 0">
              <el-button link type="primary" @click="openReview(row, 'approve')">通过</el-button>
              <el-button link type="danger" @click="openReview(row, 'reject')">拒绝</el-button>
            </template>
            <el-button v-else-if="isStudent && [0, 1].includes(row.status)" link type="danger" @click="drop(row)">
              退选
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="reviewDialogVisible" :title="reviewAction === 'approve' ? '通过选课' : '拒绝选课'" width="420px">
      <el-form label-width="82px" :model="reviewForm">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.reviewComment" :rows="4" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button :type="reviewAction === 'approve' ? 'primary' : 'danger'" @click="submitReview">
          确认
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>
