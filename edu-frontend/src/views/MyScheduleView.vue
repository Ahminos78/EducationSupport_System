<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getMyScheduleAllWeeks, getMaxWeek } from '../api/schedule'

const weekNum = ref(1)
const maxWeek = ref(16)
const scheduleList = ref([])
const loading = ref(false)
const showDetail = ref(false)
const selectedCourse = ref(null)

const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const periods = Array.from({ length: 12 }, (_, i) => i + 1)

const periodTime = {
  1: '08:00', 2: '08:55', 3: '10:00', 4: '10:55',
  5: '14:00', 6: '14:55', 7: '16:00', 8: '16:55',
  9: '19:00', 10: '19:55', 11: '20:50', 12: '21:45'
}

const periodEndTime = {
  1: '08:45', 2: '09:40', 3: '10:45', 4: '11:40',
  5: '14:45', 6: '15:40', 7: '16:45', 8: '17:40',
  9: '19:45', 10: '20:40', 11: '21:35', 12: '22:30'
}

const colorPool = ['#1677ff', '#52c41a', '#fa8c16', '#eb2f96', '#722ed1', '#13c2c2', '#f5222d', '#2f54eb']
const courseColorMap = {}

function getCourseColor(courseId) {
  if (!courseColorMap[courseId]) {
    const keys = Object.keys(courseColorMap)
    courseColorMap[courseId] = colorPool[keys.length % colorPool.length]
  }
  return courseColorMap[courseId]
}

const now = ref(new Date())
let timer = null

const currentDay = computed(() => {
  const d = now.value.getDay()
  return d === 0 ? 7 : d
})

const currentPeriod = computed(() => {
  const h = now.value.getHours()
  const m = now.value.getMinutes()
  const t = h * 60 + m
  if (t < 525) return 0
  if (t < 580) return 1
  if (t < 640) return 2
  if (t < 700) return 3
  if (t < 760) return 4
  if (t < 840) return 5
  if (t < 900) return 6
  if (t < 960) return 7
  if (t < 1020) return 8
  if (t < 1140) return 9
  if (t < 1200) return 10
  if (t < 1260) return 11
  if (t < 1350) return 12
  return 13
})

const filteredSchedule = computed(() => {
  return scheduleList.value.filter(
    (s) => s.startWeek <= weekNum.value && s.endWeek >= weekNum.value
  )
})

const weekStats = computed(() => {
  const courseIds = new Set()
  let credits = 0
  filteredSchedule.value.forEach((s) => {
    if (!courseIds.has(s.courseId)) {
      courseIds.add(s.courseId)
      credits += s.credit || 0
    }
  })
  return { count: courseIds.size, credits: credits.toFixed(1) }
})

const gridData = computed(() => {
  const occupied = new Set()
  const grid = {}
  for (const period of periods) {
    grid[period] = {}
    for (let day = 1; day <= 7; day++) {
      const key = `${day}-${period}`
      if (occupied.has(key)) {
        grid[period][day] = { type: 'skip' }
        continue
      }
      const item = filteredSchedule.value.find(
        (s) => s.dayOfWeek === day && s.startPeriod === period
      )
      if (item) {
        grid[period][day] = { ...item, type: 'head', color: getCourseColor(item.courseId) }
        for (let p = period + 1; p <= item.endPeriod; p++) {
          occupied.add(`${day}-${p}`)
        }
      } else {
        grid[period][day] = { type: 'empty' }
      }
    }
  }
  return grid
})

const legendItems = computed(() => {
  const map = new Map()
  filteredSchedule.value.forEach((s) => {
    if (!map.has(s.courseId)) {
      map.set(s.courseId, {
        courseId: s.courseId,
        courseName: s.courseName,
        teacherName: s.teacherName,
        location: s.location,
        color: getCourseColor(s.courseId)
      })
    }
  })
  return Array.from(map.values())
})

const dayParts = [
  { label: '上午', range: [1, 4] },
  { label: '下午', range: [5, 8] },
  { label: '晚上', range: [9, 12] }
]

function getCellSpan(item) {
  return item.endPeriod - item.startPeriod + 1
}

function prevWeek() {
  if (weekNum.value > 1) weekNum.value--
}

function nextWeek() {
  if (weekNum.value < maxWeek.value) weekNum.value++
}

function openDetail(item) {
  selectedCourse.value = item
  showDetail.value = true
}

function periodRangeLabel(start, end) {
  const startH = periodTime[start]
  const endH = periodEndTime[end]
  return `${startH}-${endH}`
}

