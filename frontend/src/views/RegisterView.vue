<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const email = ref('')
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await authStore.register(username.value, password.value, email.value)
    router.push('/login')
  } catch (e: any) {
    if (e.code === 'ERR_NETWORK' || e.message?.includes('Network Error')) {
      error.value = '无法连接服务器，请确认后端已启动'
    } else if (e.response?.data?.message) {
      error.value = e.response.data.message
    } else {
      error.value = `注册失败: ${e.message || '未知错误'}`
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="bg-shape shape-1" />
      <div class="bg-shape shape-2" />
    </div>

    <div class="auth-card">
      <div class="card-header">
        <div class="logo-mark">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <h1>智能知识库</h1>
        <p>创建你的账号</p>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="field">
          <input v-model="username" type="text" placeholder="用户名" autocomplete="username" required />
        </div>
        <div class="field">
          <input v-model="email" type="email" placeholder="邮箱地址" autocomplete="email" required />
        </div>
        <div class="field">
          <input v-model="password" type="password" placeholder="密码 (至少6位)" autocomplete="new-password" required minlength="6" />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button type="submit" :disabled="loading" class="btn-register">
          <span v-if="loading" class="spinner" />
          <span>{{ loading ? '注册中...' : '注册' }}</span>
        </button>
      </form>

      <p class="footer-link">
        已有账号？<router-link to="/login">登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  position: relative;
  overflow: hidden;
  padding: 24px;
}
.auth-bg { position: absolute; inset: 0; pointer-events: none; }
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.35;
}
.shape-1 {
  width: 500px; height: 500px;
  background: var(--blue-light);
  top: -200px; right: -150px;
}
.shape-2 {
  width: 400px; height: 400px;
  background: var(--sage-light);
  bottom: -150px; left: -100px;
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 380px;
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: 36px 32px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  animation: fadeIn 0.6s var(--ease);
}

.card-header { text-align: center; margin-bottom: 28px; }
.logo-mark {
  width: 44px; height: 44px;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--dusty-blue), var(--sage));
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
}
.card-header h1 {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: 4px;
}
.card-header p { color: var(--text-muted); font-size: var(--text-xs); }

.field { margin-bottom: 14px; }
.field input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  font-size: var(--text-sm);
  font-family: var(--font);
  background: var(--surface-alt);
  color: var(--text);
  transition: border-color 0.2s var(--ease), box-shadow 0.2s var(--ease);
  outline: none;
}
.field input:focus {
  border-color: var(--dusty-blue);
  box-shadow: 0 0 0 4px var(--blue-light);
  background: var(--surface);
}
.field input::placeholder { color: var(--text-muted); }

.error-msg {
  color: var(--error);
  font-size: var(--text-xs);
  margin: 8px 0;
  padding: 8px 12px;
  background: var(--rose-light);
  border-radius: var(--radius-sm);
  border: 1px solid #edd5ce;
}

.btn-register {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--dusty-blue), #6d8091);
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 600;
  font-family: var(--font);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 4px;
  transition: transform 0.15s var(--ease), box-shadow 0.15s var(--ease);
}
.btn-register:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
.btn-register:active:not(:disabled) { transform: translateY(0); }
.btn-register:disabled { opacity: 0.7; cursor: not-allowed; }

.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.footer-link {
  text-align: center;
  margin-top: 20px;
  color: var(--text-muted);
  font-size: var(--text-xs);
}
.footer-link a { color: var(--dusty-blue); font-weight: 500; }
</style>
