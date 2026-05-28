<script setup lang="ts">
const data = [45, 70, 50, 90, 65, 110, 95];
</script>

<template>
  <section class="dash-card chart-card">
    <div class="dash-card-head">
      <div>
        <span>Trend</span>
        <h2>学习趋势</h2>
      </div>
    </div>
    <div class="trend-bars">
      <i v-for="(item, index) in data" :key="index" :style="{ height: `${item}px` }" />
    </div>
    <div class="chart-labels">
      <span v-for="day in ['一', '二', '三', '四', '五', '六', '日']" :key="day">周{{ day }}</span>
    </div>
  </section>
</template>
