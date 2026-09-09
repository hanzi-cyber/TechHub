<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/')">TechHub</div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          :ellipsis="false"
          router
          class="menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/notifications">
            通知
            <span v-if="unreadCount > 0" class="noti-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </el-menu-item>
        </el-menu>
        <div class="right">
          <template v-if="userStore.token">
            <el-button type="primary" size="small" @click="router.push('/write')">
              <el-icon style="margin-right: 4px"><EditPen /></el-icon>发帖
            </el-button>
            <el-dropdown @command="onCommand">
              <span class="user-name">
                <el-avatar :size="28" style="vertical-align: middle">
                  {{ (userStore.userInfo?.username || 'U').slice(0, 1).toUpperCase() }}
                </el-avatar>
                <span style="margin-left: 6px">{{ userStore.userInfo?.username || '我' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="router.push('/login')">登录</el-button>
            <el-button type="primary" size="small" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getMe } from '@/api/user'
import { unreadCount as getUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

/** 未读通知数(导航栏红点角标) */
const unreadCount = ref(0)

async function fetchUnreadCount() {
  if (!userStore.token) {
    unreadCount.value = 0
    return
  }
  try {
    unreadCount.value = await getUnreadCount()
  } catch {
    /* 忽略 */
  }
}

onMounted(async () => {
  fetchUnreadCount()
  // 已有 token 但用户信息为空时,拉取当前用户信息
  if (userStore.token && !userStore.userInfo) {
    try {
      const me = await getMe()
      userStore.setUserInfo(me)
    } catch {
      /* 忽略,token 失效由拦截器处理 */
    }
  }
})

// 路由变化时刷新未读数:读完通知返回首页后,红点自动消失
watch(() => route.path, fetchUnreadCount)

function onCommand(command: string) {
  if (command === 'profile') {
    const id = userStore.userInfo?.id
    if (id) router.push(`/user/${id}`)
  } else if (command === 'logout') {
    userStore.logout().then(() => router.push('/login'))
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
}
.header-inner {
  max-width: 1100px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
}
.logo {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  cursor: pointer;
  margin-right: 24px;
}
.menu {
  flex: 1;
  border-bottom: none;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.noti-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  margin-left: 6px;
  border-radius: 9px;
  font-size: 12px;
  color: #fff;
  background: #f56c6c;
  text-align: center;
  vertical-align: middle;
}
.main {
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding-top: 20px;
}
</style>
