<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getExamDetail, listExamQuestions, startExamAttempt, submitExamAttempt } from '../api/assessment'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)
const courseId = Number(route.params.courseId)

const exam = ref(null)
const questions = ref([])
const answers = ref({})
const loading = ref(true)
const started = ref(false)
const submitted = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = 5
const timeLeft = ref(0)
let timerInterval = null

const examTitle = computed(() => exam.value?.title || '考试加载中...')
const totalScore = computed(() => exam.value?.fullScore || 0)
const duration = computed(() => exam.value?.duration || 0)
const minutes = computed(() => Math.floor(timeLeft.value / 60))
const seconds = computed(() => timeLeft.value % 60)
const timeDisplay = computed(() => `${String(minutes.value).padStart(2, '0')}:${String(seconds.value).padStart(2, '0')}`)
const isTimeLow = computed(() => timeLeft.value < 300 && timeLeft.value > 0)

const totalPages = computed(() => {
  if (!questions.value.length) return 1
  return Math.ceil(questions.value.length / pageSize)
})

const pagedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return questions.value.slice(start, start + pageSize)
})

const answeredCount = computed(() => Object.keys(answers.value).length)
const allAnswered = computed(() => answeredCount.value === questions.value.length)

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

function parseOptions(optionsStr) {
  if (!optionsStr) return []
  try {
    return JSON.parse(optionsStr)
  } catch {
    return []
  }
}

function selectOption(questionId, option) {
  answers.value[questionId] = option
}

function setFillAnswer(questionId, value) {
  answers.value[questionId] = value
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}

function startTimer() {
  timeLeft.value = duration.value * 60
  timerInterval = setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) {
      clearInterval(timerInterval)
      timerInterval = null
      doSubmit()
    }
  }, 1000)
}

async function confirmStart() {
  loading.value = true
  try {
    await startExamAttempt(examId)
    started.value = true
    startTimer()
  } catch (error) {
    ElMessage.error(error.message || '开始考试失败')
  } finally {
    loading.value = false
  }
}

async function doSubmit() {
  if (submitting.value) return
  submitting.value = true
  const answerContent = questions.value.map(q => ({
    questionId: q.id,
    type: q.type,
    answer: answers.value[q.id] || '',
  }))
  try {
    await submitExamAttempt(examId, { answerContent: JSON.stringify(answerContent) })
    submitted.value = true
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    ElMessage.success('交卷成功')
  } catch (error) {
    ElMessage.error(error.message || '交卷失败')
  } finally {
    submitting.value = false
  }
}

async function handleSubmit() {
  if (submitted.value) return
  const unanswered = questions.value.length - answeredCount.value
  let msg = '确定要交卷吗？'
  if (unanswered > 0) {
    msg = `还有 ${unanswered} 道题目未作答，确定交卷吗？`
  }
  try {
    await ElMessageBox.confirm(msg, '交卷确认', {
      confirmButtonText: '确认交卷',
      cancelButtonText: '继续答题',
      type: 'warning',
    })
    await doSubmit()
  } catch {
    // cancelled
  }
}

function goBack() {
  router.push(`/courses/${courseId}`)
}

