import { http } from './request'
import type { PageResult, Post } from '@/types'

export interface PostListParams {
  pageNum: number
  pageSize: number
  sort?: 'latest' | 'hot'
  keyword?: string
  tagId?: number
}

export interface SavePostParams {
  id?: number
  title: string
  content: string
  summary?: string
  tagIds?: number[]
}

export function listPosts(params: PostListParams) {
  return http.get<PageResult<Post>>('/posts', params)
}

export function getPostDetail(id: number) {
  return http.get<Post>(`/posts/${id}`)
}

export function getFollowFeed(params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<Post>>('/posts/feed', params)
}

export function createPost(data: SavePostParams) {
  return http.post<Post>('/posts', data)
}

export function updatePost(id: number, data: SavePostParams) {
  return http.put<Post>(`/posts/${id}`, data)
}

export function deletePost(id: number) {
  return http.delete<void>(`/posts/${id}`)
}
