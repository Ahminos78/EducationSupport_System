<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { listCourses, listCourseClasses } from '../api/course'
import { applyEnrollment, listMyEnrollments, checkConflict, dropEnrollment } from '../api/enrollment'

const authStore = useAuthStore()
const tableRef = ref(null)
const loading = ref(false)
const courses = ref([])
const myEnrollments = ref([])
const enrolledClassIds = ref(new Set())
const enrolledCourseIds = ref(new Set())
const expandedRowId = ref(null)
const classLoadingId = ref(null)
const classDataMap = ref({})
const enrolling = ref(null)
const conflictMap = reactive({})

const activeSubNav = ref('required')
const subNavItems = [
  { key: 'required', label: '本学期应修课程' },
  { key: 'other', label: '其他方案内课程' },
  { key: 'elective', label: '通识选修和个性课程' },
  { key: 'selected', label: '已选课程' },
]

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

const dayNames = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }

async function loadData() {
  loading.value = true
  try {
    const [courseList, enrollments] = await Promise.all([
      listCourses({ page: 1, size: 999 }),
      listMyEnrollments(),
    ])
    courses.value = courseList?.records || courseList || []
    for (const c of courses.value) {
      if (typeof c.classCount === 'number' && c.classCount >= 0) continue
      if (c.classCount == null || c.classCount < 0) c.classCount = 0
    }
    myEnrollments.value = enrollments || []
    enrolledClassIds.value = new Set(
      enrollments?.filter((e) => (e.status === 1 || e.status === 0) && e.classId).map((e) => e.classId) || []
    )
    enrolledCourseIds.value = new Set(
      enrollments?.filter((e) => e.status === 1 || e.status === 0).map((e) => e.courseId) || []
    )
  } catch {
    ElMessage.error('课程数据加载失败')
  } finally {
    loading.value = false
  }
}

async function handleExpand(row, expanded) {
  const courseId = row.id
  const isExpanding = expanded.some(r => r.id === courseId)

  if (expandedRowId.value && expandedRowId.value !== courseId) {
    const table = tableRef.value
    if (table) {
      const prevRow = courses.value.find(c => c.id === expandedRowId.value)
      if (prevRow) table.toggleRowExpansion(prevRow, false)
    }
  }

  if (isExpanding) {
    expandedRowId.value = courseId
    if (!classDataMap.value[courseId]) {
      classLoadingId.value = courseId
      try {
        const data = await listCourseClasses(courseId)
        classDataMap.value = { ...classDataMap.value, [courseId]: data || [] }
        row.classCount = (data || []).length
        await nextTick()
        checkAllConflicts(courseId)
      } catch {
        ElMessage.error('教学班数据加载失败')
      } finally {
        classLoadingId.value = null
      }
    }
  } else {
    expandedRowId.value = null
  }
}

function formatSchedule(schedule) {
  if (!schedule || !schedule.length) return '暂无排课'
  return schedule.map(s => {
    const day = dayNames[s.dayOfWeek] || `周${s.dayOfWeek}`
    return `${day} 第${s.startPeriod}-${s.endPeriod}节`
  }).join('；')
}

function formatLocation(schedule) {
  if (!schedule || !schedule.length) return ''
  return schedule.map(s => s.location).filter(Boolean).join('、')
}

async function checkAllConflicts(courseId) {
  const classes = classDataMap.value[courseId] || []
  for (const cls of classes) {
    if (enrolledClassIds.value.has(cls.id)) continue
    try {
      const res = await checkConflict(cls.id)
      conflictMap[cls.id] = res?.conflicts || []
    } catch {
      conflictMap[cls.id] = []
    }
  }
}

async function handleEnroll(classItem) {
  if (enrolling.value) return
  enrolling.value = classItem.id
  try {
    const conflictRes = await checkConflict(classItem.id)
    if (conflictRes?.hasConflict) {
      ElMessage.warning(`时间冲突：${conflictRes.conflicts.join('、')}`)
      return
    }
    const newEnrollment = await applyEnrollment({ courseId: classItem.courseId, classId: classItem.id })
    ElMessage.success('选课申请已提交，等待审核')
    if (newEnrollment) {
      myEnrollments.value = [...myEnrollments.value, newEnrollment]
    }
    enrolledClassIds.value = new Set([...enrolledClassIds.value, classItem.id])
    enrolledCourseIds.value = new Set([...enrolledCourseIds.value, classItem.courseId])
    classDataMap.value = { ...classDataMap.value }
  } catch (e) {
    ElMessage.error(e.message || '选课失败')
  } finally {
    enrolling.value = null
  }
}

