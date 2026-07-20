<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { autoGenerateQuestions, createExamWithQuestions, deleteExam, getExamDetail, listExamQuestions, updateExamWithQuestions } from '../api/assessment'
import { aiGenerateQuestions } from '../api/ai'

const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.courseId)
const examId = route.params.examId ? Number(route.params.examId) : null
const courseName = route.query.courseName || ''
const isEdit = !!examId

const examInfo = reactive({
  title: '',
  description: '',
  startTime: null,
  endTime: null,
  fullScore: 100,
  duration: 60,
})

const questions = ref([])
const publishing = ref(false)
const deleting = ref(false)
const generating = ref(false)
const loading = ref(isEdit)
const loadError = ref('')
const activeQuestion = ref(-1)

onMounted(async () => {
  if (isEdit && examId) {
    const [examResult, questionResult] = await Promise.allSettled([
        getExamDetail(examId),
        listExamQuestions(examId, true),
    ])
    if (examResult.status === 'fulfilled') {
      const exam = examResult.value
      examInfo.title = exam.title || ''
      examInfo.description = exam.description || ''
      examInfo.startTime = exam.startTime || null
      examInfo.endTime = exam.endTime || null
      examInfo.duration = exam.duration || 60
    } else {
      loadError.value = examResult.reason?.message || '考试信息加载失败'
    }
    if (questionResult.status === 'fulfilled') {
      const qs = questionResult.value
      questions.value = (qs || []).map(q => ({
        id: q.id,
        type: q.type,
        title: q.title || '',
        options: q.type === 0 ? parseOptions(q.options) : [],
        answer: q.answer || '',
        score: q.score || 10,
      }))
      if (questions.value.length) activeQuestion.value = 0
    } else {
      const questionError = questionResult.reason?.message || '试卷题目加载失败'
      loadError.value = loadError.value ? `${loadError.value}；${questionError}` : questionError
    }
    if (loadError.value) ElMessage.error(loadError.value)
    loading.value = false
  }
})

function parseOptions(str) {
  if (!str) return []
  try { return JSON.parse(str) } catch { return [] }
}

const totalScore = computed(() => questions.value.reduce((sum, q) => sum + (q.score || 0), 0))

function addQuestion(type) {
  questions.value.push({
    id: Date.now(),
    type,
    title: '',
    options: type === 0 ? ['', '', '', ''] : [],
    answer: '',
    score: 10,
  })
  activeQuestion.value = questions.value.length - 1
}

function removeQuestion(idx) {
  questions.value.splice(idx, 1)
  if (activeQuestion.value >= questions.value.length) {
    activeQuestion.value = questions.value.length - 1
  }
}

async function autoGenerate() {
  generating.value = true
  try {
    let data
    try {
      data = await aiGenerateQuestions({ courseId, courseName, count: 10 })
    } catch {
      data = null
    }
    if (!data || !data.length) {
      data = await autoGenerateQuestions(courseId, 10)
    }
    if (!data || !data.length) {
      ElMessage.info('暂无匹配的题目模板')
      return
    }
    data.forEach((q, i) => {
      let options = []
      if (q.options) {
        try { options = JSON.parse(q.options) } catch { options = [] }
      }
      questions.value.push({
        id: Date.now() + i,
        type: q.type,
        title: q.title,
        options: q.type === 0 ? options : [],
        answer: q.answer || '',
        score: q.score || 10,
      })
    })
    activeQuestion.value = questions.value.length - 1
    ElMessage.success(`已自动生成 ${data.length} 道题目，请检查后发布`)
  } catch (error) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

function moveQuestion(idx, dir) {
  const target = idx + dir
  if (target < 0 || target >= questions.value.length) return
  const temp = questions.value[idx]
  questions.value[idx] = questions.value[target]
  questions.value[target] = temp
  if (activeQuestion.value === idx) activeQuestion.value = target
  else if (activeQuestion.value === target) activeQuestion.value = idx
}

function addOption(q) {
  q.options.push('')
}

function removeOption(q, oi) {
  if (q.options.length <= 2) return
  q.options.splice(oi, 1)
}

function selectCorrect(q, optionLabel) {
  q.answer = optionLabel
}

async function publish() {
  if (!examInfo.title.trim()) {
    ElMessage.warning('请输入考试标题')
    return
  }
  if (!examInfo.startTime || !examInfo.endTime) {
    ElMessage.warning('请选择考试起止时间')
    return
  }
  if (questions.value.length === 0) {
    ElMessage.warning('请至少添加一道题目')
    return
  }
  for (const [i, q] of questions.value.entries()) {
    if (!q.title.trim()) {
      ElMessage.warning(`第 ${i + 1} 题标题不能为空`)
      return
    }
    if (q.type === 0 && q.options.some(o => !o.trim())) {
      ElMessage.warning(`第 ${i + 1} 题选项不能为空`)
      return
    }
    if (!q.answer) {
      ElMessage.warning(`第 ${i + 1} 题请设置正确答案`)
      return
    }
  }
  try {
    await ElMessageBox.confirm(`确认发布考试「${examInfo.title}」？共 ${questions.value.length} 题，总分 ${totalScore.value} 分。`, '发布确认', {
      confirmButtonText: '发布',
      cancelButtonText: '返回修改',
      type: 'info',
    })
  } catch {
    return
  }
  publishing.value = true
  try {
    const payload = {
      courseId,
      title: examInfo.title,
      description: examInfo.description,
      startTime: examInfo.startTime,
      endTime: examInfo.endTime,
      fullScore: totalScore.value,
      duration: examInfo.duration,
      status: 1,
      questions: questions.value.map((q, i) => ({
        type: q.type,
        title: q.title,
        options: q.type === 0 ? JSON.stringify(q.options) : null,
        answer: q.answer,
        score: q.score,
        sortOrder: i + 1,
      })),
    }
    if (isEdit && examId) {
      await updateExamWithQuestions(examId, payload)
      ElMessage.success('考试已更新')
    } else {
      await createExamWithQuestions(payload)
      ElMessage.success('考试发布成功')
    }
    router.push(`/courses/${courseId}`)
  } catch (error) {
    ElMessage.error(error.message || (isEdit ? '保存失败' : '发布失败'))
  } finally {
    publishing.value = false
  }
}

async function deleteExamById() {
  if (!examId) return
  try {
    await ElMessageBox.confirm('确认删除该考试吗？删除后不可恢复。', '删除考试', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消',
    })
    deleting.value = true
    await deleteExam(examId)
    ElMessage.success('考试已删除')
    router.push(`/courses/${courseId}`)
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  } finally {
    deleting.value = false
  }
}

