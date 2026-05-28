<script setup lang="ts">
import { reactive, watch } from 'vue';

export type PlanForm = {
  subject: string;
  examDate: string;
  targetScore: number;
  dailyMinutes: number;
  currentLevel: string;
};

const props = defineProps<{ initial?: Partial<PlanForm>; loading?: boolean }>();
const emit = defineEmits<{ regenerate: [form: PlanForm] }>();

const form = reactive<PlanForm>({
  subject: '高数',
  examDate: '2026-06-20',
  targetScore: 85,
  dailyMinutes: 90,
  currentLevel: '目前基础较弱，需要从基础开始'
});

watch(
  () => props.initial,
  (value) => {
    if (!value) {
      return;
    }
    Object.assign(form, value);
  },
  { immediate: true }
);
</script>

<template>
  <section class="dash-card plan-builder">
    <div class="dash-card-head">
      <div>
        <span>Plan Builder</span>
        <h2>计划生成器</h2>
      </div>
    </div>
    <form class="builder-form" @submit.prevent="emit('regenerate', { ...form })">
      <label>科目<input v-model="form.subject" /></label>
      <label>考试日期<input v-model="form.examDate" type="date" /></label>
      <label>目标分数<input v-model.number="form.targetScore" type="number" min="1" max="100" /></label>
      <label>每日学习时间<input v-model.number="form.dailyMinutes" type="number" min="15" max="600" /></label>
      <label class="full">当前水平<textarea v-model="form.currentLevel" rows="4" /></label>
      <button class="dash-primary full" type="submit" :disabled="loading">{{ loading ? '生成中...' : '重新生成计划' }}</button>
    </form>
  </section>
</template>
