<template>
  <div class="home">
    <!-- 轮播图/头条 -->
    <el-carousel height="300px" class="banner" v-if="topNews.length">
      <el-carousel-item v-for="item in topNews" :key="item.id">
        <div class="banner-item" @click="goDetail(item.id)">
          <h2>{{ item.title }}</h2>
          <p>{{ item.summary }}</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 新闻列表 -->
    <div class="news-section">
      <div class="section-header">
        <h2>最新新闻</h2>
      </div>

      <el-row :gutter="20">
        <el-col :span="18">
          <div class="news-list">
            <div
              v-for="item in newsList"
              :key="item.id"
              class="news-item"
              @click="goDetail(item.id)"
            >
              <div class="news-content">
                <h3 class="news-title">{{ item.title }}</h3>
                <p class="news-summary">{{ item.summary }}</p>
                <div class="news-meta">
                  <span><el-icon><FolderOpened /></el-icon> {{ item.categoryName }}</span>
                  <span><el-icon><User /></el-icon> {{ item.authorName }}</span>
                  <span><el-icon><Clock /></el-icon> {{ formatTime(item.publishTime) }}</span>
                  <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="page"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="fetchNews"
              />
            </div>
          </div>
        </el-col>

        <!-- 侧边栏 -->
        <el-col :span="6">
          <!-- 热搜榜 -->
          <el-card class="sidebar-card hot-search-card">
            <template #header>
              <span>实时热搜</span>
            </template>
            <el-tabs v-model="activeHotTab" @tab-change="handleHotTabChange">
              <el-tab-pane label="微博" name="weibo">
                <div v-loading="hotLoading" class="hot-list">
                  <div
                    v-for="item in hotSearchList"
                    :key="item.rank"
                    class="hot-search-item"
                    @click="openHotLink(item.url)"
                  >
                    <span class="rank" :class="{ top: item.rank <= 3 }">{{ item.rank }}</span>
                    <span class="title">{{ item.title }}</span>
                    <span class="hot-value" v-if="item.hot">{{ item.hot }}</span>
                  </div>
                  <el-empty v-if="!hotLoading && hotSearchList.length === 0" description="暂无数据" :image-size="60" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="抖音" name="douyin">
                <div v-loading="hotLoading" class="hot-list">
                  <div
                    v-for="item in hotSearchList"
                    :key="item.rank"
                    class="hot-search-item"
                    @click="openHotLink(item.url)"
                  >
                    <span class="rank" :class="{ top: item.rank <= 3 }">{{ item.rank }}</span>
                    <span class="title">{{ item.title }}</span>
                    <span class="hot-value" v-if="item.hot">{{ item.hot }}</span>
                  </div>
                  <el-empty v-if="!hotLoading && hotSearchList.length === 0" description="暂无数据" :image-size="60" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="知乎" name="zhihu">
                <div v-loading="hotLoading" class="hot-list">
                  <div
                    v-for="item in hotSearchList"
                    :key="item.rank"
                    class="hot-search-item"
                    @click="openHotLink(item.url)"
                  >
                    <span class="rank" :class="{ top: item.rank <= 3 }">{{ item.rank }}</span>
                    <span class="title">{{ item.title }}</span>
                    <span class="hot-value" v-if="item.hot">{{ item.hot }}</span>
                  </div>
                  <el-empty v-if="!hotLoading && hotSearchList.length === 0" description="暂无数据" :image-size="60" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="百度" name="baidu">
                <div v-loading="hotLoading" class="hot-list">
                  <div
                    v-for="item in hotSearchList"
                    :key="item.rank"
                    class="hot-search-item"
                    @click="openHotLink(item.url)"
                  >
                    <span class="rank" :class="{ top: item.rank <= 3 }">{{ item.rank }}</span>
                    <span class="title">{{ item.title }}</span>
                    <span class="hot-value" v-if="item.hot">{{ item.hot }}</span>
                  </div>
                  <el-empty v-if="!hotLoading && hotSearchList.length === 0" description="暂无数据" :image-size="60" />
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>

          <!-- 热门新闻 -->
          <el-card class="sidebar-card">
            <template #header>
              <span>热门新闻</span>
            </template>
            <div
              v-for="(item, index) in hotNews"
              :key="item.id"
              class="hot-item"
              @click="goDetail(item.id)"
            >
              <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="title">{{ item.title }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicNewsList } from '@/api/news'
