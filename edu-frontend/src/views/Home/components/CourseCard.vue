<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  course: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

function enterCourse() {
  router.push(`/courses/${props.course.id}`)
}
</script>

<template>
  <article class="course-card" @click="enterCourse">
    <div class="card-cover">
      <div class="cover-placeholder">
        {{ course.courseName?.slice(0, 2) || '课程' }}
      </div>
      <el-tag
        v-if="course.status !== undefined"
        :type="course.status === 1 ? 'success' : 'info'"
        size="small"
        class="card-status"
        effect="dark"
      >
        {{ course.status === 1 ? '进行中' : '已结束' }}
      </el-tag>
    </div>
    <div class="card-body">
      <h4 class="card-title">{{ course.courseName }}</h4>
      <p class="card-meta">
        <span>授课教师：{{ course.teacherName || '--' }}</span>
      </p>
      <p class="card-meta">
        <span>{{ course.semester || '--' }}</span>
      </p>
      <div class="card-progress">
        <el-progress
          :percentage="course.progress || 0"
          :stroke-width="6"
          :show-text="false"
          color="#1677ff"
        />
        <span class="progress-text">{{ course.progress || 0 }}%</span>
      </div>
      <el-button type="primary" size="small" class="enter-btn" @click.stop="enterCourse">
        进入课程
      </el-button>
    </div>
  </article>
</template>

<style scoped lang="scss">
.course-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    border-color: #e6f0ff;
  }
}

.card-cover {
  position: relative;
  height: 100px;
  background: linear-gradient(135deg, #e6f4ff, #f0f5ff);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder {
  font-size: 28px;
  font-weight: 700;
  color: #1677ff;
  opacity: 0.5;
  letter-spacing: 2px;
}

.card-status {
  position: absolute;
  top: 10px;
  right: 10px;
}

.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #999;
  margin: 0;
  line-height: 1.4;
}

.card-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.progress-text {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.enter-btn {
  margin-top: 4px;
  width: 100%;
  border-radius: 8px;
}
</style>
