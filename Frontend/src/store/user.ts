import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import * as authApi from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function setUserInfo(u: User) {
    userInfo.value = u
  }

  function clear() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function login(params: authApi.LoginParams) {
    const res = await authApi.login(params)
    setToken(res.token)
    setUserInfo(res.user)
    return res
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clear()
    }
  }

  return { token, userInfo, setToken, setUserInfo, clear, login, logout }
})
