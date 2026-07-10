<script setup>
import { ref } from 'vue'
import CourseCard from './CourseCard.vue'

const courses = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const loading = ref(false)
const currentPage = ref(1)
const total = ref(0)

// TODO: Step 4 - 对接后端 GET /api/courses/page
</script>

<template>
  <section class="course-grid">
    <div class="course-header">
      <h3 class="course-title">我的课程</h3>
      <div class="course-toolbar">
        <el-select
          v-model="statusFilter"
          placeholder="全部"
          size="default"
          style="width: 120px"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已结束" value="ended" />
        </el-select>
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程"
          prefix-icon="Search"
          clearable
          style="width: 240px"
        />
      </div>
    </div>

    <div v-if="loading" class="grid-loading">
      <el-skeleton :rows="2" animated />
    </div>

    <div v-else-if="courses.length === 0" class="grid-empty">
      <el-empty description="暂未找到课程" />
    </div>

    <div v-else class="course-list">
      <CourseCard
        v-for="course in courses"
        :key="course.id"
        :course="course"
      />
    </div>

    <div v-if="total > 0" class="grid-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="12"
        :total="total"
        layout="prev, pager, next"
        small
      />
    </div>
  </section>
</template>

<style scoped lang="scss">
.course-grid {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.course-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.course-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.grid-loading,
.grid-empty {
  padding: 40px 0;
}

.grid-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .course-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .course-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .course-list {
    grid-template-columns: 1fr;
  }
}
</style>
