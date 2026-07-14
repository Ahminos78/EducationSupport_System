<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  createAssignment,
  createExam,
  listAssignments,
  listExams,
  listMyExamAttempts,
  listMySubmissions,
  uploadAssignmentAttachment,
} from '../api/assessment'
import { getCourse } from '../api/course'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseId = computed(() => Number(route.params.id))

const loading = ref(false)
const saving = ref(false)
const activeSection = ref('study')
const course = ref(null)
const assignments = ref([])
const exams = ref([])
const submissions = ref([])
const examAttempts = ref([])

const assignmentFormRef = ref()
const examFormRef = ref()
const assignmentEditorRef = ref()
const assignmentFiles = ref([])

const assignmentForm = reactive({
  title: '',
  description: '',
  fullScore: 100,
  startTime: '',
  deadline: '',
  allowLateSubmission: false,
  status: 1,
})

const examForm = reactive({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  fullScore: 100,
  status: 1,
})

const assignmentRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
}

const examRules = {
  title: [{ required: true, message: '请输入考试标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const isStudent = computed(() => authStore.user?.role === 1)
const isTeacher = computed(() => authStore.user?.role === 2)

const menuItems = computed(() => {
  const items = [
    { key: 'study', label: '我的学情', icon: '▦' },
    { key: 'assignments', label: isTeacher.value ? '作业管理' : '我的作业', icon: '✓' },
    { key: 'exams', label: '我的考试', icon: '□' },
  ]
  if (isTeacher.value) {
    items.push(
      { key: 'publish-assignment', label: '发布作业', icon: '+' },
      { key: 'publish-exam', label: '发布考试', icon: '+' },
    )
  }
  return items
})

const submissionMap = computed(() => new Map(
  submissions.value
    .filter((item) => item.courseId === courseId.value)
    .map((item) => [item.assignmentId, item]),
))

const attemptMap = computed(() => new Map(
  examAttempts.value
    .filter((item) => item.courseId === courseId.value)
    .map((item) => [item.examId, item]),
))

const completedAssignments = computed(() => assignments.value.filter(
  (item) => submissionMap.value.has(item.id),
).length)

const completedExams = computed(() => exams.value.filter((item) => {
  const attempt = attemptMap.value.get(item.id)
  return attempt && attempt.status >= 1
}).length)

const totalTasks = computed(() => assignments.value.length + exams.value.length)
const completedTasks = computed(() => completedAssignments.value + completedExams.value)
const completionPercent = computed(() => {
  if (!totalTasks.value) return 0
  return Math.round((completedTasks.value / totalTasks.value) * 100)
})

const recentActivities = computed(() => {
  const assignmentItems = assignments.value.map((item) => ({
    id: `assignment-${item.id}`,
    type: '作业',
    title: item.title,
    detail: submissionMap.value.has(item.id) ? '你已提交该作业' : `截止时间：${formatDate(item.deadline)}`,
    time: item.createdAt,
    completed: submissionMap.value.has(item.id),
  }))
  const examItems = exams.value.map((item) => ({
    id: `exam-${item.id}`,
    type: '考试',
    title: item.title,
    detail: attemptMap.value.get(item.id)?.status >= 1 ? '你已完成该考试' : `考试时间：${formatDate(item.startTime)}`,
    time: item.startTime,
    completed: attemptMap.value.get(item.id)?.status >= 1,
  }))
  return [...assignmentItems, ...examItems]
    .sort((a, b) => new Date(b.time || 0) - new Date(a.time || 0))
    .slice(0, 6)
})

watch(
  () => route.params.id,
  () => {
    activeSection.value = 'study'
    loadPage()
  },
  { immediate: true },
)

async function loadPage() {
  const requestedCourseId = courseId.value
  if (!Number.isFinite(requestedCourseId)) {
    ElMessage.error('课程ID不正确')
    router.replace('/courses')
    return
  }
  loading.value = true
  course.value = null
  assignments.value = []
  exams.value = []
  submissions.value = []
  examAttempts.value = []
  try {
    // Course metadata must remain visible even when an assessment endpoint is unavailable.
    course.value = await getCourse(requestedCourseId)

    const assignmentRequest = listAssignments(requestedCourseId).catch((error) => {
      ElMessage.warning(error.message || '课程作业暂时无法加载')
      return []
    })
    const examRequest = listExams(requestedCourseId).catch((error) => {
      ElMessage.warning(error.message || '课程考试暂时无法加载')
      return []
    })

    if (isStudent.value) {
      const [assignmentData, examData, submissionData, attemptData] = await Promise.all([
        assignmentRequest,
        examRequest,
        listMySubmissions().catch(() => []),
        listMyExamAttempts().catch(() => []),
      ])
      assignments.value = assignmentData || []
      exams.value = examData || []
      submissions.value = submissionData || []
      examAttempts.value = attemptData || []
    } else {
      const [assignmentData, examData] = await Promise.all([assignmentRequest, examRequest])
      assignments.value = assignmentData || []
      exams.value = examData || []
    }
  } catch (error) {
    ElMessage.error(error.message || '课程详情加载失败')
  } finally {
    loading.value = false
  }
}

function assignmentStatus(item) {
  const submission = submissionMap.value.get(item.id)
  const now = new Date()
  if (submission?.status === 1) return '已提交'
  if (item.status === 2 || now > new Date(item.deadline)) return '已截止'
  if (now < new Date(item.startTime)) return '未开始'
  return '进行中'
}

function assignmentStatusType(item) {
  const status = assignmentStatus(item)
  if (status === '已提交') return 'success'
  if (status === '进行中') return 'primary'
  if (status === '已截止') return 'danger'
  return 'info'
}

function openAssignmentDetail(item) {
  router.push(`/courses/${courseId.value}/homework/${item.id}`)
}

function examStatus(item) {
  const attempt = attemptMap.value.get(item.id)
  if (!attempt) return '未参加'
  if (attempt.status === 0) return '进行中'
  if (attempt.status === 1) return '已交卷'
  return attempt.score == null ? '已批改' : `${attempt.score} 分`
}

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

async function publishAssignment() {
  await assignmentFormRef.value.validate()
  assignmentForm.description = sanitizeRichText(assignmentEditorRef.value?.innerHTML || '')
  saving.value = true
  try {
    const created = await createAssignment({ ...assignmentForm, courseId: courseId.value })
    for (const file of assignmentFiles.value) {
      await uploadAssignmentAttachment(created.id, file.raw)
    }
    ElMessage.success('作业发布成功')
    Object.assign(assignmentForm, {
      title: '', description: '', fullScore: 100, startTime: '', deadline: '', allowLateSubmission: false, status: 1,
    })
    if (assignmentEditorRef.value) assignmentEditorRef.value.innerHTML = ''
    assignmentFiles.value = []
    assignments.value = (await listAssignments(courseId.value)) || []
    activeSection.value = 'assignments'
  } catch (error) {
    ElMessage.error(error.message || '作业发布失败')
  } finally {
    saving.value = false
  }
}

function formatAssignment(command) {
  assignmentEditorRef.value?.focus()
  document.execCommand(command, false)
}

function handleAssignmentFiles(file, files) {
  assignmentFiles.value = files
}

function sanitizeRichText(html) {
  const documentNode = new DOMParser().parseFromString(html, 'text/html')
  const allowedTags = new Set(['P', 'DIV', 'BR', 'STRONG', 'B', 'EM', 'I', 'U', 'UL', 'OL', 'LI'])
  documentNode.body.querySelectorAll('*').forEach((element) => {
    if (!allowedTags.has(element.tagName)) {
      element.replaceWith(...element.childNodes)
      return
    }
    Array.from(element.attributes).forEach((attribute) => element.removeAttribute(attribute.name))
  })
  return documentNode.body.innerHTML.trim()
}

async function publishExam() {
  await examFormRef.value.validate()
  saving.value = true
  try {
    await createExam({ ...examForm, courseId: courseId.value })
    ElMessage.success('考试发布成功')
    Object.assign(examForm, {
      title: '', description: '', startTime: '', endTime: '', fullScore: 100, status: 1,
    })
    exams.value = (await listExams(courseId.value)) || []
    activeSection.value = 'exams'
  } catch (error) {
    ElMessage.error(error.message || '考试发布失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div v-loading="loading" class="course-detail-page">
    <aside class="course-sidebar">
      <button class="back-link" @click="router.push('/courses')">← 返回我的课程</button>
      <div class="sidebar-course-mark">{{ course?.name?.slice(0, 1) || '课' }}</div>
      <h2>{{ course?.name || '课程详情' }}</h2>
      <p>{{ course?.code ? `课程号 ${course.code}` : '在线课程' }}</p>

      <nav class="course-menu">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="course-menu-item"
          :class="{ active: activeSection === item.key }"
          @click="activeSection = item.key"
        >
          <span>{{ item.icon }}</span>
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <main class="course-content">
      <header class="course-hero">
        <div>
          <p class="hero-eyebrow">{{ course?.category || '课程学习空间' }}</p>
          <h1>{{ course?.name || '课程详情' }}</h1>
          <p class="hero-description">{{ course?.description || '在这里查看课程任务、作业与考试情况。' }}</p>
        </div>
        <div class="course-facts">
          <span>授课教师 <strong>{{ course?.teacherName || `教师 ${course?.teacherId || '--'}` }}</strong></span>
          <span>课程学分 <strong>{{ course?.credit ?? '--' }}</strong></span>
          <span>课程性质 <strong>{{ course?.category || '暂无分类' }}</strong></span>
        </div>
      </header>

      <template v-if="activeSection === 'study'">
        <section class="section-heading">
          <div>
            <p>LEARNING OVERVIEW</p>
            <h2>{{ isStudent ? '我的学情' : '课程概览' }}</h2>
          </div>
          <span>数据根据课程作业与考试实时计算</span>
        </section>

        <section class="overview-grid">
          <article class="completion-card">
            <div class="progress-shell">
              <el-progress
                type="circle"
                :percentage="isStudent ? completionPercent : 0"
                :width="154"
                :stroke-width="12"
                color="#1677ff"
              />
            </div>
            <div>
              <p class="card-kicker">课程任务完成度</p>
              <h3>{{ isStudent ? `${completedTasks}/${totalTasks}` : `${totalTasks} 项课程任务` }}</h3>
              <p class="muted-copy">
                {{ isStudent ? '完成作业和考试后，课程完成度会自动更新。' : '教师端展示当前课程已发布的任务数量。' }}
              </p>
            </div>
          </article>

          <div class="stat-card-grid">
            <article class="learning-stat blue">
              <span>作业完成</span>
              <strong>{{ isStudent ? `${completedAssignments}/${assignments.length}` : assignments.length }}</strong>
              <small>{{ isStudent ? '已提交作业' : '已发布作业' }}</small>
            </article>
            <article class="learning-stat green">
              <span>考试完成</span>
              <strong>{{ isStudent ? `${completedExams}/${exams.length}` : exams.length }}</strong>
              <small>{{ isStudent ? '已交卷考试' : '已发布考试' }}</small>
            </article>
            <article class="learning-stat orange">
              <span>学习进度</span>
              <strong>{{ isStudent ? `${completionPercent}%` : '--' }}</strong>
              <small>按课程任务计算</small>
            </article>
            <article class="learning-stat purple">
              <span>课程任务</span>
              <strong>{{ totalTasks }}</strong>
              <small>作业与考试总数</small>
            </article>
          </div>
        </section>

        <section class="activity-card">
          <div class="activity-header">
            <div>
              <p class="card-kicker">RECENT ACTIVITY</p>
              <h3>最近课程动态</h3>
            </div>
            <el-button text @click="loadPage">刷新</el-button>
          </div>
          <div v-if="recentActivities.length" class="activity-list">
            <article v-for="item in recentActivities" :key="item.id" class="activity-item">
              <span class="activity-dot" :class="{ completed: item.completed }" />
              <div>
                <div class="activity-title"><el-tag size="small" effect="plain">{{ item.type }}</el-tag>{{ item.title }}</div>
                <p>{{ item.detail }}</p>
              </div>
              <time>{{ formatDate(item.time) }}</time>
            </article>
          </div>
          <el-empty v-else description="课程暂时还没有发布任务" :image-size="80" />
        </section>
      </template>

      <section v-else-if="activeSection === 'assignments'" class="content-card">
        <div class="content-card-header"><div><p class="card-kicker">ASSIGNMENTS</p><h2>{{ isTeacher ? '作业管理' : '我的作业' }}</h2></div><span>共 {{ assignments.length }} 项</span></div>
        <el-table :data="assignments" stripe>
          <el-table-column label="作业名称" min-width="220">
            <template #default="{ row }"><el-button link type="primary" class="assignment-title-link" @click="openAssignmentDetail(row)">{{ row.title }}</el-button></template>
          </el-table-column>
          <el-table-column label="发布时间" min-width="170"><template #default="{ row }">{{ formatDate(row.publishedAt || row.createdAt) }}</template></el-table-column>
          <el-table-column label="截止时间" min-width="170"><template #default="{ row }">{{ formatDate(row.deadline) }}</template></el-table-column>
          <el-table-column label="提交状态" width="110"><template #default="{ row }"><el-tag :type="isStudent ? assignmentStatusType(row) : row.status === 1 ? 'success' : 'info'" effect="plain">{{ isStudent ? assignmentStatus(row) : row.status === 0 ? '草稿' : row.status === 1 ? '已发布' : '已截止' }}</el-tag></template></el-table-column>
          <el-table-column label="成绩" width="90"><template #default="{ row }">{{ isStudent ? (submissionMap.get(row.id)?.score ?? '--') : '--' }}</template></el-table-column>
          <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openAssignmentDetail(row)">查看详情</el-button></template></el-table-column>
        </el-table>
      </section>

      <section v-else-if="activeSection === 'exams'" class="content-card">
        <div class="content-card-header"><div><p class="card-kicker">EXAMS</p><h2>我的考试</h2></div><span>共 {{ exams.length }} 项</span></div>
        <el-table :data="exams" stripe>
          <el-table-column label="考试名称" min-width="220"><template #default="{ row }"><strong>{{ row.title }}</strong><p class="table-note">{{ row.description || '暂无说明' }}</p></template></el-table-column>
          <el-table-column label="满分" prop="fullScore" width="90" />
          <el-table-column label="开始时间" min-width="170"><template #default="{ row }">{{ formatDate(row.startTime) }}</template></el-table-column>
          <el-table-column label="结束时间" min-width="170"><template #default="{ row }">{{ formatDate(row.endTime) }}</template></el-table-column>
          <el-table-column v-if="isStudent" label="完成情况" width="120"><template #default="{ row }"><el-tag :type="attemptMap.get(row.id)?.status >= 1 ? 'success' : 'info'" effect="plain">{{ examStatus(row) }}</el-tag></template></el-table-column>
        </el-table>
      </section>

      <section v-else-if="activeSection === 'publish-assignment'" class="content-card form-card">
        <div class="content-card-header"><div><p class="card-kicker">PUBLISH</p><h2>发布作业</h2></div><span>发布到 {{ course?.name }}</span></div>
        <el-form ref="assignmentFormRef" :model="assignmentForm" :rules="assignmentRules" label-position="top">
          <el-form-item label="作业标题" prop="title"><el-input v-model="assignmentForm.title" placeholder="请输入作业标题" /></el-form-item>
          <el-form-item label="作业描述（富文本）">
            <div class="rich-editor">
              <div class="rich-toolbar">
                <button type="button" @click="formatAssignment('bold')"><strong>B</strong></button>
                <button type="button" @click="formatAssignment('italic')"><em>I</em></button>
                <button type="button" @click="formatAssignment('underline')"><u>U</u></button>
                <button type="button" @click="formatAssignment('insertUnorderedList')">• 列表</button>
                <button type="button" @click="formatAssignment('insertOrderedList')">1. 列表</button>
              </div>
              <div ref="assignmentEditorRef" class="rich-content" contenteditable="true" data-placeholder="说明任务要求、提交格式等" />
            </div>
          </el-form-item>
          <div class="form-row">
            <el-form-item label="满分"><el-input-number v-model="assignmentForm.fullScore" :min="1" /></el-form-item>
            <el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="assignmentForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" /></el-form-item>
            <el-form-item label="截止时间" prop="deadline"><el-date-picker v-model="assignmentForm.deadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择截止时间" /></el-form-item>
          </div>
          <el-form-item label="附件上传">
            <el-upload drag multiple :auto-upload="false" :file-list="assignmentFiles" :on-change="handleAssignmentFiles" :on-remove="handleAssignmentFiles">
              <div class="publish-upload-icon">＋</div>
              <div class="el-upload__text">拖拽文件到这里，或 <em>点击选择</em></div>
              <template #tip><div class="el-upload__tip">单个文件不超过 20MB</div></template>
            </el-upload>
          </el-form-item>
          <el-form-item label="延期提交">
            <el-switch v-model="assignmentForm.allowLateSubmission" active-text="允许截止后提交" inactive-text="截止后禁止提交" />
          </el-form-item>
          <el-form-item label="发布状态" class="narrow-field"><el-select v-model="assignmentForm.status"><el-option label="立即发布" :value="1" /><el-option label="保存草稿" :value="0" /></el-select></el-form-item>
          <el-button type="primary" :loading="saving" @click="publishAssignment">发布作业</el-button>
        </el-form>
      </section>

      <section v-else-if="activeSection === 'publish-exam'" class="content-card form-card">
        <div class="content-card-header"><div><p class="card-kicker">PUBLISH</p><h2>发布考试</h2></div><span>发布到 {{ course?.name }}</span></div>
        <el-form ref="examFormRef" :model="examForm" :rules="examRules" label-position="top">
          <el-form-item label="考试标题" prop="title"><el-input v-model="examForm.title" placeholder="请输入考试标题" /></el-form-item>
          <el-form-item label="考试说明"><el-input v-model="examForm.description" type="textarea" :rows="5" placeholder="说明考试范围与注意事项" /></el-form-item>
          <div class="form-row">
            <el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="examForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" /></el-form-item>
            <el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="examForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择结束时间" /></el-form-item>
          </div>
          <div class="form-row compact">
            <el-form-item label="满分"><el-input-number v-model="examForm.fullScore" :min="1" /></el-form-item>
            <el-form-item label="发布状态"><el-select v-model="examForm.status"><el-option label="立即发布" :value="1" /><el-option label="保存草稿" :value="0" /></el-select></el-form-item>
          </div>
          <el-button type="primary" :loading="saving" @click="publishExam">发布考试</el-button>
        </el-form>
      </section>

    </main>
  </div>
</template>

<style scoped lang="scss">
.course-detail-page { display: grid; grid-template-columns: 228px minmax(0, 1fr); gap: 24px; min-height: calc(100vh - 118px); }
.course-sidebar { align-self: start; position: sticky; top: 94px; padding: 22px 16px; background: #fff; border: 1px solid #edf0f5; border-radius: 18px; box-shadow: 0 4px 20px rgba(31, 45, 61, .04); }
.back-link { border: 0; background: transparent; color: #7b8798; cursor: pointer; padding: 0; margin-bottom: 24px; font-size: 13px; }
.back-link:hover { color: #1677ff; }
.sidebar-course-mark { width: 46px; height: 46px; display: grid; place-items: center; border-radius: 14px; background: linear-gradient(135deg, #1677ff, #69b1ff); color: #fff; font-size: 21px; font-weight: 700; box-shadow: 0 8px 20px rgba(22,119,255,.2); }
.course-sidebar h2 { margin: 14px 0 5px; color: #182230; font-size: 17px; line-height: 1.45; }
.course-sidebar > p { margin: 0; color: #98a2b3; font-size: 12px; }
.course-menu { display: flex; flex-direction: column; gap: 6px; margin-top: 25px; padding-top: 18px; border-top: 1px solid #f0f2f5; }
.course-menu-item { display: flex; align-items: center; gap: 11px; width: 100%; padding: 11px 13px; border: 0; border-radius: 10px; background: transparent; color: #606b7b; cursor: pointer; font-size: 14px; text-align: left; transition: .2s; }
.course-menu-item span { display: grid; place-items: center; width: 20px; height: 20px; color: #98a2b3; font-weight: 700; }
.course-menu-item:hover { color: #1677ff; background: #f5f9ff; }
.course-menu-item.active { color: #1677ff; background: #eaf3ff; font-weight: 600; }
.course-menu-item.active span { color: #1677ff; }
.course-content { min-width: 0; display: flex; flex-direction: column; gap: 22px; }
.course-hero { display: flex; align-items: flex-end; justify-content: space-between; gap: 28px; padding: 28px 32px; border-radius: 20px; color: #fff; background: linear-gradient(120deg, #1557c0 0%, #1677ff 55%, #6aaeff 100%); box-shadow: 0 12px 32px rgba(22,119,255,.18); }
.hero-eyebrow { margin: 0 0 8px; font-size: 12px; letter-spacing: .08em; opacity: .8; }
.course-hero h1 { margin: 0; font-size: 28px; }
.hero-description { max-width: 620px; margin: 10px 0 0; color: rgba(255,255,255,.82); font-size: 14px; line-height: 1.7; }
.course-facts { display: flex; gap: 22px; flex-shrink: 0; }
.course-facts span { display: flex; flex-direction: column; gap: 5px; color: rgba(255,255,255,.7); font-size: 11px; }
.course-facts strong { color: #fff; font-size: 14px; white-space: nowrap; }
.section-heading, .content-card-header, .activity-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
.section-heading p, .card-kicker { margin: 0 0 5px; color: #1677ff; font-size: 11px; font-weight: 700; letter-spacing: .1em; }
.section-heading h2, .content-card-header h2, .activity-header h3 { margin: 0; color: #182230; }
.section-heading > span, .content-card-header > span { color: #98a2b3; font-size: 12px; }
.overview-grid { display: grid; grid-template-columns: 1.1fr 1.4fr; gap: 18px; }
.completion-card, .learning-stat, .activity-card, .content-card { background: #fff; border: 1px solid #edf0f5; border-radius: 18px; box-shadow: 0 4px 20px rgba(31,45,61,.035); }
.completion-card { display: flex; align-items: center; gap: 28px; padding: 30px; }
.progress-shell { padding: 8px; border-radius: 50%; background: #f7faff; }
.completion-card h3 { margin: 3px 0 8px; color: #182230; font-size: 26px; }
.muted-copy { max-width: 240px; margin: 0; color: #8490a2; font-size: 13px; line-height: 1.65; }
.stat-card-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.learning-stat { position: relative; overflow: hidden; padding: 20px 22px; }
.learning-stat::after { content: ''; position: absolute; right: -18px; bottom: -24px; width: 80px; height: 80px; border-radius: 50%; background: currentColor; opacity: .06; }
.learning-stat span { display: block; color: #778196; font-size: 13px; }
.learning-stat strong { display: block; margin: 10px 0 5px; color: #182230; font-size: 25px; }
.learning-stat small { color: #a0a8b6; }
.learning-stat.blue { color: #1677ff; } .learning-stat.green { color: #24a148; } .learning-stat.orange { color: #e98213; } .learning-stat.purple { color: #7a4bd0; }
.activity-card, .content-card { padding: 24px 26px; }
.activity-list { margin-top: 18px; }
.activity-item { display: grid; grid-template-columns: 12px 1fr auto; gap: 14px; align-items: center; padding: 15px 4px; border-top: 1px solid #f1f3f6; }
.activity-dot { width: 9px; height: 9px; border-radius: 50%; background: #d0d5dd; box-shadow: 0 0 0 4px #f4f5f7; }
.activity-dot.completed { background: #2fb344; box-shadow: 0 0 0 4px #e9f8ec; }
.activity-title { display: flex; align-items: center; gap: 9px; color: #344054; font-size: 14px; font-weight: 600; }
.activity-item p, .table-note { margin: 5px 0 0; color: #98a2b3; font-size: 12px; }
.activity-item time { color: #a8b0bd; font-size: 12px; }
.content-card-header { margin-bottom: 20px; }
.form-card { max-width: 900px; }
.form-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.form-row.compact { grid-template-columns: repeat(2, 220px); }
.form-row :deep(.el-date-editor), .form-row :deep(.el-select) { width: 100%; }
.narrow-field { max-width: 220px; }
.assignment-title-link { padding: 0; font-weight: 600; }
.rich-editor { width: 100%; overflow: hidden; border: 1px solid #dcdfe6; border-radius: 10px; background: #fff; }
.rich-toolbar { display: flex; gap: 5px; padding: 8px 10px; border-bottom: 1px solid #ebeef5; background: #f8fafc; }
.rich-toolbar button { min-width: 32px; height: 30px; padding: 0 9px; border: 1px solid transparent; border-radius: 6px; background: transparent; color: #475467; cursor: pointer; }
.rich-toolbar button:hover { border-color: #b7d8ff; color: #1677ff; background: #eaf3ff; }
.rich-content { min-height: 150px; padding: 14px; color: #344054; line-height: 1.7; outline: none; }
.rich-content:empty::before { content: attr(data-placeholder); color: #a8abb2; }
.publish-upload-icon { margin-bottom: 8px; color: #1677ff; font-size: 28px; }
.form-card :deep(.el-upload), .form-card :deep(.el-upload-dragger) { width: 100%; }
@media (max-width: 1100px) { .overview-grid { grid-template-columns: 1fr; } .course-hero { align-items: flex-start; flex-direction: column; } }
@media (max-width: 760px) { .course-detail-page { grid-template-columns: 1fr; } .course-sidebar { position: static; } .course-menu { display: grid; grid-template-columns: repeat(2, 1fr); } .course-facts { flex-wrap: wrap; } .stat-card-grid { grid-template-columns: 1fr; } .completion-card { align-items: flex-start; flex-direction: column; } .form-row, .form-row.compact { grid-template-columns: 1fr; } }
</style>
