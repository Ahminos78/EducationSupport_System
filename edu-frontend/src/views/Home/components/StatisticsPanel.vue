<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../../../stores/auth'
import { getDashboardStats } from '../../../api/dashboard'

const authStore = useAuthStore()
const statsData = ref(null)
const loading = ref(true)

async function loadStats() {
  loading.value = true
  try {
    const role = authStore.user?.role
    const data = await getDashboardStats(role)
    statsData.value = data
  } catch {
    statsData.value = null
  } finally {
    loading.value = false
  }
}

const stats = computed(() => {
  const role = authStore.user?.role
  const d = statsData.value

  if (loading.value) {
    // 骨架态
    return [
      { label: '--', value: '--', icon: '📚', color: '#1677ff' },
      { label: '--', value: '--', icon: '📖', color: '#52c41a' },
      { label: '--', value: '--', icon: '📝', color: '#faad14' },
      { label: '--', value: '--', icon: '🏆', color: '#722ed1' },
    ]
  }

  if (role === 1 && d) {
    return [
      { label: '已选课程', value: String(d.enrolledCourses ?? 0), icon: '📚', color: '#1677ff' },
      { label: '进行中的课程', value: String(d.activeCourses ?? 0), icon: '📖', color: '#52c41a' },
      { label: '待完成作业', value: String(d.pendingAssignments ?? 0), icon: '📝', color: '#faad14' },
      { label: '平均成绩', value: d.avgScore ? String(d.avgScore) : '--', icon: '🏆', color: '#722ed1' },
    ]
  }

  if (role === 2 && d) {
    return [
      { label: '授课课程', value: String(d.courseCount ?? 0), icon: '📚', color: '#1677ff' },
      { label: '学生人数', value: d.studentCount ? String(d.studentCount) : '--', icon: '👥', color: '#52c41a' },
      { label: '待批改作业', value: d.pendingGrading ? String(d.pendingGrading) : '--', icon: '📝', color: '#faad14' },
      { label: '已发布考试', value: d.examCount ? String(d.examCount) : '--', icon: '📋', color: '#722ed1' },
    ]
  }

  // 管理员或 fallback
  return [
    { label: '平台课程', value: d?.totalCourses ? String(d.totalCourses) : '--', icon: '📚', color: '#1677ff' },
    { label: '教师人数', value: d?.teacherCount ? String(d.teacherCount) : '--', icon: '👥', color: '#52c41a' },
    { label: '学生人数', value: d?.studentCount ? String(d.studentCount) : '--', icon: '🎓', color: '#faad14' },
    { label: '今日在线', value: d?.onlineCount ? String(d.onlineCount) : '--', icon: '📊', color: '#722ed1' },
  ]
})

onMounted(() => {
  loadStats()
})
</script>

<template>
  <section class="statistics-panel">
    <div v-if="loading" class="stats-grid">
      <div v-for="i in 4" :key="i" class="stat-card">
        <el-skeleton :loading="true" animated>
          <template #template>
            <div style="display: flex; align-items: center; gap: 16px; padding: 4px 0;">
              <el-skeleton-item variant="circle" style="width: 48px; height: 48px;" />
              <div style="flex: 1;">
                <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 6px;" />
                <el-skeleton-item variant="text" style="width: 40%;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <div v-else class="stats-grid">
      <div
        v-for="(stat, index) in stats"
        :key="index"
        class="stat-card"
      >
        <div class="stat-icon" :style="{ background: stat.color + '15', color: stat.color }">
          {{ stat.icon }}
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.statistics-panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #fafafa;
  transition: all 0.2s ease;

  &:hover {
    background: #f0f5ff;
    transform: translateY(-1px);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
