<template>
  <div class="search-page">
    <div class="page-header">
      <h2>搜索结果：{{ keyword }}</h2>
      <span class="result-count" v-if="total > 0">共找到 {{ total }} 条结果</span>
    </div>

    <!-- 排序筛选栏 -->
    <div class="filter-bar" v-if="total > 0">
      <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
        <el-radio-button label="">默认排序</el-radio-button>
        <el-radio-button label="publishTime">最新发布</el-radio-button>
        <el-radio-button label="viewCount">最多阅读</el-radio-button>
      </el-radio-group>
    </div>

    <div class="news-list" v-loading="loading">
      <div
        v-for="item in newsList"
        :key="item.id"
        class="news-item"
        @click="$router.push(`/home/news/${item.id}`)"
      >
        <h3 class="news-title" v-html="highlightKeyword(item.title)"></h3>
        <p class="news-summary" v-html="highlightKeyword(item.summary)"></p>
        <div class="news-meta">
          <span><el-icon><FolderOpened /></el-icon> {{ item.categoryName }}</span>
          <span><el-icon><User /></el-icon> {{ item.authorName }}</span>
          <span><el-icon><Clock /></el-icon> {{ formatTime(item.publishTime) }}</span>
          <span><el-icon><View /></el-icon> {{ item.viewCount }} 阅读</span>
        </div>
      </div>

      <el-empty v-if="!loading && !newsList.length" description="未找到相关新闻" />

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchNews } from '@/api/news'

const route = useRoute()

const loading = ref(false)
const keyword = ref('')
const newsList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortBy = ref('')

// 高亮关键词
const highlightKeyword = (text) => {
  if (!text || !keyword.value) return text || ''
  // 转义正则特殊字符
  const escapedKeyword = keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  return text.replace(regex, '<mark class="highlight">$1</mark>')
}

// 排序变更
const handleSortChange = () => {
  page.value = 1
  handleSearch()
}

const handleSearch = async () => {
  keyword.value = route.query.keyword || ''
  if (!keyword.value) return

  loading.value = true
  try {
    const res = await searchNews({
      keyword: keyword.value,
      pageNum: page.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value || undefined
    })
    newsList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('搜索失败', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

watch(() => route.query.keyword, () => {
  page.value = 1
  sortBy.value = ''
  handleSearch()
})

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.search-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
  gap: 15px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: 0.1rem;
  border-left: 4px solid var(--color-primary);
  padding-left: 15px;
}

.result-count {
  color: var(--text-muted);
  font-size: 14px;
}

.filter-bar {
  margin-bottom: 20px;
}

.filter-bar :deep(.el-radio-button__inner) {
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.filter-bar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.news-list {
  background: var(--bg-card);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px var(--shadow-color);
  border: 1px solid var(--border-color);
}

.news-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s;
}

.news-item:hover {
  padding-left: 10px;
}

.news-item:last-child {
  border-bottom: none;
}

.news-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
  transition: color 0.3s;
}

.news-item:hover .news-title {
  color: var(--color-primary);
}

.news-summary {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  gap: 20px;
  color: var(--text-muted);
  font-size: 13px;
}

.news-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  margin-top: 25px;
  display: flex;
  justify-content: center;
}

/* 关键词高亮样式 */
:deep(.highlight) {
  background-color: rgba(108, 93, 171, 0.3);
  color: var(--color-primary);
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
}
</style>
