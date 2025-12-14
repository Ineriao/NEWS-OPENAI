<template>
  <div class="news-edit">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑新闻' : '发布新闻' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 800px;"
      >
        <el-form-item label="新闻标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入新闻标题" />
        </el-form-item>

        <el-form-item label="新闻分类" prop="categoryId">
          <el-cascader
            v-model="form.categoryId"
            :options="categories"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
            placeholder="请选择分类"
            clearable
          />
        </el-form-item>

        <el-form-item label="新闻摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入新闻摘要"
          />
        </el-form-item>

        <el-form-item label="新闻内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入新闻内容（支持HTML）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave(0)">
            保存草稿
          </el-button>
          <el-button type="success" :loading="loading" @click="handleSave(1)">
            保存并提交审核
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryTree } from '@/api/category'
import { getNewsDetail, createNews, updateNews, submitForReview } from '@/api/news'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const categories = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  categoryId: null,
  summary: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入新闻标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入新闻内容', trigger: 'blur' }]
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchNews = async () => {
  if (!isEdit.value) return

  try {
    const res = await getNewsDetail(route.params.id)
    const data = res.data
    form.title = data.title
    form.categoryId = data.categoryId
    form.summary = data.summary
    form.content = data.content
  } catch (error) {
    console.error('获取新闻失败', error)
  }
}

const handleSave = async (submitStatus) => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    let newsId

    if (isEdit.value) {
      await updateNews(route.params.id, form)
      newsId = route.params.id
      ElMessage.success('更新成功')
    } else {
      const res = await createNews(form)
      newsId = res.data?.id
      ElMessage.success('保存成功')
    }

    // 提交审核
    if (submitStatus === 1 && newsId) {
      await submitForReview(newsId)
      ElMessage.success('已提交审核')
    }

    router.push('/admin/news')
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchNews()
})
</script>

<style scoped>
.news-edit {
  max-width: 1000px;
}
</style>
