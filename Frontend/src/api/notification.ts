import { http } from './request'
import type { Notification, PageResult } from '@/types'

export function listNotifications(params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<Notification>>('/notifications', params)
}

export function unreadCount() {
  return http.get<number>('/notifications/unread-count')
}

export function markRead(ids: number[]) {
  return http.put<void>('/notifications/read', { ids })
}
