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

// 状态标签
const statusConfig = computed(() => {
  const map = {
    active: { label: '进行中', type: 'success' },
    upcoming: { label: '即将开始', type: 'primary' },
    ended: { label: '已结束', type: 'info' },
  }
  return map[props.course.status] || { label: '未知', type: 'info' }
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
      <el-tag
        :type="statusConfig.type"
        size="small"
        class="card-status-tag"
        effect="dark"
      >
        {{ statusConfig.label }}
      </el-tag>
    </div>

    <!-- 内容区 -->
    <div class="card-body">
      <h4 class="card-title" :title="course.name">{{ course.name }}</h4>
      <div class="card-teacher">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" class="meta-icon">
          <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
        </svg>
        {{ course.teacherName }}
      </div>
      <div class="card-semester">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" class="meta-icon">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        {{ course.semester }}
      </div>

      <!-- 进度条 -->
      <div v-if="course.status === 'active'" class="card-progress">
        <el-progress
          :percentage="course.progress"
          :stroke-width="6"
          :show-text="false"
          color="#1677ff"
        />
        <span class="progress-label">学习进度 {{ course.progress }}%</span>
      </div>
      <div v-else-if="course.status === 'ended'" class="card-progress">
        <el-progress
          :percentage="100"
          :stroke-width="6"
          :show-text="false"
          color="#bbb"
        />
        <span class="progress-label completed">已完成</span>
      </div>
      <div v-else class="card-progress-placeholder" />

      <!-- 按钮 -->
      <el-button type="primary" size="small" class="enter-btn" @click.stop="enterCourse">
        {{ course.status === 'ended' ? '查看回顾' : '进入课程' }}
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

.card-status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
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
.card-semester {
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

/* 进度 */
.card-progress {
  margin-top: 4px;
}

.progress-label {
  display: block;
  font-size: 11px;
  color: #aaa;
  margin-top: 4px;
  text-align: right;

  &.completed {
    color: #bbb;
  }
}

.card-progress-placeholder {
  height: 24px;
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
