<template>
  <div class="news-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新闻列表</span>
          <el-button type="primary" @click="$router.push('/admin/news/create')">
            <el-icon><Plus /></el-icon> 发布新闻
          </el-button>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :inline="true" class="search-form">
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="搜索标题" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.status" placeholder="状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="已存档" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchNews">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="newsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/admin/news/edit/${row.id}`)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handleSubmit(row.id)"
            >
              提交审核
            </el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchNews"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getNewsList, deleteNews, submitForReview } from '@/api/news'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const newsList = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null
})

const statusText = (status) => {
  const map = { 0: '草稿', 1: '待审核', 2: '已发布', 3: '已存档' }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: '' }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getNewsList(query)
    newsList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取新闻失败', error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async (id) => {
  try {
    await ElMessageBox.confirm('确定提交审核吗？', '提示')
    await submitForReview(id)
    ElMessage.success('已提交审核')
    fetchNews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败', error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条新闻吗？', '警告', { type: 'warning' })
    await deleteNews(id)
    ElMessage.success('删除成功')
    fetchNews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
