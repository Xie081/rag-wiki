import api from './index'
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types'

export function login(data: LoginRequest) {
  return api.post<AuthResponse>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return api.post<AuthResponse>('/auth/register', data)
}
