<script setup>
import { ref } from 'vue'
import DashboardBanner from './Home/components/DashboardBanner.vue'
import StatisticsPanel from './Home/components/StatisticsPanel.vue'
import CourseGrid from './Home/components/CourseGrid.vue'
import HomeworkDialog from './Home/components/HomeworkDialog.vue'
import TodayCourses from './Home/components/TodayCourses.vue'

const homeworkVisible = ref(false)
</script>

<template>
  <div class="dashboard-page">
    <DashboardBanner />
    <div class="dashboard-content">
      <div class="dashboard-main">
        <StatisticsPanel @open-homework="homeworkVisible = true" />
        <CourseGrid />
      </div>
      <TodayCourses />
    </div>
    <HomeworkDialog v-model:visible="homeworkVisible" />
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

@media (max-width: 1200px) {
  .dashboard-content {
    flex-direction: column;
  }

}
</style>
