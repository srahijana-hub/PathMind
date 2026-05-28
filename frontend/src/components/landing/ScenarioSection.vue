<script setup lang="ts">
import { ArrowRight, BookMarked, GraduationCap, Trophy } from 'lucide-vue-next';

const scenarios = [
  {
    icon: Trophy,
    title: '期末考试冲刺',
    description: '根据剩余时间自动安排复习节奏，优先处理高频考点和薄弱模块。'
  },
  {
    icon: GraduationCap,
    title: '考研 / 专升本备考',
    description: '把长期目标拆成阶段计划，结合反馈持续优化复习优先级。'
  },
  {
    icon: BookMarked,
    title: '数学建模 / 课程学习',
    description: '围绕课程项目、竞赛训练和知识补全，形成可执行的学习路线。'
  }
];
</script>

<template>
  <section class="scenario-section page-section">
    <div class="section-container">
      <div class="section-heading">
        <span>Use Cases</span>
        <h2>适用于多种学习场景</h2>
      </div>

      <div class="scenario-grid">
        <article v-for="scenario in scenarios" :key="scenario.title" class="scenario-card">
          <div class="scenario-icon">
            <component :is="scenario.icon" :size="22" />
          </div>
          <h3>{{ scenario.title }}</h3>
          <p>{{ scenario.description }}</p>
          <a href="#cta">
            查看方案
            <ArrowRight :size="16" />
          </a>
        </article>
      </div>
    </div>
  </section>
</template>
