import request from '@/utils/request'

// 获取 Dashboard 统计数据
export function getDashboardStats() {
  return request.get('/stats/dashboard')
}

// 获取分类统计
export function getCategoryStats() {
  return request.get('/stats/category')
}

// 获取发布趋势
export function getTrendStats() {
  return request.get('/stats/trend')
}
