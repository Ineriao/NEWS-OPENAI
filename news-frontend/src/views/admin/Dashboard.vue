<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newsCount }}</div>
              <div class="stat-label">新闻总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.commentCount }}</div>
              <div class="stat-label">评论总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingCount }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>分类分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>近7天发布趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近新闻和快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>最近发布的新闻</span>
          </template>
          <el-table :data="recentNews" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="authorName" label="作者" width="100" />
            <el-table-column prop="publishTime" label="发布时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.publishTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/news/create')">
              <el-icon><Edit /></el-icon> 发布新闻
            </el-button>
            <el-button v-if="userStore.role >= 3" @click="$router.push('/admin/news/review')">
              <el-icon><View /></el-icon> 审核新闻
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { getPublicNewsList } from '@/api/news'
import { getDashboardStats, getCategoryStats, getTrendStats } from '@/api/stats'
import * as echarts from 'echarts'

const userStore = useUserStore()

const statsLoading = ref(false)
const stats = ref({
  newsCount: 0,
  userCount: 0,
  commentCount: 0,
  pendingCount: 0
})

const recentNews = ref([])

// 图表引用
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

// 获取统计数据
const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await getDashboardStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  } finally {
    statsLoading.value = false
  }
}

// 获取分类统计并渲染饼图
const fetchCategoryStats = async () => {
  try {
    const res = await getCategoryStats()
    if (res.data) {
      renderPieChart(res.data)
    }
  } catch (error) {
    console.error('获取分类统计失败', error)
  }
}

// 获取趋势统计并渲染折线图
const fetchTrendStats = async () => {
  try {
    const res = await getTrendStats()
    if (res.data) {
      renderLineChart(res.data)
    }
  } catch (error) {
    console.error('获取趋势统计失败', error)
  }
}

// 渲染饼图
const renderPieChart = (data) => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: data.map(item => ({
          name: item.name,
          value: item.count
        }))
      }
    ],
    color: ['#6C5DAB', '#A795BF', '#409eff', '#67c23a', '#e6a23c', '#f56c6c']
  }
  pieChart.setOption(option)
}

// 渲染折线图
const renderLineChart = (data) => {
  if (!lineChartRef.value) return

  lineChart = echarts.init(lineChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => item.date.slice(5)) // 只显示月-日
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '发布数量',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(108, 93, 171, 0.5)' },
            { offset: 1, color: 'rgba(108, 93, 171, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#6C5DAB',
          width: 2
        },
        itemStyle: {
          color: '#6C5DAB'
        },
        data: data.map(item => item.count)
      }
    ]
  }
  lineChart.setOption(option)
}

const fetchRecentNews = async () => {
  try {
    const res = await getPublicNewsList({ pageNum: 1, pageSize: 5 })
    recentNews.value = res.data?.list || []
  } catch (error) {
    console.error('获取新闻失败', error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(async () => {
  fetchStats()
  fetchRecentNews()

  await nextTick()
  fetchCategoryStats()
  fetchTrendStats()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-actions .el-button {
  width: 100%;
}
</style>
