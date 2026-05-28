<script setup lang="ts">
import { Bell, Plus, RefreshCw, Search, UserRound } from 'lucide-vue-next';

defineEmits<{ adjust: []; newPlan: [] }>();
</script>

<template>
  <header class="dash-header">
    <div>
      <h1>学习工作台</h1>
      <p>高数 14 天冲刺计划 · 目标 85 分</p>
    </div>
    <div class="dash-header-actions">
      <label class="dash-search">
        <Search :size="17" />
        <input type="text" placeholder="搜索任务、知识点" />
      </label>
      <button class="dash-secondary" type="button" @click="$emit('adjust')">
        <RefreshCw :size="17" />
        智能调整
      </button>
      <button class="dash-primary" type="button" @click="$emit('newPlan')">
        <Plus :size="17" />
        生成新计划
      </button>
      <button class="dash-icon-button" type="button" aria-label="通知">
        <Bell :size="18" />
      </button>
      <span class="dash-top-avatar"><UserRound :size="18" /></span>
    </div>
  </header>
</template>
