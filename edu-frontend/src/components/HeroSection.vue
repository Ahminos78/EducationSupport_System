<script setup>
import {
  Aim,
  ChatDotRound,
  DataAnalysis,
  DocumentChecked,
  Notebook,
  Reading,
} from '@element-plus/icons-vue'
import StatsPanel from './StatsPanel.vue'

const capabilities = [
  {
    icon: Reading,
    title: '智能课程问答',
    description: '围绕课程资料提供即时答疑与学习引导。',
  },
  {
    icon: DocumentChecked,
    title: 'AI作业评语',
    description: '辅助教师生成结构化反馈，提升批改效率。',
  },
  {
    icon: DataAnalysis,
    title: '学习进度分析',
    description: '汇总学习行为，识别进度变化与薄弱环节。',
  },
  {
    icon: Notebook,
    title: '知识点摘要',
    description: '自动提炼章节重点，帮助学生快速复习。',
  },
  {
    icon: Aim,
    title: '智能组卷',
    description: '按知识点和难度组合练习题与阶段测验。',
  },
]
</script>

<template>
  <section class="hero-section" aria-label="平台介绍">
    <div class="hero-overlay"></div>
    <div class="geometry geometry-one"></div>
    <div class="geometry geometry-two"></div>
    <div class="geometry geometry-three"></div>

    <div class="hero-content">
      <header class="hero-brand">
        <div class="brand-symbol">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div>
          <h1>在线教育辅助教学系统</h1>
          <p>AI-Native Education Platform</p>
        </div>
      </header>

      <section class="hero-intro">
        <span class="hero-kicker">高校教务系统 · AI教育平台</span>
        <h2>打造融合人工智能的在线教育平台。</h2>
        <p>
          支持课程学习、在线考试、课程论坛、AI课程助手、学习预警等功能，
          为教师教学管理与学生自主学习提供稳定、高效、清晰的数字化支撑。
        </p>
      </section>

      <section class="capability-grid" aria-label="AI能力展示">
        <article v-for="item in capabilities" :key="item.title" class="capability-card">
          <div class="capability-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
        </article>
      </section>

      <StatsPanel />

      <footer class="hero-footer">© 2026 在线教育辅助教学系统</footer>
    </div>
  </section>
</template>

<style scoped>
.hero-section {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #ffffff;
  background:
    linear-gradient(135deg, rgba(13, 71, 161, 0.94), rgba(22, 119, 255, 0.9)),
    #1677ff;
  animation: hero-in 640ms ease both;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.08), transparent 36%),
    linear-gradient(180deg, transparent, rgba(3, 33, 83, 0.2));
}

.geometry {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
  pointer-events: none;
}

.geometry-one {
  width: 220px;
  height: 220px;
  right: 10%;
  top: 9%;
  transform: rotate(24deg);
}

.geometry-two {
  width: 320px;
  height: 320px;
  right: -120px;
  bottom: 16%;
  border-radius: 50%;
}

.geometry-three {
  width: 180px;
  height: 100px;
  left: 8%;
  bottom: 12%;
  transform: skewX(-16deg);
}

.hero-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 34px;
  justify-content: center;
  max-width: 980px;
  padding: 48px clamp(40px, 7vw, 96px);
}

.hero-brand {
  display: flex;
  gap: 16px;
  align-items: center;
}

.brand-symbol {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  box-shadow: 0 18px 40px rgba(6, 36, 86, 0.2);
}

.brand-symbol .el-icon {
  font-size: 28px;
}

.hero-brand h1 {
  font-size: 25px;
  line-height: 1.25;
  font-weight: 800;
}

.hero-brand p {
  margin-top: 5px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 14px;
}

.hero-intro {
  max-width: 720px;
}

.hero-kicker {
  display: block;
  margin-bottom: 14px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 14px;
  font-weight: 700;
}

.hero-intro h2 {
  font-size: clamp(34px, 4.4vw, 56px);
  line-height: 1.12;
  font-weight: 820;
}

.hero-intro p {
  max-width: 680px;
  margin-top: 18px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 17px;
  line-height: 1.8;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.capability-card {
  min-height: 150px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  transition:
    transform 180ms ease,
    background 180ms ease,
    box-shadow 180ms ease;
}

.capability-card:hover {
  transform: translateY(-3px) scale(1.015);
  background: rgba(255, 255, 255, 0.17);
  box-shadow: 0 18px 36px rgba(3, 28, 72, 0.2);
}

.capability-icon {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.18);
}

.capability-icon .el-icon {
  font-size: 21px;
}

.capability-card h3 {
  font-size: 16px;
  line-height: 1.35;
  font-weight: 780;
}

.capability-card p {
  margin-top: 7px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
  line-height: 1.55;
}

.hero-footer {
  color: rgba(255, 255, 255, 0.66);
  font-size: 13px;
}

@keyframes hero-in {
  from {
    opacity: 0;
    transform: translateX(-12px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@media (max-width: 1280px) {
  .capability-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .hero-section,
  .hero-content {
    min-height: auto;
  }

  .hero-content {
    padding: 36px 24px;
  }
}

@media (max-width: 700px) {
  .hero-content {
    gap: 24px;
    padding: 28px 18px;
  }

  .hero-brand h1 {
    font-size: 20px;
  }

  .hero-intro h2 {
    font-size: 30px;
  }

  .hero-intro p {
    font-size: 15px;
  }

  .capability-grid {
    grid-template-columns: 1fr;
  }
}
</style>
