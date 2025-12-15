import request from '@/utils/request'

// ==================== 公开接口 ====================

// 获取新闻评论列表
export function getComments(newsId) {
  return request.get(`/public/comments/${newsId}`)
}

// 获取评论数
export function getCommentCount(newsId) {
  return request.get(`/public/comments/${newsId}/count`)
}

// ==================== 需要登录的接口 ====================

// 发表评论
export function createComment(data) {
  return request.post('/comments', data)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}

// 点赞/取消点赞评论
export function toggleCommentLike(id) {
  return request.post(`/comments/${id}/like`)
}

// 获取用户的评论历史
export function getUserComments(params) {
  return request.get('/user/comments', { params })
}
