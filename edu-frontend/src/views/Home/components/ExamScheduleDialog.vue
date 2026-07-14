<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../../stores/auth'
import { listExams, listMyExamAttempts } from '../../../api/assessment'
import { listMyEnrollments } from '../../../api/enrollment'

const props = defineProps({
  visible: { type: Boolean, default: false },
})
const emit = defineEmits(['update:visible'])
const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('upcoming')
const exams = ref([])
const attemptMap = ref({})
const loading = ref(false)

function isUpcoming(exam) {
  const now = new Date()
  return new Date(exam.startTime) > now
}

function isOngoing(exam) {
  const now = new Date()
  return new Date(exam.startTime) <= now && new Date(exam.endTime) >= now
}

function isEnded(exam) {
  return new Date(exam.endTime) < new Date()
}

const filteredExams = computed(() => {
  const list = exams.value
  if (activeTab.value === 'upcoming') return list.filter(isUpcoming)
  if (activeTab.value === 'ongoing') return list.filter(isOngoing)
  if (activeTab.value === 'ended') return list.filter(isEnded)
  if (activeTab.value === 'done') return list.filter(e => attemptMap.value[e.id]?.status >= 1)
  return list
})

function examStatus(exam) {
  const a = attemptMap.value[exam.id]
  if (!a) return '未参加'
  if (a.status === 0) return '进行中'
  if (a.status === 1) return '已交卷'
  return a.score != null ? `${a.score} 分` : '已批改'
}

function formatDate(v) {
  if (!v) return '--'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function loadData() {
  if (authStore.user?.role !== 1) return
  loading.value = true
  try {
    const enrollments = (await listMyEnrollments()) || []
    const approved = enrollments.filter(e => e.status === 1)
    const allExams = []
    for (const enrollment of approved) {
      try {
        const list = await listExams(enrollment.courseId)
        if (list && list.length) {
          allExams.push(...list.map(e => ({ ...e, courseName: enrollment.courseName, courseId: enrollment.courseId })))
        }
      } catch { /* skip */ }
    }
    exams.value = allExams
    const attempts = (await listMyExamAttempts()) || []
    const map = {}
    attempts.forEach(a => { map[a.examId] = a })
    attemptMap.value = map
  } catch {
    exams.value = []
  } finally {
    loading.value = false
  }
}

function enterExam(exam) {
  emit('update:visible', false)
  router.push(`/courses/${exam.courseId || 0}/exams/${exam.id}`)
}

watch(() => props.visible, (v) => { if (v) loadData() })
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="我的考试"
    width="880px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="exam-dialog-body">
      <template v-if="authStore.user?.role !== 1">
        <el-empty description="考试功能目前仅对学生开放" />
      </template>
      <template v-else-if="loading">
        <div style="padding:40px"><el-skeleton :rows="4" animated /></div>
      </template>
      <template v-else>
        <el-tabs v-model="activeTab" class="dialog-tabs">
          <el-tab-pane label="未开始" name="upcoming" />
          <el-tab-pane label="进行中" name="ongoing" />
          <el-tab-pane label="已结束" name="ended" />
          <el-tab-pane label="已完成" name="done" />
          <el-tab-pane label="全部" name="all" />
        </el-tabs>
        <div v-if="!exams.length" class="empty-state"><el-empty description="暂无考试安排" /></div>
        <div v-else class="exam-list">
          <div v-for="item in filteredExams" :key="item.id" class="exam-item" @click="enterExam(item)">
            <div class="exam-info">
              <span class="exam-course">{{ item.courseName }}</span>
              <span class="exam-title">{{ item.title }}</span>
            </div>
            <div class="exam-meta">
              <span class="exam-time">{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</span>
              <span class="exam-duration">{{ item.duration || 60 }}分钟</span>
              <el-tag
                size="small"
                :type="attemptMap[item.id]?.status >= 1 ? 'success' : isOngoing(item) ? 'warning' : 'info'"
                effect="plain"
              >{{ examStatus(item) }}</el-tag>
            </div>
          </div>
        </div>
      </template>
    </div>
  </el-dialog>
</template>

<style scoped>
.exam-dialog-body { min-height: 260px; }
.dialog-tabs { margin-bottom: 16px; }
.empty-state { padding: 20px 0; }

.exam-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 420px;
  overflow-y: auto;
}

.exam-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fafafa;
  cursor: pointer;
  transition: background .15s;
}
.exam-item:hover { background: #f0f5ff; }

.exam-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
  flex: 1;
}

.exam-course { font-size: 12px; color: #999; }
.exam-title { font-size: 14px; font-weight: 500; color: #333; }

.exam-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.exam-time { font-size: 12px; color: #999; white-space: nowrap; }
.exam-duration { font-size: 12px; color: #999; white-space: nowrap; }
</style>
