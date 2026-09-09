import { http } from './request'
import type { PageResult, Post, Tag } from '@/types'

export function listTags() {
  return http.get<Tag[]>('/tags')
}

export function listTagPosts(id: number, params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<Post>>(`/tags/${id}/posts`, params)
}
