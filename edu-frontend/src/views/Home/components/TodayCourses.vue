<script setup>
import { onMounted, ref } from 'vue'
import { getTodaySchedule } from '../../../api/schedule'

const todayCourses = ref([])
const loading = ref(true)

// 小节课节次 → 时间映射
const PERIOD_MAP = {
  1: '08:00', 2: '08:55', 3: '10:00', 4: '10:55',
  5: '14:00', 6: '14:55', 7: '16:00', 8: '16:55',
  9: '19:00', 10: '19:55',
}

function formatPeriod(start, end) {
  const s = PERIOD_MAP[start] || `第${start}节`
  const e = PERIOD_MAP[end] || `第${end}节`
  return `${s} - ${e}`
}

const DAY_NAMES = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

onMounted(async () => {
  try {
    todayCourses.value = await getTodaySchedule() || []
  } catch {
    todayCourses.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <aside class="today-sidebar">
    <div class="sidebar-header">
      <h3>
        <span class="sidebar-icon">📅</span>
        今日课程
        <span class="day-badge">{{ DAY_NAMES[new Date().getDay() || 7] }}</span>
      </h3>
    </div>

    <div class="sidebar-body">
      <div v-if="loading" class="empty-state">加载中...</div>

      <div v-else-if="todayCourses.length === 0" class="empty-state">
        <div class="empty-icon">🎉</div>
        <p>今天没有课程安排</p>
      </div>

      <div v-else class="course-list">
        <div v-for="(course, idx) in todayCourses" :key="course.classId + '-' + course.startPeriod" class="course-item">
          <div class="course-time-marker">
            <span class="time-dot" :class="'dot-' + (idx % 4)"></span>
            <span class="time-line"></span>
          </div>
          <div class="course-info">
            <div class="course-name">{{ course.courseName }}</div>
            <div class="course-meta">
              <span class="meta-time">{{ formatPeriod(course.startPeriod, course.endPeriod) }}</span>
              <span class="meta-location">{{ course.location || '待定' }}</span>
            </div>
            <div class="course-teacher" v-if="course.teacherName">
              <span class="teacher-icon">👤</span>
              {{ course.teacherName }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.today-sidebar {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 18px;
  box-shadow: 0 4px 20px rgba(31,45,61,.035);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 560px;
}

.sidebar-header {
  padding: 20px 22px 14px;
  border-bottom: 1px solid #f1f3f6;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #182230;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-icon {
  font-size: 18px;
}

.day-badge {
  display: inline-flex;
  align-items: center;
  padding: 1px 10px;
  border-radius: 12px;
  background: #f0f5ff;
  color: #1677ff;
  font-size: 12px;
  font-weight: 600;
  margin-left: auto;
}

.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #98a2b3;
  font-size: 14px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
}

.course-list {
  padding: 4px 0;
}

.course-item {
  display: flex;
  gap: 12px;
  padding: 14px 22px;
  transition: background .15s;
}

.course-item:hover {
  background: #f7f9fc;
}

.course-time-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 10px;
  flex-shrink: 0;
  padding-top: 4px;
}

.time-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d0d5dd;
  flex-shrink: 0;
}

.time-dot.dot-0 { background: #1677ff; }
.time-dot.dot-1 { background: #24a148; }
.time-dot.dot-2 { background: #e98213; }
.time-dot.dot-3 { background: #7a4bd0; }

.time-line {
  width: 2px;
  flex: 1;
  background: #edf0f5;
  min-height: 40px;
  margin-top: 6px;
}

.course-item:last-child .time-line {
  display: none;
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-name {
  font-size: 14px;
  font-weight: 600;
  color: #182230;
  margin-bottom: 6px;
  line-height: 1.4;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 4px;
}

.meta-time, .meta-location {
  font-size: 12px;
  color: #667085;
  display: flex;
  align-items: center;
}

.meta-time::before {
  content: '⏰';
  margin-right: 3px;
}

.meta-location::before {
  content: '📍';
  margin-right: 3px;
}

.course-teacher {
  font-size: 12px;
  color: #8490a2;
  display: flex;
  align-items: center;
  gap: 3px;
}

.teacher-icon {
  font-size: 11px;
}

/* 滚动条 */
.sidebar-body::-webkit-scrollbar {
  width: 4px;
}
.sidebar-body::-webkit-scrollbar-thumb {
  background: #e5e9f2;
  border-radius: 4px;
}

@media (max-width: 1200px) {
  .today-sidebar {
    width: 100%;
    max-height: none;
  }
}
</style>
