<template>
  <div v-loading="loading">
    <div class="back-row">
      <el-button text @click="router.back()">
        <el-icon style="margin-right: 4px"><ArrowLeft /></el-icon>返回
      </el-button>
    </div>
    <el-card v-if="user" class="profile-card">
      <div class="profile-header">
        <el-avatar :size="64" :src="user.avatarUrl || ''">
          {{ (user.username || 'U').slice(0, 1).toUpperCase() }}
        </el-avatar>
        <div class="info">
          <div class="username">{{ user.username }}</div>
          <div class="bio">{{ user.bio || '这个人很懒,什么都没写' }}</div>
        </div>
        <el-button
          v-if="isSelf"
          type="primary"
          plain
          @click="openEdit"
        >
          编辑资料
        </el-button>
        <el-button
          v-else
          :type="followed ? 'default' : 'primary'"
          @click="toggleFollow"
        >
          {{ followed ? '已关注' : '关注' }}
        </el-button>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="他的帖子" name="posts">
        <PostCard v-for="p in posts" :key="p.id" :post="p" />
        <el-empty v-if="posts.length === 0" description="暂无帖子" />
        <div v-if="postsTotal > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="postsPage"
            :page-size="pageSize"
            :total="postsTotal"
            layout="prev, pager, next, total"
            background
            @current-change="fetchPosts"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="关注" name="following">
        <div v-for="u in following" :key="u.id" class="user-row" @click="goProfile(u.id)">
          <el-avatar :size="36" :src="u.avatarUrl || ''">{{ (u.username || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
          <span class="user-name">{{ u.username }}</span>
        </div>
        <el-empty v-if="following.length === 0" description="暂无关注" />
        <div v-if="followingTotal > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="followingPage"
            :page-size="pageSize"
            :total="followingTotal"
            layout="prev, pager, next, total"
            background
            @current-change="fetchFollowing"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="粉丝" name="followers">
        <div v-for="u in followers" :key="u.id" class="user-row" @click="goProfile(u.id)">
          <el-avatar :size="36" :src="u.avatarUrl || ''">{{ (u.username || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
          <span class="user-name">{{ u.username }}</span>
        </div>
        <el-empty v-if="followers.length === 0" description="暂无粉丝" />
        <div v-if="followersTotal > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="followersPage"
            :page-size="pageSize"
            :total="followersTotal"
            layout="prev, pager, next, total"
            background
            @current-change="fetchFollowers"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="editVisible" title="编辑资料" width="480px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" maxlength="20" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" maxlength="11" placeholder="选填,如 13800138000" />
        </el-form-item>
        <el-form-item label="头像地址">
          <div class="avatar-upload">
            <el-input v-model="editForm.avatarUrl" placeholder="粘贴图片 URL,或点击右侧按钮上传" />
            <el-button :loading="uploading" @click="triggerUpload">上传</el-button>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              class="file-input"
              @change="onFileChange"
            />
          </div>
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="介绍一下自己"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserById, getUserPosts, getFollowers, getFollowing, updateMe } from '@/api/user'
import { follow, unfollow } from '@/api/follow'
import { uploadAvatar } from '@/api/upload'
import { useUserStore } from '@/store/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { Post, User } from '@/types'
import PostCard from '@/components/PostCard.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userId = ref(Number(route.params.id))

const loading = ref(false)
const user = ref<User | null>(null)
const posts = ref<Post[]>([])
const followers = ref<User[]>([])
const following = ref<User[]>([])
const activeTab = ref('posts')
const followed = ref(false)

// 分页状态
const postsPage = ref(1)
const postsTotal = ref(0)
const followingPage = ref(1)
const followingTotal = ref(0)
const followersPage = ref(1)
const followersTotal = ref(0)
const pageSize = 10

const editVisible = ref(false)
const saving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({ username: '', email: '', phone: '', avatarUrl: '', bio: '' })
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const editRules: FormRules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 个字符', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确(11位)', trigger: 'blur' }]
}

// 是否在看自己的主页(只有自己能看到编辑按钮)
const isSelf = computed(() => userStore.userInfo?.id === user.value?.id)

async function fetchUser() {
  user.value = await getUserById(userId.value)
  // 用后端返回的初始关注状态,避免刷新后按钮状态丢失
  followed.value = user.value?.followed ?? false
}

async function fetchPosts() {
  const res = await getUserPosts(userId.value, { pageNum: postsPage.value, pageSize })
  posts.value = res.records
  postsTotal.value = res.total
}

async function fetchFollowing() {
  const res = await getFollowing(userId.value, { pageNum: followingPage.value, pageSize })
  following.value = res.records
  followingTotal.value = res.total
}

async function fetchFollowers() {
  const res = await getFollowers(userId.value, { pageNum: followersPage.value, pageSize })
  followers.value = res.records
  followersTotal.value = res.total
}

function onTabChange(tab: string | number) {
  if (tab === 'posts') fetchPosts()
  else if (tab === 'following') fetchFollowing()
  else if (tab === 'followers') fetchFollowers()
}

async function fetchProfile() {
  loading.value = true
  try {
    await fetchUser()
    await fetchPosts()
  } finally {
    loading.value = false
  }
}

async function toggleFollow() {
  if (!userStore.token) return ElMessage.warning('请先登录')
  // 关注/取消关注都采用后端返回的真实状态
  const res = followed.value ? await unfollow(userId.value) : await follow(userId.value)
  followed.value = res.followed
}

function goProfile(id?: number) {
  if (id) router.push(`/user/${id}`)
}

function openEdit() {
  editForm.username = user.value?.username || ''
  editForm.email = user.value?.email || ''
  editForm.phone = user.value?.phone || ''
  editForm.avatarUrl = user.value?.avatarUrl || ''
  editForm.bio = user.value?.bio || ''
  editVisible.value = true
}

function triggerUpload() {
  fileInputRef.value?.click()
}

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const url = await uploadAvatar(file)
    editForm.avatarUrl = url
    ElMessage.success('上传成功')
  } catch {
    // 错误提示已由响应拦截器统一处理
  } finally {
    uploading.value = false
    input.value = '' // 清空,允许重复选同一个文件
  }
}

async function submitEdit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const updated = await updateMe({
      username: editForm.username,
      email: editForm.email,
      phone: editForm.phone,
      avatarUrl: editForm.avatarUrl,
      bio: editForm.bio
    })
    user.value = updated
    userStore.setUserInfo(updated)
    ElMessage.success('保存成功')
    editVisible.value = false
  } finally {
    saving.value = false
  }
}

// 从粉丝/关注列表跳到另一个用户时,路由复用同一组件,需监听参数变化重新拉取
watch(() => route.params.id, (newId) => {
  userId.value = Number(newId)
  // 重置状态,避免残留上一个用户的数据
  followed.value = false
  posts.value = []
  followers.value = []
  following.value = []
  activeTab.value = 'posts'
  postsPage.value = 1
  followingPage.value = 1
  followersPage.value = 1
  fetchProfile()
})

onMounted(fetchProfile)
</script>

<style scoped>
.back-row {
  margin-bottom: 12px;
}
.profile-card {
  margin-bottom: 16px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.info {
  flex: 1;
}
.username {
  font-size: 20px;
  font-weight: 700;
}
.bio {
  color: #909399;
  margin-top: 6px;
}
.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: #fff;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
}
.user-row:hover .user-name {
  color: #409eff;
}
.user-name {
  font-size: 14px;
  color: #303133;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.avatar-upload {
  display: flex;
  gap: 8px;
  width: 100%;
}
.avatar-upload .el-input {
  flex: 1;
}
.file-input {
  display: none;
}
</style>
