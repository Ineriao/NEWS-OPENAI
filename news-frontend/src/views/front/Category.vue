<template>
  <div class="category-page">
    <div class="page-header">
      <h2>{{ categoryName }}</h2>
    </div>

    <div class="news-list" v-loading="loading">
      <div
        v-for="item in newsList"
        :key="item.id"
        class="news-item"
        @click="$router.push(`/home/news/${item.id}`)"
      >
        <h3 class="news-title">{{ item.title }}</h3>
        <p class="news-summary">{{ item.summary }}</p>
        <div class="news-meta">
          <span><el-icon><User /></el-icon> {{ item.authorName }}</span>
          <span><el-icon><Clock /></el-icon> {{ formatTime(item.publishTime) }}</span>
          <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
        </div>
      </div>

      <el-empty v-if="!loading && !newsList.length" description="暂无新闻" />

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchNews"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicNewsList } from '@/api/news'
import { getCategoryById } from '@/api/category'

const route = useRoute()

const loading = ref(false)
const categoryName = ref('')
const newsList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getPublicNewsList({
      page: page.value,
      size: pageSize.value,
      categoryId: route.params.id
    })
    newsList.value = res.data?.list || []
    total.value = res.data?.total || 0

  } catch (error) {
    console.error('获取新闻失败', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

const fetchCategory = async () => {
  try {
    const res = await getCategoryById(route.params.id)
    if (res.data) {
      categoryName.value = res.data.name
    }
  } catch (error) {
    console.error('获取分类信息失败', error)
  }
}

watch(() => route.params.id, () => {
  page.value = 1
  fetchCategory()
  fetchNews()
})

onMounted(() => {
  fetchCategory()
  fetchNews()
})
</script>

<style scoped>
.category-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 25px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.85);
  letter-spacing: 0.1rem;
  text-transform: uppercase;
  border-left: 4px solid #6C5DAB;
  padding-left: 15px;
}

.news-list {
  background: rgba(255, 255, 255, 0.9);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.news-item {
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
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
  color: rgba(0, 0, 0, 0.85);
  margin-bottom: 10px;
  transition: color 0.3s;
}

.news-item:hover .news-title {
  color: #6C5DAB;
}

.news-summary {
  color: rgba(0, 0, 0, 0.6);
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
  color: rgba(0, 0, 0, 0.45);
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
</style>
