<script setup>
const stats = [
  {
    label: '课程数量',
    value: '120+',
  },
  {
    label: '教师',
    value: '80+',
  },
  {
    label: '学生',
    value: '3500+',
  },
  {
    label: '今日在线',
    value: '850+',
  },
]
</script>

<template>
  <section class="stats-panel" aria-label="平台数据">
    <div v-for="item in stats" :key="item.label" class="stat-item">
      <strong>{{ item.value }}</strong>
      <span>{{ item.label }}</span>
    </div>
  </section>
</template>

<style scoped>
.stats-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
}

.stat-item strong {
  display: block;
  color: #ffffff;
  font-size: 28px;
  line-height: 1;
  font-weight: 820;
}

.stat-item span {
  display: block;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
}

@media (max-width: 700px) {
  .stats-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stat-item {
    padding: 16px;
  }
}
</style>
