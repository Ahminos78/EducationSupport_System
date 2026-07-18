<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  createAssignment,
  downloadAttachment,
  getSubmissionDetail,
  gradeSubmission,
  listAssignments,
  listAssignmentSubmissions,
  listExams,
  listExamAttempts,
  listMyExamAttempts,
  listMySubmissions,
  listSubmissionAttachments,
  uploadAssignmentAttachment,
} from '../api/assessment'
import { getCourse } from '../api/course'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseId = Number(route.params.id)

const loading = ref(false)
const saving = ref(false)
const activeSection = ref(localStorage.getItem(`activeSection_${courseId}`) || 'study')
watch(activeSection, (val) => localStorage.setItem(`activeSection_${courseId}`, val))
const course = ref(null)
const assignments = ref([])
const exams = ref([])
const submissions = ref([])
const examAttempts = ref([])
const submissionsDrawerVisible = ref(false)
const submissionsList = ref([])
const selectedExam = ref(null)
const loadingSubmissions = ref(false)
const assignmentDrawerVisible = ref(false)
const assignmentSubmissionList = ref([])
const selectedAssignment = ref(null)
const loadingAssignmentSubmissions = ref(false)
const selectedAssignmentSubmission = ref(null)
const submissionAttachments = ref([])
const loadingSubmissionDetail = ref(false)
const savingGrade = ref(false)
const pendingFiles = ref([])

const gradeForm = reactive({
  score: null,
  teacherComment: '',
})

const assignmentFormRef = ref()

const assignmentForm = reactive({
  title: '',
  description: '',
  fullScore: 100,
  deadline: '',
  status: 1,
})

const assignmentRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
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
    )
  }
  return items
})

const submissionMap = computed(() => new Map(
  submissions.value
    .filter((item) => item.courseId === courseId)
    .map((item) => [item.assignmentId, item]),
))

