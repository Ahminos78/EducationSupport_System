<script setup>
import DashboardBanner from './Home/components/DashboardBanner.vue'
import StatisticsPanel from './Home/components/StatisticsPanel.vue'
import CourseGrid from './Home/components/CourseGrid.vue'
</script>

<template>
  <div class="dashboard-page">
    <DashboardBanner />
    <div class="dashboard-content">
      <div class="dashboard-main">
        <StatisticsPanel />
        <CourseGrid />
      </div>
      <aside class="dashboard-side" aria-label="AI 功能预留区域" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-content {
  display: flex;
  gap: 24px;
}

.dashboard-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

.dashboard-side {
  width: 320px;
  flex-shrink: 0;
}

@media (max-width: 1200px) {
  .dashboard-content {
    flex-direction: column;
  }

  .dashboard-side {
    width: 100%;
  }
}
</style>
