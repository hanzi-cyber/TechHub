// 与后端统一返回体对齐
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  avatarUrl?: string
  bio?: string
  status?: number
  followed?: boolean
  createdAt?: string
}

export interface Tag {
  id: number
  name: string
  description?: string
  postCount?: number
}

export interface Post {
  id: number
  userId: number
  title: string
  summary?: string
  content?: string
  status?: number
  viewCount: number
  likeCount: number
  commentCount: number
  collectCount: number
  liked?: boolean
  collected?: boolean
  isTop?: number
  createdAt: string
  publishedAt?: string
  author?: User
  tags?: Tag[]
}

export interface Comment {
  id: number
  postId: number
  userId: number
  parentId: number
  replyToUserId?: number
  content: string
  likeCount: number
  liked?: boolean
  createdAt: string
  user?: User
  replyToUser?: User
  /** 楼中楼回复列表(仅一级评论有值) */
  replies?: Comment[]
}

export interface LikeResult {
  liked: boolean
  likeCount: number
}

export interface CollectResult {
  collected: boolean
  collectCount: number
}

export interface FollowResult {
  followed: boolean
}

export interface Notification {
  id: number
  userId: number
  senderId: number
  type: number
  targetType: number
  targetId: number
  content: string
  isRead: number
  createdAt: string
  sender?: User
}
