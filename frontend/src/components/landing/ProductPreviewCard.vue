<script setup lang="ts">
import { BookOpen, CalendarDays, Home, LineChart, ListChecks } from 'lucide-vue-next';

const bars = [34, 58, 46, 72, 61, 84, 76];
const tasks = [
  { title: '复习二叉树遍历', status: '进行中' },
  { title: '完成链表综合题 6 道', status: '已完成' },
  { title: '整理错题与薄弱点', status: '待开始' }
];
</script>

<template>
  <div id="preview" class="preview-card" aria-label="产品界面预览">
    <aside class="preview-nav">
      <span class="preview-nav-logo"><BookOpen :size="17" /></span>
      <span class="preview-nav-item active"><Home :size="17" /></span>
      <span class="preview-nav-item"><ListChecks :size="17" /></span>
      <span class="preview-nav-item"><LineChart :size="17" /></span>
      <span class="preview-nav-item"><CalendarDays :size="17" /></span>
    </aside>

    <section class="preview-main">
      <div class="preview-top">
        <div>
          <span>StudyAgent Dashboard</span>
          <h3>今日学习概览</h3>
        </div>
        <div class="preview-user" />
      </div>

      <div class="preview-stats">
        <article>
          <span>计划进度</span>
          <strong>72%</strong>
        </article>
        <article>
          <span>今日专注</span>
          <strong>45min</strong>
        </article>
        <article>
          <span>任务完成</span>
          <strong>8/10</strong>
        </article>
      </div>

      <div class="preview-analytics">
        <div class="mini-chart">
          <i v-for="(bar, index) in bars" :key="index" :style="{ height: `${bar}%` }" />
        </div>
        <div class="progress-circle">
          <svg viewBox="0 0 120 120" role="img" aria-label="72% 进度">
            <circle cx="60" cy="60" r="48" />
            <circle class="progress" cx="60" cy="60" r="48" />
          </svg>
          <strong>72%</strong>
        </div>
      </div>

      <div class="preview-tasks">
        <div v-for="task in tasks" :key="task.title" class="preview-task">
          <span />
          <p>{{ task.title }}</p>
          <em>{{ task.status }}</em>
        </div>
      </div>
    </section>
  </div>
</template>
