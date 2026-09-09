import { http, request } from './request'
import type { Comment, PageResult } from '@/types'

export interface CreateCommentParams {
  content: string
  parentId?: number
  replyToUserId?: number
}

export function listComments(postId: number, params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<Comment>>(`/posts/${postId}/comments`, params)
}

export function createComment(postId: number, data: CreateCommentParams, idempotentKey?: string) {
  // 带幂等标识(Idempotent-Key),后端据此防重复提交
  return request<Comment>({
    url: `/posts/${postId}/comments`,
    method: 'POST',
    data,
    headers: idempotentKey ? { 'Idempotent-Key': idempotentKey } : undefined
  })
}

export function deleteComment(id: number) {
  return http.delete<void>(`/comments/${id}`)
}
