<template>
  <div class="post-card" @click="router.push(`/post/${post.id}`)">
    <div class="title">{{ post.title }}</div>
    <div v-if="post.summary" class="summary">{{ post.summary }}</div>
    <div class="tags" v-if="post.tags?.length">
      <el-tag v-for="tag in post.tags" :key="tag.id" size="small" class="tag">{{ tag.name }}</el-tag>
    </div>
    <div class="meta">
      <span class="author" @click.stop="goProfile">
        <el-avatar :size="20" :src="post.author?.avatarUrl || ''">{{ (post.author?.username || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
        <span class="author-name">{{ post.author?.username || '匿名' }}</span>
      </span>
      <span class="divider">·</span>
      <span>{{ formatTime(post.createdAt) }}</span>
      <span class="stat"><el-icon><View /></el-icon>{{ post.viewCount }}</span>
      <span class="stat"><el-icon><Promotion /></el-icon>{{ post.likeCount }}</span>
      <span class="stat"><el-icon><ChatDotRound /></el-icon>{{ post.commentCount }}</span>
      <span class="stat"><el-icon><CollectionTag /></el-icon>{{ post.collectCount }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Post } from '@/types'

const props = defineProps<{ post: Post }>()
const router = useRouter()

function formatTime(time?: string) {
  if (!time) return ''
  return time.replace('T', ' ').slice(0, 16)
}

function goProfile() {
  if (props.post.userId) router.push(`/user/${props.post.userId}`)
}
</script>

<style scoped>
.post-card {
  background: #fff;
  padding: 16px 20px;
  border-radius: 6px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.post-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.summary {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tags {
  margin-bottom: 10px;
}
.tag {
  margin-right: 6px;
}
.meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}
.meta .author {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.meta .author:hover .author-name {
  color: #409eff;
}
.stat {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: 12px;
}
</style>
