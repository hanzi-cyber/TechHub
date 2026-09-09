<template>
  <div>
    <div class="back-row">
      <el-button text @click="router.back()">
        <el-icon style="margin-right: 4px"><ArrowLeft /></el-icon>返回
      </el-button>
    </div>
    <el-card>
    <template #header>{{ isEdit ? '编辑帖子' : '发布帖子' }}</template>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" maxlength="128" show-word-limit />
      </el-form-item>
      <el-form-item label="摘要(选填)">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简要描述,列表页展示" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select
          v-model="form.tagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入标签"
          style="width: 100%"
        >
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="正文(Markdown)" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="14" placeholder="支持 Markdown 语法" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">发布</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createPost, getPostDetail, updatePost } from '@/api/post'
import { listTags } from '@/api/tag'
import type { Tag } from '@/types'

const route = useRoute()
const router = useRouter()
const editId = route.params.id ? Number(route.params.id) : undefined
const isEdit = !!editId

const formRef = ref<FormInstance>()
const loading = ref(false)
const tags = ref<Tag[]>([])
const form = reactive({
  title: '',
  summary: '',
  content: '',
  tagIds: [] as number[]
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

onMounted(async () => {
  listTags().then((res) => (tags.value = res)).catch(() => {})
  if (isEdit && editId) {
    const post = await getPostDetail(editId)
    form.title = post.title
    form.summary = post.summary || ''
    form.content = post.content || ''
    form.tagIds = post.tags?.map((t) => t.id) || []
  }
})

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const data = {
      title: form.title,
      summary: form.summary,
      content: form.content,
      tagIds: form.tagIds
    }
    if (isEdit && editId) {
      await updatePost(editId, data)
      ElMessage.success('更新成功')
      router.push(`/post/${editId}`)
    } else {
      const post = await createPost(data)
      ElMessage.success('发布成功')
      router.push(`/post/${post.id}`)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.back-row {
  margin-bottom: 12px;
}
</style>
