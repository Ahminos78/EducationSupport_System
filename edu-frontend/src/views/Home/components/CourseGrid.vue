<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listCourses } from '../../../api/course'
import { listMyEnrollments } from '../../../api/enrollment'
import { useAuthStore } from '../../../stores/auth'
import CourseCard from './CourseCard.vue'

const router = useRouter()
const authStore = useAuthStore()

const courses = ref([])
const searchQuery = ref('')
const sortBy = ref('default')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(100)

const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '课程名称', value: 'name' },
]

async function loadCourses() {
  loading.value = true
  try {
    const courseListPromise = listCourses({ page: 1, size: 100 })
    if (authStore.user?.role === 1) {
      const [courseListResult, enrollments] = await Promise.all([
        courseListPromise,
        listMyEnrollments(),
      ])
      const courseList = courseListResult.records || courseListResult || []
      const approvedCourseIds = new Set(
        (enrollments || [])
          .filter((item) => item.status === 1)
          .map((item) => item.courseId),
      )
      courses.value = courseList.filter((course) => approvedCourseIds.has(course.id))
    } else {
      const result = await courseListPromise
      courses.value = result.records || result || []
    }
  } catch (error) {
    courses.value = []
    ElMessage.error(error.message || '我的课程加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索过滤
const searched = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return courses.value
  return courses.value.filter((course) => course.name.toLowerCase().includes(q))
})

// 排序
const sorted = computed(() => {
  const list = [...searched.value]
  if (sortBy.value === 'name') {
    list.sort((a, b) => a.name.localeCompare(b.name, 'zh'))
  }
  return list
})

// 分页
const displayList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sorted.value.slice(start, start + pageSize.value)
})

const totalFiltered = computed(() => sorted.value.length)

function goEnroll() {
  router.push('/course-selection')
}

watch([searchQuery, sortBy], () => {
  currentPage.value = 1
})

onMounted(() => {
  loadCourses()
})
</script>

<template>
  <section class="course-grid">
    <!-- 顶部标题栏 -->
    <div class="course-header">
      <h3 class="course-title">我的课程</h3>
      <div class="course-toolbar">
        <el-select
          v-model="sortBy"
          placeholder="排序"
          size="default"
          style="width: 120px"
        >
          <el-option
            v-for="opt in sortOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程"
          prefix-icon="Search"
          clearable
          style="width: 220px"
        />
      </div>
    </div>

    <!-- 加载态 -->
    <div v-if="loading" class="course-list">
      <div v-for="i in 4" :key="i" class="skeleton-wrapper">
        <el-skeleton animated>
          <template #template>
            <div class="skeleton-inner">
              <el-skeleton-item variant="image" style="width: 100%; height: 140px;" />
              <div class="skeleton-body">
                <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 50%; margin-bottom: 6px;" />
                <el-skeleton-item variant="text" style="width: 40%; margin-bottom: 10px;" />
                <el-skeleton-item variant="rect" style="width: 100%; height: 32px; border-radius: 8px;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="displayList.length === 0" class="course-empty">
      <el-empty description="暂无课程">
        <el-button type="primary" @click="goEnroll">立即去选课</el-button>
      </el-empty>
    </div>

    <!-- 课程卡片网格 -->
    <div v-else class="course-list">
      <CourseCard
        v-for="course in displayList"
        :key="course.id"
        :course="course"
      />
    </div>

    <!-- 分页 -->
    <div v-if="totalFiltered > pageSize" class="course-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalFiltered"
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
  flex-wrap: wrap;
  gap: 12px;
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
  gap: 10px;
  flex-wrap: wrap;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

/* 骨架 */
.skeleton-wrapper {
  width: 100%;
}

.skeleton-inner {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.skeleton-body {
  padding: 14px 16px 16px;
}

/* 空状态 */
.course-empty {
  padding: 40px 0;
}

/* 分页 */
.course-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式 */
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
