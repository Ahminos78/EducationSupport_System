<script setup>
import { ref } from 'vue'

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['update:visible'])

// Mock 数据：学习任务
const taskGroups = ref([
  {
    title: '阅读任务',
    icon: '📖',
    items: [
      { course: 'Java Web 开发', task: '阅读第 3 章 Servlet 生命周期', deadline: '2026-07-15' },
      { course: '数据结构', task: '阅读第 5 章 二叉树遍历', deadline: '2026-07-18' },
      { course: '操作系统', task: '阅读第 4 章 进程调度', deadline: '2026-07-20' },
    ],
  },
  {
    title: '实验任务',
    icon: '🔬',
    items: [
      { course: 'Java Web 开发', task: '完成实验二：JSP+Servlet 登录注册', deadline: '2026-07-22' },
      { course: '数据库原理', task: '实验三：SQL 高级查询练习', deadline: '2026-07-25' },
    ],
  },
  {
    title: '视频学习任务',
    icon: '🎥',
    items: [
      { course: 'Java Web 开发', task: '观看 Spring MVC 入门视频（共 3 节）', deadline: '2026-07-16' },
      { course: '数据结构', task: '观看图论算法讲解视频', deadline: '2026-07-19' },
    ],
  },
  {
    title: '课程任务',
    icon: '📋',
    items: [
      { course: 'Java Web 开发', task: '小组项目选题提交', deadline: '2026-07-20' },
      { course: '操作系统', task: '课程论文初稿提交', deadline: '2026-07-28' },
    ],
  },
])
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="查看学习任务"
    width="950px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="task-body">
      <div
        v-for="(group, gi) in taskGroups"
        :key="gi"
        class="task-group"
      >
        <div class="group-header">
          <span class="group-icon">{{ group.icon }}</span>
          <span class="group-title">{{ group.title }}</span>
          <span class="group-count">共 {{ group.items.length }} 项</span>
        </div>
        <div class="group-items">
          <div
            v-for="(item, ii) in group.items"
            :key="ii"
            class="task-item"
          >
            <div class="task-info">
              <span class="task-course">{{ item.course }}</span>
              <span class="task-desc">{{ item.task }}</span>
            </div>
            <span class="task-deadline">截止：{{ item.deadline }}</span>
          </div>
        </div>
      </div>

      <div v-if="taskGroups.length === 0" class="task-empty">
        <el-empty description="暂无学习任务" />
      </div>
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
.task-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-height: 500px;
  overflow-y: auto;
}

.task-group {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.group-icon {
  font-size: 18px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.group-count {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.group-items {
  display: flex;
  flex-direction: column;
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  transition: background 0.15s;

  &:hover {
    background: #f5f7fa;
  }

  & + & {
    border-top: 1px solid #f5f5f5;
  }
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.task-course {
  font-size: 12px;
  color: #999;
}

.task-desc {
  font-size: 14px;
  color: #333;
}

.task-deadline {
  font-size: 12px;
  color: #bbb;
  white-space: nowrap;
  flex-shrink: 0;
  margin-left: 16px;
}

.task-empty {
  padding: 40px 0;
}
</style>
