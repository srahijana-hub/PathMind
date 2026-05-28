<script setup lang="ts">
defineProps<{
  items: Array<{
    name: string;
    value: number;
    doneCount: number;
    totalCount: number;
  }>;
}>();
</script>

<template>
  <section class="dash-card knowledge-card">
    <div class="dash-card-head">
      <div>
        <span>Knowledge</span>
        <h2>知识点掌握</h2>
      </div>
    </div>
    <div class="knowledge-list">
      <div v-for="item in items" :key="item.name" class="knowledge-item">
        <div>
          <span>{{ item.name }}</span>
          <strong>{{ item.value }}%</strong>
        </div>
        <p><i :style="{ width: `${item.value}%` }" /></p>
        <small>{{ item.doneCount }} / {{ item.totalCount }} 个相关任务已完成</small>
      </div>
      <p v-if="items.length === 0" class="dashboard-empty">生成计划后，会根据任务自动统计知识点掌握情况。</p>
    </div>
  </section>
</template>
