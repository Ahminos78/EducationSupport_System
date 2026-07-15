<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { listCourses } from '../api/course'
import { applyEnrollment, listMyEnrollments } from '../api/enrollment'

const authStore = useAuthStore()
const loading = ref(false)
const courses = ref([])
const myEnrollments = ref([])
const enrolledCourseIds = ref(new Set())

// 子导航
const activeSubNav = ref('required')
const subNavItems = [
  { key: 'required', label: '本学期应修课程' },
  { key: 'other', label: '其他方案内课程' },
  { key: 'elective', label: '通识选修和个性课程' },
  { key: 'selected', label: '已选课程' },
]

// 筛选条件
const natureFilter = ref('')
const categoryFilter = ref('')
const keyword = ref('')

const natureOptions = [
  { label: '全部', value: '' },
  { label: '必修', value: '必修' },
  { label: '选修', value: '选修' },
  { label: '通识', value: '通识' },
]

const categoryOptions = [
  { label: '全部', value: '' },
  { label: '核心课', value: '核心课' },
  { label: '专业课', value: '专业课' },
  { label: '实践课', value: '实践课' },
  { label: '专业选修', value: '专业选修' },
  { label: 'AI课程', value: 'AI课程' },
  { label: '通识课', value: '通识课' },
  { label: '思政课', value: '思政课' },
  { label: '公共课', value: '公共课' },
  { label: '创新实践', value: '创新实践' },
  { label: '创新创业', value: '创新创业' },
  { label: '毕业实践', value: '毕业实践' },
]

async function loadData() {
  loading.value = true
  try {
    const [courseList, enrollments] = await Promise.all([
      listCourses({ page: 1, size: 999 }),
      listMyEnrollments(),
    ])

    courses.value = courseList?.records || courseList || []
    myEnrollments.value = enrollments || []
    enrolledCourseIds.value = new Set(
      enrollments
        ?.filter((e) => e.status === 1 || e.status === 0)
        .map((e) => e.courseId) || []
    )
  } catch {
    ElMessage.error('课程数据加载失败')
  } finally {
    loading.value = false
  }
}

// 申请选课
async function handleEnroll(courseId) {
  try {
    await applyEnrollment({ courseId })
    ElMessage.success('选课申请已提交，等待审核')
    enrolledCourseIds.value = new Set([...enrolledCourseIds.value, courseId])
  } catch (e) {
    ElMessage.error(e.message || '选课失败')
  }
}

// 过滤后的课程列表
const filteredCourses = computed(() => {
  let list = [...courses.value]

  // 关键词搜索
  const q = keyword.value.trim().toLowerCase()
  if (q) {
    list = list.filter((c) => c.name.toLowerCase().includes(q))
  }

  // 子导航过滤
  switch (activeSubNav.value) {
    case 'required':
      list = list.filter((c) => c.tags === '核心课')
      break
    case 'other':
      list = list.filter((c) => c.category === '必修' || c.category === '选修')
      break
    case 'elective':
      list = list.filter((c) => c.category === '通识' || c.category === '个性课程')
      break
    case 'selected':
      list = list.filter((c) => enrolledCourseIds.value.has(c.id))
      break
  }

  // 课程性质筛选
  if (natureFilter.value) {
    list = list.filter((c) => c.category === natureFilter.value)
  }

  // 课程类别筛选
  if (categoryFilter.value) {
    list = list.filter((c) => c.tags === categoryFilter.value)
  }

  return list
})

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="course-selection-page">
    <!-- 子导航栏 -->
    <div class="sub-navbar">
      <div class="sub-navbar-inner">
        <div class="sub-navbar-left">
          <span class="school-logo-placeholder" />
          <span class="school-name">武汉理工大学</span>
          <span class="divider">|</span>
          <span class="system-name">本科选课系统</span>
        </div>
        <div class="sub-navbar-menu">
          <button
            v-for="item in subNavItems"
            :key="item.key"
            class="sub-nav-item"
            :class="{ active: activeSubNav === item.key }"
            @click="activeSubNav = item.key"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filters-bar">
      <el-select v-model="natureFilter" placeholder="课程性质" clearable style="width: 140px">
        <el-option v-for="opt in natureOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="categoryFilter" placeholder="课程类别" clearable style="width: 150px">
        <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="请输入关键词"
        clearable
        style="width: 220px"
      />
    </div>

    <!-- 课程表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filteredCourses"
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
              :disabled="enrolledCourseIds.has(row.id)"
              @click="handleEnroll(row.id)"
            >
              {{ enrolledCourseIds.has(row.id) ? '已选课' : '选课' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.course-selection-page {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 子导航栏 */
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

/* 筛选工具栏 */
.filters-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 0 16px;
  flex-wrap: wrap;
}

/* 表格 */
.table-container {
  background: #fff;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>

<style lang="scss">
/* el-table 斑马纹默认已支持 stripe 属性，这里确认样式 */
.el-table .table-row {
  &:hover td {
    background-color: #f0f5ff;
  }
}
</style>