async function handleDrop(classItem) {
  const enrollment = myEnrollments.value.find(
    e => e.classId === classItem.id && (e.status === 0 || e.status === 1)
  )
  if (!enrollment) return
  try {
    await dropEnrollment(enrollment.id)
    ElMessage.success('已退选')
    myEnrollments.value = myEnrollments.value.filter(e => e.id !== enrollment.id)
    enrolledClassIds.value = new Set([...enrolledClassIds.value].filter(id => id !== classItem.id))
    const stillHasCourse = myEnrollments.value.some(
      e => e.courseId === classItem.courseId && e.classId !== classItem.id && (e.status === 0 || e.status === 1)
    )
    if (!stillHasCourse) {
      enrolledCourseIds.value = new Set([...enrolledCourseIds.value].filter(id => id !== classItem.courseId))
    }
    classDataMap.value = { ...classDataMap.value }
  } catch (e) {
    ElMessage.error(e.message || '退选失败')
  }
}

function isClassEnrolled(classId) {
  return enrolledClassIds.value.has(classId)
}

function isCourseEnrolled(courseId) {
  return enrolledCourseIds.value.has(courseId)
}

function getEnrollmentStatus(classId) {
  const enrollment = myEnrollments.value.find(e => e.classId === classId)
  return enrollment?.status
}

