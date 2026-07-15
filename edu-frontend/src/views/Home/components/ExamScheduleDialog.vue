<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../../stores/auth'
import { listExams, listMyExamAttempts } from '../../../api/assessment'
import { useScheduleData } from './useScheduleData'

const props = defineProps({ visible: { type: Boolean, default: false } })
const emit = defineEmits(['update:visible'])
const router = useRouter()
const authStore = useAuthStore()
const { courseMap, loading: baseLoading, loadBaseData } = useScheduleData()

const exams = ref([])
const attemptMap = ref({})
const loading = ref(false)
const activeFilter = ref('all')
const currentMonth = ref(new Date())
const selectedDate = ref(null)

const statusStyles = {
  upcoming: { label: '未开始', color: '#722ed1', soft: '#f4edff' },
  ongoing:  { label: '进行中', color: '#fa8c16', soft: '#fff3e6' },
  ended:    { label: '已结束', color: '#98a2b3', soft: '#f4f6f8' },
  done:     { label: '已完成', color: '#18a875', soft: '#e6fff5' },
}

const monthLabel = computed(() => `${currentMonth.value.getFullYear()}年 ${currentMonth.value.getMonth() + 1}月`)

const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const firstWeekday = new Date(year, month, 1).getDay()
  const mondayOffset = firstWeekday === 0 ? 6 : firstWeekday - 1
  const start = new Date(year, month, 1 - mondayOffset)
  return Array.from({ length: 42 }, (_, i) => {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    return date
  })
})

function sameDay(a, b) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

function isOutsideMonth(date) {
  return date.getMonth() !== currentMonth.value.getMonth()
}

function getExamStatus(exam) {
  const now = new Date()
  const a = attemptMap.value[exam.id]
  if (a?.status >= 1) return 'done'
  if (new Date(exam.startTime) > now) return 'upcoming'
  if (new Date(exam.endTime) >= now) return 'ongoing'
  return 'ended'
}

function examStatusLabel(exam) {
  const key = getExamStatus(exam)
  if (key === 'done') {
    const a = attemptMap.value[exam.id]
    return a?.score != null ? `${a.score} 分` : '已批改'
  }
  return statusStyles[key].label
}

function examColor(exam) {
  return statusStyles[getExamStatus(exam)].color
}

function examSoft(exam) {
  return statusStyles[getExamStatus(exam)].soft
}

function examCountOnDate(date) {
  return exams.value.filter(e => sameDay(new Date(e.startTime), date)).length
}

function filteredExamsByDate(date) {
  if (!date) return filteredExams.value
  return filteredExams.value.filter(e => sameDay(new Date(e.startTime), date))
}

const pendingCount = computed(() => exams.value.filter(e => getExamStatus(e) === 'upcoming' || getExamStatus(e) === 'ongoing').length)

const doneCount = computed(() => exams.value.filter(e => getExamStatus(e) === 'done').length)

const filteredExams = computed(() => {
  let list = exams.value
  if (activeFilter.value === 'pending') list = list.filter(e => { const s = getExamStatus(e); return s === 'upcoming' || s === 'ongoing' })
  else if (activeFilter.value === 'done') list = list.filter(e => getExamStatus(e) === 'done')
  return [...list].sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
})

const displayExams = computed(() => filteredExamsByDate(selectedDate.value))

function changeMonth(offset) {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + offset, 1)
  selectedDate.value = null
}

function chooseDate(date) {
  selectedDate.value = selectedDate.value && sameDay(selectedDate.value, date) ? null : date
}

function focusNearestMonth() {
  const now = new Date()
  const nearest = [...exams.value]
    .filter(e => new Date(e.startTime) >= now)
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))[0]
  currentMonth.value = nearest ? new Date(nearest.startTime) : new Date()
}

function formatDate(v) {
  if (!v) return '--'
  return String(v).replace('T', ' ').slice(0, 16)
}

function enterExam(exam) {
  emit('update:visible', false)
  router.push(`/courses/${exam.courseId || 0}/exams/${exam.id}`)
}

