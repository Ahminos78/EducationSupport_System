<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  course: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

// 封面图：没有图片时使用默认渐变色 + 课程名首字
const hasCover = computed(() => Boolean(props.course.coverUrl))

const coverColorIndex = computed(() => {
  // 基于 id 生成不同的封面颜色
  return (props.course.id || 1) % 6
})

const coverGradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
]

const coverStyle = computed(() => {
  if (hasCover.value) {
    return { backgroundImage: `url(${props.course.coverUrl})` }
  }
  return { background: coverGradients[coverColorIndex.value] }
})

const coverInitial = computed(() => {
  return props.course.name.slice(0, 1)
})

function enterCourse() {
  router.push(`/courses/${props.course.id}`)
}
</script>

<template>
  <article class="course-card" @click="enterCourse">
    <!-- 封面 -->
    <div class="card-cover" :style="coverStyle">
      <span v-if="!hasCover" class="cover-letter">{{ coverInitial }}</span>
    </div>

    <!-- 内容区 -->
    <div class="card-body">
      <h4 class="card-title" :title="course.name">{{ course.name }}</h4>
      <div class="card-teacher">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" class="meta-icon">
          <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
        </svg>
        授课老师：{{ course.teacherName || `教师 ${course.teacherId}` }}
      </div>
      <div class="card-meta">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" class="meta-icon">
          <circle cx="12" cy="12" r="9"/>
          <path d="M9 9.5c.6-1 1.6-1.5 3-1.5 1.8 0 3 1 3 2.4 0 1.3-.8 2-2.1 2.6-.9.4-1.4 1-1.4 2"/>
          <path d="M12 18h.01"/>
        </svg>
        课程学分：{{ course.credit ?? '--' }}
      </div>
      <div class="card-tags">
        <el-tag size="small" effect="plain">
          {{ course.tags || '暂无标签' }}
        </el-tag>
      </div>

      <!-- 按钮 -->
      <el-button type="primary" size="small" class="enter-btn" @click.stop="enterCourse">
        进入课程
      </el-button>
    </div>
  </article>
</template>

<style scoped lang="scss">
.course-card {
  background: #fff;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-color: #e0e8f5;
  }
}

/* 封面 */
.card-cover {
  position: relative;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
}

.cover-letter {
  font-size: 40px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 内容 */
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 42px;
}

.card-teacher,
.card-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

.meta-icon {
  flex-shrink: 0;
  opacity: 0.6;
}

.card-tags {
  margin-top: 4px;
  min-height: 24px;
}

/* 按钮 */
.enter-btn {
  margin-top: 4px;
  width: 100%;
  border-radius: 8px;
  height: 34px;
  font-size: 13px;
}
</style>
