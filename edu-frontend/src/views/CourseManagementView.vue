<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { listMyEnrollments } from '../api/enrollment'
import { createCourse, deleteCourse, deleteCourseClass, listCourses, listMyTaughtCourses, listCourseClasses, searchCourseByName, updateCourse, updateCourseStatus } from '../api/course'
import { COURSE_STATUS_OPTIONS, courseStatusLabel } from '../utils/options'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingCourse = ref(null)
const courses = ref([])
const total = ref(0)
const approvedCourseIds = ref(new Set())
const tableRef = ref()
const classSectionsMap = ref({})
const expandedCourseId = ref(null)
const query = reactive({
  page: 1,
  size: 20,
})

const canManage = computed(() => [2, 3].includes(authStore.user?.role))
const isStudent = computed(() => authStore.user?.role === 1)

const formRef = ref()
const form = reactive({
  name: '',
  description: '',
  coverUrl: '',
  maxStudents: 100,
  status: 1,
  scheduleSlots: [],
})

const dayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
]

function addScheduleSlot() {
  form.scheduleSlots.push({ dayOfWeek: 1, startPeriod: 1, endPeriod: 2, location: '' })
}

function removeScheduleSlot(index) {
  form.scheduleSlots.splice(index, 1)
}

async function querySearch(queryString, cb) {
  if (!queryString || !queryString.trim()) {
    cb([])
    return
  }
  try {
    const data = await searchCourseByName(queryString.trim())
    cb((data || []).map(item => ({ value: item.name, id: item.id })))
  } catch {
    cb([])
  }
}

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  maxStudents: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
}

onMounted(loadCourses)

async function loadCourses() {
  loading.value = true
  try {
    if (isStudent.value) {
      const [courseList, enrollments] = await Promise.all([
        listCourses({ page: 1, size: 200 }),
        listMyEnrollments(),
      ])
      approvedCourseIds.value = new Set(
        (enrollments || []).filter((item) => item.status === 1).map((item) => item.courseId),
      )
      const records = courseList.records || courseList || []
      courses.value = records.filter((course) => approvedCourseIds.value.has(course.id))
      total.value = courses.value.length
    } else if (canManage.value) {
      courses.value = await listMyTaughtCourses()
      total.value = courses.value.length
    } else {
      const result = await listCourses(query)
      courses.value = result.records || result || []
      total.value = result.total || courses.value.length
    }
  } catch (error) {
    ElMessage.error(error.message || '课程列表加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingCourse.value = null
  Object.assign(form, {
    name: '',
    description: '',
    coverUrl: '',
    maxStudents: 100,
    status: 1,
    scheduleSlots: [],
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingCourse.value = row
  Object.assign(form, {
    name: row.name,
    description: row.description,
    coverUrl: row.coverUrl,
    maxStudents: row.maxStudents,
    status: row.status,
  })
  dialogVisible.value = true
}

async function saveCourse() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingCourse.value) {
      await updateCourse(editingCourse.value.id, {
        name: form.name,
        description: form.description,
        coverUrl: form.coverUrl,
        maxStudents: form.maxStudents,
      })
      ElMessage.success('课程已更新')
    } else {
      const result = await createCourse({
        name: form.name,
        description: form.description,
        coverUrl: form.coverUrl,
        maxStudents: form.maxStudents,
        status: form.status,
        scheduleSlots: form.scheduleSlots.length ? form.scheduleSlots : undefined,
      })
      ElMessage.success(result?.hint || '课程已创建')
    }
    dialogVisible.value = false
    await loadCourses()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await updateCourseStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '课程已上架' : '课程已下架')
    await loadCourses()
  } catch (error) {
    ElMessage.error(error.message || '状态修改失败')
  }
}

