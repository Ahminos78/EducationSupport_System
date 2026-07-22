<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAssignment,
  deleteAssignment,
  generateAiComment,
  gradeSubmission,
  listAssignments,
  listAssignmentSubmissions,
  listMySubmissions,
  submitAssignment,
  updateAssignment,
  updateAssignmentStatus,
} from '../api/assessment'
import { listCourses } from '../api/course'
import { ASSIGNMENT_STATUS_OPTIONS, assignmentStatusLabel } from '../utils/options'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const courses = ref([])
const assignments = ref([])
const mySubmissions = ref([])
const submissions = ref([])
const selectedCourseId = ref(null)
const selectedAssignment = ref(null)
const loadingCourses = ref(false)
const loadingAssignments = ref(false)
const loadingSubmissions = ref(false)
const assignmentDialogVisible = ref(false)
const submitDialogVisible = ref(false)
const submissionsDrawerVisible = ref(false)
const gradeDialogVisible = ref(false)
const editingAssignment = ref(null)
const gradingSubmission = ref(null)

const assignmentFormRef = ref()
const assignmentForm = reactive({
  title: '',
  description: '',
  fullScore: 100,
  deadline: '',
  status: 1,
})

const submissionForm = reactive({
  content: '',
  attachmentUrl: '',
})

const gradeForm = reactive({
  score: 0,
  teacherComment: '',
  aiComment: '',
})
const aiLoading = ref(false)

async function generateAiCommentFunc() {
  if (!gradingSubmission.value) return
  aiLoading.value = true
  try {
    const comment = await generateAiComment(gradingSubmission.value.id)
    gradeForm.aiComment = comment || ''
    ElMessage.success('AI 评语已生成')
  } catch (e) {
    ElMessage.error(e.message || '生成失败')
  } finally {
    aiLoading.value = false
  }
}

const assignmentRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  fullScore: [{ required: true, message: '请输入满分', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
}

const isStudent = computed(() => authStore.user?.role === 1)
const canManage = computed(() => [2, 3].includes(authStore.user?.role))
const selectedCourse = computed(() => courses.value.find((course) => course.id === selectedCourseId.value))

onMounted(async () => {
  await loadCourses()
  if (isStudent.value) {
    await loadMySubmissions()
  }
})

async function loadCourses() {
  loadingCourses.value = true
  try {
    const result = await listCourses({ page: 1, size: 100 })
    courses.value = result.records || result || []
    if (courses.value.length && !selectedCourseId.value) {
      selectedCourseId.value = courses.value[0].id
      await loadAssignments()
    }
  } catch (error) {
    ElMessage.error(error.message || '课程加载失败')
  } finally {
    loadingCourses.value = false
  }
}

async function loadAssignments() {
  if (!selectedCourseId.value) {
    assignments.value = []
    return
  }
  loadingAssignments.value = true
  try {
    assignments.value = await listAssignments(selectedCourseId.value)
  } catch (error) {
    ElMessage.error(error.message || '作业加载失败')
  } finally {
    loadingAssignments.value = false
  }
}

async function loadMySubmissions() {
  try {
    mySubmissions.value = await listMySubmissions()
  } catch (error) {
    ElMessage.error(error.message || '提交记录加载失败')
  }
}

function mySubmissionOf(assignmentId) {
  return mySubmissions.value.find((item) => item.assignmentId === assignmentId)
}

function openCreateDialog() {
  editingAssignment.value = null
  Object.assign(assignmentForm, {
    title: '',
    description: '',
    fullScore: 100,
    deadline: '',
    status: 1,
  })
  assignmentDialogVisible.value = true
}

function openEditDialog(row) {
  editingAssignment.value = row
  Object.assign(assignmentForm, {
    title: row.title,
    description: row.description,
    fullScore: row.fullScore,
    deadline: row.deadline,
    status: row.status,
  })
  assignmentDialogVisible.value = true
}

async function saveAssignment() {
  await assignmentFormRef.value.validate()
  const payload = {
    title: assignmentForm.title,
    description: assignmentForm.description,
    fullScore: assignmentForm.fullScore,
    deadline: assignmentForm.deadline,
  }
  try {
    if (editingAssignment.value) {
      await updateAssignment(editingAssignment.value.id, payload)
      ElMessage.success('作业已更新')
    } else {
      await createAssignment({
        ...payload,
        courseId: selectedCourseId.value,
        status: assignmentForm.status,
      })
      ElMessage.success('作业已发布')
    }
    assignmentDialogVisible.value = false
    await loadAssignments()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function changeStatus(row, status) {
    try {
      await updateAssignmentStatus(row.id, status)
      ElMessage.success('作业状态已更新')
      await loadAssignments()
    } catch (error) {
      ElMessage.error(error.message || '状态修改失败')
    }
  }

  function editAssignment(command, row) {
    if (command === 'edit') {
      openEditDialog(row)
    } else if (command === 'delete') {
      removeAssignment(row)
    } else if (command.startsWith('status')) {
      changeStatus(row, parseInt(command.replace('status', '')))
    }
  }

  async function removeAssignment(row) {
  await ElMessageBox.confirm(`确认删除作业「${row.title}」吗？`, '删除作业', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  try {
    await deleteAssignment(row.id)
    ElMessage.success('作业已删除')
    await loadAssignments()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

function openSubmitDialog(row) {
  selectedAssignment.value = row
  const existing = mySubmissionOf(row.id)
  Object.assign(submissionForm, {
    content: existing?.content || '',
    attachmentUrl: existing?.attachmentUrl || '',
  })
  submitDialogVisible.value = true
}

async function saveSubmission() {
  if (!submissionForm.content.trim()) {
    ElMessage.warning('请输入作业内容')
    return
  }
  try {
    await submitAssignment(selectedAssignment.value.id, {
      content: submissionForm.content,
      attachmentUrl: submissionForm.attachmentUrl,
    })
    ElMessage.success('作业已提交')
    submitDialogVisible.value = false
    await loadMySubmissions()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

async function openSubmissionsDrawer(row) {
  selectedAssignment.value = row
  submissionsDrawerVisible.value = true
  await loadSubmissions(row.id)
}

async function loadSubmissions(assignmentId) {
  loadingSubmissions.value = true
  try {
    submissions.value = await listAssignmentSubmissions(assignmentId)
  } catch (error) {
    ElMessage.error(error.message || '提交列表加载失败')
  } finally {
    loadingSubmissions.value = false
  }
}

function openGradeDialog(row) {
  gradingSubmission.value = row
  Object.assign(gradeForm, {
    score: row.score ?? 0,
    teacherComment: row.teacherComment || '',
  })
  gradeDialogVisible.value = true
}

async function saveGrade() {
  try {
    await gradeSubmission(gradingSubmission.value.id, {
      score: gradeForm.score,
      teacherComment: gradeForm.teacherComment,
    })
    ElMessage.success('批改已保存')
    gradeDialogVisible.value = false
    await loadSubmissions(selectedAssignment.value.id)
  } catch (error) {
    ElMessage.error(error.message || '批改失败')
  }
}
</script>

<template>
  <section class="page-stack">
    <section class="surface page-toolbar">
      <div>
        <p class="eyebrow">作业与批改</p>
        <h2>{{ canManage ? '作业批改' : '我的作业' }}</h2>
      </div>
      <div class="toolbar-actions">
        <el-select
          v-model="selectedCourseId"
          :loading="loadingCourses"
          placeholder="选择课程"
          style="width: 240px"
          @change="loadAssignments"
        >
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
        <el-button @click="loadAssignments">刷新</el-button>
        <el-button v-if="canManage" type="primary" :disabled="!selectedCourseId" @click="openCreateDialog">
          发布作业
        </el-button>
      </div>
    </section>

    <section class="surface assignment-summary">
      <p class="eyebrow">当前课程</p>
      <h3>{{ selectedCourse?.name || '请选择课程' }}</h3>
      <p class="muted">{{ selectedCourse?.description || '作业列表会随课程切换刷新。' }}</p>
    </section>

    <section class="surface table-surface">
      <el-table v-loading="loadingAssignments" :data="assignments" stripe>
        <el-table-column label="作业" min-width="240">
          <template #default="{ row }">
            <strong>{{ row.title }}</strong>
            <p class="table-subtitle">{{ row.description || '暂无说明' }}</p>
          </template>
        </el-table-column>
        <el-table-column label="满分" prop="fullScore" width="90" />
        <el-table-column label="截止时间" prop="deadline" min-width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'" effect="plain">
              {{ assignmentStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isStudent" label="我的提交" min-width="180">
          <template #default="{ row }">
            <template v-if="mySubmissionOf(row.id)">
              <el-tag effect="plain">{{ mySubmissionOf(row.id).score == null ? '已提交' : `${mySubmissionOf(row.id).score} 分` }}</el-tag>
              <p class="table-subtitle">{{ mySubmissionOf(row.id).teacherComment || '等待批改' }}</p>
            </template>
            <span v-else class="muted">未提交</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <template v-if="canManage">
              <el-button size="small" @click="openSubmissionsDrawer(row)">查看提交</el-button>
              <el-dropdown trigger="click" @command="(val) => editAssignment(val, row)">
                <el-button size="small" type="primary">编辑<el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">修改作业信息</el-dropdown-item>
                    <el-dropdown-item command="status0" :disabled="row.status === 0">设为草稿</el-dropdown-item>
                    <el-dropdown-item command="status1" :disabled="row.status === 1">设为已发布</el-dropdown-item>
                    <el-dropdown-item command="status2" :disabled="row.status === 2">设为已截止</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除作业</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <el-button v-else-if="isStudent" link type="primary" @click="openSubmitDialog(row)">
              {{ mySubmissionOf(row.id) ? '重新提交' : '提交作业' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="assignmentDialogVisible" :title="editingAssignment ? '编辑作业' : '发布作业'" width="600px">
      <el-form ref="assignmentFormRef" label-width="96px" :model="assignmentForm" :rules="assignmentRules">
        <el-form-item label="标题" prop="title">
          <el-input v-model="assignmentForm.title" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="assignmentForm.description" :rows="4" type="textarea" />
        </el-form-item>
        <el-form-item label="满分" prop="fullScore">
          <el-input-number v-model="assignmentForm.fullScore" :min="1" class="full-input" />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="assignmentForm.deadline"
            class="full-input"
            format="YYYY-MM-DD HH:mm:ss"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item v-if="!editingAssignment" label="状态">
          <el-select v-model="assignmentForm.status" class="full-input">
            <el-option
              v-for="item in ASSIGNMENT_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAssignment">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submitDialogVisible" title="提交作业" width="560px">
      <el-form label-width="86px" :model="submissionForm">
        <el-form-item label="作业内容">
          <el-input v-model="submissionForm.content" :rows="6" type="textarea" />
        </el-form-item>
        <el-form-item label="附件地址">
          <el-input v-model="submissionForm.attachmentUrl" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSubmission">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="submissionsDrawerVisible" size="720px" title="提交列表">
      <section class="drawer-stack">
        <div v-if="selectedAssignment" class="surface drawer-summary">
          <p class="eyebrow">当前作业</p>
          <h3>{{ selectedAssignment.title }}</h3>
          <p class="muted">满分 {{ selectedAssignment.fullScore }} · 截止 {{ selectedAssignment.deadline }}</p>
        </div>
        <el-table v-loading="loadingSubmissions" :data="submissions" stripe>
          <el-table-column label="学生ID" prop="studentId" width="100" />
          <el-table-column label="内容" prop="content" min-width="220" />
          <el-table-column label="附件" prop="attachmentUrl" min-width="160" />
          <el-table-column label="分数" width="90">
            <template #default="{ row }">{{ row.score ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="评语" prop="teacherComment" min-width="140" />
          <el-table-column label="AI评语" prop="aiComment" min-width="140">
            <template #default="{ row }">
              <el-tooltip v-if="row.aiComment" :content="row.aiComment" placement="top">
                <span class="ai-comment-preview">{{ row.aiComment?.slice(0, 20) }}{{ row.aiComment?.length > 20 ? '...' : '' }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openGradeDialog(row)">批改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-drawer>

    <el-dialog v-model="gradeDialogVisible" title="批改作业" width="460px">
      <el-form label-width="82px" :model="gradeForm">
        <el-form-item label="分数">
          <el-input-number v-model="gradeForm.score" :max="selectedAssignment?.fullScore || 100" :min="0" class="full-input" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="gradeForm.teacherComment" :rows="4" type="textarea" />
        </el-form-item>
        <el-form-item label="AI评语">
          <el-input v-model="gradeForm.aiComment" :rows="3" type="textarea" placeholder="点击下方按钮生成 AI 评语" />
          <el-button size="small" :loading="aiLoading" type="primary" @click="generateAiCommentFunc" style="margin-top:8px">
            🤖 生成 AI 评语
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGrade">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.ai-comment-preview { color: #2f54eb; cursor: pointer; font-size: 13px; border-bottom: 1px dashed #2f54eb; }
</style>
