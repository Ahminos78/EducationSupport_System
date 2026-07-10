<script setup>
import { ref } from 'vue'

defineEmits(['show-homework', 'show-tasks'])

const notices = ref([
  { title: '2026年秋季学期选课开始', date: '2026-07-08' },
  { title: 'Java Web 开发课程作业即将截止', date: '2026-07-20' },
  { title: '系统维护通知：7月12日 02:00-06:00', date: '2026-07-10' },
])
</script>

<template>
  <aside class="notice-panel">
    <div class="quick-actions">
      <el-button type="primary" size="large" class="action-btn" @click="$emit('show-homework')">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 6px;">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
        </svg>
        查看当前作业
      </el-button>
      <el-button size="large" class="action-btn" @click="$emit('show-tasks')">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" style="margin-right: 6px;">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <line x1="9" y1="9" x2="15" y2="9" />
          <line x1="9" y1="13" x2="15" y2="13" />
          <line x1="9" y1="17" x2="12" y2="17" />
        </svg>
        查看学习任务
      </el-button>
    </div>

    <div class="recent-notices">
      <h4 class="notices-title">最近通知</h4>
      <div v-for="(notice, index) in notices" :key="index" class="notice-item">
        <div class="notice-dot" />
        <div class="notice-content">
          <span class="notice-text">{{ notice.title }}</span>
          <span class="notice-date">{{ notice.date }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.notice-panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  justify-content: center;
  height: 44px;
  font-size: 14px;
  border-radius: 10px;
}

.recent-notices {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notices-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.notice-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.notice-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1677ff;
  flex-shrink: 0;
  margin-top: 6px;
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.notice-text {
  font-size: 13px;
  color: #555;
  line-height: 1.4;
}

.notice-date {
  font-size: 12px;
  color: #bbb;
}
</style>