const filteredCourses = computed(() => {
  let list = [...courses.value]
  const q = keyword.value.trim().toLowerCase()
  if (q) {
    list = list.filter((c) => c.name.toLowerCase().includes(q) || (c.code && c.code.toLowerCase().includes(q)))
  }
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
  if (natureFilter.value) {
    list = list.filter((c) => c.category === natureFilter.value)
  }
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

    <div class="filters-bar">
      <el-select v-model="natureFilter" placeholder="课程性质" clearable style="width: 140px">
        <el-option v-for="opt in natureOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="categoryFilter" placeholder="课程类别" clearable style="width: 150px">
        <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-input v-model="keyword" placeholder="请输入课程名或课程号" clearable style="width: 260px" />
    </div>

    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="filteredCourses"
        stripe
        style="width: 100%"
        row-class-name="table-row"
        border
        @expand-change="handleExpand"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="class-section-wrapper">
              <div v-if="classLoadingId === row.id" class="class-loading">
                <el-icon class="is-loading"><Loading /></el-icon> 加载教学班中...
              </div>
              <div v-else-if="(classDataMap[row.id] || []).length === 0" class="class-empty">
                暂无教学班数据
              </div>
              <div v-else class="class-cards">
                <div
                  v-for="cls in classDataMap[row.id]"
                  :key="cls.id"
                  class="class-card"
                  :class="{
                    'is-enrolled': isClassEnrolled(cls.id)
                  }"
                >
                  <div class="card-header">
                    <div class="card-title">
                      <span class="class-name">{{ cls.name }}</span>
                      <el-tag
                        v-if="isClassEnrolled(cls.id)"
                        size="small"
                        :type="getEnrollmentStatus(cls.id) === 0 ? 'warning' : 'success'"
                        effect="dark"
                      >
                        {{ getEnrollmentStatus(cls.id) === 0 ? '待审核' : '已选' }}
                      </el-tag>
                      <el-tag
                        v-else-if="isCourseEnrolled(cls.courseId)"
                        size="small"
                        type="info"
                        effect="dark"
                      >
                        该课程已选其他教学班
                      </el-tag>
                      <el-tag
                        v-else-if="(conflictMap[cls.id] || []).length > 0"
                        size="small"
                        type="danger"
                        effect="dark"
                      >
                        时间冲突
                      </el-tag>
                      <el-tag
                        v-else-if="cls.enrolledCount >= cls.maxStudents"
                        size="small"
                        type="info"
                        effect="dark"
                      >
                        已满
                      </el-tag>
                    </div>
                    <div class="card-capacity">
                      <span class="capacity-num" :class="{ 'capacity-full': cls.enrolledCount >= cls.maxStudents }">
                        {{ cls.enrolledCount || 0 }}
                      </span>
                      <span class="capacity-sep">/</span>
                      <span class="capacity-total">{{ cls.maxStudents }}</span>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="card-info-row">
                      <span class="info-label">授课教师</span>
                      <span class="info-value">{{ cls.teacherName || '未分配' }}</span>
                    </div>
                    <div class="card-info-row">
                      <span class="info-label">上课时间</span>
                      <span class="info-value">{{ formatSchedule(cls.schedule) }}</span>
                    </div>
                    <div class="card-info-row" v-if="formatLocation(cls.schedule)">
                      <span class="info-label">上课地点</span>
                      <span class="info-value">{{ formatLocation(cls.schedule) }}</span>
                    </div>
                    <div
                      v-if="(conflictMap[cls.id] || []).length > 0 && !isClassEnrolled(cls.id)"
                      class="conflict-detail"
                    >
                      <span class="conflict-icon">!</span>
                      <span>与已选课程时间冲突：{{ conflictMap[cls.id].join('、') }}</span>
                    </div>
                  </div>
                  <div class="card-footer">
                    <el-button
                      v-if="isClassEnrolled(cls.id)"
                      type="danger"
                      size="small"
                      plain
                      @click="handleDrop(cls)"
                    >
                      退选
                    </el-button>
                    <el-button
                      v-else
                      type="primary"
                      size="small"
                      :disabled="cls.enrolledCount >= cls.maxStudents || (conflictMap[cls.id] || []).length > 0 || isCourseEnrolled(cls.courseId)"
                      :loading="enrolling === cls.id"
                      @click="handleEnroll(cls)"
                    >
                      {{ isCourseEnrolled(cls.courseId) ? '已选其他班' : cls.enrolledCount >= cls.maxStudents ? '已满' : '选课' }}
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="课程号" prop="code" width="100" align="center" />
        <el-table-column label="课程名称" prop="name" min-width="220" />
        <el-table-column label="教学班数" width="100" align="center" prop="classCount" />
        <el-table-column label="课程性质" width="100" align="center" prop="category" />
        <el-table-column label="开课单位" prop="dept" min-width="160" />
        <el-table-column label="课程标签" width="110" align="center" prop="tags" />
        <el-table-column label="学分" width="70" align="center" prop="credit" />
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.course-selection-page { display: flex; flex-direction: column; gap: 0; }

.sub-navbar { background: #1677ff; color: #fff; margin: -24px -24px 0; }
.sub-navbar-inner { display: flex; align-items: center; max-width: 1400px; margin: 0 auto; padding: 0 24px; height: 56px; gap: 32px; }
.sub-navbar-left { display: flex; align-items: center; gap: 10px; flex-shrink: 0; font-size: 15px; font-weight: 600; }
.school-logo-placeholder { width: 28px; height: 28px; border-radius: 4px; background: rgba(255,255,255,.2); display: inline-block; }
.school-name { white-space: nowrap; }
.divider { color: rgba(255,255,255,.5); font-weight: 300; }
.system-name { font-weight: 400; opacity: .85; }
.sub-navbar-menu { display: flex; align-items: center; gap: 4px; flex: 1; }
.sub-nav-item { padding: 8px 18px; font-size: 14px; color: rgba(255,255,255,.8); background: none; border: none; cursor: pointer; border-radius: 6px; white-space: nowrap; transition: all .2s; }
.sub-nav-item:hover { background: rgba(255,255,255,.15); color: #fff; }
.sub-nav-item.active { background: rgba(255,255,255,.2); color: #fff; font-weight: 600; }

.filters-bar { display: flex; align-items: center; gap: 12px; padding: 20px 0 16px; flex-wrap: wrap; }

.table-container { background: #fff; border-radius: 12px; padding: 4px; box-shadow: 0 2px 8px rgba(0,0,0,.04); }

.class-section-wrapper {
  padding: 10px 20px 20px;
}
.class-loading, .class-empty { padding: 24px 0; text-align: center; color: #8a94a3; font-size: 13px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.class-cards { display: flex; flex-wrap: wrap; gap: 14px; }

.class-card {
  flex: 1 1 calc(50% - 7px); min-width: 340px; max-width: calc(50% - 7px);
  background: #fff; border: 1px solid #e8ebf0; border-radius: 10px;
  padding: 16px 18px; transition: all .2s;
  &:hover { border-color: #1677ff; box-shadow: 0 2px 12px rgba(22,119,255,.1); }
  &.is-enrolled { border-color: #52c41a; background: #f6ffed; }
}

.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.card-title { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.class-name { font-size: 15px; font-weight: 600; color: #1f2d3d; }
.card-capacity { font-size: 14px; color: #8a94a3; white-space: nowrap; }
.capacity-num { font-weight: 700; color: #1677ff; &.capacity-full { color: #ff4d4f; } }
.capacity-sep { margin: 0 2px; }
.capacity-total { color: #4a5568; }

.card-body { display: flex; flex-direction: column; gap: 8px; }
.card-info-row { display: flex; gap: 8px; font-size: 13px; line-height: 1.6; }
.info-label { color: #8a94a3; flex-shrink: 0; min-width: 64px; }
.info-value { color: #4a5568; }
.conflict-detail { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #ff4d4f; padding: 8px 10px; background: #fff2f0; border-radius: 6px; margin-top: 4px; }
.conflict-icon { font-size: 14px; }

.card-footer { display: flex; justify-content: flex-end; margin-top: 14px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
</style>

<style lang="scss">
.el-table .table-row:hover td { background-color: #f0f5ff; }
.el-table .el-table__expanded-cell { padding: 10px 20px !important; background-color: #fafbfc !important; }
</style>
