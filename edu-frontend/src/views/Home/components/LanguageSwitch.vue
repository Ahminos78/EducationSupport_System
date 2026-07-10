<script setup>
import { ref } from 'vue'

const currentLang = ref('中文')
const languages = ['中文', 'English']

function selectLang(lang) {
  currentLang.value = lang
}
</script>

<template>
  <el-dropdown trigger="click" @command="selectLang">
    <el-tooltip content="语言切换" placement="bottom">
      <el-icon :size="20" class="lang-icon">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <path d="M2 12h20" />
          <path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z" />
        </svg>
      </el-icon>
    </el-tooltip>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="lang in languages"
          :key="lang"
          :command="lang"
          :class="{ 'is-selected': lang === currentLang }"
        >
          {{ lang }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped lang="scss">
.lang-icon {
  color: #555;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #1677ff;
  }
}

.el-dropdown-menu__item.is-selected {
  color: #1677ff;
  font-weight: 600;
}
</style>
