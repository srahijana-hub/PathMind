<script setup lang="ts">
import { computed, ref } from 'vue';
import { Bot, CheckCircle2, History, MessageCircle, Plus, Send, UserRound } from 'lucide-vue-next';

export type ChatMessage = {
  role: 'user' | 'assistant';
  content: string;
};

export type AssistantConversation = {
  id: string;
  title: string;
  messages: ChatMessage[];
  updatedAt: string;
  closed?: boolean;
};

export type SummaryReport = {
  reportId: number;
  userQuestion: string;
  aiAnswer: string;
  summary: string;
  suggestions: string[];
  createdAt: string;
};

const props = defineProps<{
  conversations: AssistantConversation[];
  activeConversationId: string;
  messages: ChatMessage[];
  reports: SummaryReport[];
  latestSummary?: string;
  loading?: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  send: [message: string];
  refreshReports: [];
  newConversation: [];
  selectConversation: [id: string];
  finishConversation: [];
}>();
const input = ref('');
const showReports = ref(false);
const confirmingFinish = ref(false);

const hasMessages = computed(() => props.messages.length > 0);

function sendMessage() {
  if (!input.value.trim()) {
    return;
  }
  emit('send', input.value.trim());
  input.value = '';
}

function askFinishConversation() {
  if (!hasMessages.value || props.disabled || props.loading) {
    return;
  }
  confirmingFinish.value = true;
}

function confirmFinishConversation() {
  confirmingFinish.value = false;
  emit('finishConversation');
}
</script>

<template>
  <section class="assistant-layout">
    <aside class="dash-card assistant-session-card">
      <div class="assistant-session-head">
        <div>
          <span>Conversations</span>
          <h2>对话记录</h2>
        </div>
        <button class="icon-action" type="button" title="新对话" @click="emit('newConversation')">
          <Plus :size="17" />
        </button>
      </div>

      <div class="assistant-session-list">
        <button
          v-for="conversation in conversations"
          :key="conversation.id"
          type="button"
          class="assistant-session-row"
          :class="{ active: conversation.id === activeConversationId }"
          @click="emit('selectConversation', conversation.id)"
        >
          <MessageCircle :size="16" />
          <span>
            <strong>{{ conversation.title }}</strong>
            <small>{{ conversation.closed ? '已更新总结' : conversation.messages.length + ' 条消息' }}</small>
          </span>
        </button>
      </div>

      <p class="assistant-session-tip">
        最多保留 5 段历史对话。聊天只会调用一次 AI，点击结束对话后才统一更新计划和总结。
      </p>
    </aside>

    <div class="dash-card assistant-chat-card">
      <div class="dash-card-head assistant-chat-head">
        <div>
          <span>AI Assistant</span>
          <h2>AI 学习助手</h2>
        </div>
        <button class="dash-secondary finish-chat-btn" type="button" :disabled="disabled || loading || !hasMessages" @click="askFinishConversation">
          <CheckCircle2 :size="17" />
          结束对话并更新
        </button>
      </div>

      <div v-if="confirmingFinish" class="finish-confirm">
        <span>确定结束本次对话，并结合全部聊天内容更新学习计划和总结吗？</span>
        <div>
          <button type="button" class="dash-secondary" @click="confirmingFinish = false">取消</button>
          <button type="button" class="dash-primary" @click="confirmFinishConversation">确认更新</button>
        </div>
      </div>

      <div class="assistant-messages">
        <div v-if="messages.length === 0" class="assistant-empty">
          <Bot :size="28" />
          <strong>可以直接向我提问你的学习计划</strong>
          <p>例如：我今天的任务太多怎么办？链表还是不会该怎么调整？</p>
        </div>
        <article v-for="(message, index) in messages" :key="index + message.content + message.role" class="chat-message" :class="message.role">
          <span class="chat-avatar">
            <UserRound v-if="message.role === 'user'" :size="16" />
            <Bot v-else :size="16" />
          </span>
          <p>{{ message.content }}</p>
        </article>
      </div>

      <form class="assistant-input" @submit.prevent="sendMessage">
        <input v-model="input" :disabled="disabled || loading" placeholder="输入你对计划的疑问，AI 会先专注回答；需要时再统一更新计划" />
        <button class="dash-primary" type="submit" :disabled="disabled || loading">
          <Send :size="17" />
          {{ loading ? '思考中' : '发送' }}
        </button>
      </form>
    </div>

    <aside class="dash-card assistant-summary-card">
      <div class="dash-card-head">
        <div>
          <span>Latest Report</span>
          <h2>实时总结</h2>
        </div>
      </div>
      <p>{{ latestSummary || '和 AI 助手对话后，这里会显示最新一版学习总结。' }}</p>
      <button class="dash-secondary" type="button" @click="showReports = !showReports; emit('refreshReports')">
        <History :size="16" />
        {{ showReports ? '收起完整报告' : '查看完整报告' }}
      </button>

      <div v-if="showReports" class="report-history">
        <article v-for="report in reports" :key="report.reportId">
          <strong>{{ report.createdAt?.replace('T', ' ') }}</strong>
          <span>提问：{{ report.userQuestion }}</span>
          <p>{{ report.summary }}</p>
        </article>
        <p v-if="reports.length === 0" class="dashboard-empty">暂无历史总结。</p>
      </div>
    </aside>
  </section>
</template>
