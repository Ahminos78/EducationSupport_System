<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../../stores/auth'
import CourseCard from './CourseCard.vue'

const router = useRouter()
const authStore = useAuthStore()

const courses = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const sortBy = ref('default')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '进行中', value: 'active' },
  { label: '即将开始', value: 'upcoming' },
  { label: '已结束', value: 'ended' },
]

const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '最新学习', value: 'latest' },
  { label: '课程名称', value: 'name' },
]

// Mock 课程数据
const mockCourses = [
  {
    id: 1,
    name: 'Java Web 开发',
    teacherName: '张老师',
    semester: '2026-2027 第一学期',
    status: 'active',
    progress: 65,
    coverUrl: '',
  },
  {
    id: 2,
    name: '数据结构与算法',
    teacherName: '李老师',
    semester: '2026-2027 第一学期',
    status: 'active',
    progress: 42,
    coverUrl: '',
  },
  {
    id: 3,
    name: '操作系统原理',
    teacherName: '王老师',
    semester: '2026-2027 第一学期',
    status: 'active',
    progress: 78,
    coverUrl: '',
  },
  {
    id: 4,
    name: '计算机网络',
    teacherName: '陈老师',
    semester: '2026-2027 第一学期',
    status: 'active',
    progress: 30,
    coverUrl: '',
  },
  {
    id: 5,
    name: '数据库系统概论',
    teacherName: '刘老师',
    semester: '2026-2027 第一学期',
    status: 'upcoming',
    progress: 0,
    coverUrl: '',
  },
  {
    id: 6,
    name: '软件工程',
    teacherName: '赵老师',
    semester: '2025-2026 第二学期',
    status: 'ended',
    progress: 100,
    coverUrl: '',
  },
  {
    id: 7,
    name: '编译原理',
    teacherName: '孙老师',
    semester: '2026-2027 第一学期',
    status: 'upcoming',
    progress: 0,
    coverUrl: '',
  },
  {
    id: 8,
    name: '人工智能导论',
    teacherName: '周老师',
    semester: '2025-2026 第二学期',
    status: 'ended',
    progress: 100,
    coverUrl: '',
  },
]

function loadCourses() {
  loading.value = true
  // 使用 setTimeout 模拟网络延迟
  setTimeout(() => {
    courses.value = mockCourses
    loading.value = false
  }, 300)
}

// 状态过滤
const statusFiltered = computed(() => {
  if (!statusFilter.value) return courses.value
  return courses.value.filter((c) => c.status === statusFilter.value)
})

// 搜索过滤
const searched = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return statusFiltered.value
  return statusFiltered.value.filter((c) => c.name.toLowerCase().includes(q))
})

// 排序
const sorted = computed(() => {
  const list = [...searched.value]
  if (sortBy.value === 'name') {
    list.sort((a, b) => a.name.localeCompare(b.name, 'zh'))
  } else if (sortBy.value === 'latest') {
    list.sort((a, b) => {
      const order = { active: 0, upcoming: 1, ended: 2 }
      return order[a.status] - order[b.status]
    })
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
  router.push('/course-market')
}

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
        <el-select
          v-model="statusFilter"
          placeholder="全部"
          size="default"
          style="width: 120px"
          clearable
        >
          <el-option
            v-for="opt in statusOptions"
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
                <el-skeleton-item variant="text" style="width: 100%; height: 6px; margin-bottom: 12px;" />
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
