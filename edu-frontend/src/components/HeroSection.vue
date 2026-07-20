<script setup>
import { onMounted, ref } from 'vue'
import {
  Calendar,
  CollectionTag,
  EditPen,
  List,
  Reading,
  TrendCharts,
  User,
} from '@element-plus/icons-vue'

const tags = [
  '多教学班管理', '时间冲突检测', '学业预警',
  '在线考试', '周次课表', '选课审批',
  '弹性排课', '智能选课',
]

const typewriterText = ref('')
const fullText = '数字驱动的教学辅助管理平台'

onMounted(() => {
  let idx = 0
  const timer = setInterval(() => {
    if (idx < fullText.length) {
      typewriterText.value += fullText[idx]
      idx++
    } else {
      clearInterval(timer)
    }
  }, 80)
})

const capabilities = [
  {
    icon: CollectionTag,
    title: '教学班管理',
    description: '支持课程多教学班开设，教师灵活分配教学资源。',
  },
  {
    icon: List,
    title: '智能选课',
    description: '学生按教学班选课，时间冲突自动检测，审批流程完整。',
  },
  {
    icon: Calendar,
    title: '课表视图',
    description: '周次课表一目了然，当日课程快捷查看。',
  },
  {
    icon: EditPen,
    title: '作业考试',
    description: '在线作业提交、考试作答、自动评分与成绩管理。',
  },
  {
    icon: TrendCharts,
    title: '学业预警',
    description: '基于成绩分析自动预警，及时干预学习风险。',
  },
  {
    icon: User,
    title: '个人中心',
    description: '信息修改、密码重置、选课记录与学习进度追踪。',
  },
]
</script>

<template>
  <section class="hero-section" aria-label="平台介绍">
    <div class="hero-overlay"></div>
    <div class="geometry geometry-one"></div>
    <div class="geometry geometry-two"></div>
    <div class="geometry geometry-three"></div>
    <div class="dot dot-1"></div>
    <div class="dot dot-2"></div>
    <div class="dot dot-3"></div>

    <div class="hero-content">
      <header class="hero-brand">
        <div class="brand-symbol">
          <svg viewBox="0 0 56 56" width="34" height="34" fill="none">
            <circle cx="28" cy="28" r="26" fill="rgba(255,255,255,0.15)" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
            <path d="M28 8l-16 10v16l16 10 16-10V18L28 8z" fill="rgba(255,255,255,0.2)" stroke="rgba(255,255,255,0.5)" stroke-width="1.2"/>
            <path d="M28 16l-10 6.5v9L28 38l10-6.5v-9L28 16z" fill="rgba(255,255,255,0.3)" stroke="rgba(255,255,255,0.5)" stroke-width="1"/>
            <path d="M22 25v5l6 3.5 6-3.5v-5l-6-3.5-6 3.5z" fill="rgba(255,255,255,0.2)" stroke="rgba(255,255,255,0.45)" stroke-width="0.8"/>
            <circle cx="28" cy="27" r="2.5" fill="rgba(255,255,255,0.5)"/>
            <path d="M18 16l10-6 10 6" stroke="rgba(255,255,255,0.3)" stroke-width="1" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <h1>在线教育辅助教学系统</h1>
          <p>Smart Education Platform</p>
        </div>
      </header>

      <section class="hero-intro">
        <span class="hero-kicker">高校教务系统 · 智慧教学平台</span>
        <h2>{{ typewriterText }}</h2>
        <p>
          支持多教学班课程管理、学生选课审批、在线作业考试、学业预警分析等功能，
          为教师教学管理与学生自主学习提供稳定、高效、清晰的数字化支撑。
        </p>
      </section>

      <section class="capability-grid" aria-label="平台功能展示">
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

      <section class="tags-section">
        <span v-for="tag in tags" :key="tag" class="feature-tag">{{ tag }}</span>
      </section>

      <footer class="hero-footer">武汉理工大学 · 在线教育辅助教学系统 © 2026</footer>
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

.hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(255,255,255,0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(130,200,255,0.15) 0%, transparent 50%);
  animation: pulse-glow 3s ease-in-out infinite alternate;
  pointer-events: none;
}

@keyframes pulse-glow {
  0% { opacity: 0.3; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.03); }
}

@keyframes breathe-bg {
  0% { filter: brightness(1); }
  100% { filter: brightness(1.12); }
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
  width: 180px;
  height: 180px;
  right: 8%;
  top: 6%;
  transform: rotate(24deg);
  animation: float-1 5s ease-in-out infinite alternate;
}

.geometry-two {
  width: 260px;
  height: 260px;
  right: -100px;
  bottom: 10%;
  border-radius: 50%;
  animation: float-2 7s ease-in-out infinite alternate;
}

.geometry-three {
  width: 140px;
  height: 80px;
  left: 5%;
  bottom: 6%;
  transform: skewX(-16deg);
  animation: float-3 6s ease-in-out infinite alternate;
}

@keyframes float-1 {
  0% { transform: rotate(24deg) translateY(0px); }
  50% { transform: rotate(24deg) translateY(-30px) rotate(12deg); }
  100% { transform: rotate(24deg) translateY(0px); }
}

@keyframes float-2 {
  0% { transform: translateY(0px) scale(1); }
  50% { transform: translateY(-40px) scale(1.08); }
  100% { transform: translateY(0px) scale(1); }
}

@keyframes float-3 {
  0% { transform: skewX(-16deg) translateX(0px); }
  50% { transform: skewX(-16deg) translateX(30px) scaleY(1.1); }
  100% { transform: skewX(-16deg) translateX(0px); }
}

.dot {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  pointer-events: none;
}

.dot-1 {
  width: 8px; height: 8px;
  left: 18%; top: 14%;
  animation: float-1 4s ease-in-out infinite;
}

.dot-2 {
  width: 6px; height: 6px;
  right: 22%; top: 36%;
  animation: float-2 5s ease-in-out infinite;
}

.dot-3 {
  width: 10px; height: 10px;
  left: 42%; bottom: 28%;
  animation: float-3 4.5s ease-in-out infinite;
}

.hero-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 22px;
  justify-content: center;
  max-width: 980px;
  padding: 32px clamp(34px, 6vw, 80px);
}

.hero-brand {
  display: flex;
  gap: 14px;
  align-items: center;
}

.brand-symbol {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.16);
  box-shadow: 0 18px 40px rgba(6, 36, 86, 0.2);
}

.brand-symbol .el-icon {
  font-size: 24px;
}

.hero-brand h1 {
  font-size: 22px;
  line-height: 1.25;
  font-weight: 800;
  background: linear-gradient(90deg, #fff, rgba(255,255,255,0.75));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-brand p {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
}

.hero-intro {
  max-width: 720px;
}

.hero-kicker {
  display: block;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  font-weight: 700;
}

.hero-intro h2 {
  font-size: clamp(28px, 3.6vw, 44px);
  font-weight: 820;
  line-height: 1.12;
  min-height: 1.3em;
}

.hero-intro p {
  max-width: 680px;
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 15px;
  line-height: 1.7;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.capability-card {
  min-height: 110px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
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
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.18);
}

.capability-icon .el-icon {
  font-size: 18px;
}

.capability-card h3 {
  font-size: 15px;
  line-height: 1.3;
  font-weight: 780;
}

.capability-card p {
  margin-top: 5px;
  color: rgba(255, 255, 255, 0.76);
  font-size: 13px;
  line-height: 1.45;
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
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.tags-section {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,.3) transparent;
}

.tags-section::-webkit-scrollbar {
  height: 4px;
}

.tags-section::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,.3);
  border-radius: 4px;
}

.feature-tag {
  display: inline-block;
  padding: 6px 16px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.88);
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(6px);
  transition: background 180ms ease, transform 180ms ease;
}

.feature-tag:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
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
