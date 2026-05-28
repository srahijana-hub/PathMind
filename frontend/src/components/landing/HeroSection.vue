<script setup lang="ts">
import { ArrowRight, CheckCircle2, PlayCircle, Sparkles } from 'lucide-vue-next';
import ProductPreviewCard from './ProductPreviewCard.vue';

const tags = ['AI 智能规划', '动态任务调整', '学习进度可视化'];
</script>

<template>
  <section class="hero-section">
    <div class="section-container hero-grid">
      <div class="hero-copy">
        <div class="hero-badge">
          <Sparkles :size="16" />
          面向大学生的 AI 学习规划平台
        </div>
        <h1>
          AI 驱动的<br />
          个性化
          <span>学习规划助手</span>
        </h1>
        <p class="hero-description">
          根据考试日期、目标分数和当前水平，自动生成每日学习任务，并根据学习反馈动态调整计划，让学习更高效，让成长看得见。
        </p>
        <div class="hero-actions">
          <RouterLink class="primary-link" to="/register">
            立即开始
            <ArrowRight :size="18" />
          </RouterLink>
          <a class="secondary-link" href="#preview">
            <PlayCircle :size="18" />
            查看演示
          </a>
        </div>
        <div class="hero-tags">
          <span v-for="tag in tags" :key="tag">
            <CheckCircle2 :size="16" />
            {{ tag }}
          </span>
        </div>
      </div>

      <ProductPreviewCard />
    </div>
  </section>
</template>
