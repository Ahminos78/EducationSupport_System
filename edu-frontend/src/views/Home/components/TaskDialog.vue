<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['update:visible'])
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
    <div class="task-dialog-placeholder">
      <el-empty description="学习任务功能将在后续步骤中实现" />
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
.task-dialog-placeholder {
  padding: 40px 0;
}
</style>
