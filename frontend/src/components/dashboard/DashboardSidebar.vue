<script setup lang="ts">
import { BookOpen, Bot, CircleHelp, FileText, LayoutDashboard, ListChecks, LogOut, Settings, UserRound } from 'lucide-vue-next';

defineProps<{ userName: string; activeView: string }>();
defineEmits<{ logout: []; changeView: [view: string] }>();

const navItems = [
  { icon: LayoutDashboard, label: '总览', view: 'overview' },
  { icon: BookOpen, label: '学习计划', view: 'plans' },
  { icon: ListChecks, label: '每日任务', view: 'tasks' },
  { icon: FileText, label: '学习报告', view: 'report' },
  { icon: Bot, label: 'AI 助手', view: 'assistant' },
  { icon: Settings, label: '个人设置', view: 'settings' }
];
</script>

<template>
  <aside class="dash-sidebar">
    <div class="dash-brand">
      <span><BookOpen :size="22" /></span>
      <div>
        <strong>StudyAgent</strong>
        <small>学习助手工作台</small>
      </div>
    </div>

    <nav class="dash-nav">
      <button
        v-for="item in navItems"
        :key="item.label"
        type="button"
        :class="{ active: activeView === item.view }"
        @click="$emit('changeView', item.view)"
      >
        <component :is="item.icon" :size="18" />
        {{ item.label }}
      </button>
    </nav>

    <div class="dash-sidebar-bottom">
      <button class="help-link" type="button">
        <CircleHelp :size="18" />
        帮助中心
      </button>
      <div class="dash-user-card">
        <span class="dash-avatar"><UserRound :size="18" /></span>
        <div>
          <strong>{{ userName }}</strong>
          <small>学习中</small>
        </div>
      </div>
      <button class="logout-button" type="button" @click="$emit('logout')">
        <LogOut :size="17" />
        退出登录
      </button>
    </div>
  </aside>
</template>
