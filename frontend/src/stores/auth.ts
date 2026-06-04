import { defineStore } from 'pinia'
import api from '@/api'

export const useAuthStore = defineStore('auth', () => {

  async function login(username: string, password: string) {
    const { data } = await api.post('/auth/login', { username, password })
    localStorage.setItem('token', data.token)
    return data
  }

  async function register(username: string, password: string, email: string) {
    const { data } = await api.post('/auth/register', { username, password, email })
    return data
  }

  function logout() {
    localStorage.removeItem('token')
    window.location.href = '/login'
  }

  return { login, register, logout }
})
