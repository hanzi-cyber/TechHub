import { http } from './request'
import type { LikeResult } from '@/types'

/** 点赞:targetType 1帖子 2评论 */
export function like(targetType: number, targetId: number) {
  return http.post<LikeResult>('/like', { targetType, targetId })
}

export function unlike(targetType: number, targetId: number) {
  return http.delete<LikeResult>('/like', { targetType, targetId })
}
