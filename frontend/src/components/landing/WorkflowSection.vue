<script setup lang="ts">
const steps = [
  {
    number: '01',
    title: '设置学习目标',
    description: '输入科目、考试时间、目标分数和当前水平。'
  },
  {
    number: '02',
    title: '生成学习计划',
    description: '系统自动拆分知识点，安排每日学习任务。'
  },
  {
    number: '03',
    title: '每日执行反馈',
    description: '完成任务后记录掌握程度和学习状态。'
  },
  {
    number: '04',
    title: 'AI 动态优化',
    description: '根据反馈自动调整后续任务和复习重点。'
  }
];
</script>

<template>
  <section class="workflow-section page-section">
    <div class="section-container">
      <div class="section-heading">
        <span>Workflow</span>
        <h2>从目标到结果，只需要四步</h2>
      </div>

      <div class="workflow-grid">
        <article v-for="step in steps" :key="step.number" class="workflow-card">
          <strong>{{ step.number }}</strong>
          <h3>{{ step.title }}</h3>
          <p>{{ step.description }}</p>
        </article>
      </div>
    </div>
  </section>
</template>