function goBack() {
  router.push(`/courses/${courseId}`)
}
</script>

<template>
  <div class="builder-page">
    <header class="builder-header">
      <button class="builder-back" @click="goBack">&larr; 返回课程</button>
      <h1>{{ isEdit ? '编辑考试' : '发布考试' }}</h1>
      <span class="builder-course">{{ courseName || `课程 ${courseId}` }}</span>
      <div class="builder-actions">
        <el-button v-if="isEdit" type="danger" :loading="deleting" size="large" @click="deleteExamById">删除考试</el-button>
        <el-button
          type="primary"
          :loading="publishing || loading"
          :disabled="isEdit && !!loadError"
          size="large"
          @click="publish"
        >
          {{ isEdit ? '保存修改' : '发布' }}
        </el-button>
      </div>
    </header>

    <div class="builder-body">
      <aside class="builder-sidebar">
        <div class="sidebar-section">
          <h3>考试信息</h3>
          <el-form label-position="top" size="small">
            <el-form-item label="考试标题">
              <el-input v-model="examInfo.title" placeholder="例：Java Web 期中考试" />
            </el-form-item>
            <el-form-item label="考试说明">
              <el-input v-model="examInfo.description" type="textarea" :rows="3" placeholder="考试范围、注意事项等" />
            </el-form-item>
            <el-form-item label="时长（分钟）">
              <el-input-number v-model="examInfo.duration" :min="1" :max="600" style="width:100%" />
            </el-form-item>
            <el-form-item label="开始时间">
              <el-date-picker v-model="examInfo.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" style="width:100%" />
            </el-form-item>
            <el-form-item label="结束时间">
              <el-date-picker v-model="examInfo.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择结束时间" style="width:100%" />
            </el-form-item>
          </el-form>
        </div>
        <div class="sidebar-section">
          <h3>题目列表</h3>
          <div class="question-toc">
            <div
              v-for="(q, i) in questions"
              :key="q.id"
              class="toc-item"
              :class="{ active: activeQuestion === i }"
              @click="activeQuestion = i"
            >
              <span class="toc-num">{{ i + 1 }}</span>
              <span class="toc-type">{{ q.type === 0 ? '选择' : '填空' }}</span>
              <span class="toc-score">{{ q.score }}分</span>
              <button class="toc-del" @click.stop="removeQuestion(i)">&times;</button>
            </div>
          </div>
          <div v-if="questions.length === 0" class="toc-empty">暂无题目</div>
          <div class="add-actions" style="display:flex;flex-direction:column;gap:8px">
            <el-button size="small" :loading="generating" @click="autoGenerate">
              &#9889; AI 智能出题
            </el-button>
            <el-dropdown>
              <el-button size="small" type="primary">+ 添加题目</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="addQuestion(0)">选择题</el-dropdown-item>
                  <el-dropdown-item @click="addQuestion(1)">填空题</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div class="sidebar-summary">
          <p>共 <strong>{{ questions.length }}</strong> 题</p>
          <p>合计 <strong>{{ totalScore }}</strong> 分</p>
        </div>
      </aside>

      <main class="builder-main">
        <div v-if="loading" class="builder-empty">
          <p>正在加载考试和试卷题目...</p>
        </div>

        <div v-else-if="loadError && questions.length === 0" class="builder-empty">
          <p>{{ loadError }}</p>
          <el-button type="primary" @click="router.go(0)">重新加载</el-button>
        </div>

        <template v-else-if="questions.length === 0">
          <div class="builder-empty">
            <p>点击左侧「添加题目」开始创建试卷</p>
          </div>
        </template>

        <div v-for="(q, idx) in questions" v-else :key="q.id" class="question-card" :class="{ active: activeQuestion === idx }" @click="activeQuestion = idx">
          <div class="q-head">
            <span class="q-badge">{{ idx + 1 }}</span>
            <el-tag size="small" :type="q.type === 0 ? 'primary' : 'success'" effect="plain">
              {{ q.type === 0 ? '选择题' : '填空题' }}
            </el-tag>
            <span class="q-score-label">分数</span>
            <el-input-number v-model="q.score" :min="1" size="small" style="width:90px" />
            <div class="q-actions">
              <el-button size="small" text :disabled="idx === 0" @click.stop="moveQuestion(idx, -1)">&#8593;</el-button>
              <el-button size="small" text :disabled="idx === questions.length - 1" @click.stop="moveQuestion(idx, 1)">&#8595;</el-button>
              <el-button size="small" text type="danger" @click.stop="removeQuestion(idx)">删除</el-button>
            </div>
          </div>
          <div class="q-body">
            <el-input v-model="q.title" type="textarea" :rows="2" placeholder="请输入题目内容" />
          </div>
          <div v-if="q.type === 0" class="q-options">
            <div v-for="(opt, oi) in q.options" :key="oi" class="option-row" :class="{ correct: q.answer === String.fromCharCode(65 + oi) }" @click.stop="selectCorrect(q, String.fromCharCode(65 + oi))">
              <span class="option-letter">{{ String.fromCharCode(65 + oi) }}</span>
              <el-input v-model="q.options[oi]" placeholder="选项内容" size="small" @click.stop />
              <el-button v-if="q.options.length > 2" size="small" text type="danger" @click.stop="removeOption(q, oi)">&times;</el-button>
            </div>
            <el-button size="small" text @click.stop="addOption(q)">+ 添加选项</el-button>
            <div class="correct-hint">
              <el-tag v-if="q.answer" size="small" type="success">正确答案：{{ q.answer }}</el-tag>
              <span v-else class="muted">点击选项设置为正确答案</span>
            </div>
          </div>
          <div v-else class="q-fill">
            <el-input v-model="q.answer" placeholder="设置正确答案" size="small" style="max-width:300px" />
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.builder-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.builder-header {
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

.builder-back {
  border: 0;
  background: #f0f2f5;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
}
.builder-back:hover { background: #e4e8ed; }

.builder-header h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a2332;
}

.builder-course {
  color: #98a2b3;
  font-size: 13px;
}

.builder-actions { margin-left: auto; }

.builder-body {
  flex: 1;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 0;
}

.builder-sidebar {
  background: #fff;
  border-right: 1px solid #e8ecf0;
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 64px);
}

