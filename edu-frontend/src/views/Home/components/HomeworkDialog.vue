<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '../../../stores/auth'
import { listAssignments, listMySubmissions } from '../../../api/assessment'
import { listMyEnrollments } from '../../../api/enrollment'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['update:visible'])

const authStore = useAuthStore()

const activeTab = ref('pending')
const assignments = ref([])
const submissionsMap = ref({})
const loading = ref(false)
const currentMonth = ref(new Date())

// 生成月历天数
const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const days = []

  for (let i = 0; i < firstDay; i++) {
    days.push(null)
  }
  for (let d = 1; d <= daysInMonth; d++) {
    days.push(new Date(year, month, d))
  }
  return days
})

const monthLabel = computed(() => {
  const y = currentMonth.value.getFullYear()
  const m = currentMonth.value.getMonth() + 1
  return `${y} 年 ${m} 月`
})

function prevMonth() {
  const d = new Date(currentMonth.value)
  d.setMonth(d.getMonth() - 1)
  currentMonth.value = d
}

function nextMonth() {
  const d = new Date(currentMonth.value)
  d.setMonth(d.getMonth() + 1)
  currentMonth.value = d
}

function isToday(date) {
  if (!date) return false
  const today = new Date()
  return (
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate()
  )
}

function hasDeadlineOnDate(date) {
  if (!date) return false
  return filteredAssignments.value.some((a) => {
    if (!a.deadline) return false
    const d = new Date(a.deadline)
    return (
      d.getFullYear() === date.getFullYear() &&
      d.getMonth() === date.getMonth() &&
      d.getDate() === date.getDate()
    )
  })
}

// 根据 tab 过滤
const filteredAssignments = computed(() => {
  const list = assignments.value
  if (activeTab.value === 'pending') {
    return list.filter((a) => {
      const sub = submissionsMap.value[a.id]
      return !sub || (sub.score === null && sub.score === undefined)
    })
  }
  if (activeTab.value === 'completed') {
    return list.filter((a) => {
      const sub = submissionsMap.value[a.id]
      return sub && (sub.score !== null && sub.score !== undefined)
    })
  }
  return list
})

async function loadData() {
  loading.value = true
  try {
    const role = authStore.user?.role

    if (role === 1) {
      // 学生：获取选课列表
      const enrollments = (await listMyEnrollments()) || []
      const approved = enrollments.filter((e) => e.status === 1)

      // 逐课程获取作业列表
      const allAssignments = []
      for (const enrollment of approved) {
        try {
          const list = await listAssignments(enrollment.courseId)
          if (list && list.length) {
            allAssignments.push(
              ...list.map((a) => ({
                ...a,
                courseName: a.courseName || enrollment.courseName,
              }))
            )
          }
        } catch {
          // 单个课程失败不影响其他
        }
      }
      assignments.value = allAssignments

      // 获取提交记录，建立映射
      const submissions = (await listMySubmissions()) || []
      const map = {}
      submissions.forEach((s) => {
        map[s.assignmentId] = s
      })
      submissionsMap.value = map
    } else {
      assignments.value = []
      submissionsMap.value = {}
    }
  } catch {
    assignments.value = []
    submissionsMap.value = {}
  } finally {
    loading.value = false
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      loadData()
    }
  }
)

const isStudent = computed(() => authStore.user?.role === 1)
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="查看当前作业"
    width="950px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="homework-body">
      <template v-if="!isStudent">
        <el-empty description="作业详情功能目前仅对学生开放" />
      </template>

      <template v-else-if="loading">
        <div class="homework-loading">
          <el-skeleton :rows="4" animated />
        </div>
      </template>

      <template v-else>
        <el-tabs v-model="activeTab" class="homework-tabs">
          <el-tab-pane label="待完成" name="pending" />
          <el-tab-pane label="已完成" name="completed" />
          <el-tab-pane label="全部" name="all" />
        </el-tabs>

        <div v-if="assignments.length === 0" class="homework-empty">
          <el-empty description="暂无作业数据" />
        </div>

        <div v-else class="homework-layout">
          <!-- 左侧月历 -->
          <div class="homework-calendar">
            <div class="calendar-header">
              <el-button text @click="prevMonth">&lt;</el-button>
              <span class="calendar-month">{{ monthLabel }}</span>
              <el-button text @click="nextMonth">&gt;</el-button>
            </div>
            <div class="calendar-weekdays">
              <span
                v-for="w in ['日', '一', '二', '三', '四', '五', '六']"
                :key="w"
                class="weekday"
              >{{ w }}</span>
            </div>
            <div class="calendar-grid">
              <div
                v-for="(day, idx) in calendarDays"
                :key="idx"
                class="calendar-cell"
                :class="{
                  'is-today': isToday(day),
                  'has-deadline': hasDeadlineOnDate(day),
                  'is-empty': !day,
                }"
              >
                <span v-if="day" class="cell-day">{{ day.getDate() }}</span>
              </div>
            </div>
          </div>

          <!-- 右侧任务列表 -->
          <div class="homework-list">
            <div
              v-for="item in filteredAssignments"
              :key="item.id"
              class="homework-item"
            >
              <div class="hw-info">
                <span class="hw-course">{{ item.courseName }}</span>
                <span class="hw-title">{{ item.title }}</span>
              </div>
              <div class="hw-meta">
                <span class="hw-deadline">
                  截止：{{ item.deadline?.slice(0, 10) || '--' }}
                </span>
                <el-tag
                  v-if="submissionsMap[item.id]"
                  size="small"
                  :type="
                    submissionsMap[item.id].score !== null &&
                    submissionsMap[item.id].score !== undefined
                      ? 'success'
                      : 'warning'
                  "
                  effect="plain"
                >
                  {{
                    submissionsMap[item.id].score !== null &&
                    submissionsMap[item.id].score !== undefined
                      ? '已批改'
                      : '已提交'
                  }}
                </el-tag>
                <el-tag v-else size="small" type="danger" effect="plain">
                  未提交
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
.homework-body {
  min-height: 300px;
}

.homework-loading {
  padding: 40px;
}

.homework-empty {
  padding: 20px 0;
}

.homework-tabs {
  margin-bottom: 16px;
}

.homework-layout {
  display: flex;
  gap: 24px;
}

/* 月历 */
.homework-calendar {
  width: 320px;
  flex-shrink: 0;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.calendar-month {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.weekday {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 4px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
  color: #555;
  cursor: default;

  &.is-empty {
    visibility: hidden;
  }

  &.is-today {
    background: #1677ff;
    color: #fff;
    font-weight: 600;
  }

  &.has-deadline:not(.is-today) {
    background: #fff7e6;
    color: #d46b08;
  }
}

.cell-day {
  line-height: 1;
}

/* 任务列表 */
.homework-list {
  flex: 1;
  min-width: 0;
  max-height: 420px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.homework-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  background: #fafafa;
  transition: background 0.15s;

  &:hover {
    background: #f0f5ff;
  }
}

.hw-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.hw-course {
  font-size: 12px;
  color: #999;
}

.hw-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.hw-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.hw-deadline {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}
</style>
