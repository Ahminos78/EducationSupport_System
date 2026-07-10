<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '../../../stores/auth'
import { listCourses } from '../../../api/course'
import { listMyEnrollments } from '../../../api/enrollment'
import CourseCard from './CourseCard.vue'

const authStore = useAuthStore()

const courses = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

async function loadCourses() {
  loading.value = true

  try {
    const role = authStore.user?.role

    if (role === 1) {
      // 学生：从选课获取已选课程
      const enrollments = await listMyEnrollments()
      const approved = (enrollments || []).filter((e) => e.status === 1)
      courses.value = approved.map((e) => ({
        id: e.courseId,
        name: e.courseName,
        status: 1,
        teacherId: null,
        teacherName: '--',
        semester: '2026年秋季',
        progress: 0,
      }))
      total.value = approved.length
    } else if (role === 2) {
      // 教师：查询自己的课程
      const params = { page: currentPage.value, size: pageSize.value }
      if (statusFilter.value === 'active') params.status = 1
      else if (statusFilter.value === 'ended') params.status = 0
      const res = await listCourses(params)
      const list = res?.records || res || []
      courses.value = list.map(normalizeCourse)
      total.value = res?.total || res?.length || list.length
    } else {
      // 管理员：查询全部课程
      const params = { page: currentPage.value, size: pageSize.value }
      if (statusFilter.value === 'active') params.status = 1
      else if (statusFilter.value === 'ended') params.status = 0
      const res = await listCourses(params)
      const list = res?.records || res || []
      courses.value = list.map(normalizeCourse)
      total.value = res?.total || list.length
    }
  } catch {
    courses.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function normalizeCourse(raw) {
  return {
    id: raw.id,
    name: raw.name || raw.courseName || '未命名课程',
    status: raw.status,
    teacherId: raw.teacherId,
    teacherName: raw.teacherName || '--',
    semester: raw.semester || '2026年秋季',
    progress: raw.progress || 0,
  }
}

const filteredCourses = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return courses.value
  return courses.value.filter((c) => c.name.toLowerCase().includes(q))
})

const displayList = computed(() => {
  // 前端先过滤搜索，再分页
  const filtered = filteredCourses.value
  const start = (currentPage.value - 1) * pageSize.value
  return filtered.slice(start, start + pageSize.value)
})

watch([searchQuery, statusFilter], () => {
  currentPage.value = 1
})

watch(currentPage, () => {
  if (authStore.user?.role !== 1) {
    loadCourses()
  }
})

onMounted(() => {
  loadCourses()
})
</script>

<template>
  <section class="course-grid">
    <div class="course-header">
      <h3 class="course-title">我的课程</h3>
      <div class="course-toolbar">
        <el-select
          v-model="statusFilter"
          placeholder="全部"
          size="default"
          style="width: 120px"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已结束" value="ended" />
        </el-select>
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程"
          prefix-icon="Search"
          clearable
          style="width: 240px"
        />
      </div>
    </div>

    <div v-if="loading" class="course-loading">
      <div class="course-list">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <el-skeleton animated>
            <template #template>
              <div style="border-radius: 12px; overflow: hidden; border: 1px solid #f0f0f0;">
                <el-skeleton-item variant="rect" style="width: 100%; height: 100px;" />
                <div style="padding: 14px 16px 16px;">
                  <el-skeleton-item variant="text" style="width: 70%; margin-bottom: 8px;" />
                  <el-skeleton-item variant="text" style="width: 50%; margin-bottom: 8px;" />
                  <el-skeleton-item variant="text" style="width: 40%;" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>

    <div v-else-if="displayList.length === 0" class="course-empty">
      <el-empty description="暂未找到课程" />
    </div>

    <div v-else class="course-list">
      <CourseCard
        v-for="course in displayList"
        :key="course.id"
        :course="course"
      />
    </div>

    <div v-if="total > pageSize || displayList.length > 0" class="course-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        small
        background
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.course-grid {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.course-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.course-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.skeleton-card {
  width: 100%;
}

.course-loading,
.course-empty {
  padding: 20px 0;
}

.course-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .course-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .course-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .course-list {
    grid-template-columns: 1fr;
  }
}
</style>