async function removeCourse(row) {
  await ElMessageBox.confirm(`确认删除课程「${row.name}」吗？`, '删除课程', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  try {
    await deleteCourse(row.id)
    ElMessage.success('课程已删除')
    await loadCourses()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

function openCourseDetail(row) {
  router.push(`/courses/${row.id}`)
}

function handlePageChange(page) {
  query.page = page
  loadCourses()
}

function handleSizeChange(size) {
  query.size = size
  query.page = 1
  loadCourses()
}

async function loadClassSections(courseId) {
  if (classSectionsMap.value[courseId]) return
  try {
    const data = await listCourseClasses(courseId)
    classSectionsMap.value = { ...classSectionsMap.value, [courseId]: data || [] }
  } catch {
    classSectionsMap.value[courseId] = []
  }
}

async function handleRowExpandChange(row, expanded) {
  const isExpanding = expanded.some(r => r.id === row.id)
  if (isExpanding) {
    if (expandedCourseId.value && expandedCourseId.value !== row.id) {
      const prevRow = courses.value.find(c => c.id === expandedCourseId.value)
      if (prevRow && tableRef.value) {
        tableRef.value.toggleRowExpansion(prevRow, false)
      }
    }
    expandedCourseId.value = row.id
    if (canManage.value) {
      await loadClassSections(row.id)
    }
  } else {
    expandedCourseId.value = null
  }
}

async function removeClassSection(cls) {
  await ElMessageBox.confirm(`确认删除教学班「${cls.name}」吗？`, '删除教学班', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  try {
    await deleteCourseClass(cls.id)
    ElMessage.success('教学班已删除')
    classSectionsMap.value = { ...classSectionsMap.value }
    const courseClasses = classSectionsMap.value[cls.courseId]
    if (courseClasses) {
      classSectionsMap.value[cls.courseId] = courseClasses.filter(c => c.id !== cls.id)
    }
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

</script>

<template>
  <section class="page-stack">
    <section class="surface page-toolbar">
      <div>
        <p class="eyebrow">课程中心</p>
        <h2>{{ canManage ? '课程管理' : '我的课程' }}</h2>
      </div>
      <div class="toolbar-actions">
        <el-button @click="loadCourses">刷新</el-button>
        <el-button v-if="canManage" type="primary" @click="openCreateDialog">新增课程</el-button>
      </div>
    </section>

    <section class="surface table-surface">
      <div class="table-body">
        <el-table ref="tableRef" v-loading="loading" :data="courses" stripe @expand-change="handleRowExpandChange">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div v-if="canManage" class="class-section-list">
                <div v-for="cls in (classSectionsMap[row.id] || []).filter(c => authStore.user?.role === 3 || c.teacherId === authStore.user?.id)" :key="cls.id" class="class-section-item">
                  <span class="cs-name">{{ cls.name }}</span>
                  <span class="cs-teacher">{{ cls.teacherName }}</span>
                  <span class="cs-capacity">{{ cls.enrolledCount || 0 }}/{{ cls.maxStudents }}</span>
                  <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
                  <el-button link size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
                  <el-button link type="danger" size="small" @click="removeCourse(row)">删除课程</el-button>
                </div>
                <div v-if="!(classSectionsMap[row.id] || []).filter(c => authStore.user?.role === 3 || c.teacherId === authStore.user?.id).length" class="cs-empty">暂无教学班</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="课程" min-width="220">
            <template #default="{ row }">
              <strong>{{ row.name }}</strong>
              <p class="table-subtitle">{{ row.description || '暂无简介' }}</p>
            </template>
          </el-table-column>
          <el-table-column label="教师ID" prop="teacherId" width="100" />
          <el-table-column label="人数" width="120">
            <template #default="{ row }">{{ row.enrolledCount }} / {{ row.maxStudents }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
                {{ courseStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createdAt" min-width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openCourseDetail(row)">课程详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-if="authStore.user?.role === 3" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingCourse ? '编辑课程' : '新增课程'" width="560px">
      <el-form ref="formRef" label-width="96px" :model="form" :rules="rules">
        <el-form-item label="课程名称" prop="name">
          <el-autocomplete
            v-model="form.name"
            :fetch-suggestions="querySearch"
            :trigger-on-focus="false"
            placeholder="请输入课程名称（自动匹配已有课程）"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="form.description" :rows="4" type="textarea" />
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="form.coverUrl" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="1" class="full-input" />
        </el-form-item>
        <el-form-item v-if="!editingCourse" label="课程状态">
          <el-select v-model="form.status" class="full-input">
            <el-option
              v-for="item in COURSE_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingCourse" label="排课设置">
          <div class="schedule-editor">
            <div v-for="(slot, idx) in form.scheduleSlots" :key="idx" class="schedule-row">
              <el-select v-model="slot.dayOfWeek" style="width:100px">
                <el-option v-for="d in dayOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
              <span class="schedule-label">第</span>
              <el-input-number v-model="slot.startPeriod" :min="1" :max="12" size="small" style="width:90px" />
              <span class="schedule-label">-</span>
              <el-input-number v-model="slot.endPeriod" :min="1" :max="12" size="small" style="width:90px" />
              <span class="schedule-label">节</span>
              <el-input v-model="slot.location" placeholder="教室" style="width:140px" />
              <el-button type="danger" :icon="Delete" size="small" circle @click="removeScheduleSlot(idx)" />
            </div>
            <el-button type="primary" size="small" @click="addScheduleSlot">+ 添加排课</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCourse">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.table-body {
  overflow: auto;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid #e8ebf0;
}

.schedule-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.schedule-label {
  font-size: 13px;
  color: #4a5568;
  white-space: nowrap;
}

.class-section-list {
  padding: 4px 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.class-section-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 6px 12px;
  border-radius: 6px;
  background: #f8f9fb;
  font-size: 13px;
}

.cs-name { font-weight: 600; color: #1f2d3d; min-width: 200px; }
.cs-teacher { color: #8a94a3; min-width: 100px; }
.cs-capacity { color: #4a5568; min-width: 80px; }
.cs-empty { padding: 12px; text-align: center; color: #8a94a3; font-size: 13px; }
</style>
