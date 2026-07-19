<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyActiveWarnings } from '../../../api/warning'

const router = useRouter()
const warnings = ref([])
const loading = ref(true)

const severityMap = { 1: '一般', 2: '严重', 3: '非常严重' }
const severityClass = { 1: 'sev-mild', 2: 'sev-serious', 3: 'sev-critical' }

const hasWarnings = computed(() => warnings.value.length > 0)

onMounted(async () => {
  try {
    warnings.value = await getMyActiveWarnings() || []
  } catch {
    warnings.value = []
  } finally {
    loading.value = false
  }
})

function goToCourse(courseId) {
  router.push(`/courses/${courseId}?tab=study`)
}
</script>

<template>
  <div v-if="!loading && hasWarnings" class="warning-banner">
    <div class="warning-header">
      <span class="warning-icon">⚠️</span>
      <span class="warning-title">学业预警</span>
      <span class="warning-count">{{ warnings.length }} 门课程未通过</span>
    </div>
    <div class="warning-list">
      <div
        v-for="w in warnings"
        :key="w.id"
        class="warning-item"
        :class="severityClass[w.severity]"
        @click="goToCourse(w.courseId)"
      >
        <div class="warning-item-left">
          <span class="sev-badge" :class="severityClass[w.severity]">
            {{ severityMap[w.severity] || '一般' }}
          </span>
          <span class="warning-course-name">{{ w.courseName || '未知课程' }}</span>
        </div>
        <div class="warning-item-right">
          <span class="warning-score">{{ w.currentScore ?? '--' }} 分</span>
          <span class="warning-arrow">→</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.warning-banner {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border: 1px solid #ffcdd2;
  border-radius: 16px;
  padding: 18px 22px;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.warning-icon {
  font-size: 20px;
}

.warning-title {
  font-weight: 700;
  font-size: 15px;
  color: #c62828;
}

.warning-count {
  margin-left: auto;
  font-size: 12px;
  color: #e57373;
  background: #ffebee;
  padding: 2px 10px;
  border-radius: 12px;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.warning-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(255,255,255,.8);
  cursor: pointer;
  transition: background .15s;
}

.warning-item:hover {
  background: #fff;
}

.warning-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.sev-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
  white-space: nowrap;
}

.sev-badge.sev-mild { background: #fff3e0; color: #b35d0e; }
.sev-badge.sev-serious { background: #ffebee; color: #c62828; }
.sev-badge.sev-critical { background: #fce4ec; color: #b71c1c; font-weight: 700; }

.warning-item.sev-mild { border-left: 3px solid #ffb74d; }
.warning-item.sev-serious { border-left: 3px solid #ef5350; }
.warning-item.sev-critical { border-left: 3px solid #c62828; }

.warning-course-name {
  font-size: 13px;
  font-weight: 600;
  color: #344054;
}

.warning-item-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.warning-score {
  font-size: 12px;
  font-weight: 700;
  color: #c62828;
}

.warning-arrow {
  font-size: 13px;
  color: #adb5bd;
}
</style>
