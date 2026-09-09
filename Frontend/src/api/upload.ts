import { http } from './request'

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<string>('/upload/avatar', formData)
}