.sidebar-section {
  margin-bottom: 24px;
}

.sidebar-section h3 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #1a2332;
}

.question-toc {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.toc-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  background: #f8f9fb;
  transition: .15s;
}
.toc-item:hover { background: #f0f5ff; }
.toc-item.active { background: #eaf3ff; color: #1677ff; }

.toc-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e8ecf0;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.toc-item.active .toc-num { background: #1677ff; color: #fff; }

.toc-type { font-size: 12px; }
.toc-score { margin-left: auto; font-size: 12px; color: #98a2b3; }
.toc-del { border: 0; background: none; cursor: pointer; color: #ccc; font-size: 16px; padding: 0 2px; }
.toc-del:hover { color: #e82a2a; }
.toc-empty { color: #98a2b3; font-size: 13px; padding: 12px 0; }
.add-actions { margin-top: 8px; }
.sidebar-summary { padding-top: 16px; border-top: 1px solid #e8ecf0; }
.sidebar-summary p { margin: 4px 0; font-size: 13px; color: #667; }

.builder-main {
  padding: 24px;
  overflow-y: auto;
  max-height: calc(100vh - 64px);
}

.builder-empty {
  display: grid;
  place-items: center;
  height: 300px;
  color: #98a2b3;
  font-size: 15px;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  border: 2px solid transparent;
  cursor: pointer;
  transition: .15s;
}
.question-card:hover { border-color: #d0e0ff; }
.question-card.active { border-color: #1677ff; }

.q-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.q-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #1677ff;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
}

.q-score-label { font-size: 12px; color: #98a2b3; margin-left: auto; }

.q-actions { margin-left: 8px; display: flex; gap: 2px; }

.q-body { margin-bottom: 14px; }

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: .15s;
}
.option-row:hover { border-color: #1677ff; }
.option-row.correct { border-color: #24a148; background: #e8f5e9; }

.option-letter {
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

.option-row.correct .option-letter { background: #24a148; border-color: #24a148; color: #fff; }

.correct-hint { margin-top: 8px; }
.muted { color: #98a2b3; font-size: 12px; }

.q-fill { margin-top: 8px; }
</style>
