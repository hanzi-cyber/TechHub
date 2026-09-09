import { http } from './request'
import type { User } from '@/types'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  email?: string
}

export interface LoginResult {
  token: string
  user: User
}

export function login(data: LoginParams) {
  return http.post<LoginResult>('/auth/login', data)
}

export function register(data: RegisterParams) {
  return http.post<void>('/auth/register', data)
}

export function logout() {
  return http.post<void>('/auth/logout')
}
