<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCourses, listCourseClasses } from '../api/course'
import {
  approveEnrollment,
  listCourseEnrollments,
  rejectEnrollment,
  removeEnrollment,
} from '../api/enrollment'
import { enrollmentStatusLabel } from '../utils/options'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.user?.role === 3)

const activeTab = ref('courses')
const loading = ref(false)
const detailLoading = ref(false)
const courses = ref([])
const selectedCourseId = ref(null)
const classMap = ref({})
const selectedClassId = ref(null)
const enrollmentMap = ref({})
const reviewVisible = ref(false)
const reviewAction = ref('approve')
const reviewingRow = ref(null)
const reviewComment = ref('')

const coursePage = ref(1)
const coursePageSize = ref(10)
const sidebarPage = ref(1)
const sidebarPageSize = ref(10)

const tabs = [
  { key: 'courses', label: '开设课程' },
  { key: 'applications', label: '选课申请' },
  { key: 'students', label: '学生管理' },
]

const selectedCourse = computed(() =>
  courses.value.find((course) => course.id === selectedCourseId.value),
)

const courseClasses = computed(() => classMap.value[selectedCourseId.value] || [])

const selectedClass = computed(() =>
  courseClasses.value.find((cls) => cls.id === selectedClassId.value),
)

const classEnrollments = computed(() => {
  if (!selectedClassId.value) return []
  return enrollmentMap.value[selectedClassId.value] || []
})

const pendingApplications = computed(() =>
  classEnrollments.value.filter((item) => item.status === 0)
)

const approvedStudents = computed(() =>
  classEnrollments.value.filter((item) => item.status === 1)
)

const pagedCourses = computed(() => {
  if (!isAdmin.value) return courses.value
  const start = (coursePage.value - 1) * coursePageSize.value
  return courses.value.slice(start, start + coursePageSize.value)
})

const sidebarCourses = computed(() => {
  if (!isAdmin.value) return courses.value
  const start = (sidebarPage.value - 1) * sidebarPageSize.value
  return courses.value.slice(start, start + sidebarPageSize.value)
})

function coursePendingCount(courseId) {
  let count = 0
  const classes = classMap.value[courseId] || []
  for (const cls of classes) {
    count += (enrollmentMap.value[cls.id] || []).filter((item) => item.status === 0).length
  }
  return count
}

function courseApprovedCount(courseId) {
  let count = 0
  const classes = classMap.value[courseId] || []
  for (const cls of classes) {
    count += (enrollmentMap.value[cls.id] || []).filter((item) => item.status === 1).length
  }
  return count
}

function classPendingCount(classId) {
  return (enrollmentMap.value[classId] || []).filter((item) => item.status === 0).length
}

function classApprovedCount(classId) {
  return (enrollmentMap.value[classId] || []).filter((item) => item.status === 1).length
}

async function loadClasses(courseId, force = false) {
  if (!courseId || (!force && classMap.value[courseId])) return
  const data = await listCourseClasses(courseId)
  classMap.value = { ...classMap.value, [courseId]: data || [] }
  if (data?.length && !selectedClassId.value) {
    selectedClassId.value = data[0].id
  }
}

async function loadEnrollments(classId, force = false) {
  if (!classId || (!force && enrollmentMap.value[classId])) return
  const records = await listCourseEnrollments(selectedCourseId.value, { classId })
  enrollmentMap.value = { ...enrollmentMap.value, [classId]: records || [] }
}

