<script setup lang="ts">
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { BookOpen, CheckCircle2, Mail, LockKeyhole } from 'lucide-vue-next';

const router = useRouter();
const error = ref('');
const form = reactive({
  account: '',
  password: '',
  remember: true
});

type ApiResult<T> = {
  success: boolean;
  message: string;
  data: T | null;
};

type AuthResp = {
  token: string;
  userId: string;
  nickname: string;
  account: string;
};

async function login() {
  error.value = '';
  if (!form.account.trim() || !form.password.trim()) {
    error.value = '请输入账号和密码';
    return;
  }
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        account: form.account,
        password: form.password
      })
    });
    const result = (await response.json()) as ApiResult<AuthResp>;
    if (!result.success || !result.data) {
      error.value = result.message || '登录失败';
      return;
    }
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('authToken', result.data.token);
    localStorage.setItem('userId', result.data.userId);
    localStorage.setItem('userName', result.data.nickname);
    router.push('/dashboard');
  } catch {
    error.value = '无法连接登录服务，请确认后端已启动';
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-shell">
      <aside class="auth-intro">
        <RouterLink class="auth-logo" to="/">
          <span><BookOpen :size="22" /></span>
          StudyAgent
        </RouterLink>
        <div>
          <p class="auth-kicker">Welcome back</p>
          <h1>欢迎回到 StudyAgent</h1>
          <p>继续你的学习计划，让每一天的努力都有方向。</p>
        </div>
        <div class="auth-benefits">
          <span><CheckCircle2 :size="17" /> 个性化学习计划</span>
          <span><CheckCircle2 :size="17" /> 每日任务追踪</span>
          <span><CheckCircle2 :size="17" /> 学习进度反馈</span>
        </div>
      </aside>

      <section class="auth-card">
        <h2>登录账号</h2>
        <p>请输入账号信息进入学习工作台</p>
        <div v-if="error" class="form-error">{{ error }}</div>
        <form class="auth-form" @submit.prevent="login">
          <label>
            手机号 / 邮箱
            <span class="input-wrap">
              <Mail :size="18" />
              <input v-model="form.account" type="text" placeholder="请输入手机号或邮箱" />
            </span>
          </label>
          <label>
            密码
            <span class="input-wrap">
              <LockKeyhole :size="18" />
              <input v-model="form.password" type="password" placeholder="请输入密码" />
            </span>
          </label>
          <div class="form-row">
            <label class="check-line">
              <input v-model="form.remember" type="checkbox" />
              记住我
            </label>
            <a href="#">忘记密码</a>
          </div>
          <button class="auth-submit" type="submit">登录</button>
        </form>
        <p class="auth-switch">还没有账号？<RouterLink to="/register">立即注册</RouterLink></p>
      </section>
    </section>
  </main>
</template>
