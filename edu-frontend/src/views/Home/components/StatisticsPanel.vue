<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../../stores/auth'

const authStore = useAuthStore()

const stats = computed(() => {
  const role = authStore.user?.role
  if (role === 1) {
    return [
      { label: '已选课程', value: '6', icon: '📚', color: '#1677ff' },
      { label: '进行中的课程', value: '4', icon: '📖', color: '#52c41a' },
      { label: '待完成作业', value: '3', icon: '📝', color: '#faad14' },
      { label: '平均成绩', value: '86.5', icon: '🏆', color: '#722ed1' },
    ]
  }
  if (role === 2) {
    return [
      { label: '授课课程', value: '3', icon: '📚', color: '#1677ff' },
      { label: '学生人数', value: '156', icon: '👥', color: '#52c41a' },
      { label: '待批改作业', value: '12', icon: '📝', color: '#faad14' },
      { label: '已发布考试', value: '5', icon: '📋', color: '#722ed1' },
    ]
  }
  return [
    { label: '平台课程', value: '120+', icon: '📚', color: '#1677ff' },
    { label: '教师人数', value: '80+', icon: '👥', color: '#52c41a' },
    { label: '学生人数', value: '3500+', icon: '🎓', color: '#faad14' },
    { label: '今日在线', value: '850+', icon: '📊', color: '#722ed1' },
  ]
})
</script>

<template>
  <section class="statistics-panel">
    <div class="stats-grid">
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
