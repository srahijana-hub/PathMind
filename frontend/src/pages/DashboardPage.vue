<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import AssistantPanel, { type AssistantConversation, type ChatMessage, type SummaryReport } from '../components/dashboard/AssistantPanel.vue';
import DashboardHeader from '../components/dashboard/DashboardHeader.vue';
import DashboardSidebar from '../components/dashboard/DashboardSidebar.vue';
import KnowledgeProgress from '../components/dashboard/KnowledgeProgress.vue';
import PlanBuilder, { type PlanForm } from '../components/dashboard/PlanBuilder.vue';
import StatCard from '../components/dashboard/StatCard.vue';
import StudySummary from '../components/dashboard/StudySummary.vue';
import TodayTasks from '../components/dashboard/TodayTasks.vue';
import { BookOpen, CalendarCheck2, Clock3, Target } from 'lucide-vue-next';

type ApiResult<T> = {
  success: boolean;
  message: string;
  data: T | null;
};

type BackendTask = {
  taskId: number;
  dayIndex: number;
  taskDate: string;
  title: string;
  content: string;
  estimatedMinutes: number;
  status: string;
};

type BackendPlan = {
  planId: string;
  title: string;
  subject: string;
  examDate: string;
  dailyMinutes: number;
  targetScore: number;
  tasks: BackendTask[];
};

type BackendPlanItem = Omit<BackendPlan, 'tasks'> & { status: string };

type BackendSummary = {
  summary: string;
  suggestions: string[];
  completedTaskCount: number;
  totalTaskCount: number;
  completionRate: number;
};

type AssistantChatResp = {
  answer: string;
  latestSummary?: BackendSummary | null;
  reports?: SummaryReport[];
};

type AssistantFinishResp = {
  latestSummary: BackendSummary;
  reports: SummaryReport[];
};

export type StudyTask = {
  id: number;
  title: string;
  minutes: number;
  description: string;
  status: 'done' | 'doing' | 'todo';
};

const router = useRouter();
const toast = ref('');
const activeView = ref('overview');
const loading = ref(false);
const userName = ref(localStorage.getItem('userName') || '李同学');
const currentPlan = ref<BackendPlan | null>(null);
const planList = ref<BackendPlanItem[]>([]);
const summary = ref<BackendSummary | null>(null);
const conversations = ref<AssistantConversation[]>([]);
const activeConversationId = ref('');
const summaryReports = ref<SummaryReport[]>([]);

const fallbackTasks: StudyTask[] = [
  { id: 1, title: '极限基础', minutes: 90, description: '学习极限的定义、左右极限、极限四则运算', status: 'done' },
  { id: 2, title: '极限进阶与无穷小', minutes: 90, description: '两个重要极限、无穷小与无穷大、等价无穷小替换', status: 'doing' },
  { id: 3, title: '导数定义与求导法则', minutes: 60, description: '导数定义、基本求导公式、复合函数求导', status: 'todo' },
  { id: 4, title: '错题复盘', minutes: 30, description: '整理今日错题，记录易错知识点', status: 'todo' }
];

const localTasks = ref<StudyTask[]>(fallbackTasks);

const planForm = computed<Partial<PlanForm>>(() => ({
  subject: currentPlan.value?.subject || '高数',
  examDate: currentPlan.value?.examDate || '2026-06-20',
  targetScore: currentPlan.value?.targetScore || 85,
  dailyMinutes: currentPlan.value?.dailyMinutes || 90,
  currentLevel: '目前基础较弱，需要从基础开始'
}));

const tasks = computed(() => (currentPlan.value ? mapTasks(currentPlan.value.tasks) : localTasks.value));
const activeConversation = computed(() => conversations.value.find((conversation) => conversation.id === activeConversationId.value) || conversations.value[0]);
const chatMessages = computed(() => activeConversation.value?.messages ?? []);
const completedCount = computed(() => tasks.value.filter((task) => task.status === 'done').length);
const totalMinutes = computed(() => tasks.value.reduce((sum, task) => sum + task.minutes, 0));
const completionRate = computed(() => Math.round((completedCount.value / Math.max(tasks.value.length, 1)) * 100));
const knowledgeItems = computed(() => {
  const source = currentPlan.value?.tasks ?? fallbackTasks.map((task) => ({
    taskId: task.id,
    dayIndex: task.id,
    taskDate: '',
    title: task.title,
    content: task.description,
    estimatedMinutes: task.minutes,
    status: task.status === 'done' ? 'DONE' : 'TODO'
  }));
  const grouped = new Map<string, { doneCount: number; totalCount: number }>();

  source.forEach((task) => {
    const name = extractKnowledgeName(task.title, task.content);
    const current = grouped.get(name) ?? { doneCount: 0, totalCount: 0 };
    current.totalCount += 1;
    if (task.status === 'DONE') {
      current.doneCount += 1;
    }
    grouped.set(name, current);
  });

  return Array.from(grouped.entries())
    .map(([name, item]) => ({
      name,
      doneCount: item.doneCount,
      totalCount: item.totalCount,
      value: Math.round((item.doneCount / Math.max(item.totalCount, 1)) * 100)
    }))
    .slice(0, 6);
});