async function loadCourses() {
  loading.value = true
  try {
    const result = await listCourses({ page: 1, size: 999 })
    courses.value = result.records || result || []
    if (courses.value.length) {
      selectedCourseId.value ||= courses.value[0].id
      await Promise.all(courses.value.map((course) => loadClasses(course.id)))
      for (const [courseId, classes] of Object.entries(classMap.value)) {
        for (const cls of classes) {
          await loadEnrollments(cls.id)
        }
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '课程加载失败')
  } finally {
    loading.value = false
  }
}

async function selectCourse(courseId) {
  selectedCourseId.value = courseId
  selectedClassId.value = null
  detailLoading.value = true
  try {
    await loadClasses(courseId)
    const classes = classMap.value[courseId] || []
    if (classes.length) {
      selectedClassId.value = classes[0].id
      await Promise.all(classes.map((cls) => loadEnrollments(cls.id, true)))
    }
  } catch (error) {
    ElMessage.error(error.message || '课程数据加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function selectClass(classId) {
  selectedClassId.value = classId
  detailLoading.value = true
  try {
    await loadEnrollments(classId, true)
  } catch (error) {
    ElMessage.error(error.message || '选课数据加载失败')
  } finally {
    detailLoading.value = false
  }
}

function changeTab(tab) {
  activeTab.value = tab
  if (isAdmin.value) {
    coursePage.value = 1
    sidebarPage.value = 1
  }
}

function openReview(row, action) {
  reviewingRow.value = row
  reviewAction.value = action
  reviewComment.value = action === 'approve' ? '通过' : '不通过'
  reviewVisible.value = true
}

async function submitReview() {
  try {
    const payload = { reviewComment: reviewComment.value }
    if (reviewAction.value === 'approve') {
      await approveEnrollment(reviewingRow.value.id, payload)
      ElMessage.success('已通过申请')
    } else {
      await rejectEnrollment(reviewingRow.value.id, payload)
      ElMessage.success('已拒绝申请')
    }
    reviewVisible.value = false
    await loadEnrollments(selectedClassId.value, true)
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  }
}

async function removeStudent(row) {
  try {
    await ElMessageBox.confirm(`确认将 ${row.studentName || row.studentId} 移出课程吗？`, '移出课程', {
      type: 'warning',
    })
    await removeEnrollment(row.id)
    ElMessage.success('学生已移出课程')
    await loadEnrollments(selectedClassId.value, true)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '操作失败')
  }
}

function formatSchedule(schedule) {
  if (!schedule || !schedule.length) return '暂无排课'
  const dayNames = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
  return schedule.map(s => `${dayNames[s.dayOfWeek] || '周' + s.dayOfWeek} 第${s.startPeriod}-${s.endPeriod}节`).join('；')
}

onMounted(loadCourses)
</script>

<template>
  <div class="teacher-enrollment-page">
    <div class="sub-navbar">
      <div class="sub-navbar-inner">
        <div class="sub-navbar-left">
          <span class="school-logo-placeholder" />
          <span class="school-name">武汉理工大学</span>
          <span class="divider">|</span>
          <span class="system-name">{{ isAdmin ? '管理员选课系统' : '教师选课系统' }}</span>
        </div>
        <div class="sub-navbar-menu">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="sub-nav-item"
            :class="{ active: activeTab === tab.key }"
            @click="changeTab(tab.key)"
          >{{ tab.label }}</button>
        </div>
      </div>
    </div>

    <section v-if="activeTab === 'courses'" class="course-table-card">
      <div class="table-body">
        <el-table v-loading="loading" :data="pagedCourses" stripe border>
          <el-table-column label="课程号" prop="code" width="100" align="center" />
          <el-table-column label="课程名称" prop="name" min-width="200" />
          <el-table-column label="教学班个数" width="100" align="center">
            <template #default="{ row }">
              {{ (classMap[row.id] || []).length }}
            </template>
          </el-table-column>
          <el-table-column label="课程性质" prop="category" width="100" align="center" />
          <el-table-column label="开课单位" prop="dept" min-width="160" />
          <el-table-column label="学分" prop="credit" width="70" align="center" />
          <el-table-column label="已选人数" width="90" align="center">
            <template #default="{ row }">{{ courseApprovedCount(row.id) }}</template>
          </el-table-column>
          <el-table-column label="待审核" width="80" align="center">
            <template #default="{ row }">{{ coursePendingCount(row.id) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <div v-if="isAdmin" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="coursePage"
          v-model:page-size="coursePageSize"
          :page-sizes="[10, 20, 50]"
          :total="courses.length"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </section>

    <section v-else class="workspace" v-loading="loading">
      <aside class="course-sidebar">
        <div class="sidebar-title">我的课程</div>
        <div class="sidebar-list">
          <button
            v-for="course in sidebarCourses"
            :key="course.id"
            class="course-item"
            :class="{ active: selectedCourseId === course.id }"
            @click="selectCourse(course.id)"
          >
            <span>{{ course.name }}</span>
            <el-badge
              :value="activeTab === 'applications' ? coursePendingCount(course.id) : courseApprovedCount(course.id)"
              :hidden="(activeTab === 'applications' ? coursePendingCount(course.id) : courseApprovedCount(course.id)) === 0"
            />
          </button>
        </div>
        <el-empty v-if="!courses.length" description="暂无课程" :image-size="70" />
        <div v-if="isAdmin" class="sidebar-pagination">
          <el-pagination
            v-model:current-page="sidebarPage"
            :page-size="sidebarPageSize"
            :total="courses.length"
            layout="prev, pager, next"
            small
          />
        </div>
      </aside>

      <main class="content-panel" v-loading="detailLoading">
        <template v-if="selectedCourse">
          <header class="course-summary">
            <div>
              <p class="summary-label">{{ activeTab === 'applications' ? '选课申请' : '学生管理' }}</p>
              <h2>{{ selectedCourse.name }}</h2>
            </div>
            <div class="summary-stats">
              <span>课程编号<strong>{{ selectedCourse.code || selectedCourse.id }}</strong></span>
              <span>学期<strong>2025-2026 第二学期</strong></span>
            </div>
          </header>

          <div class="class-tabs" v-if="courseClasses.length > 0">
            <button
              v-for="cls in courseClasses"
              :key="cls.id"
              class="class-tab"
              :class="{ active: selectedClassId === cls.id }"
              @click="selectClass(cls.id)"
            >
              <span class="class-tab-name">{{ cls.name }}</span>
              <span class="class-tab-teacher">{{ cls.teacherName }}</span>
              <span class="class-tab-count">
                <el-tag size="small" type="success" v-if="classApprovedCount(cls.id)">{{ classApprovedCount(cls.id) }}人</el-tag>
                <el-tag size="small" type="warning" v-if="classPendingCount(cls.id)">{{ classPendingCount(cls.id) }}待审</el-tag>
              </span>
            </button>
          </div>
          <div v-else class="no-classes-hint">暂无教学班</div>

          <template v-if="selectedClass">
            <div class="class-info-bar">
              <span><strong>{{ selectedClass.name }}</strong></span>
              <span>教师：{{ selectedClass.teacherName }}</span>
              <span>容量：{{ selectedClass.enrolledCount || 0 }}/{{ selectedClass.maxStudents }}</span>
              <span v-if="selectedClass.schedule?.length">{{ formatSchedule(selectedClass.schedule) }}</span>
            </div>

            <el-table v-if="activeTab === 'applications'" :data="pendingApplications" stripe>
              <el-table-column label="学号" prop="studentId" width="130" />
              <el-table-column label="姓名" prop="studentName" min-width="150" />
              <el-table-column label="申请时间" prop="appliedAt" min-width="190" />
              <el-table-column label="申请状态" width="100">
                <template #default="{ row }"><el-tag effect="plain">{{ enrollmentStatusLabel(row.status) }}</el-tag></template>
              </el-table-column>
              <el-table-column label="申请说明" prop="applyReason" min-width="180" />
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openReview(row, 'approve')">通过</el-button>
                  <el-button link type="danger" @click="openReview(row, 'reject')">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-table v-else :data="approvedStudents" stripe>
              <el-table-column label="学号" prop="studentId" width="130" />
              <el-table-column label="姓名" prop="studentName" min-width="150" />
              <el-table-column label="选课时间" prop="reviewedAt" min-width="190" />
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
                  <el-button link type="danger" @click="removeStudent(row)">移出</el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
          <el-empty v-else description="请选择教学班" />
        </template>
        <el-empty v-else description="请选择课程" />
      </main>
    </section>

    <el-dialog v-model="reviewVisible" :title="reviewAction === 'approve' ? '通过申请' : '拒绝申请'" width="460">
      <el-input v-model="reviewComment" type="textarea" :rows="3" placeholder="请输入审核意见" />
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.teacher-enrollment-page { display: flex; flex-direction: column; gap: 20px; }
.sub-navbar { margin: -24px -24px 0; background: #1677ff; color: #fff; }
.sub-navbar-inner { display: flex; align-items: center; max-width: 1400px; height: 56px; margin: 0 auto; padding: 0 24px; gap: 32px; }
.sub-navbar-left, .sub-navbar-menu { display: flex; align-items: center; gap: 10px; }
.sub-navbar-menu { flex: 1; gap: 4px; }
.school-logo-placeholder { width: 28px; height: 28px; border-radius: 4px; background: rgba(255,255,255,.2); }
.school-name { font-weight: 600; white-space: nowrap; }
.divider { color: rgba(255,255,255,.5); }
.system-name { opacity: .85; }
.sub-nav-item { padding: 8px 18px; border: 0; border-radius: 6px; background: transparent; color: rgba(255,255,255,.82); cursor: pointer; font-size: 14px; }
.sub-nav-item:hover, .sub-nav-item.active { background: rgba(255,255,255,.18); color: #fff; }
.sub-nav-item.active { font-weight: 600; }
.course-table-card { background: #fff; border: 1px solid #e8ebf0; border-radius: 12px; }
.table-body { overflow: auto; }
.workspace { display: grid; grid-template-columns: 220px minmax(0, 1fr); min-height: 540px; background: #fff; border: 1px solid #e8ebf0; border-radius: 12px; overflow: hidden; }
.course-sidebar { border-right: 1px solid #e8ebf0; padding: 18px 12px; display: flex; flex-direction: column; }
.sidebar-title { padding: 0 10px 12px; color: #8a94a3; font-size: 13px; font-weight: 600; }
.sidebar-list { flex: 1; overflow-y: auto; }
.course-item { display: flex; align-items: center; justify-content: space-between; width: 100%; padding: 11px 10px; border: 0; border-radius: 7px; background: transparent; color: #4a5568; text-align: left; cursor: pointer; gap: 8px; }
.course-item:hover { background: #f6f8fb; }
.course-item.active { background: #edf5ff; color: #1677ff; font-weight: 600; }
.course-item span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sidebar-pagination { padding: 10px 0 0; display: flex; justify-content: center; border-top: 1px solid #e8ebf0; margin-top: 8px; }
.content-panel { min-width: 0; padding: 24px; }
.course-summary { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; margin-bottom: 20px; }
.summary-label { margin: 0 0 6px; color: #8a94a3; font-size: 13px; }
.course-summary h2 { margin: 0; color: #1f2d3d; font-size: 22px; }
.summary-stats { display: flex; flex-wrap: wrap; gap: 12px; justify-content: flex-end; }
.summary-stats span { min-width: 90px; color: #8a94a3; font-size: 12px; }
.summary-stats strong { display: block; margin-top: 4px; color: #2d3748; font-size: 14px; }
.pagination-wrapper { display: flex; justify-content: flex-end; padding: 16px; border-top: 1px solid #e8ebf0; }

.class-tabs {
  display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; padding-bottom: 14px; border-bottom: 1px solid #e8ebf0;
}
.class-tab {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 16px; border: 1px solid #e8ebf0; border-radius: 8px;
  background: #fff; cursor: pointer; transition: all .2s; font-size: 13px;
  &:hover { border-color: #1677ff; background: #f0f5ff; }
  &.active { border-color: #1677ff; background: #edf5ff; box-shadow: 0 0 0 1px #1677ff; }
}
.class-tab-name { font-weight: 600; color: #1f2d3d; }
.class-tab-teacher { color: #8a94a3; }
.class-tab-count { display: flex; gap: 4px; }
.no-classes-hint { padding: 20px 0; text-align: center; color: #8a94a3; font-size: 13px; }

.class-info-bar {
  display: flex; align-items: center; gap: 20px; padding: 12px 16px;
  background: #f6f8fb; border-radius: 8px; margin-bottom: 16px; font-size: 13px; color: #4a5568;
  span { white-space: nowrap; }
}
@media (max-width: 900px) { .workspace { grid-template-columns: 1fr; } .course-sidebar { border-right: 0; border-bottom: 1px solid #e8ebf0; } .course-summary { flex-direction: column; } }
</style>
