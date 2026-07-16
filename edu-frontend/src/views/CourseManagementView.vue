<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { listMyEnrollments } from '../api/enrollment'
import { createCourse, deleteCourse, listCourses, updateCourse, updateCourseStatus } from '../api/course'
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
})

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
        listCourses(query),
        listMyEnrollments(),
      ])
      approvedCourseIds.value = new Set(
        (enrollments || []).filter((item) => item.status === 1).map((item) => item.courseId),
      )
      const records = courseList.records || courseList || []
      courses.value = records.filter((course) => approvedCourseIds.value.has(course.id))
      total.value = courseList.total || records.length
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
      await createCourse({
        name: form.name,
        description: form.description,
        coverUrl: form.coverUrl,
        maxStudents: form.maxStudents,
        status: form.status,
      })
      ElMessage.success('课程已创建')
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
        <el-table v-loading="loading" :data="courses" stripe>
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
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <template v-if="canManage">
                <el-button link type="primary" @click="openCourseDetail(row)">课程详情</el-button>
                <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
                <el-button link @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
                <el-button link type="danger" @click="removeCourse(row)">删除</el-button>
              </template>
              <el-button v-else-if="isStudent" link type="primary" @click="openCourseDetail(row)">课程详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-wrapper">
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
          <el-input v-model="form.name" />
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
</style>
