<script setup lang="ts">
import { CheckCircle2, Star, UsersRound, Workflow } from 'lucide-vue-next';

const stats = [
  { icon: UsersRound, value: '50万+', label: '用户信赖' },
  { icon: Workflow, value: '3000万+', label: '学习任务完成' },
  { icon: CheckCircle2, value: '98.6%', label: '用户满意度' },
  { icon: Star, value: '4.9/5', label: '应用评分' }
];
</script>

<template>
  <section class="stats-section">
    <div class="section-container">
      <div class="stats-card">
        <article v-for="stat in stats" :key="stat.label">
          <div class="stat-icon">
            <component :is="stat.icon" :size="20" />
          </div>
          <strong>{{ stat.value }}</strong>
          <span>{{ stat.label }}</span>
        </article>
      </div>
    </div>
  </section>
</template>
