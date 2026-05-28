<script setup lang="ts">
import type { Component } from 'vue';

defineProps<{
  icon: Component;
  title: string;
  value: string;
  note: string;
  trend: string;
}>();
</script>

<template>
  <article class="dash-card stat-card">
    <div class="stat-icon-wrap">
      <component :is="icon" :size="21" />
    </div>
    <div>
      <span>{{ title }}</span>
      <strong>{{ value }}</strong>
      <p>{{ note }}</p>
    </div>
    <em>{{ trend }}</em>
  </article>
</template>
