import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const { data } = await api.post('/auth/login', { username, password })
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    return data
  }

  async function register(username: string, password: string, email: string) {
    const { data } = await api.post('/auth/register', { username, password, email })
    return data
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    window.location.href = '/login'
  }

  return { user, token, isAuthenticated, login, register, logout }
})
