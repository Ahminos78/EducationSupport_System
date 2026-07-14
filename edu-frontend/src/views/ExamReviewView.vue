<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getExamDetail, getExamAttemptDetail, listExamAttempts, listExamQuestions } from '../api/assessment'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)
const courseId = Number(route.params.courseId)
const attemptId = Number(route.params.attemptId)

const exam = ref(null)
const questions = ref([])
const attempt = ref(null)
const loading = ref(true)

function parseOptions(optionsStr) {
  if (!optionsStr) return []
  try { return JSON.parse(optionsStr) } catch { return [] }
}

function parseAnswers(answerContent) {
  if (!answerContent) return {}
  try {
    const arr = JSON.parse(answerContent)
    const map = {}
    arr.forEach(item => { map[item.questionId] = item.answer })
    return map
  } catch { return {} }
}

const answers = computed(() => attempt.value ? parseAnswers(attempt.value.answerContent) : {})
const studentName = computed(() => attempt.value?.studentName || '未知')
const studentScore = computed(() => attempt.value?.score != null ? `${attempt.value.score} 分` : '未批改')
const attemptStatus = computed(() => {
  const s = attempt.value?.status
  if (s === 0) return '进行中'
  if (s === 1) return '已交卷'
  if (s === 2) return '已批改'
  return '未知'
})

const totalPossibleScore = computed(() => questions.value.reduce((s, q) => s + (q.score || 0), 0))

function isCorrect(questionId, answer) {
  const q = questions.value.find(x => x.id === questionId)
  if (!q || !q.answer) return null
  return q.answer.trim().toLowerCase() === (answer || '').trim().toLowerCase()
}

function goBack() {
  router.push(`/courses/${courseId}`)
}

onMounted(async () => {
  loading.value = true
  try {
    const [examData, questionData, attemptData] = await Promise.all([
      getExamDetail(examId),
      listExamQuestions(examId, true),
      getExamAttemptDetail(attemptId),
    ])
    exam.value = examData
    questions.value = questionData || []
    attempt.value = attemptData
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="review-page">
    <header class="review-header">
      <button class="review-back" @click="goBack">&larr; 返回课程</button>
      <h1>{{ exam?.title || '试卷批阅' }}</h1>
      <div class="review-meta">
        <span><strong>学生：</strong>{{ studentName }}</span>
        <span><strong>状态：</strong>{{ attemptStatus }}</span>
        <span><strong>得分：</strong>{{ studentScore }} / {{ totalPossibleScore }} 分</span>
      </div>
    </header>

    <div v-loading="loading" class="review-body">
      <div v-for="(q, idx) in questions" :key="q.id" class="review-question" :class="{ correct: isCorrect(q.id, answers[q.id]) === true, incorrect: isCorrect(q.id, answers[q.id]) === false }">
        <div class="rq-head">
          <span class="rq-num">{{ idx + 1 }}</span>
          <el-tag size="small" :type="q.type === 0 ? 'primary' : 'success'" effect="plain">
            {{ q.type === 0 ? '选择题' : '填空题' }}
          </el-tag>
          <span class="rq-score">{{ q.score }} 分</span>
        </div>
        <p class="rq-title">{{ q.title }}</p>

        <template v-if="q.type === 0">
          <div class="rq-options">
            <div v-for="(opt, oi) in parseOptions(q.options)" :key="oi" class="rq-option"
              :class="{
                'is-correct': q.answer === String.fromCharCode(65 + oi),
                'is-wrong': answers[q.id] === String.fromCharCode(65 + oi) && q.answer !== String.fromCharCode(65 + oi),
              }"
            >
              <span class="rq-opt-letter">{{ String.fromCharCode(65 + oi) }}</span>
              <span>{{ opt }}</span>
              <el-tag v-if="q.answer === String.fromCharCode(65 + oi)" size="small" type="success" effect="dark">正确答案</el-tag>
              <el-tag v-if="answers[q.id] === String.fromCharCode(65 + oi) && q.answer !== String.fromCharCode(65 + oi)" size="small" type="danger" effect="dark">学生选择</el-tag>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="rq-fill">
            <p><strong>正确答案：</strong>{{ q.answer }}</p>
            <p><strong>学生答案：</strong>
              <span :class="{ 'answer-correct': isCorrect(q.id, answers[q.id]) === true, 'answer-wrong': isCorrect(q.id, answers[q.id]) === false }">
                {{ answers[q.id] || '(未作答)' }}
              </span>
            </p>
          </div>
        </template>

        <div class="rq-result">
          <el-tag v-if="isCorrect(q.id, answers[q.id]) === true" size="small" type="success">正确</el-tag>
          <el-tag v-else-if="isCorrect(q.id, answers[q.id]) === false" size="small" type="danger">错误</el-tag>
          <el-tag v-else size="small" type="info">未批改</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.review-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 32px;
  background: #fff;
  border-bottom: 1px solid #e8ecf0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.review-back {
  border: 0;
  background: #f0f2f5;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
}
.review-back:hover { background: #e4e8ed; }

.review-header h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a2332;
}

.review-meta {
  margin-left: auto;
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #667;
}

.review-body {
  flex: 1;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.review-question {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  border-left: 4px solid transparent;
}
.review-question.correct { border-left-color: #24a148; }
.review-question.incorrect { border-left-color: #e82a2a; }

.rq-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.rq-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f0f2f5;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
}

.rq-score { margin-left: auto; font-size: 13px; color: #98a2b3; }

.rq-title {
  margin: 0 0 14px;
  font-size: 15px;
  color: #1a2332;
  line-height: 1.7;
}

.rq-options { display: flex; flex-direction: column; gap: 6px; }

.rq-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  font-size: 14px;
}
.rq-option.is-correct { border-color: #24a148; background: #e8f5e9; }
.rq-option.is-wrong { border-color: #e82a2a; background: #fff0f0; }

.rq-opt-letter {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid #d0d5dd;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.rq-option.is-correct .rq-opt-letter { background: #24a148; border-color: #24a148; color: #fff; }
.rq-option.is-wrong .rq-opt-letter { background: #e82a2a; border-color: #e82a2a; color: #fff; }

.rq-fill p { margin: 6px 0; font-size: 14px; color: #333; }
.answer-correct { color: #24a148; font-weight: 600; }
.answer-wrong { color: #e82a2a; font-weight: 600; }

.rq-result { margin-top: 12px; }
</style>
