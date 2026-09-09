import { http } from './request'
import type { CollectResult } from '@/types'

export function collect(postId: number) {
  return http.post<CollectResult>('/collect', { postId })
}

export function uncollect(postId: number) {
  return http.delete<CollectResult>('/collect', { postId })
}
