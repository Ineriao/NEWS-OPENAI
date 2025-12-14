import request from '@/utils/request'

// ==================== 公开接口 ====================

// 获取已发布新闻列表
export function getPublicNewsList(params) {
  return request.get('/public/news', { params })
}

// 获取新闻详情
export function getNewsDetail(id) {
  return request.get(`/public/news/${id}`)
}

// 搜索新闻
export function searchNews(params) {
  return request.get('/public/news/search', { params })
}

// ==================== 管理接口 ====================

// 获取新闻列表（编辑/总编）
export function getNewsList(params) {
  return request.get('/news', { params })
}

// 创建新闻
export function createNews(data) {
  return request.post('/news', data)
}

// 更新新闻
export function updateNews(id, data) {
  return request.put(`/news/${id}`, data)
}

// 删除新闻
export function deleteNews(id) {
  return request.delete(`/news/${id}`)
}

// 提交审核
export function submitForReview(id) {
  return request.put(`/news/${id}/submit`)
}

// 审核通过
export function approveNews(id) {
  return request.put(`/news/${id}/approve`)
}

// 审核退回
export function rejectNews(id, reason) {
  return request.put(`/news/${id}/reject`, { reason })
}

// 存档
export function archiveNews(id) {
  return request.put(`/news/${id}/archive`)
}
