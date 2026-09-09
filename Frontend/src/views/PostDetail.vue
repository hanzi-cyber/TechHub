<template>
  <div v-loading="loading">
    <div class="back-row">
      <el-button text @click="router.back()">
        <el-icon style="margin-right: 4px"><ArrowLeft /></el-icon>返回
      </el-button>
    </div>
    <el-card v-if="post" class="post-body">
      <h1 class="title">{{ post.title }}</h1>
      <div class="meta">
        <span class="author" @click="goProfile">
          <el-avatar :size="24" :src="post.author?.avatarUrl || ''">{{ (post.author?.username || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
          <span class="author-name">{{ post.author?.username }}</span>
        </span>
        <span>{{ post.publishedAt || post.createdAt }}</span>
        <span><el-icon><View /></el-icon>{{ post.viewCount }}</span>
      </div>
      <el-divider />
      <div class="content">{{ post.content }}</div>
      <div class="actions">
        <el-button :type="liked ? 'primary' : 'default'" @click="toggleLike">
          <el-icon style="margin-right: 4px"><Promotion /></el-icon>{{ liked ? '已赞' : '点赞' }} {{ post.likeCount }}
        </el-button>
        <el-button :type="collected ? 'warning' : 'default'" @click="toggleCollect">
          <el-icon style="margin-right: 4px"><CollectionTag /></el-icon>{{ collected ? '已收藏' : '收藏' }} {{ post.collectCount }}
        </el-button>
        <template v-if="isAuthor">
          <el-button @click="goEdit">
            <el-icon style="margin-right: 4px"><EditPen /></el-icon>编辑
          </el-button>
          <el-button type="danger" plain @click="onDeletePost">删除</el-button>
        </template>
      </div>
    </el-card>

    <el-card class="comment-card">
      <template #header>评论 ({{ post?.commentCount ?? 0 }})</template>

      <!-- 一级评论输入框 -->
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        :disabled="!userStore.token"
      />
      <div class="comment-submit">
        <el-button type="primary" :disabled="!userStore.token" @click="submitComment">发表评论</el-button>
      </div>
      <div v-if="!userStore.token" class="tip">请先登录后评论</div>

      <!-- 楼中楼回复输入框 -->
      <div v-if="replyTarget" class="reply-box">
        <div class="reply-tip">
          回复 <span class="reply-at">@{{ replyTarget.replyToUsername }}</span>
          <el-button text size="small" @click="cancelReply">取消</el-button>
        </div>
        <el-input v-model="replyContent" type="textarea" :rows="2" placeholder="写下你的回复..." />
        <div class="comment-submit">
          <el-button type="primary" @click="submitReply">回复</el-button>
        </div>
      </div>

      <!-- 楼层列表(一级评论) -->
      <div v-for="floor in comments" :key="floor.id" class="floor">
        <div class="floor-main">
          <div class="comment-user">{{ floor.user?.username }}</div>
          <div class="comment-content">{{ floor.content }}</div>
          <div class="comment-meta">
            <span class="comment-time">{{ floor.createdAt }}</span>
            <el-button text size="small" :type="floor.liked ? 'primary' : 'default'" @click="toggleCommentLike(floor)">
              <el-icon style="margin-right: 2px"><Promotion /></el-icon>{{ floor.likeCount }}
            </el-button>
            <el-button text size="small" @click="startReply(floor.id, floor.userId, floor.user?.username)">回复</el-button>
            <el-button v-if="floor.userId === userStore.userInfo?.id" text size="small" type="danger" @click="onDeleteComment(floor.id)">删除</el-button>
          </div>
        </div>

        <!-- 楼中楼回复 -->
        <div class="replies">
          <div v-for="r in visibleReplies(floor)" :key="r.id" class="reply">
            <span class="reply-user">{{ r.user?.username }}</span>
            <template v-if="r.replyToUser"> 回复 <span class="reply-at">@{{ r.replyToUser.username }}</span></template>
            : {{ r.content }}
            <span class="reply-time">{{ r.createdAt }}</span>
            <el-button text size="small" :type="r.liked ? 'primary' : 'default'" @click="toggleCommentLike(r)">
              <el-icon style="margin-right: 2px"><Promotion /></el-icon>{{ r.likeCount }}
            </el-button>
            <el-button text size="small" @click="startReply(floor.id, r.userId, r.user?.username)">回复</el-button>
            <el-button v-if="r.userId === userStore.userInfo?.id" text size="small" type="danger" @click="onDeleteComment(r.id)">删除</el-button>
          </div>
        </div>

        <el-button
          v-if="(floor.replies?.length ?? 0) > REPLY_PREVIEW"
          text
          size="small"
          class="expand-btn"
          @click="toggleExpand(floor)"
        >
          {{ expandedFloors[floor.id] ? '收起' : `展开剩余 ${(floor.replies?.length ?? 0) - REPLY_PREVIEW} 条回复` }}
        </el-button>
      </div>

      <el-empty v-if="comments.length === 0" description="暂无评论" :image-size="80" />

      <!-- 楼层分页 -->
      <div v-if="commentTotal > pageSize" class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="commentTotal"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePost, getPostDetail } from '@/api/post'
import { createComment, deleteComment, listComments } from '@/api/comment'
import { like, unlike } from '@/api/like'
import { collect, uncollect } from '@/api/collect'
import { useUserStore } from '@/store/user'
import type { Comment, Post } from '@/types'

/** 每条楼层默认展示的回复条数,超过则折叠 */
const REPLY_PREVIEW = 3

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const postId = Number(route.params.id)

const loading = ref(false)
const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const commentTotal = ref(0)
const pageNum = ref(1)
const pageSize = 10
const commentContent = ref('')
/** 评论幂等标识:同一次提交复用,成功后重置 */
const commentIdempotentKey = ref('')
const liked = ref(false)
const collected = ref(false)

/** 当前登录用户是否是帖子作者(决定是否显示编辑/删除按钮) */
const isAuthor = computed(() => {
  const me = userStore.userInfo?.id
  const authorId = post.value?.userId
  return me != null && authorId != null && me === authorId
})

/** 楼中楼回复目标 */
interface ReplyTarget {
  parentId: number
  replyToUserId?: number
  replyToUsername: string
}
const replyTarget = ref<ReplyTarget | null>(null)
const replyContent = ref('')
/** 回复幂等标识:同一次提交复用,成功后重置 */
const replyIdempotentKey = ref('')
/** 已展开回复的楼层 id 集合 */
const expandedFloors = reactive<Record<number, boolean>>({})

async function fetchPost() {
  loading.value = true
  try {
    post.value = await getPostDetail(postId)
    // 用后端返回的初始点赞/收藏状态,避免刷新后按钮状态丢失
    liked.value = post.value?.liked ?? false
    collected.value = post.value?.collected ?? false
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  const res = await listComments(postId, { pageNum: pageNum.value, pageSize })
  comments.value = res.records
  commentTotal.value = res.total
}

function goProfile() {
  const id = post.value?.userId
  if (id) router.push(`/user/${id}`)
}

function goEdit() {
  router.push(`/edit/${postId}`)
}

async function onDeletePost() {
  try {
    await ElMessageBox.confirm('确定删除该帖子吗？删除后不可恢复', '提示', { type: 'warning' })
  } catch {
    // 用户点击了取消
    return
  }
  try {
    await deletePost(postId)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (e) {
    ElMessage.error('删除失败，请稍后重试')
    console.error('删除帖子失败:', e)
  }
}

async function toggleLike() {
  if (!userStore.token) return ElMessage.warning('请先登录')
  // 后端返回操作后的真实状态与数量,前端直接同步,不再自己 +1/-1 硬猜
  const res = liked.value ? await unlike(1, postId) : await like(1, postId)
  liked.value = res.liked
  if (post.value) post.value.likeCount = res.likeCount
}

async function toggleCommentLike(comment: Comment) {
  if (!userStore.token) return ElMessage.warning('请先登录')
  const res = comment.liked ? await unlike(2, comment.id) : await like(2, comment.id)
  comment.liked = res.liked
  comment.likeCount = res.likeCount
}

async function toggleCollect() {
  if (!userStore.token) return ElMessage.warning('请先登录')
  // 收藏/取消收藏都采用后端返回的真实状态与数量,前端直接同步
  const res = collected.value ? await uncollect(postId) : await collect(postId)
  collected.value = res.collected
  if (post.value) post.value.collectCount = res.collectCount
}

async function submitComment() {
  if (!commentContent.value.trim()) return ElMessage.warning('评论内容不能为空')
  if (!commentIdempotentKey.value) commentIdempotentKey.value = crypto.randomUUID()
  try {
    await createComment(postId, { content: commentContent.value.trim() }, commentIdempotentKey.value)
  } catch {
    // 失败:保留幂等 key 与内容,便于重试复用(后端在失败时已释放该 key)
    return
  }
  commentContent.value = ''
  commentIdempotentKey.value = ''
  ElMessage.success('评论成功')
  if (post.value) post.value.commentCount++
  // 新楼层排在时间倒序最前,回到第一页
  pageNum.value = 1
  fetchComments()
}

function startReply(parentId: number, replyToUserId: number, replyToUsername?: string) {
  if (!userStore.token) return ElMessage.warning('请先登录')
  replyTarget.value = { parentId, replyToUserId, replyToUsername: replyToUsername || 'TA' }
  replyContent.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

async function submitReply() {
  if (!replyContent.value.trim()) return ElMessage.warning('回复内容不能为空')
  const target = replyTarget.value
  if (!target) return
  if (!replyIdempotentKey.value) replyIdempotentKey.value = crypto.randomUUID()
  try {
    await createComment(postId, {
      content: replyContent.value.trim(),
      parentId: target.parentId,
      replyToUserId: target.replyToUserId
    }, replyIdempotentKey.value)
  } catch {
    // 失败:保留幂等 key 与内容,便于重试复用
    return
  }
  cancelReply()
  replyIdempotentKey.value = ''
  ElMessage.success('回复成功')
  if (post.value) post.value.commentCount++
  fetchComments()
}

function visibleReplies(floor: Comment): Comment[] {
  const list = floor.replies || []
  if (expandedFloors[floor.id] || list.length <= REPLY_PREVIEW) return list
  return list.slice(0, REPLY_PREVIEW)
}

function toggleExpand(floor: Comment) {
  expandedFloors[floor.id] = !expandedFloors[floor.id]
}

function onPageChange(p: number) {
  pageNum.value = p
  fetchComments()
}

async function onDeleteComment(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
  } catch {
    // 用户点击了取消,直接返回
    return
  }
  try {
    await deleteComment(id)
    ElMessage.success('删除成功')
    // 楼层可能连带删了楼中楼回复,重新拉帖子拿到准确计数
    post.value = await getPostDetail(postId)
    fetchComments()
  } catch (e) {
    ElMessage.error('删除失败，请稍后重试')
    console.error('删除评论失败:', e)
  }
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.back-row {
  margin-bottom: 12px;
}
.post-body .title {
  font-size: 24px;
  margin-bottom: 12px;
}
.post-body .meta {
  color: #909399;
  font-size: 13px;
  display: flex;
  gap: 16px;
  align-items: center;
}
.post-body .meta .author {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.post-body .meta .author:hover .author-name {
  color: #409eff;
}
.post-body .content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}
.actions {
  margin-top: 24px;
}
.comment-card {
  margin-top: 16px;
}
.comment-submit {
  margin-top: 10px;
  text-align: right;
}
.tip {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}
.reply-box {
  margin-top: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 6px;
}
.reply-tip {
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}
.reply-at {
  color: #409eff;
}
.floor {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.floor-main .comment-user {
  font-weight: 600;
  font-size: 14px;
  color: #409eff;
}
.comment-content {
  margin: 6px 0;
  font-size: 14px;
  color: #303133;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-time {
  font-size: 12px;
  color: #c0c4cc;
}
.replies {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 6px;
}
.reply {
  padding: 4px 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
}
.reply-user {
  color: #409eff;
}
.reply-time {
  font-size: 12px;
  color: #c0c4cc;
  margin: 0 6px;
}
.expand-btn {
  margin-top: 6px;
  padding-left: 0;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