const stats = computed(() => [
  {
    icon: Target,
    title: '当前计划',
    value: currentPlan.value?.title || '高数 14 天冲刺',
    note: `目标分数 ${currentPlan.value?.targetScore || 85} 分`,
    trend: currentPlan.value ? '已同步' : '示例'
  },
  {
    icon: CalendarCheck2,
    title: '计划进度',
    value: `${completionRate.value}%`,
    note: `已完成 ${completedCount.value} / ${tasks.value.length} 天`,
    trend: `+${completionRate.value}%`
  },
  {
    icon: BookOpen,
    title: '今日学习',
    value: `${currentPlan.value?.dailyMinutes || 90} 分钟`,
    note: `建议完成 ${tasks.value.length} 个任务`,
    trend: '今日'
  },
  { icon: Clock3, title: '累计专注', value: `${(totalMinutes.value / 60).toFixed(1)} 小时`, note: '根据当前任务估算', trend: '+35%' }
]);

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const payload = (await response.json()) as ApiResult<T>;
  if (!payload.success || payload.data === null) {
    throw new Error(payload.message || '请求失败');
  }
  return payload.data;
}

function mapTasks(items: BackendTask[]): StudyTask[] {
  let firstTodoUsed = false;
  return items.map((task) => {
    const done = task.status === 'DONE';
    const status = done ? 'done' : firstTodoUsed ? 'todo' : 'doing';
    if (!done) {
      firstTodoUsed = true;
    }
    return {
      id: task.taskId,
      title: task.title,
      minutes: task.estimatedMinutes,
      description: task.content,
      status
    };
  });
}

function extractKnowledgeName(title: string, content: string) {
  const raw = title
    .replace(/^Day\s*\d+[:：-]?\s*/i, '')
    .split(/[：:，,、（(]/)[0]
    .replace(/复习|学习|练习|基础强化|综合|总结|巩固/g, '')
    .trim();
  if (raw.length >= 2 && raw.length <= 12) {
    return raw;
  }
  return content.split(/[，,。、；;（(]/)[0].slice(0, 12) || '综合知识点';
}

function showToast(text: string) {
  toast.value = text;
  window.setTimeout(() => {
    toast.value = '';
  }, 2200);
}

function conversationStorageKey(planId = currentPlan.value?.planId || 'default') {
  return `study-agent-assistant-conversations:${planId}`;
}

function createConversation(title = '新对话') {
  const conversation: AssistantConversation = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    title,
    messages: [],
    updatedAt: new Date().toISOString()
  };
  conversations.value = [conversation, ...conversations.value].slice(0, 5);
  activeConversationId.value = conversation.id;
  saveConversations();
}

function loadConversations(planId = currentPlan.value?.planId || 'default') {
  try {
    const raw = localStorage.getItem(conversationStorageKey(planId));
    const parsed = raw ? (JSON.parse(raw) as AssistantConversation[]) : [];
    conversations.value = Array.isArray(parsed) ? parsed.slice(0, 5) : [];
  } catch {
    conversations.value = [];
  }
  if (conversations.value.length === 0) {
    createConversation();
    return;
  }
  activeConversationId.value = conversations.value[0].id;
}

function saveConversations() {
  localStorage.setItem(conversationStorageKey(), JSON.stringify(conversations.value.slice(0, 5)));
}

function selectConversation(id: string) {
  activeConversationId.value = id;
}

function renameConversation(conversation: AssistantConversation, message: string) {
  if (conversation.title !== '新对话') {
    return;
  }
  conversation.title = message.length > 16 ? `${message.slice(0, 16)}...` : message;
}

function buildConversationText(messages: ChatMessage[] = chatMessages.value) {
  return messages
    .map((message) => `${message.role === 'user' ? '学生' : 'AI助手'}：${message.content}`)
    .join('\n');
}

function parseSseEvents(buffer: string) {
  const events = [];
  const parts = buffer.split(/\n\n/);
  const rest = parts.pop() || '';
  for (const part of parts) {
    let event = 'message';
    const dataLines: string[] = [];
    part.split(/\n/).forEach((line) => {
      if (line.startsWith('event:')) {
        event = line.slice('event:'.length).trim();
      }
      if (line.startsWith('data:')) {
        dataLines.push(line.slice('data:'.length).trimStart());
      }
    });
    events.push({ event, data: dataLines.join('\n') });
  }
  return { events, rest };
}

async function loadPlans() {
  try {
    planList.value = await request<BackendPlanItem[]>('/api/study/plans');
    if (planList.value.length > 0) {
      await selectPlan(planList.value[0].planId);
    }
  } catch {
    showToast('后端暂未连接，当前显示示例数据');
  }
}

async function selectPlan(planId: string) {
  currentPlan.value = await request<BackendPlan>(`/api/study/plans/${planId}`);
  loadConversations(planId);
  await loadSummary();
  await loadSummaryReports();
}

async function loadSummary() {
  if (!currentPlan.value) {
    return;
  }
  try {
    summary.value = await request<BackendSummary>(`/api/study/plans/${currentPlan.value.planId}/summary`);
  } catch {
    summary.value = null;
  }
}

async function loadSummaryReports() {
  if (!currentPlan.value) {
    return;
  }
  try {
    summaryReports.value = await request<SummaryReport[]>(`/api/study/plans/${currentPlan.value.planId}/summary-reports`);
  } catch {
    summaryReports.value = [];
  }
}

async function createPlan(form: PlanForm) {
  loading.value = true;
  try {
    currentPlan.value = await request<BackendPlan>('/api/study/plans', {
      method: 'POST',
      body: JSON.stringify(form)
    });
    loadConversations(currentPlan.value.planId);
    await loadSummary();
    await loadSummaryReports();
    await loadPlans();
    activeView.value = 'overview';
    showToast('计划已根据当前参数生成');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '计划生成失败');
  } finally {
    loading.value = false;
  }
}

