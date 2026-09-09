import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器:自动带上 token
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    // 业界标准:Authorization: Bearer <token>
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

// 响应拦截器:统一解包 Result
service.interceptors.response.use(
  (response) => {
    const res = response.data as Result<unknown>
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 直接返回 data
    return res.data as never
  },
  (error) => {
    // 401:token 失效 / 被单点登录踢下线,清理登录态并跳转登录页
    if (error?.response?.status === 401) {
      localStorage.removeItem('token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    const msg = error?.response?.data?.message || '网络错误,请稍后重试'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

/** 统一请求方法,返回解包后的 data */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as unknown as Promise<T>
}

export const http = {
  get: <T = unknown>(url: string, params?: object) =>
    request<T>({ url, method: 'GET', params }),
  post: <T = unknown>(url: string, data?: object) =>
    request<T>({ url, method: 'POST', data }),
  put: <T = unknown>(url: string, data?: object) =>
    request<T>({ url, method: 'PUT', data }),
  delete: <T = unknown>(url: string, params?: object, data?: object) =>
    request<T>({ url, method: 'DELETE', params, data })
}
