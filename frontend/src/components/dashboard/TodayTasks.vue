<script setup lang="ts">
import type { StudyTask } from '../../pages/DashboardPage.vue';
import { Clock3, PlayCircle } from 'lucide-vue-next';

defineProps<{
  tasks: StudyTask[];
  completedCount: number;
}>();

defineEmits<{ toggleTask: [id: number] }>();

function statusText(status: StudyTask['status']) {
  return status === 'done' ? '已完成' : status === 'doing' ? '进行中' : '待开始';
}
</script>

<template>
  <section class="dash-card today-card">
    <div class="dash-card-head">
      <div>
        <span>Today Tasks</span>
        <h2>今日任务</h2>
      </div>
      <strong>{{ completedCount }} / {{ tasks.length }} 已完成</strong>
    </div>

    <div class="task-stack">
      <article v-for="task in tasks" :key="task.id" class="study-task" :class="task.status">
        <button class="task-check" type="button" @click="$emit('toggleTask', task.id)" :aria-label="`切换 ${task.title} 状态`" />
        <div class="task-copy">
          <h3>{{ task.title }}</h3>
          <p>{{ task.description }}</p>
          <span><Clock3 :size="15" /> {{ task.minutes }} 分钟</span>
        </div>
        <div class="task-actions">
          <em>{{ statusText(task.status) }}</em>
          <button type="button">
            <PlayCircle :size="15" />
            {{ task.status === 'todo' ? '开始学习' : '查看' }}
          </button>
        </div>
      </article>
    </div>
  </section>
</template>