async function sendAssistantMessage(message: string) {
  if (!currentPlan.value) {
    showToast('请先生成或选择一个学习计划');
    return;
  }
  if (!activeConversation.value) {
    createConversation();
  }
  const conversation = activeConversation.value;
  if (conversation.closed) {
    createConversation();
  }
  const targetConversation = activeConversation.value;
  targetConversation.messages.push({ role: 'user', content: message });
  targetConversation.updatedAt = new Date().toISOString();
  targetConversation.closed = false;
  renameConversation(targetConversation, message);
  const conversationText = buildConversationText(targetConversation.messages);
  targetConversation.messages.push({ role: 'assistant', content: '' });
  const assistantMessage = targetConversation.messages[targetConversation.messages.length - 1];
  saveConversations();
  loading.value = true;
  try {
    const response = await fetch(`/api/study/plans/${currentPlan.value.planId}/assistant/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        message,
        conversationText
      })
    });

    if (!response.ok || !response.body) {
      throw new Error('AI 流式响应失败');
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let done = false;
    while (!done) {
      const result = await reader.read();
      done = result.done;
      buffer += decoder.decode(result.value || new Uint8Array(), { stream: !done });
      const parsed = parseSseEvents(buffer);
      buffer = parsed.rest;
      parsed.events.forEach((item) => {
        if (item.event === 'message') {
          assistantMessage.content += item.data;
        }
        if (item.event === 'error') {
          throw new Error(item.data || 'AI 助手暂时不可用');
        }
      });
      targetConversation.updatedAt = new Date().toISOString();
      saveConversations();
    }

    if (!assistantMessage.content.trim()) {
      assistantMessage.content = 'AI 暂时没有返回内容，请稍后再试。';
    }
    targetConversation.updatedAt = new Date().toISOString();
    saveConversations();
    showToast('AI 已回复。需要更新计划时，点击“结束对话并更新”。');
  } catch (error) {
    assistantMessage.content = error instanceof Error ? error.message : 'AI 助手暂时不可用';
    showToast(error instanceof Error ? error.message : 'AI 助手暂时不可用');
  } finally {
    loading.value = false;
  }
}

async function finishAssistantConversation() {
  if (!currentPlan.value) {
    showToast('请先生成或选择一个学习计划');
    return;
  }
  const conversation = activeConversation.value;
  if (!conversation || conversation.messages.length === 0) {
    showToast('当前对话还没有内容');
    return;
  }
  loading.value = true;
  try {
    const data = await request<AssistantFinishResp>(`/api/study/plans/${currentPlan.value.planId}/assistant/finish`, {
      method: 'POST',
      body: JSON.stringify({ conversationText: buildConversationText(conversation.messages) })
    });
    summary.value = data.latestSummary;
    summaryReports.value = data.reports;
    conversation.closed = true;
    conversation.updatedAt = new Date().toISOString();
    saveConversations();
    currentPlan.value = await request<BackendPlan>(`/api/study/plans/${currentPlan.value.planId}`);
    showToast('已结合本次对话更新计划和学习总结');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '更新计划和总结失败');
  } finally {
    loading.value = false;
  }
}

async function toggleTask(id: number) {
  if (!currentPlan.value) {
    localTasks.value = localTasks.value.map((task) => (task.id === id ? { ...task, status: task.status === 'done' ? 'todo' : 'done' } : task));
    return;
  }
  const task = tasks.value.find((item) => item.id === id);
  if (!task) {
    return;
  }
  try {
    await request(`/api/study/plans/${currentPlan.value.planId}/feedback`, {
      method: 'POST',
      body: JSON.stringify({
        taskId: id,
        completed: task.status !== 'done',
        difficulty: '中等',
        problem: task.status === 'done' ? '需要重新复习' : '已完成该任务'
      })
    });
    await selectPlan(currentPlan.value.planId);
    showToast('任务反馈已同步到后端');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '任务状态更新失败');
  }
}

async function adjustPlan() {
  if (!currentPlan.value) {
    showToast('请先生成或选择一个学习计划');
    return;
  }
  loading.value = true;
  try {
    await request(`/api/study/plans/${currentPlan.value.planId}/adjust`, { method: 'POST' });
    await selectPlan(currentPlan.value.planId);
    showToast('已根据今日完成情况调整学习重点');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '智能调整失败');
  } finally {
    loading.value = false;
  }
}

function logout() {
  localStorage.removeItem('isLoggedIn');
  localStorage.removeItem('authToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('userName');
  router.push('/login');
}

onMounted(loadPlans);
</script>

<template>
  <main class="dashboard-page">
    <DashboardSidebar :user-name="userName" :active-view="activeView" @change-view="activeView = $event" @logout="logout" />
    <section class="dashboard-main">
      <DashboardHeader @adjust="adjustPlan" @new-plan="activeView = 'plans'" />

      <div v-if="toast" class="dashboard-toast">{{ toast }}</div>

      <template v-if="activeView === 'overview'">
        <section class="dashboard-grid stat-grid">
          <StatCard v-for="stat in stats" :key="stat.title" v-bind="stat" />
        </section>
        <section class="dashboard-grid work-grid">
          <TodayTasks :tasks="tasks" :completed-count="completedCount" @toggle-task="toggleTask" />
          <div class="middle-column">
            <PlanBuilder :initial="planForm" :loading="loading" @regenerate="createPlan" />
            <KnowledgeProgress :items="knowledgeItems" />
          </div>
          <StudySummary :summary="summary?.summary" :suggestions="summary?.suggestions" />
        </section>
      </template>

      <template v-else-if="activeView === 'plans'">
        <section class="dashboard-grid plan-view-grid">
          <PlanBuilder :initial="planForm" :loading="loading" @regenerate="createPlan" />
          <section class="dash-card plan-list-card">
            <div class="dash-card-head">
              <div>
                <span>Plans</span>
                <h2>我的学习计划</h2>
              </div>
            </div>
            <button v-for="plan in planList" :key="plan.planId" type="button" class="dash-plan-row" @click="selectPlan(plan.planId)">
              <strong>{{ plan.title }}</strong>
              <span>{{ plan.subject }} · {{ plan.examDate }} · 目标 {{ plan.targetScore }} 分</span>
            </button>
            <p v-if="planList.length === 0" class="dashboard-empty">暂无后端计划，先在左侧生成一个。</p>
          </section>
        </section>
      </template>

      <template v-else-if="activeView === 'tasks'">
        <TodayTasks :tasks="tasks" :completed-count="completedCount" @toggle-task="toggleTask" />
      </template>

      <template v-else-if="activeView === 'report'">
        <section class="dashboard-grid report-grid">
          <StudySummary :summary="summary?.summary" :suggestions="summary?.suggestions" />
          <KnowledgeProgress :items="knowledgeItems" />
        </section>
      </template>

      <template v-else-if="activeView === 'assistant'">
        <AssistantPanel
          :conversations="conversations"
          :active-conversation-id="activeConversationId"
          :messages="chatMessages"
          :reports="summaryReports"
          :latest-summary="summary?.summary"
          :loading="loading"
          :disabled="!currentPlan"
          @send="sendAssistantMessage"
          @refresh-reports="loadSummaryReports"
          @new-conversation="createConversation"
          @select-conversation="selectConversation"
          @finish-conversation="finishAssistantConversation"
        />
      </template>

      <template v-else>
        <section class="dash-card placeholder-card">
          <h2>个人设置</h2>
          <p>当前登录用户：{{ userName }}。后续可以在这里补充头像、目标院校、默认学习时间等设置。</p>
        </section>
      </template>
    </section>
  </main>
</template>
