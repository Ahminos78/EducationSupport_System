<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listAssignments, listMySubmissions } from '../../../api/assessment'
import { useAuthStore } from '../../../stores/auth'
import { useScheduleData } from './useScheduleData'

const props = defineProps({
  visible: { type: Boolean, default: false },
})

const emit = defineEmits(['update:visible'])
const router = useRouter()
const authStore = useAuthStore()
const { courseMap, loading: baseLoading, loadBaseData } = useScheduleData()

const assignments = ref([])
const submissions = ref(new Map())
const loading = ref(false)
const activeFilter = ref('all')
const currentMonth = ref(new Date())
const selectedDate = ref(null)

const categoryStyles = {
  必修: { color: '#1677ff', soft: '#eaf3ff' },
  选修: { color: '#13a8a8', soft: '#e6fffb' },
  通识: { color: '#722ed1', soft: '#f4edff' },
  个性课程: { color: '#fa8c16', soft: '#fff3e6' },
}

const monthLabel = computed(() => `${currentMonth.value.getFullYear()}年 ${currentMonth.value.getMonth() + 1}月`)

const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const firstWeekday = new Date(year, month, 1).getDay()
  const mondayOffset = firstWeekday === 0 ? 6 : firstWeekday - 1
  const start = new Date(year, month, 1 - mondayOffset)
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return date
  })
})

const filteredAssignments = computed(() => {
  let list = assignments.value
  if (activeFilter.value === 'pending') {
    list = list.filter((item) => !submissions.value.has(item.id) && new Date(item.deadline) >= new Date())
  } else if (activeFilter.value === 'completed') {
    list = list.filter((item) => submissions.value.has(item.id))
  }
  if (selectedDate.value) {
    list = list.filter((item) => sameDay(new Date(item.deadline), selectedDate.value))
  }
  return [...list].sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
})

const pendingCount = computed(() => assignments.value.filter(
  (item) => !submissions.value.has(item.id) && new Date(item.deadline) >= new Date(),
).length)

watch(() => props.visible, (visible) => {
  if (visible) {
    selectedDate.value = null
    activeFilter.value = 'all'
    loadData()
  }
})

async function loadData() {
  if (authStore.user?.role !== 1) return
  loading.value = true
  try {
    const approved = await loadBaseData()
    const submissionList = await listMySubmissions().catch(() => [])
    const results = await Promise.all([...courseMap.value.keys()].map(async (courseId) => {
      try {
        return await listAssignments(courseId)
      } catch {
        return []
      }
    }))
    assignments.value = results.flat().map((item) => ({
      ...item,
      courseName: item.courseName || courseMap.value.get(item.courseId)?.name,
      category: courseMap.value.get(item.courseId)?.category || '其他',
    }))
    submissions.value = new Map((submissionList || []).map((item) => [item.assignmentId, item]))
    focusNearestMonth()
  } catch (error) {
    assignments.value = []
    submissions.value = new Map()
    ElMessage.error(error.message || '作业列表加载失败')
  } finally {
    loading.value = false
  }
}

function focusNearestMonth() {
  const nearest = [...assignments.value]
    .filter((item) => new Date(item.deadline) >= new Date())
    .sort((a, b) => new Date(a.deadline) - new Date(b.deadline))[0]
  currentMonth.value = nearest ? new Date(nearest.deadline) : new Date()
}

function changeMonth(offset) {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + offset, 1)
  selectedDate.value = null
}

function chooseDate(date) {
  selectedDate.value = selectedDate.value && sameDay(selectedDate.value, date) ? null : date
}

function sameDay(left, right) {
  return left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate()
}

function deadlineCount(date) {
  return assignments.value.filter((item) => sameDay(new Date(item.deadline), date)).length
}

function isOutsideMonth(date) {
  return date.getMonth() !== currentMonth.value.getMonth()
}

function cardStyle(item) {
  return categoryStyles[item.category] || { color: '#64748b', soft: '#f1f5f9' }
}

