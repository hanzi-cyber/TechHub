<template>
  <div>
    <el-card class="search-card">
      <div class="search-row">
        <el-tabs v-model="sort" class="tabs" @tab-change="onSortChange">
          <el-tab-pane label="最新" name="latest" />
          <el-tab-pane label="热门" name="hot" />
          <el-tab-pane label="关注" name="follow" />
        </el-tabs>
        <el-select
          v-model="tagId"
          placeholder="全部标签"
          clearable
          class="tag-select"
          @change="onTagChange"
        >
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索帖子标题"
          clearable
          class="search-input"
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="onSearch" />
          </template>
        </el-input>
      </div>
    </el-card>

    <div v-loading="loading">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <el-empty v-if="!loading && posts.length === 0" description="暂无内容" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        background
        @current-change="fetchPosts"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { listPosts, getFollowFeed } from '@/api/post'
import { listTags } from '@/api/tag'
import type { Post, Tag } from '@/types'
import PostCard from '@/components/PostCard.vue'

const router = useRouter()

const sort = ref<'latest' | 'hot' | 'follow'>('latest')
const keyword = ref('')
const tagId = ref<number | undefined>(undefined)
const tags = ref<Tag[]>([])
const posts = ref<Post[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

async function fetchPosts() {
  loading.value = true
  try {
    // 关注流(拉模式):走独立接口,只查我关注的人发布的帖子
    if (sort.value === 'follow') {
      const res = await getFollowFeed({ pageNum: pageNum.value, pageSize: pageSize.value })
      posts.value = res.records
      total.value = res.total
      return
    }
    const res = await listPosts({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sort: sort.value as 'latest' | 'hot',
      keyword: keyword.value || undefined,
      tagId: tagId.value
    })
    posts.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onSortChange() {
  // 关注流需要登录态,未登录跳到登录页并回退到「最新」
  if (sort.value === 'follow' && !localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    sort.value = 'latest'
    router.push('/login')
    return
  }
  pageNum.value = 1
  fetchPosts()
}

function onSearch() {
  pageNum.value = 1
  fetchPosts()
}

function onTagChange() {
  pageNum.value = 1
  fetchPosts()
}

onMounted(() => {
  listTags().then((res) => (tags.value = res)).catch(() => {})
  fetchPosts()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 20px;
}
.tabs {
  flex: 1;
}
.tag-select {
  width: 150px;
}
.search-input {
  width: 260px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
