<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  downloadAttachment,
  getAssignment,
  listAssignmentAttachments,
  listMySubmissions,
  listSubmissionAttachments,
  submitAssignment,
  uploadSubmissionAttachment,
} from '../api/assessment'
import { getCourse } from '../api/course'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseId = Number(route.params.courseId)
const homeworkId = Number(route.params.homeworkId)

const loading = ref(false)
const submitting = ref(false)
const assignment = ref(null)
const course = ref(null)
const submission = ref(null)
const assignmentAttachments = ref([])
const submissionAttachments = ref([])
const pendingFiles = ref([])
const remark = ref('')

const isStudent = computed(() => authStore.user?.role === 1)

const homeworkStatus = computed(() => {
  if (!assignment.value) return '加载中'
  if (submission.value?.status === 1) return '已提交'
  const now = new Date()
  if (assignment.value.status === 2 || now > new Date(assignment.value.deadline)) return '已截止'
  if (now < new Date(assignment.value.startTime)) return '未开始'
  return '进行中'
})

const canSubmit = computed(() => isStudent.value && ['进行中', '已提交'].includes(homeworkStatus.value))

onMounted(loadPage)

async function loadPage() {
  if (!Number.isFinite(courseId) || !Number.isFinite(homeworkId)) {
    ElMessage.error('作业地址不正确')
    router.replace('/courses')
    return
  }
  loading.value = true
  assignment.value = null
  course.value = null
  submission.value = null
  assignmentAttachments.value = []
  submissionAttachments.value = []
  try {
    // Load the assignment first so optional course, attachment or submission failures
    // never erase the basic homework information.
    const assignmentData = await getAssignment(homeworkId)
    if (assignmentData.courseId !== courseId) {
      throw new Error('该作业不属于当前课程')
    }
    assignment.value = assignmentData

    const [courseData, attachmentData] = await Promise.all([
      getCourse(courseId).catch(() => null),
      // Attachments are optional; an older assessment service may not expose this endpoint yet.
      listAssignmentAttachments(homeworkId).catch(() => []),
    ])
    course.value = courseData
    assignmentAttachments.value = attachmentData || []

    if (isStudent.value) {
      const submissions = await listMySubmissions().catch((error) => {
        ElMessage.warning(error.message || '提交记录暂时无法加载')
        return []
      })
      submission.value = (submissions || []).find((item) => item.assignmentId === homeworkId) || null
      remark.value = submission.value?.content || ''
      if (submission.value) {
        submissionAttachments.value = await listSubmissionAttachments(submission.value.id).catch((error) => {
          ElMessage.warning(error.message || '已上传文件暂时无法加载')
          return []
        })
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '作业详情加载失败')
  } finally {
    loading.value = false
  }
}

function handleFileChange(file, files) {
  pendingFiles.value = files
}

function handleFileRemove(file, files) {
  pendingFiles.value = files
}

async function saveSubmission() {
  if (!remark.value.trim() && pendingFiles.value.length === 0 && submissionAttachments.value.length === 0) {
    ElMessage.warning('请填写备注或选择提交文件')
    return
  }
  submitting.value = true
  try {
    const saved = await submitAssignment(homeworkId, {
      content: remark.value.trim(),
      attachmentUrl: null,
    })
    for (const item of pendingFiles.value) {
      await uploadSubmissionAttachment(saved.id, item.raw)
    }
    ElMessage.success(submission.value ? '作业已重新提交' : '作业提交成功')
    pendingFiles.value = []
    submission.value = saved
    submissionAttachments.value = await listSubmissionAttachments(saved.id)
  } catch (error) {
    ElMessage.error(error.message || '作业提交失败')
  } finally {
    submitting.value = false
  }
}

async function downloadFile(file) {
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

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatSize(size) {
  if (size == null) return '--'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>

<template>
  <div v-loading="loading" class="homework-page">
    <button class="back-button" @click="router.push(`/courses/${courseId}`)">← 返回 {{ course?.name || '课程详情' }}</button>

    <header class="homework-header">
      <div>
        <p>{{ course?.name || assignment?.courseName }}</p>
        <h1>{{ assignment?.title || '作业详情' }}</h1>
      </div>
      <el-tag :type="homeworkStatus === '已提交' ? 'success' : homeworkStatus === '已截止' ? 'danger' : homeworkStatus === '进行中' ? 'primary' : 'info'" effect="dark">
        {{ homeworkStatus }}
      </el-tag>
    </header>

    <section class="homework-card basic-card">
      <div class="section-title"><span>01</span><div><p>HOMEWORK INFO</p><h2>作业基本信息</h2></div></div>
      <div class="info-grid">
        <span>作业名称<strong>{{ assignment?.title }}</strong></span>
        <span>发布时间<strong>{{ formatDate(assignment?.publishedAt || assignment?.createdAt) }}</strong></span>
        <span>截止时间<strong>{{ formatDate(assignment?.deadline) }}</strong></span>
        <span>任课教师<strong>{{ assignment?.teacherName || `教师 ${assignment?.teacherId || '--'}` }}</strong></span>
      </div>
    </section>

    <section class="homework-card">
      <div class="section-title"><span>02</span><div><p>DESCRIPTION</p><h2>作业说明</h2></div></div>
      <div class="description-content">{{ assignment?.description || '教师暂未填写作业说明。' }}</div>
      <div v-if="assignmentAttachments.length" class="description-attachments">
        <span>附件：</span>
        <button
          v-for="file in assignmentAttachments"
          :key="file.id"
          class="attachment-link"
          @click="downloadFile(file)"
        >
          {{ file.originalName }}
        </button>
      </div>
    </section>

    <section v-if="isStudent" class="homework-card submission-card">
      <div class="section-title"><span>03</span><div><p>MY SUBMISSION</p><h2>我的提交</h2></div></div>

      <div v-if="submission" class="submission-summary">
        <span>提交时间<strong>{{ formatDate(submission.submittedAt) }}</strong></span>
        <span>提交状态<strong>{{ submission.status === 1 ? '已提交' : '未提交' }}</strong></span>
        <span>批改状态<strong>{{ submission.gradingStatus === 1 ? '已批改' : '待批改' }}</strong></span>
        <span>教师评分<strong>{{ submission.score ?? '--' }}</strong></span>
      </div>

      <div v-if="submissionAttachments.length" class="existing-files">
        <h3>已上传文件</h3>
        <div class="file-list compact">
          <button v-for="file in submissionAttachments" :key="file.id" class="file-item" @click="downloadFile(file)">
            <span class="file-icon">✓</span><span><strong>{{ file.originalName }}</strong><small>{{ formatSize(file.fileSize) }}</small></span><em>下载</em>
          </button>
        </div>
      </div>

      <el-upload
        v-if="canSubmit"
        class="upload-area"
        drag
        multiple
        :auto-upload="false"
        :file-list="pendingFiles"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <div class="upload-symbol">＋</div>
        <div class="el-upload__text">将文件拖到这里，或 <em>点击选择文件</em></div>
        <template #tip><div class="el-upload__tip">单个文件不超过 20MB，可同时选择多个文件</div></template>
      </el-upload>

      <div class="remark-field">
        <label>提交备注</label>
        <el-input v-model="remark" type="textarea" :rows="5" :disabled="!canSubmit" placeholder="填写作业说明、补充信息或给老师的备注" />
      </div>

      <div v-if="submission?.teacherComment" class="teacher-comment">
        <h3>教师评语</h3><p>{{ submission.teacherComment }}</p>
      </div>

      <div class="submission-actions">
        <span v-if="homeworkStatus === '已截止'">作业已截止，不能继续提交。</span>
        <el-button v-if="canSubmit" type="primary" size="large" :loading="submitting" @click="saveSubmission">
          {{ submission ? '重新提交作业' : '提交作业' }}
        </el-button>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.homework-page { max-width: 1120px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
.back-button { align-self: flex-start; padding: 0; border: 0; background: none; color: #667085; cursor: pointer; }
.back-button:hover { color: #1677ff; }
.homework-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding: 30px 34px; border-radius: 20px; color: #fff; background: linear-gradient(125deg, #1557c0, #1677ff 58%, #78b7ff); box-shadow: 0 12px 30px rgba(22,119,255,.16); }
.homework-header p { margin: 0 0 8px; color: rgba(255,255,255,.75); font-size: 13px; }
.homework-header h1 { margin: 0; font-size: 28px; }
.homework-card { padding: 26px 30px; border: 1px solid #edf0f5; border-radius: 18px; background: #fff; box-shadow: 0 4px 20px rgba(31,45,61,.035); }
.section-title { display: flex; align-items: center; gap: 14px; margin-bottom: 22px; }
.section-title > span { display: grid; place-items: center; width: 38px; height: 38px; border-radius: 12px; color: #1677ff; background: #eaf3ff; font-weight: 700; }
.section-title p { margin: 0 0 3px; color: #98a2b3; font-size: 10px; letter-spacing: .12em; }
.section-title h2 { margin: 0; color: #182230; font-size: 18px; }
.info-grid, .submission-summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.info-grid span, .submission-summary span { display: flex; flex-direction: column; gap: 7px; padding: 15px 17px; border-radius: 12px; color: #98a2b3; background: #f7f9fc; font-size: 12px; }
.info-grid strong, .submission-summary strong { color: #344054; font-size: 14px; line-height: 1.5; }
.description-content { padding: 20px; border-radius: 14px; color: #475467; background: #fafbfc; line-height: 1.9; white-space: pre-wrap; }
.description-attachments { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-top: 14px; padding: 0 4px; color: #98a2b3; font-size: 13px; }
.attachment-link { padding: 0; border: 0; border-bottom: 1px solid transparent; background: transparent; color: #1677ff; cursor: pointer; font-size: 13px; }
.attachment-link:hover { border-bottom-color: #1677ff; }
.file-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.file-list.compact { margin-top: 10px; }
.file-item { display: grid; grid-template-columns: 38px 1fr auto; align-items: center; gap: 12px; padding: 13px 15px; border: 1px solid #e8edf3; border-radius: 12px; background: #fff; cursor: pointer; text-align: left; }
.file-item:hover { border-color: #91caff; background: #f7fbff; }
.file-icon { display: grid; place-items: center; width: 36px; height: 36px; border-radius: 10px; color: #1677ff; background: #eaf3ff; font-weight: 700; }
.file-item strong, .file-item small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-item strong { color: #344054; font-size: 13px; }.file-item small { margin-top: 4px; color: #98a2b3; }.file-item em { color: #1677ff; font-size: 12px; font-style: normal; }
.submission-summary { margin-bottom: 22px; }.existing-files { margin-bottom: 22px; }.existing-files h3, .teacher-comment h3 { margin: 0; color: #344054; font-size: 14px; }
.upload-area { margin-top: 8px; }.upload-area :deep(.el-upload-dragger) { padding: 28px; border-radius: 14px; background: #fbfcfe; }.upload-symbol { margin: 0 auto 10px; color: #1677ff; font-size: 30px; }
.remark-field { margin-top: 22px; }.remark-field label { display: block; margin-bottom: 9px; color: #344054; font-size: 14px; font-weight: 600; }
.teacher-comment { margin-top: 22px; padding: 18px; border-radius: 12px; background: #f6fbf7; }.teacher-comment p { margin: 8px 0 0; color: #477653; line-height: 1.7; }
.submission-actions { display: flex; align-items: center; justify-content: flex-end; gap: 18px; margin-top: 24px; }.submission-actions span { color: #d92d20; font-size: 13px; }
@media (max-width: 900px) { .info-grid, .submission-summary { grid-template-columns: repeat(2, 1fr); }.file-list { grid-template-columns: 1fr; } }
@media (max-width: 600px) { .homework-header { align-items: flex-start; flex-direction: column; }.info-grid, .submission-summary { grid-template-columns: 1fr; }.homework-card { padding: 22px 18px; } }
</style>
