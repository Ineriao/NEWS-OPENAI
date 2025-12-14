<template>
  <div class="news-review">
    <el-card>
      <template #header>
        <span>新闻审核</span>
      </template>

      <el-table :data="newsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="previewNews(row)">
              预览
            </el-button>
            <el-button type="success" link @click="handleApprove(row.id)">
              通过
            </el-button>
            <el-button type="danger" link @click="handleReject(row.id)">
              退回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchNews"
        />
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="新闻预览" width="800px">
      <template v-if="currentNews">
        <h2>{{ currentNews.title }}</h2>
        <p style="color: #999; margin: 10px 0;">{{ currentNews.summary }}</p>
        <el-divider />
        <div v-html="currentNews.content"></div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNewsList, getNewsDetail, approveNews, rejectNews } from '@/api/news'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const newsList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const previewVisible = ref(false)
const currentNews = ref(null)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getNewsList({ page: page.value, size: pageSize.value, status: 1 })
    newsList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取新闻失败', error)
  } finally {
    loading.value = false
  }
}

const previewNews = async (row) => {
  try {
    const res = await getNewsDetail(row.id)
    currentNews.value = res.data
    previewVisible.value = true
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定审核通过吗？', '提示')
    await approveNews(id)
    ElMessage.success('审核通过')
    fetchNews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败', error)
    }
  }
}

const handleReject = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入退回原因', '退回修改', {
      inputPattern: /\S+/,
      inputErrorMessage: '请输入退回原因'
    })
    await rejectNews(id, value)
    ElMessage.success('已退回')
    fetchNews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退回失败', error)
    }
  }
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