async function loadData() {
  if (authStore.user?.role !== 1) return
  loading.value = true
  try {
    const approved = await loadBaseData()
    const allExams = []
    for (const e of approved) {
      try {
        const list = await listExams(e.courseId)
        if (list?.length) {
          allExams.push(...list.map(ex => ({ ...ex, courseName: e.courseName || courseMap.value.get(e.courseId)?.name, courseId: e.courseId })))
        }
      } catch { /* skip */ }
    }
    exams.value = allExams
    const attempts = (await listMyExamAttempts()) || []
    const map = {}
    attempts.forEach(a => { map[a.examId] = a })
    attemptMap.value = map
    focusNearestMonth()
  } catch {
    exams.value = []
    attemptMap.value = {}
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, v => { if (v) { selectedDate.value = null; activeFilter.value = 'all'; loadData() } })
</script>

<template>
  <el-dialog
    :model-value="visible"
    class="exam-dialog"
    width="min(1240px, 94vw)"
    :show-close="false"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-tabs">
          <button :class="{ active: activeFilter === 'all' }" @click="activeFilter = 'all'">全部考试【{{ exams.length }}项】</button>
          <button :class="{ active: activeFilter === 'pending' }" @click="activeFilter = 'pending'">待参加【{{ pendingCount }}项】</button>
          <button :class="{ active: activeFilter === 'done' }" @click="activeFilter = 'done'">已完成【{{ doneCount }}项】</button>
        </div>
        <button class="dialog-close" aria-label="关闭" @click="$emit('update:visible', false)">×</button>
      </div>
    </template>

    <el-skeleton v-if="loading || baseLoading" :rows="8" animated />
    <el-empty v-else-if="authStore.user?.role !== 1" description="考试功能仅对学生开放" />
    <div v-else class="dialog-layout">
      <aside class="calendar-panel">
        <div class="calendar-header">
          <button @click="changeMonth(-1)">‹</button>
          <strong>{{ monthLabel }}</strong>
          <button @click="changeMonth(1)">›</button>
        </div>
        <div class="calendar-weekdays"><span v-for="day in ['一', '二', '三', '四', '五', '六', '日']" :key="day">{{ day }}</span></div>
        <div class="calendar-grid">
          <button
            v-for="date in calendarDays"
            :key="date.toISOString()"
            :class="{ outside: isOutsideMonth(date), today: sameDay(date, new Date()), selected: selectedDate && sameDay(date, selectedDate), marked: examCountOnDate(date) }"
            @click="chooseDate(date)"
          >
            {{ date.getDate() }}
            <i v-if="examCountOnDate(date)">{{ examCountOnDate(date) }}</i>
          </button>
        </div>
        <div class="calendar-footer"><button @click="selectedDate = null; currentMonth = new Date()">今天</button><span>有圆点日期为考试日</span></div>
      </aside>

      <main class="exam-panel">
        <div class="exam-toolbar">
          <div><h3>{{ selectedDate ? `${selectedDate.getMonth() + 1}月${selectedDate.getDate()}日 考试` : '考试安排' }}</h3><p>点击卡片可直接进入考试</p></div>
          <button v-if="selectedDate" @click="selectedDate = null">清除日期筛选</button>
        </div>
        <div v-if="displayExams.length" class="exam-grid">
          <article
            v-for="item in displayExams"
            :key="item.id"
            class="exam-card"
            :style="{ '--card-color': examColor(item), '--card-soft': examSoft(item) }"
            @click="enterExam(item)"
          >
            <div class="card-topline"><span>{{ examStatusLabel(item) }}</span><em>{{ item.duration || 60 }}分钟</em></div>
            <p>{{ item.courseName }}</p>
            <h4>{{ item.title }}</h4>
            <div class="card-time">
              <span>{{ formatDate(item.startTime) }}</span>
              <strong>~</strong>
              <span>{{ formatDate(item.endTime) }}</span>
            </div>
          </article>
        </div>
        <el-empty v-else description="当前筛选条件下暂无考试" />
      </main>
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
:deep(.exam-dialog) { border-radius: 18px; overflow: hidden; }
:deep(.exam-dialog .el-dialog__header) { padding: 0; margin: 0; }
:deep(.exam-dialog .el-dialog__body) { padding: 24px 28px 30px; }
.dialog-header { display: flex; align-items: center; justify-content: space-between; padding: 0 18px 0 28px; border-bottom: 1px solid #edf0f5; }
.dialog-tabs { display: flex; gap: 30px; }.dialog-tabs button { position: relative; padding: 22px 2px 18px; border: 0; background: transparent; color: #667085; cursor: pointer; font-size: 15px; font-weight: 600; }.dialog-tabs button.active { color: #722ed1; }.dialog-tabs button.active::after { content: ''; position: absolute; right: 0; bottom: -1px; left: 0; height: 4px; border-radius: 4px 4px 0 0; background: #722ed1; }
.dialog-close { width: 38px; height: 38px; border: 0; border-radius: 50%; background: #4b5563; color: #fff; cursor: pointer; font-size: 27px; line-height: 1; box-shadow: 0 4px 12px rgba(0,0,0,.18); }
.dialog-layout { display: grid; grid-template-columns: 330px minmax(0, 1fr); gap: 28px; min-height: 500px; }
.calendar-panel { align-self: start; overflow: hidden; border: 1px solid #e8ecf1; border-radius: 14px; background: #fff; box-shadow: 0 6px 24px rgba(31,45,61,.08); }
.calendar-header { display: flex; align-items: center; justify-content: space-between; padding: 22px 20px; border-bottom: 1px solid #edf0f5; }.calendar-header strong { color: #344054; font-size: 18px; }.calendar-header button { width: 34px; height: 34px; border: 0; border-radius: 8px; background: transparent; color: #98a2b3; cursor: pointer; font-size: 24px; }.calendar-header button:hover { color: #722ed1; background: #f4edff; }
.calendar-weekdays, .calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); }.calendar-weekdays { padding: 15px 14px 7px; color: #98a2b3; font-size: 12px; text-align: center; }.calendar-grid { gap: 4px; padding: 5px 14px 18px; }.calendar-grid button { position: relative; aspect-ratio: 1; border: 0; border-radius: 9px; background: transparent; color: #475467; cursor: pointer; }.calendar-grid button:hover { background: #f4edff; }.calendar-grid button.outside { color: #c5cad3; }.calendar-grid button.today { color: #722ed1; font-weight: 700; box-shadow: inset 0 0 0 1px #b37feb; }.calendar-grid button.selected { color: #fff; background: #722ed1; }.calendar-grid button i { position: absolute; right: 3px; bottom: 2px; display: grid; place-items: center; min-width: 15px; height: 15px; padding: 0 3px; border-radius: 8px; background: #fa8c16; color: #fff; font-size: 9px; font-style: normal; }.calendar-grid button.selected i { background: #fff; color: #722ed1; }
.calendar-footer { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-top: 1px solid #edf0f5; color: #98a2b3; font-size: 11px; }.calendar-footer button { border: 0; background: transparent; color: #722ed1; cursor: pointer; }
.exam-panel { min-width: 0; }.exam-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }.exam-toolbar h3 { margin: 0; color: #182230; font-size: 18px; }.exam-toolbar p { margin: 5px 0 0; color: #98a2b3; font-size: 12px; }.exam-toolbar > button { border: 0; background: transparent; color: #722ed1; cursor: pointer; }
.exam-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; max-height: 480px; padding: 2px 4px 8px 2px; overflow-y: auto; }
.exam-card { position: relative; overflow: hidden; min-height: 178px; padding: 19px 20px; border: 1px solid #e9edf3; border-radius: 14px; background: #fff; cursor: pointer; transition: transform .2s, box-shadow .2s, border-color .2s; }.exam-card::before { content: ''; position: absolute; top: 0; right: 0; left: 0; height: 5px; background: var(--card-color); }.exam-card:hover { transform: translateY(-3px); border-color: var(--card-color); box-shadow: 0 10px 24px rgba(31,45,61,.1); }
.card-topline { display: flex; align-items: center; justify-content: space-between; }.card-topline span { padding: 4px 9px; border-radius: 12px; color: var(--card-color); background: var(--card-soft); font-size: 11px; font-weight: 600; }.card-topline em { color: #98a2b3; font-size: 11px; font-style: normal; }
.exam-card > p { margin: 17px 0 6px; color: #98a2b3; font-size: 12px; }.exam-card h4 { margin: 0; overflow: hidden; color: #27364b; font-size: 16px; line-height: 1.5; text-overflow: ellipsis; white-space: nowrap; }
.card-time { display: flex; align-items: center; gap: 6px; margin-top: 22px; padding-top: 13px; border-top: 1px solid #f0f2f5; }.card-time span { color: #667085; font-size: 12px; }.card-time strong { color: #98a2b3; font-size: 12px; }
@media (max-width: 900px) { .dialog-layout { grid-template-columns: 1fr; }.calendar-panel { width: 100%; }.exam-grid { max-height: none; } }
@media (max-width: 620px) { .exam-grid { grid-template-columns: 1fr; }.dialog-tabs { gap: 12px; }.dialog-tabs button { font-size: 12px; } }
</style>
