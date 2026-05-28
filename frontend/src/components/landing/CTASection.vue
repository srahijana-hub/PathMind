<script setup lang="ts">
import { ArrowRight } from 'lucide-vue-next';
</script>

<template>
  <section id="cta" class="cta-section">
    <div class="section-container">
      <div class="cta-card">
        <h2>现在开始，让 AI 帮你规划下一次进步</h2>
        <p>输入你的学习目标，StudyAgent 将为你生成一份可执行、可调整、可反馈的学习计划。</p>
        <RouterLink to="/register" class="cta-button">
          立即生成学习计划
          <ArrowRight :size="18" />
        </RouterLink>
      </div>
    </div>
  </section>
</template>
