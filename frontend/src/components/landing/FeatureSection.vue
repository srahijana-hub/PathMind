<script setup lang="ts">
import { BarChart3, BrainCircuit, RefreshCw, Route } from 'lucide-vue-next';

const features = [
  {
    icon: BrainCircuit,
    title: '智能计划生成',
    description: '输入考试日期、目标分数和每日可用时间，AI 自动拆分学习路径，生成每日任务。'
  },
  {
    icon: RefreshCw,
    title: '动态任务调整',
    description: '根据任务完成情况和学习反馈，自动调整后续计划优先级，避免计划失控。'
  },
  {
    icon: BarChart3,
    title: '学习进度可视化',
    description: '通过进度条、趋势图和完成率看板，清晰了解当前学习状态。'
  },
  {
    icon: Route,
    title: '知识点掌握分析',
    description: '定位薄弱知识点，生成针对性练习建议，提高复习效率。'
  }
];
</script>

<template>
  <section id="features" class="feature-section page-section">
    <div class="section-container">
      <div class="section-heading">
        <span>Core Features</span>
        <h2>让学习计划真正适合你</h2>
        <p>从目标拆解到每日执行，从学习反馈到智能调整，StudyAgent 帮你完成完整学习闭环。</p>
      </div>

      <div class="feature-grid">
        <article v-for="feature in features" :key="feature.title" class="feature-card">
          <div class="feature-icon">
            <component :is="feature.icon" :size="24" />
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </article>
      </div>
    </div>
  </section>
</template>
