<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '业务页面')
</script>

<template>
  <section class="surface placeholder-page">
    <p class="eyebrow">下一阶段</p>
    <h2>{{ title }}</h2>
    <p class="muted">当前阶段先完成基础布局、权限菜单和登录态升级，业务表格与表单会在后续阶段逐步接入。</p>
  </section>
</template>
