<script setup>
import HeroSection from '../components/HeroSection.vue'
import LoginForm from '../components/LoginForm.vue'
</script>

<template>
  <main class="login-shell">
    <HeroSection />
    <section class="login-side" aria-label="用户登录">
      <LoginForm />
    </section>
  </main>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 65fr) minmax(420px, 35fr);
  overflow: hidden;
  background:
    radial-gradient(circle at 84% 12%, rgba(22, 119, 255, 0.14), transparent 30%),
    linear-gradient(135deg, #eef6ff 0%, #f7fbff 46%, #ffffff 100%);
}

.login-side {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: rgba(255, 255, 255, 0.72);
  animation: fade-in 560ms ease both;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@media (max-width: 1100px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-side {
    min-height: auto;
    padding: 36px 18px 44px;
  }
}
</style>
