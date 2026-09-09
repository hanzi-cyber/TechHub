import { http } from './request'
import type { PageResult, Post, User } from '@/types'

export function getMe() {
  return http.get<User>('/user/me')
}

export function getUserById(id: number) {
  return http.get<User>(`/user/${id}`)
}

export function updateMe(data: Partial<User>) {
  return http.put<User>('/user/me', data)
}

export function getUserPosts(id: number, params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<Post>>(`/user/${id}/posts`, params)
}

export function getFollowers(id: number, params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<User>>(`/user/${id}/followers`, params)
}

export function getFollowing(id: number, params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<User>>(`/user/${id}/following`, params)
}
