<script setup lang="ts">
defineProps<{
  summary?: string;
  suggestions?: string[];
}>();
</script>

<template>
  <section class="dash-card summary-card">
    <div class="dash-card-head">
      <div>
        <span>Summary</span>
        <h2>学习总结</h2>
      </div>
    </div>
    <p>{{ summary || '生成学习计划并提交任务反馈后，这里会展示阶段性学习总结。' }}</p>
    <ul>
      <li v-for="item in suggestions?.length ? suggestions : ['保持每天稳定学习节奏', '完成任务后及时提交反馈', '把薄弱知识点加入复盘清单']" :key="item">
        {{ item }}
      </li>
    </ul>
    <button class="dash-secondary" type="button">查看完整报告</button>
  </section>
</template>
