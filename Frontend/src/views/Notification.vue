<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>通知</span>
        <el-button size="small" text type="primary" @click="markAllRead">全部已读</el-button>
      </div>
    </template>
    <div v-loading="loading">
      <div
        v-for="n in notifications"
        :key="n.id"
        class="noti-item"
        :class="{ unread: n.isRead === 0 }"
        @click="onClickNoti(n)"
      >
        <el-avatar :size="32" :src="n.sender?.avatarUrl || ''">
          {{ (n.sender?.username || 'U').slice(0, 1).toUpperCase() }}
        </el-avatar>
        <div class="noti-main">
          <div class="content"><span class="sender">{{ n.sender?.username }}</span> {{ n.content }}</div>
          <div class="time">{{ n.createdAt }}</div>
        </div>
      </div>
      <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listNotifications, markRead } from '@/api/notification'
import type { Notification } from '@/types'

const router = useRouter()
const loading = ref(false)
const notifications = ref<Notification[]>([])

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await listNotifications({ pageNum: 1, pageSize: 50 })
    notifications.value = res.records
  } finally {
    loading.value = false
  }
}

async function markAllRead() {
  const ids = notifications.value.filter((n) => n.isRead === 0).map((n) => n.id)
  if (ids.length === 0) return
  await markRead(ids)
  fetchNotifications()
}

function onClickNoti(n: Notification) {
  if (n.isRead === 0) markRead([n.id])
  // 点赞/评论 → 跳转帖子;关注 → 跳转用户
  if (n.targetType === 1) router.push(`/post/${n.targetId}`)
  else router.push(`/user/${n.senderId}`)
}

onMounted(fetchNotifications)
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.noti-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.noti-main {
  flex: 1;
}
.sender {
  color: #409eff;
  font-weight: 600;
  margin-right: 4px;
}
.noti-item.unread {
  background: #ecf5ff;
}
.content {
  font-size: 14px;
  color: #303133;
}
.time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>
