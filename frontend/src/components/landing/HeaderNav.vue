<script setup lang="ts">
import { ArrowRight, GraduationCap, Menu } from 'lucide-vue-next';

const navItems = ['产品功能', '解决方案', '使用场景', '定价', '资源中心'];
</script>

<template>
  <header class="site-header">
    <div class="header-inner">
      <a class="logo" href="#" aria-label="StudyAgent 首页">
        <span class="logo-mark">
          <GraduationCap :size="22" />
        </span>
        <span>StudyAgent</span>
      </a>

      <nav class="desktop-nav" aria-label="官网导航">
        <a v-for="item in navItems" :key="item" href="#features">{{ item }}</a>
      </nav>

      <div class="header-actions">
        <RouterLink class="login-link" to="/login">登录</RouterLink>
        <RouterLink class="header-cta" to="/register">
          立即开始
          <ArrowRight :size="18" />
        </RouterLink>
      </div>

      <button class="mobile-menu" type="button" aria-label="打开菜单">
        <Menu :size="22" />
      </button>
    </div>
  </header>
</template>
