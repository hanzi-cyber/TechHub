import { http } from './request'
import type { FollowResult } from '@/types'

export function follow(userId: number) {
  return http.post<FollowResult>(`/follow/${userId}`)
}

export function unfollow(userId: number) {
  return http.delete<FollowResult>(`/follow/${userId}`)
}
