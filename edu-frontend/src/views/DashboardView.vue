<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const roleTasks = computed(() => {
  const role = authStore.user?.role
  if (role === 3) {
    return ['管理用户账号', '维护课程与选课', '查看全站讨论与作业']
  }
  if (role === 2) {
    return ['维护我的课程', '审核学生选课', '发布与批改作业']
  }
  return ['浏览课程', '申请选课', '提交作业并查看反馈']
})
</script>

<template>
  <section class="dashboard">
    <el-row :gutter="18">
      <el-col :lg="16" :md="24">
        <section class="surface welcome-panel">
          <p class="eyebrow">当前登录</p>
          <h2>{{ authStore.user?.nickname || authStore.user?.username }}</h2>
          <p class="muted">
            {{ authStore.roleText }} · 用户ID {{ authStore.user?.id }}
          </p>
        </section>
      </el-col>
      <el-col :lg="8" :md="24">
        <section class="surface status-panel">
          <p class="eyebrow">网关状态</p>
          <h3>已接入认证接口</h3>
          <p class="muted">当前页面通过 Axios、Pinia 和路由守卫维护登录态。</p>
        </section>
      </el-col>
    </el-row>

    <section class="surface task-panel">
      <div class="section-title">
        <h3>可用工作</h3>
        <span>{{ authStore.roleText }}</span>
      </div>
      <el-space wrap>
        <el-tag v-for="task in roleTasks" :key="task" effect="plain" size="large">
          {{ task }}
        </el-tag>
      </el-space>
    </section>
  </section>
</template>