const attemptMap = computed(() => new Map(
  examAttempts.value
    .filter((item) => item.courseId === courseId)
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

onMounted(loadPage)

async function loadPage() {
  if (!Number.isFinite(courseId)) {
    ElMessage.error('课程ID不正确')
    router.replace('/courses')
    return
  }
  localStorage.setItem('lastCourseId', courseId)
  const validTabs = ['study', 'assignments', 'exams', 'publish-assignment']
  const tab = route.query.tab
  if (tab && validTabs.includes(tab)) {
    activeSection.value = tab
  }
  loading.value = true
  try {
    const baseRequests = [getCourse(courseId), listAssignments(courseId), listExams(courseId)]
    if (isStudent.value) {
      const [courseData, assignmentData, examData, submissionData, attemptData] = await Promise.all([
        ...baseRequests,
        listMySubmissions().catch(() => []),
        listMyExamAttempts().catch(() => []),
      ])
      course.value = courseData
      assignments.value = assignmentData || []
      exams.value = examData || []
      submissions.value = submissionData || []
      examAttempts.value = attemptData || []
    } else {
      const [courseData, assignmentData, examData] = await Promise.all(baseRequests)
      course.value = courseData
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
  if (submission?.score != null) return `${submission.score} 分`
  if (submission) return '已提交'
  return '未提交'
}

function examStatus(item) {
  const attempt = attemptMap.value.get(item.id)
  if (!attempt) return '未参加'
  if (attempt.status === 0) return '进行中'
  if (attempt.status === 1) return '已交卷'
  return attempt.score == null ? '已批改' : `${attempt.score} 分`
}

async function viewSubmissions(row) {
  selectedExam.value = row
  submissionsList.value = []
  submissionsDrawerVisible.value = true
  loadingSubmissions.value = true
  try {
    submissionsList.value = await listExamAttempts(row.id)
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loadingSubmissions.value = false
  }
}

async function viewAssignmentSubmissions(row) {
  selectedAssignment.value = row
  assignmentSubmissionList.value = []
  selectedAssignmentSubmission.value = null
  submissionAttachments.value = []
  assignmentDrawerVisible.value = true
  loadingAssignmentSubmissions.value = true
  try {
    assignmentSubmissionList.value = await listAssignmentSubmissions(row.id)
  } catch (error) {
    ElMessage.error(error.message || '作业提交概览加载失败')
  } finally {
    loadingAssignmentSubmissions.value = false
  }
}

async function openSubmissionReview(row) {
  if (!row.id) {
    ElMessage.info('该学生尚未提交作业')
    return
  }
  loadingSubmissionDetail.value = true
  try {
    const [detail, attachments] = await Promise.all([
      getSubmissionDetail(row.id),
      listSubmissionAttachments(row.id).catch(() => []),
    ])
    selectedAssignmentSubmission.value = { ...row, ...detail }
    submissionAttachments.value = attachments || []
    gradeForm.score = detail.score ?? null
    gradeForm.teacherComment = detail.teacherComment || ''
  } catch (error) {
    ElMessage.error(error.message || '提交详情加载失败')
  } finally {
    loadingSubmissionDetail.value = false
  }
}

async function saveSubmissionGrade() {
  const submission = selectedAssignmentSubmission.value
  if (!submission?.id) return
  if (gradeForm.score == null) {
    ElMessage.warning('请输入分数')
    return
  }
  savingGrade.value = true
  try {
    const graded = await gradeSubmission(submission.id, {
      score: gradeForm.score,
      teacherComment: gradeForm.teacherComment.trim(),
    })
    selectedAssignmentSubmission.value = { ...submission, ...graded }
    assignmentSubmissionList.value = assignmentSubmissionList.value.map((item) => (
      item.id === graded.id ? { ...item, ...graded } : item
    ))
    ElMessage.success('评分已保存')
  } catch (error) {
    ElMessage.error(error.message || '评分保存失败')
  } finally {
    savingGrade.value = false
  }
}

async function downloadSubmissionFile(file) {
  try {
    const response = await downloadAttachment(file.downloadUrl)
    const url = URL.createObjectURL(response.data)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = file.originalName
    anchor.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error.message || '附件下载失败')
  }
}

function openAssignmentDetail(row) {
  router.push(`/courses/${courseId}/homework/${row.id}`)
}

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

function handleAttachmentFileChange(file, files) {
  pendingFiles.value = files
}

function handleAttachmentFileRemove(file, files) {
  pendingFiles.value = files
}

function formatFileSize(size) {
  if (size == null) return '--'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

async function publishAssignment() {
  await assignmentFormRef.value.validate()
  saving.value = true
  try {
    const created = await createAssignment({ ...assignmentForm, courseId })
    for (const item of pendingFiles.value) {
      await uploadAssignmentAttachment(created.id, item.raw)
    }
    if (pendingFiles.value.length > 0) {
      ElMessage.success(`作业发布成功，${pendingFiles.value.length} 个附件已上传`)
    } else {
      ElMessage.success('作业发布成功')
    }
    Object.assign(assignmentForm, {
      title: '', description: '', fullScore: 100, deadline: '', status: 1,
    })
    pendingFiles.value = []
    assignments.value = (await listAssignments(courseId)) || []
    activeSection.value = 'assignments'
  } catch (error) {
    ElMessage.error(error.message || '作业发布失败')
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
          <span>课程标签 <strong>{{ course?.tags || '暂无标签' }}</strong></span>
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
            <template #default="{ row }">
              <el-button link type="primary" class="assignment-link" @click="openAssignmentDetail(row)">{{ row.title }}</el-button>
              <p class="table-note">{{ row.description?.replace(/<[^>]+>/g, '') || '暂无说明' }}</p>
            </template>
          </el-table-column>
          <el-table-column label="满分" prop="fullScore" width="90" />
          <el-table-column label="截止时间" min-width="170"><template #default="{ row }">{{ formatDate(row.deadline) }}</template></el-table-column>
          <el-table-column v-if="isStudent" label="完成情况" width="120"><template #default="{ row }"><el-tag :type="submissionMap.has(row.id) ? 'success' : 'info'" effect="plain">{{ assignmentStatus(row) }}</el-tag></template></el-table-column>
          <el-table-column v-else label="发布状态" width="110"><template #default="{ row }"><el-tag effect="plain">{{ row.status === 0 ? '草稿' : row.status === 1 ? '已发布' : '已截止' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" :width="isTeacher ? 190 : 110" fixed="right" align="center">
            <template #default="{ row }">
              <div v-if="isTeacher" class="assignment-actions">
                <el-button size="small" type="primary" @click="viewAssignmentSubmissions(row)">查看提交</el-button>
                <el-button size="small" @click="openAssignmentDetail(row)">编辑</el-button>
              </div>
              <el-button v-else size="small" type="primary" @click="router.push(`/courses/${courseId}/homework/${row.id}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-drawer v-model="assignmentDrawerVisible" title="作业提交与批改" size="860px">
          <div v-loading="loadingAssignmentSubmissions" class="drawer-stack">
            <template v-if="selectedAssignment">
              <div class="surface drawer-summary">
                <p class="eyebrow">{{ selectedAssignment.title }}</p>
                <p class="muted">
                  满分 {{ selectedAssignment.fullScore }} · 截止 {{ formatDate(selectedAssignment.deadline) }} ·
                  课程成员 {{ assignmentSubmissionList.length }} 人 ·
                  已提交 {{ assignmentSubmissionList.filter((item) => item.id).length }} 人
                </p>
              </div>
              <el-table :data="assignmentSubmissionList" stripe>
                <el-table-column label="学号" prop="studentNo" min-width="120" />
                <el-table-column label="姓名" prop="studentName" min-width="100" />
                <el-table-column label="专业/班级" min-width="160">
                  <template #default="{ row }">
                    <span>{{ row.major || '--' }}</span>
                    <p class="table-note">{{ row.className || '--' }}</p>
                  </template>
                </el-table-column>
                <el-table-column label="提交时间" min-width="145"><template #default="{ row }">{{ formatDate(row.submittedAt) }}</template></el-table-column>
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag :type="!row.id ? 'info' : row.gradingStatus === 1 ? 'success' : 'warning'" size="small" effect="plain">
                      {{ !row.id ? '未提交' : row.gradingStatus === 1 ? '已批改' : '待批改' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="成绩" width="70"><template #default="{ row }">{{ row.score ?? '--' }}</template></el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" :disabled="!row.id" @click="openSubmissionReview(row)">
                      {{ !row.id ? '未提交' : row.gradingStatus === 1 ? '查看/修改' : '查看/评分' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!assignmentSubmissionList.length" description="该课程暂无已通过选课的学生" />

              <section v-if="selectedAssignmentSubmission" v-loading="loadingSubmissionDetail" class="submission-review surface">
                <div class="review-heading">
                  <div>
                    <p class="card-kicker">SUBMISSION REVIEW</p>
                    <h3>{{ selectedAssignmentSubmission.studentName }}的作业</h3>
                  </div>
                  <el-tag :type="selectedAssignmentSubmission.gradingStatus === 1 ? 'success' : 'warning'" effect="plain">
                    {{ selectedAssignmentSubmission.gradingStatus === 1 ? '已批改' : '待批改' }}
                  </el-tag>
                </div>
                <div class="submission-meta">
                  <span>学号<strong>{{ selectedAssignmentSubmission.studentNo }}</strong></span>
                  <span>提交时间<strong>{{ formatDate(selectedAssignmentSubmission.submittedAt) }}</strong></span>
                  <span>专业班级<strong>{{ selectedAssignmentSubmission.major || '--' }} {{ selectedAssignmentSubmission.className || '' }}</strong></span>
                </div>
                <div class="submission-content">
                  <h4>提交说明</h4>
                  <p>{{ selectedAssignmentSubmission.content || '学生未填写提交说明。' }}</p>
                  <div v-if="submissionAttachments.length" class="submission-files">
                    <h4>提交文件</h4>
                    <el-button
                      v-for="file in submissionAttachments"
                      :key="file.id"
                      link
                      type="primary"
                      @click="downloadSubmissionFile(file)"
                    >
                      {{ file.originalName }}
                    </el-button>
                  </div>
                </div>
                <el-form label-position="top" class="grade-form">
                  <el-form-item :label="`评分（满分 ${selectedAssignment.fullScore}）`">
                    <el-input-number v-model="gradeForm.score" :min="0" :max="selectedAssignment.fullScore" />
                  </el-form-item>
                  <el-form-item label="教师评语">
                    <el-input v-model="gradeForm.teacherComment" type="textarea" :rows="4" placeholder="请输入对本次作业的评语" />
                  </el-form-item>
                  <el-button type="primary" :loading="savingGrade" @click="saveSubmissionGrade">保存评分</el-button>
                </el-form>
              </section>
            </template>
          </div>
        </el-drawer>
      </section>

      <section v-else-if="activeSection === 'exams'" class="content-card">
        <div class="content-card-header">
          <div><p class="card-kicker">EXAMS</p><h2>我的考试</h2></div>
          <div class="header-actions">
            <span>共 {{ exams.length }} 项</span>
            <el-button v-if="isTeacher" type="primary" size="small" @click="router.push(`/courses/${courseId}/exams/create?courseName=${course?.name || ''}`)">发布考试</el-button>
          </div>
        </div>
        <el-table :data="exams" stripe>
          <el-table-column label="考试名称" min-width="200">
            <template #default="{ row }">
              <router-link v-if="isStudent" :to="`/courses/${courseId}/exams/${row.id}`" class="exam-link">
                <strong>{{ row.title }}</strong>
              </router-link>
              <strong v-else>{{ row.title }}</strong>
              <p class="table-note">{{ row.description || '暂无说明' }}</p>
            </template>
          </el-table-column>
          <el-table-column label="满分" prop="fullScore" width="70" />
          <el-table-column label="时长" width="80"><template #default="{ row }">{{ row.duration || 60 }}分钟</template></el-table-column>
          <el-table-column label="开始时间" min-width="145"><template #default="{ row }">{{ formatDate(row.startTime) }}</template></el-table-column>
          <el-table-column label="结束时间" min-width="145"><template #default="{ row }">{{ formatDate(row.endTime) }}</template></el-table-column>
          <el-table-column v-if="isStudent" label="完成情况" width="90"><template #default="{ row }"><el-tag :type="attemptMap.get(row.id)?.status >= 1 ? 'success' : 'info'" effect="plain">{{ examStatus(row) }}</el-tag></template></el-table-column>
          <el-table-column v-if="isStudent" label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="router.push(`/courses/${courseId}/exams/${row.id}`)">进入考试</el-button>
            </template>
          </el-table-column>
          <el-table-column v-if="isTeacher" label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewSubmissions(row)">查看提交</el-button>
              <el-button size="small" @click="router.push(`/courses/${courseId}/exams/${row.id}/edit`)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-drawer v-model="submissionsDrawerVisible" title="学生提交" size="600px">
          <div v-loading="loadingSubmissions" class="drawer-stack">
            <template v-if="selectedExam">
              <div class="surface drawer-summary">
                <p class="eyebrow">{{ selectedExam.title }}</p>
                <p class="muted">满分 {{ selectedExam.fullScore }} · 时长 {{ selectedExam.duration }} 分钟</p>
              </div>
              <el-table :data="submissionsList" stripe>
                <el-table-column label="学生姓名" prop="studentName" min-width="120" />
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'" effect="plain" size="small">
                      {{ row.status === 0 ? '进行中' : row.status === 1 ? '已交卷' : row.status === 2 ? '已批改' : '--' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="得分" width="70">
                  <template #default="{ row }">{{ row.score != null ? row.score : '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" @click="router.push(`/courses/${courseId}/exams/${selectedExam.id}/review/${row.id}`)">查看试卷</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!submissionsList.length" description="暂无学生提交" />
            </template>
          </div>
        </el-drawer>
      </section>

      <section v-else-if="activeSection === 'publish-assignment'" class="content-card form-card">
        <div class="content-card-header"><div><p class="card-kicker">PUBLISH</p><h2>发布作业</h2></div><span>发布到 {{ course?.name }}</span></div>
        <el-form ref="assignmentFormRef" :model="assignmentForm" :rules="assignmentRules" label-position="top">
          <el-form-item label="作业标题" prop="title"><el-input v-model="assignmentForm.title" placeholder="请输入作业标题" /></el-form-item>
          <el-form-item label="作业说明"><el-input v-model="assignmentForm.description" type="textarea" :rows="5" placeholder="说明任务要求、提交格式等" /></el-form-item>
          <div class="form-row">
            <el-form-item label="满分"><el-input-number v-model="assignmentForm.fullScore" :min="1" /></el-form-item>
            <el-form-item label="截止时间" prop="deadline"><el-date-picker v-model="assignmentForm.deadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择截止时间" /></el-form-item>
            <el-form-item label="发布状态"><el-select v-model="assignmentForm.status"><el-option label="立即发布" :value="1" /><el-option label="保存草稿" :value="0" /></el-select></el-form-item>
          </div>
          <el-form-item label="作业附件">
            <div class="attachment-section">
              <el-upload
                ref="attachmentUploadRef"
                :auto-upload="false"
                :file-list="pendingFiles"
                :limit="5"
                :on-change="handleAttachmentFileChange"
                :on-remove="handleAttachmentFileRemove"
                accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar,.jpg,.jpeg,.png,.gif"
                drag
              >
                <div class="el-upload__text">拖拽文件到此处，或 <em>点击上传</em></div>
                <template #tip>
                  <div class="el-upload__tip">支持 PDF、Office 文档、图片、压缩包等，单文件不超过 20MB，最多 5 个文件</div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
          <el-button type="primary" :loading="saving" @click="publishAssignment">发布作业</el-button>
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
.assignment-link { max-width: 100%; padding: 0; font-weight: 600; }
.assignment-actions { display: flex; align-items: center; justify-content: center; gap: 8px; white-space: nowrap; }
.assignment-actions :deep(.el-button + .el-button) { margin-left: 0; }
.drawer-stack { display: flex; flex-direction: column; gap: 18px; }
.drawer-summary, .submission-review { padding: 18px 20px; border: 1px solid #edf0f5; border-radius: 14px; background: #fff; }
.drawer-summary p { margin: 0; }
.drawer-summary .muted { margin-top: 8px; color: #8490a2; font-size: 13px; }
.review-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.review-heading h3 { margin: 0; color: #182230; }
.submission-meta { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 18px 0; }
.submission-meta span { padding: 12px; border-radius: 10px; background: #f7f9fc; color: #8490a2; font-size: 12px; }
.submission-meta strong { display: block; margin-top: 6px; color: #344054; font-size: 13px; }
.submission-content { padding: 16px 0; border-top: 1px solid #edf0f5; border-bottom: 1px solid #edf0f5; }
.submission-content h4 { margin: 0 0 9px; color: #344054; }
.submission-content p { margin: 0; color: #667085; line-height: 1.7; white-space: pre-wrap; }
.submission-files { margin-top: 18px; }
.submission-files .el-button { display: flex; margin: 5px 0 0; }
.grade-form { margin-top: 18px; }
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
.exam-link { color: inherit; text-decoration: none; }
.exam-link:hover { color: #1677ff; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.content-card-header { margin-bottom: 20px; }
.form-card { max-width: 900px; }
.form-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.form-row.compact { grid-template-columns: repeat(2, 220px); }
.form-row :deep(.el-date-editor), .form-row :deep(.el-select) { width: 100%; }
.attachment-section { width: 100%; }
@media (max-width: 1100px) { .overview-grid { grid-template-columns: 1fr; } .course-hero { align-items: flex-start; flex-direction: column; } }
@media (max-width: 760px) { .course-detail-page { grid-template-columns: 1fr; } .course-sidebar { position: static; } .course-menu { display: grid; grid-template-columns: repeat(2, 1fr); } .course-facts { flex-wrap: wrap; } .stat-card-grid { grid-template-columns: 1fr; } .completion-card { align-items: flex-start; flex-direction: column; } .form-row, .form-row.compact { grid-template-columns: 1fr; } }
</style>