async function loadSchedule() {
  loading.value = true
  try {
    const res = await getMyScheduleAllWeeks()
    scheduleList.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadMaxWeek() {
  try {
    const res = await getMaxWeek()
    maxWeek.value = res?.maxWeek || 16
  } catch {}
}

onMounted(() => {
  loadMaxWeek()
  loadSchedule()
  timer = setInterval(() => { now.value = new Date() }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="schedule-page">
    <div class="schedule-top">
      <div class="schedule-title-area">
        <h2>我的课表</h2>
        <div class="stats-row">
          <div class="stat-chip">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#1677ff" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
            <span>{{ weekStats.count }} 门课程</span>
          </div>
          <div class="stat-chip">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#52c41a" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
            </svg>
            <span>{{ weekStats.credits }} 学分</span>
          </div>
        </div>
      </div>
      <div class="week-nav">
        <button class="arrow-btn" :disabled="weekNum <= 1" @click="prevWeek">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <div class="week-tags">
          <button
            v-for="w in maxWeek" :key="w"
            class="week-tag" :class="{ active: w === weekNum }"
            @click="weekNum = w"
          >{{ w }}</button>
        </div>
        <button class="arrow-btn" :disabled="weekNum >= maxWeek" @click="nextWeek">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>
    </div>

    <div class="schedule-grid" v-loading="loading">
      <div v-if="!loading && filteredSchedule.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.5">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        <p>第 {{ weekNum }} 周暂无课程安排</p>
      </div>
      <table v-else class="timetable">
        <thead>
          <tr>
            <th class="time-col">
              <div class="th-time">节次</div>
            </th>
            <th v-for="day in 7" :key="day" class="day-col" :class="{ 'today-col': day === currentDay }">
              <div class="day-label">{{ dayLabels[day - 1] }}</div>
              <div v-if="day === currentDay" class="today-dot"></div>
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-for="part in dayParts" :key="part.label">
            <tr class="part-divider">
              <td colspan="8" class="part-label">{{ part.label }}</td>
            </tr>
            <template v-for="period in periods.filter(p => p >= part.range[0] && p <= part.range[1])" :key="period">
              <tr :class="{ 'current-period': period === currentPeriod }">
                <td class="time-cell" :class="{ 'current-period-bg': period === currentPeriod }">
                  <div class="period-num">{{ period }}</div>
                  <div class="period-range">{{ periodTime[period] }}</div>
                </td>
                <template v-for="day in 7" :key="day">
                  <td
                    v-if="gridData[period]?.[day]?.type === 'head'"
                    class="course-cell"
                    :class="{ 'today-col-cell': day === currentDay }"
                    :rowspan="getCellSpan(gridData[period][day])"
                    :style="{
                      '--accent': gridData[period][day].color,
                      background: day === currentDay
                        ? gridData[period][day].color + '20'
                        : gridData[period][day].color + '12',
                      borderLeft: '3px solid ' + gridData[period][day].color
                    }"
                    @click="openDetail(gridData[period][day])"
                  >
                    <div class="course-name">{{ gridData[period][day].courseName }}</div>
                    <div class="course-meta">
                      <span class="meta-loc">
                        <svg viewBox="0 0 24 24" width="10" height="10" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                        {{ gridData[period][day].location }}
                      </span>
                      <span class="meta-teacher">{{ gridData[period][day].teacherName }}</span>
                    </div>
                  </td>
                  <td v-else-if="gridData[period]?.[day]?.type !== 'skip'" class="empty-cell" :class="{ 'today-col-cell': day === currentDay }"></td>
                </template>
              </tr>
            </template>
          </template>
        </tbody>
      </table>
    </div>

    <div v-if="legendItems.length > 0" class="schedule-legend">
      <div
        v-for="item in legendItems"
        :key="item.courseId"
        class="legend-item"
      >
        <span class="legend-dot" :style="{ background: item.color }"></span>
        <span class="legend-name">{{ item.courseName }}</span>
        <span class="legend-meta">{{ item.teacherName }} · {{ item.location }}</span>
      </div>
    </div>

    <el-dialog
      v-model="showDetail"
      width="520px"
      destroy-on-close
      :show-close="true"
    >
      <template #header>
        <div class="dialog-header" v-if="selectedCourse">
          <span class="dialog-tag" :style="{ background: selectedCourse.color + '20', color: selectedCourse.color }">
            {{ selectedCourse.category }}
          </span>
          <span class="dialog-title">{{ selectedCourse.courseName }}</span>
        </div>
      </template>
      <div v-if="selectedCourse" class="course-detail">
        <div class="detail-grid">
          <div class="detail-card">
            <div class="detail-card-label">授课教师</div>
            <div class="detail-card-value">{{ selectedCourse.teacherName }}</div>
          </div>
          <div class="detail-card">
            <div class="detail-card-label">课程学分</div>
            <div class="detail-card-value">{{ selectedCourse.credit }}</div>
          </div>
          <div class="detail-card">
            <div class="detail-card-label">教学班</div>
            <div class="detail-card-value">{{ selectedCourse.className }}</div>
          </div>
          <div class="detail-card">
            <div class="detail-card-label">课程代码</div>
            <div class="detail-card-value">{{ selectedCourse.courseCode }}</div>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-icon-row">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#1677ff" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            <span>周{{ ['一','二','三','四','五','六','日'][selectedCourse.dayOfWeek - 1] }} 第{{ selectedCourse.startPeriod }}-{{ selectedCourse.endPeriod }}节</span>
            <span class="detail-time-range">{{ periodRangeLabel(selectedCourse.startPeriod, selectedCourse.endPeriod) }}</span>
          </div>
          <div class="detail-icon-row">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#52c41a" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <span>{{ selectedCourse.location }}</span>
          </div>
          <div class="detail-icon-row">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#fa8c16" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
            <span>第 {{ selectedCourse.startWeek }}-{{ selectedCourse.endWeek }} 周</span>
          </div>
        </div>
        <div v-if="selectedCourse.description" class="detail-desc">
          <div class="detail-desc-label">课程简介</div>
          <div class="detail-desc-text">{{ selectedCourse.description }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.schedule-page {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.schedule-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.schedule-title-area {
  h2 {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 8px;
  }
}

.stats-row {
  display: flex;
  gap: 12px;
}

.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 20px;
  font-size: 13px;
  color: #555;
}

.week-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.arrow-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e0e6eb;
  border-radius: 8px;
  background: #fff;
  color: #555;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    border-color: #1677ff;
    color: #1677ff;
    background: #f0f5ff;
  }

  &:disabled {
    opacity: 0.35;
    cursor: not-allowed;
  }
}

.week-tags {
  display: flex;
  gap: 2px;
  max-width: 560px;
  overflow-x: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar { display: none; }
}

.week-tag {
  min-width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;

  &:hover {
    background: #f0f5ff;
    color: #1677ff;
  }

  &.active {
    background: #1677ff;
    color: #fff;
    font-weight: 600;
  }
}

.schedule-grid {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  gap: 12px;

  p {
    font-size: 14px;
    color: #bbb;
  }
}

.timetable {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;

  th, td {
    border: 1px solid #f0f2f5;
    text-align: center;
    vertical-align: middle;
  }

  thead th {
    background: #fafbfc;
    padding: 12px 0;
    position: relative;
  }

  .time-col {
    width: 80px;
  }

  .day-col {
    .day-label {
      font-size: 13px;
      font-weight: 600;
      color: #666;
    }

    &.today-col {
      background: #e6f4ff;

      .day-label { color: #1677ff; }
    }
  }

  .today-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #1677ff;
    margin: 3px auto 0;
  }

  .part-divider .part-label {
    background: #fafbfc;
    font-size: 11px;
    font-weight: 600;
    color: #999;
    letter-spacing: 2px;
    padding: 4px 0;
    border-bottom: 2px solid #f0f2f5;
  }

  .time-cell {
    background: #fafbfc;
    padding: 4px 0;
    width: 80px;

    .period-num {
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .period-range {
      font-size: 10px;
      color: #bbb;
      margin-top: 1px;
    }

    &.current-period-bg {
      background: #e6f4ff;

      .period-num { color: #1677ff; }
    }
  }

  .current-period {
    box-shadow: inset 0 0 0 1px #bae0ff;
  }

  .empty-cell {
    height: 48px;
  }

  .today-col-cell {
    background: #f0f7ff !important;
  }

  .course-cell {
    cursor: pointer;
    padding: 6px 8px;
    transition: all 0.2s;
    text-align: left;

    &:hover {
      filter: brightness(0.97);
      box-shadow: 0 0 0 2px var(--accent, #1677ff) inset;
      z-index: 1;
      position: relative;
    }

    .course-name {
      font-size: 13px;
      font-weight: 600;
      color: #1a1a1a;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .course-meta {
      display: flex;
      flex-direction: column;
      gap: 1px;
      margin-top: 3px;

      span {
        font-size: 11px;
        color: #999;
        display: flex;
        align-items: center;
        gap: 3px;
      }

      .meta-loc {
        color: #777;
      }
    }
  }
}

.schedule-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f9fafb;
  border: 1px solid #f0f2f5;
  border-radius: 8px;
  font-size: 12px;
  transition: all 0.2s;

  &:hover {
    border-color: #d6e8ff;
    background: #f0f5ff;
  }
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-name {
  color: #333;
  font-weight: 500;
}

.legend-meta {
  color: #bbb;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dialog-tag {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.course-detail {
  .detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 20px;
  }

  .detail-card {
    background: #f9fafb;
    border: 1px solid #f0f2f5;
    border-radius: 10px;
    padding: 12px 14px;

    .detail-card-label {
      font-size: 11px;
      color: #999;
      margin-bottom: 4px;
    }

    .detail-card-value {
      font-size: 14px;
      font-weight: 600;
      color: #1a1a1a;
    }
  }

  .detail-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 20px;
  }

  .detail-icon-row {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #333;

    .detail-time-range {
      margin-left: auto;
      font-size: 13px;
      color: #999;
    }
  }

  .detail-desc {
    background: #f9fafb;
    border: 1px solid #f0f2f5;
    border-radius: 10px;
    padding: 14px;

    .detail-desc-label {
      font-size: 12px;
      font-weight: 600;
      color: #999;
      margin-bottom: 6px;
    }

    .detail-desc-text {
      font-size: 13px;
      color: #555;
      line-height: 1.6;
    }
  }
}
</style>