import { getWeiboHot, getDouyinHot, getZhihuHot, getBaiduHot } from '@/api/hot'

const router = useRouter()

const newsList = ref([])
const topNews = ref([])
const hotNews = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 热搜相关
const activeHotTab = ref('weibo')
const hotSearchList = ref([])
const hotLoading = ref(false)

const fetchNews = async () => {
  try {
    const res = await getPublicNewsList({ page: page.value, size: pageSize.value })
    newsList.value = res.data?.list || []
    total.value = res.data?.total || 0

    // 取前3条作为头条
    if (page.value === 1) {
      topNews.value = newsList.value.slice(0, 3)
    }
  } catch (error) {
    console.error('获取新闻失败', error)
  }
}

const fetchHotNews = async () => {
  try {
    const res = await getPublicNewsList({ page: 1, size: 10, sort: 'viewCount' })
    hotNews.value = res.data?.list || []
  } catch (error) {
    console.error('获取热门新闻失败', error)
  }
}

// 获取热搜数据
const fetchHotSearch = async (platform) => {
  hotLoading.value = true
  hotSearchList.value = []
  try {
    let res
    switch (platform) {
      case 'weibo':
        res = await getWeiboHot()
        break
      case 'douyin':
        res = await getDouyinHot()
        break
      case 'zhihu':
        res = await getZhihuHot()
        break
      case 'baidu':
        res = await getBaiduHot()
        break
    }
    hotSearchList.value = res.data || []
  } catch (error) {
    console.error('获取热搜失败', error)
  } finally {
    hotLoading.value = false
  }
}

// Tab 切换
const handleHotTabChange = (tab) => {
  fetchHotSearch(tab)
}

// 打开热搜链接
const openHotLink = (url) => {
  if (url) {
    window.open(url, '_blank')
  }
}

const goDetail = (id) => {
  router.push(`/home/news/${id}`)
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchNews()
  fetchHotNews()
  fetchHotSearch('weibo') // 默认加载微博热搜
})
</script>

<style scoped>
.home {
  max-width: 1400px;
  margin: 0 auto;
}

.banner {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.banner-item {
  height: 100%;
  background: linear-gradient(135deg, #A795BF 0%, #6C5DAB 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 50px;
  cursor: pointer;
}

.banner-item h2 {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 15px;
  letter-spacing: 0.1rem;
}

.banner-item p {
  font-size: 16px;
  opacity: 0.85;
  line-height: 1.6;
}

.section-header {
  margin-bottom: 25px;
}

.section-header h2 {
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

.sidebar-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
}

.sidebar-card :deep(.el-card__header) {
  background: rgba(0, 0, 0, 0.9);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.1rem;
  text-transform: uppercase;
  font-size: 13px;
  border-radius: 12px 12px 0 0;
  padding: 15px 20px;
  border: none;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-item:hover {
  padding-left: 5px;
}

.hot-item:hover .title {
  color: #6C5DAB;
}

.rank {
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  margin-right: 12px;
  color: rgba(0, 0, 0, 0.5);
}

.rank.top {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  color: #fff;
}

.hot-item .title {
  flex: 1;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.75);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}

.hot-search-card :deep(.el-card__body) {
  padding: 15px;
}

.hot-search-card :deep(.el-tabs__header) {
  margin-bottom: 15px;
}

.hot-search-card :deep(.el-tabs__item) {
  padding: 0 12px;
  font-size: 13px;
  font-weight: 600;
}

.hot-search-card :deep(.el-tabs__item.is-active) {
  color: #6C5DAB;
}

.hot-search-card :deep(.el-tabs__active-bar) {
  background: #6C5DAB;
}

.hot-list {
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
}

.hot-search-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.hot-search-item:hover {
  background: rgba(108, 93, 171, 0.08);
}

.hot-search-item .rank {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  margin-right: 10px;
  color: rgba(0, 0, 0, 0.5);
  flex-shrink: 0;
}

.hot-search-item .rank.top {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  color: #fff;
}

.hot-search-item .title {
  flex: 1;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.75);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-search-item:hover .title {
  color: #6C5DAB;
}

.hot-search-item .hot-value {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.35);
  margin-left: 8px;
  flex-shrink: 0;
}
</style>
