<script setup lang="ts">
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { BookOpen, CheckCircle2, LockKeyhole, Mail, UserRound } from 'lucide-vue-next';

const router = useRouter();
const error = ref('');
const form = reactive({
  name: '',
  account: '',
  password: '',
  confirmPassword: '',
  agreed: false
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

async function register() {
  error.value = '';
  if (!form.name.trim() || !form.account.trim() || !form.password.trim() || !form.confirmPassword.trim()) {
    error.value = '请完整填写注册信息';
    return;
  }
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致';
    return;
  }
  if (!form.agreed) {
    error.value = '请先同意用户协议和隐私政策';
    return;
  }
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nickname: form.name,
        account: form.account,
        password: form.password
      })
    });
    const result = (await response.json()) as ApiResult<AuthResp>;
    if (!result.success || !result.data) {
      error.value = result.message || '注册失败';
      return;
    }
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('authToken', result.data.token);
    localStorage.setItem('userId', result.data.userId);
    localStorage.setItem('userName', result.data.nickname);
    router.push('/dashboard');
  } catch {
    error.value = '无法连接注册服务，请确认后端已启动';
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
          <p class="auth-kicker">Create account</p>
          <h1>创建你的学习空间</h1>
          <p>用一个清晰、可执行、可反馈的计划，开始下一次进步。</p>
        </div>
        <div class="auth-benefits">
          <span><CheckCircle2 :size="17" /> 专属计划生成</span>
          <span><CheckCircle2 :size="17" /> 任务反馈闭环</span>
          <span><CheckCircle2 :size="17" /> 阶段学习报告</span>
        </div>
      </aside>

      <section class="auth-card">
        <h2>创建账号</h2>
        <p>开始生成你的专属学习计划</p>
        <div v-if="error" class="form-error">{{ error }}</div>
        <form class="auth-form" @submit.prevent="register">
          <label>
            昵称
            <span class="input-wrap">
              <UserRound :size="18" />
              <input v-model="form.name" type="text" placeholder="例如：李同学" />
            </span>
          </label>
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
          <label>
            确认密码
            <span class="input-wrap">
              <LockKeyhole :size="18" />
              <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" />
            </span>
          </label>
          <label class="check-line">
            <input v-model="form.agreed" type="checkbox" />
            我已阅读并同意用户协议和隐私政策
          </label>
          <button class="auth-submit" type="submit">注册</button>
        </form>
        <p class="auth-switch">已有账号？<RouterLink to="/login">去登录</RouterLink></p>
      </section>
    </section>
  </main>
</template>