function submissionLabel(item) {
  const submission = submissions.value.get(item.id)
  if (submission?.gradingStatus === 1) return `已批改 · ${submission.score ?? '--'}分`
  if (submission) return '已提交'
  if (new Date(item.deadline) < new Date()) return '已截止'
  return '待完成'
}

function openHomework(item) {
  emit('update:visible', false)
  router.push(`/courses/${item.courseId}/homework/${item.id}`)
}

function formatDeadline(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    class="homework-dialog"
    width="min(1240px, 94vw)"
    :show-close="false"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <template #header>
      <div class="dialog-header">
        <div class="dialog-tabs">
          <button :class="{ active: activeFilter === 'all' }" @click="activeFilter = 'all'">全部作业【{{ assignments.length }}项】</button>
          <button :class="{ active: activeFilter === 'pending' }" @click="activeFilter = 'pending'">待完成【{{ pendingCount }}项】</button>
          <button :class="{ active: activeFilter === 'completed' }" @click="activeFilter = 'completed'">已提交【{{ submissions.size }}项】</button>
        </div>
        <button class="dialog-close" aria-label="关闭" @click="$emit('update:visible', false)">×</button>
      </div>
    </template>

    <el-skeleton v-if="loading || baseLoading" :rows="8" animated />
    <el-empty v-else-if="authStore.user?.role !== 1" description="我的作业仅对学生开放" />
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
            :class="{ outside: isOutsideMonth(date), today: sameDay(date, new Date()), selected: selectedDate && sameDay(date, selectedDate), marked: deadlineCount(date) }"
            @click="chooseDate(date)"
          >
            {{ date.getDate() }}
            <i v-if="deadlineCount(date)">{{ deadlineCount(date) }}</i>
          </button>
        </div>
        <div class="calendar-footer"><button @click="selectedDate = null; currentMonth = new Date()">今天</button><span>有圆点日期为作业截止日</span></div>
      </aside>

      <main class="assignment-panel">
        <div class="assignment-toolbar">
          <div><h3>{{ selectedDate ? `${selectedDate.getMonth() + 1}月${selectedDate.getDate()}日截止` : '课程作业' }}</h3><p>点击作业卡片可直接进入提交页面</p></div>
          <button v-if="selectedDate" @click="selectedDate = null">清除日期筛选</button>
        </div>
        <div v-if="filteredAssignments.length" class="assignment-grid">
          <article
            v-for="item in filteredAssignments"
            :key="item.id"
            class="assignment-card"
            :style="{ '--category-color': cardStyle(item).color, '--category-soft': cardStyle(item).soft }"
            @click="openHomework(item)"
          >
            <div class="card-topline"><span>{{ item.category }}</span><em>{{ submissionLabel(item) }}</em></div>
            <p>{{ item.courseName }}</p>
            <h4>{{ item.title }}</h4>
            <div class="card-deadline"><span>截止时间</span><strong>{{ formatDeadline(item.deadline) }}</strong></div>
          </article>
        </div>
        <el-empty v-else description="当前筛选条件下暂无作业" />
      </main>
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
:deep(.homework-dialog) { border-radius: 18px; overflow: hidden; }
:deep(.homework-dialog .el-dialog__header) { padding: 0; margin: 0; }
:deep(.homework-dialog .el-dialog__body) { padding: 24px 28px 30px; }
.dialog-header { display: flex; align-items: center; justify-content: space-between; padding: 0 18px 0 28px; border-bottom: 1px solid #edf0f5; }
.dialog-tabs { display: flex; gap: 30px; }.dialog-tabs button { position: relative; padding: 22px 2px 18px; border: 0; background: transparent; color: #667085; cursor: pointer; font-size: 15px; font-weight: 600; }.dialog-tabs button.active { color: #18a875; }.dialog-tabs button.active::after { content: ''; position: absolute; right: 0; bottom: -1px; left: 0; height: 4px; border-radius: 4px 4px 0 0; background: #18b981; }
.dialog-close { width: 38px; height: 38px; border: 0; border-radius: 50%; background: #4b5563; color: #fff; cursor: pointer; font-size: 27px; line-height: 1; box-shadow: 0 4px 12px rgba(0,0,0,.18); }
.dialog-layout { display: grid; grid-template-columns: 330px minmax(0, 1fr); gap: 28px; min-height: 500px; }
.calendar-panel { align-self: start; overflow: hidden; border: 1px solid #e8ecf1; border-radius: 14px; background: #fff; box-shadow: 0 6px 24px rgba(31,45,61,.08); }
.calendar-header { display: flex; align-items: center; justify-content: space-between; padding: 22px 20px; border-bottom: 1px solid #edf0f5; }.calendar-header strong { color: #344054; font-size: 18px; }.calendar-header button { width: 34px; height: 34px; border: 0; border-radius: 8px; background: transparent; color: #98a2b3; cursor: pointer; font-size: 24px; }.calendar-header button:hover { color: #1677ff; background: #f0f6ff; }
.calendar-weekdays, .calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); }.calendar-weekdays { padding: 15px 14px 7px; color: #98a2b3; font-size: 12px; text-align: center; }.calendar-grid { gap: 4px; padding: 5px 14px 18px; }.calendar-grid button { position: relative; aspect-ratio: 1; border: 0; border-radius: 9px; background: transparent; color: #475467; cursor: pointer; }.calendar-grid button:hover { background: #f1f7ff; }.calendar-grid button.outside { color: #c5cad3; }.calendar-grid button.today { color: #1677ff; font-weight: 700; box-shadow: inset 0 0 0 1px #91caff; }.calendar-grid button.selected { color: #fff; background: #1677ff; }.calendar-grid button i { position: absolute; right: 3px; bottom: 2px; display: grid; place-items: center; min-width: 15px; height: 15px; padding: 0 3px; border-radius: 8px; background: #fa8c16; color: #fff; font-size: 9px; font-style: normal; }.calendar-grid button.selected i { background: #fff; color: #1677ff; }
.calendar-footer { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-top: 1px solid #edf0f5; color: #98a2b3; font-size: 11px; }.calendar-footer button { border: 0; background: transparent; color: #18a875; cursor: pointer; }
.assignment-panel { min-width: 0; }.assignment-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }.assignment-toolbar h3 { margin: 0; color: #182230; font-size: 18px; }.assignment-toolbar p { margin: 5px 0 0; color: #98a2b3; font-size: 12px; }.assignment-toolbar > button { border: 0; background: transparent; color: #1677ff; cursor: pointer; }
.assignment-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; max-height: 480px; padding: 2px 4px 8px 2px; overflow-y: auto; }
.assignment-card { position: relative; overflow: hidden; min-height: 178px; padding: 19px 20px; border: 1px solid #e9edf3; border-radius: 14px; background: #fff; cursor: pointer; transition: transform .2s, box-shadow .2s, border-color .2s; }.assignment-card::before { content: ''; position: absolute; top: 0; right: 0; left: 0; height: 5px; background: var(--category-color); }.assignment-card:hover { transform: translateY(-3px); border-color: var(--category-color); box-shadow: 0 10px 24px rgba(31,45,61,.1); }
.card-topline { display: flex; align-items: center; justify-content: space-between; }.card-topline span { padding: 4px 9px; border-radius: 12px; color: var(--category-color); background: var(--category-soft); font-size: 11px; font-weight: 600; }.card-topline em { color: #98a2b3; font-size: 11px; font-style: normal; }.assignment-card > p { margin: 17px 0 6px; color: #98a2b3; font-size: 12px; }.assignment-card h4 { margin: 0; overflow: hidden; color: #27364b; font-size: 16px; line-height: 1.5; text-overflow: ellipsis; white-space: nowrap; }.card-deadline { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-top: 22px; padding-top: 13px; border-top: 1px solid #f0f2f5; }.card-deadline span { color: #98a2b3; font-size: 11px; }.card-deadline strong { color: #667085; font-size: 12px; }
@media (max-width: 900px) { .dialog-layout { grid-template-columns: 1fr; }.calendar-panel { width: 100%; }.assignment-grid { max-height: none; } }
@media (max-width: 620px) { .assignment-grid { grid-template-columns: 1fr; }.dialog-tabs { gap: 12px; }.dialog-tabs button { font-size: 12px; } }
</style>