onMounted(async () => {
  if (!Number.isFinite(examId) || !Number.isFinite(courseId)) {
    ElMessage.error('参数错误')
    router.push('/courses')
    return
  }
  loading.value = true
  try {
    const [examData, questionData] = await Promise.all([
      getExamDetail(examId),
      listExamQuestions(examId),
    ])
    exam.value = examData
    questions.value = questionData || []
  } catch (error) {
    ElMessage.error(error.message || '考试加载失败')
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
})
</script>

<template>
  <div class="exam-fullscreen">
    <header class="exam-header">
      <button class="exam-back" @click="goBack">&larr; 返回课程</button>
      <h1 class="exam-header-title">{{ examTitle }}</h1>
      <div class="exam-header-info">
        <span v-if="started && !submitted" class="exam-timer" :class="{ 'time-low': isTimeLow }">
          &#9202; {{ timeDisplay }}
        </span>
        <span v-else-if="submitted" class="exam-submitted-tag">已交卷</span>
        <span class="exam-score">总分 {{ totalScore }}</span>
      </div>
    </header>

    <div v-loading="loading" class="exam-body">
      <template v-if="!started && !submitted">
        <div class="exam-welcome">
          <div class="exam-welcome-card">
            <h2>{{ examTitle }}</h2>
            <ul class="exam-meta-list">
              <li><strong>总分：</strong>{{ totalScore }} 分</li>
              <li><strong>考试时间：</strong>{{ duration }} 分钟</li>
              <li><strong>开始时间：</strong>{{ formatDate(exam?.startTime) }}</li>
              <li><strong>截止时间：</strong>{{ formatDate(exam?.endTime) }}</li>
              <li><strong>题目数量：</strong>{{ questions.length }} 题</li>
            </ul>
            <div class="exam-rules">
              <h3>考生须知</h3>
              <ol>
                <li>点击下方「确认开始」按钮后，计时器将开始倒计时。</li>
                <li>考试总时长为 {{ duration }} 分钟，超时将自动交卷。</li>
                <li>题目分为选择题和填空题，请按要求作答。</li>
                <li>交卷后不可修改答案，请谨慎操作。</li>
              </ol>
            </div>
            <el-button type="primary" size="large" class="start-btn" @click="confirmStart">
              确认开始
            </el-button>
          </div>
        </div>
      </template>

      <template v-else-if="submitted">
        <div class="exam-result">
          <div class="exam-result-card">
            <div class="result-icon">&#10003;</div>
            <h2>交卷成功</h2>
            <p>你的答卷已提交，请等待教师批改。</p>
            <p class="result-summary">共 {{ questions.length }} 题 · 已答 {{ answeredCount }} 题</p>
            <el-button type="primary" @click="goBack">返回课程</el-button>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="exam-progress-bar">
          <span>已答 {{ answeredCount }}/{{ questions.length }} 题</span>
          <el-progress :percentage="Math.round(answeredCount / questions.length * 100)" :stroke-width="8" />
        </div>

        <div class="exam-questions">
          <div v-for="(question, idx) in pagedQuestions" :key="question.id" class="exam-question-card">
            <div class="question-header">
              <span class="question-number">第 {{ (currentPage - 1) * pageSize + idx + 1 }} 题</span>
              <el-tag size="small" effect="plain" :type="question.type === 0 ? 'primary' : 'success'">
                {{ question.type === 0 ? '选择题' : '填空题' }}
              </el-tag>
              <span class="question-score">{{ question.score }} 分</span>
            </div>
            <p class="question-title">{{ question.title }}</p>

            <template v-if="question.type === 0">
              <div class="options-list">
                <label
                  v-for="(option, oi) in parseOptions(question.options)"
                  :key="oi"
                  class="option-item"
                  :class="{ selected: answers[question.id] === String.fromCharCode(65 + oi) }"
                >
                  <input
                    type="radio"
                    :name="'q-' + question.id"
                    :value="String.fromCharCode(65 + oi)"
                    :checked="answers[question.id] === String.fromCharCode(65 + oi)"
                    @change="selectOption(question.id, String.fromCharCode(65 + oi))"
                  />
                  <span class="option-label">{{ String.fromCharCode(65 + oi) }}</span>
                  <span class="option-text">{{ option }}</span>
                </label>
              </div>
            </template>

            <template v-else>
              <div class="fill-answer">
                <el-input
                  :model-value="answers[question.id] || ''"
                  placeholder="请输入答案"
                  @input="val => setFillAnswer(question.id, val)"
                />
              </div>
            </template>
          </div>
        </div>

        <div class="exam-footer">
          <div class="pagination-row">
            <el-button :disabled="currentPage <= 1" @click="prevPage">上一页</el-button>
            <span class="page-indicator">第 {{ currentPage }} / {{ totalPages }} 页</span>
            <el-button :disabled="currentPage >= totalPages" @click="nextPage">下一页</el-button>
          </div>
          <el-button type="danger" size="large" :loading="submitting" @click="handleSubmit">交卷</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.exam-fullscreen {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.exam-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 32px;
  background: #fff;
  border-bottom: 1px solid #e8ecf0;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.exam-back {
  border: 0;
  background: #f0f2f5;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  white-space: nowrap;
}
.exam-back:hover { background: #e4e8ed; }

.exam-header-title {
  flex: 1;
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a2332;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.exam-header-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.exam-timer {
  font-size: 22px;
  font-weight: 800;
  color: #1677ff;
  font-variant-numeric: tabular-nums;
  padding: 4px 14px;
  background: #eaf3ff;
  border-radius: 8px;
}
.exam-timer.time-low { color: #e82a2a; background: #fff0f0; animation: pulse 1s infinite; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .6; }
}

.exam-submitted-tag {
  font-size: 16px;
  font-weight: 700;
  color: #24a148;
  padding: 4px 14px;
  background: #e8f5e9;
  border-radius: 8px;
}

.exam-score {
  font-size: 14px;
  color: #667;
}

.exam-body {
  flex: 1;
  padding: 24px 32px;
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.exam-welcome {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.exam-welcome-card {
  width: 100%;
  max-width: 600px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,.06);
}

.exam-welcome-card h2 {
  margin: 0 0 20px;
  font-size: 24px;
  color: #1a2332;
}

.exam-meta-list {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.exam-meta-list li {
  font-size: 14px;
  color: #556;
  padding: 8px 12px;
  background: #f8f9fb;
  border-radius: 8px;
}

.exam-rules {
  margin-bottom: 28px;
}

.exam-rules h3 {
  font-size: 16px;
  color: #1a2332;
  margin: 0 0 10px;
}

.exam-rules ol {
  padding-left: 20px;
  margin: 0;
}

.exam-rules li {
  font-size: 14px;
  color: #556;
  line-height: 1.8;
}

.start-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.exam-result {
  display: flex;
  justify-content: center;
  padding-top: 80px;
}

.exam-result-card {
  text-align: center;
  background: #fff;
  border-radius: 16px;
  padding: 48px 60px;
  box-shadow: 0 4px 24px rgba(0,0,0,.06);
}

.result-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #24a148;
  color: #fff;
  font-size: 32px;
  display: grid;
  place-items: center;
  margin: 0 auto 16px;
}

.exam-result-card h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1a2332;
}

.exam-result-card p {
  margin: 0 0 6px;
  color: #667;
  font-size: 14px;
}

.result-summary {
  margin-bottom: 24px !important;
  font-weight: 600;
  color: #1a2332 !important;
}

.exam-progress-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
}

.exam-progress-bar span {
  font-size: 13px;
  color: #667;
  white-space: nowrap;
}

.exam-progress-bar .el-progress {
  flex: 1;
}

.exam-questions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.exam-question-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  break-inside: avoid;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.question-number {
  font-size: 13px;
  font-weight: 700;
  color: #1677ff;
}

.question-score {
  margin-left: auto;
  font-size: 13px;
  color: #98a2b3;
}

.question-title {
  margin: 0 0 16px;
  font-size: 15px;
  color: #1a2332;
  line-height: 1.7;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  cursor: pointer;
  transition: .15s;
}

.option-item:hover {
  border-color: #1677ff;
  background: #f5f9ff;
}

.option-item.selected {
  border-color: #1677ff;
  background: #eaf3ff;
}

.option-item input {
  display: none;
}

.option-label {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid #d0d5dd;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 600;
  color: #556;
  flex-shrink: 0;
}

.option-item.selected .option-label {
  background: #1677ff;
  border-color: #1677ff;
  color: #fff;
}

.option-text {
  font-size: 14px;
  color: #333;
}

.fill-answer {
  margin-top: 8px;
}

.exam-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  position: sticky;
  bottom: 16px;
}

.pagination-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-indicator {
  font-size: 14px;
  color: #667;
}
</style>
