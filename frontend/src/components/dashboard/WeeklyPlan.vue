<script setup lang="ts">
const week = [
  ['周一', '极限基础'],
  ['周二', '极限进阶'],
  ['周三', '导数基础'],
  ['周四', '导数应用'],
  ['周五', '积分基础'],
  ['周六', '综合训练'],
  ['周日', '错题复盘']
];
</script>

<template>
  <section class="dash-card week-card">
    <div class="dash-card-head">
      <div>
        <span>Schedule</span>
        <h2>本周安排</h2>
      </div>
    </div>
    <div class="week-list">
      <div v-for="item in week" :key="item[0]">
        <strong>{{ item[0] }}</strong>
        <span>{{ item[1] }}</span>
      </div>
    </div>
  </section>
</template>
