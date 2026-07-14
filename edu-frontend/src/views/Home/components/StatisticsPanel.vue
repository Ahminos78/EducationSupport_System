<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const emit = defineEmits(['open-homework'])

const shortcuts = [
  { label: '我的课程', description: '查看当前课程', icon: '📚', color: '#1677ff', path: '/courses' },
  { label: '我的选课', description: '进入学生选课', icon: '✅', color: '#52c41a', path: '/course-selection' },
  { label: '我的作业', description: '查看课程作业', icon: '📝', color: '#faad14', action: 'homework' },
  { label: '我的考试', description: '功能开发中', icon: '📋', color: '#722ed1' },
]

function openShortcut(item) {
  if (item.path) router.push(item.path)
  if (item.action === 'homework') emit('open-homework')
}
</script>

<template>
  <section class="statistics-panel" aria-label="快捷功能">
    <div class="shortcut-grid">
      <button
        v-for="item in shortcuts"
        :key="item.label"
        type="button"
        class="shortcut-card"
        :class="{ disabled: !item.path && !item.action }"
        :disabled="!item.path && !item.action"
        @click="openShortcut(item)"
      >
        <span class="shortcut-icon" :style="{ background: item.color + '15', color: item.color }">
          {{ item.icon }}
        </span>
        <span class="shortcut-info">
          <strong>{{ item.label }}</strong>
          <small>{{ item.description }}</small>
        </span>
        <span v-if="item.path || item.action" class="shortcut-arrow">→</span>
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.statistics-panel {
  padding: 24px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.shortcut-card {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 16px;
  border: 1px solid transparent;
  border-radius: 12px;
  background: #fafafa;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;

  &:not(.disabled):hover {
    border-color: #d6e8ff;
    background: #f0f5ff;
    transform: translateY(-1px);
  }

  &.disabled {
    cursor: default;
    opacity: 0.68;
  }
}

.shortcut-icon {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
}

.shortcut-info {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
}

.shortcut-info strong {
  color: #1a1a1a;
  font-size: 16px;
}

.shortcut-info small {
  color: #999;
  font-size: 12px;
}

.shortcut-arrow {
  color: #a4afbd;
  font-size: 20px;
}

@media (max-width: 900px) {
  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .shortcut-grid {
    grid-template-columns: 1fr;
  }
}
</style>
