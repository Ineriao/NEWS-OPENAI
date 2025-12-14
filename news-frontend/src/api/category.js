import request from '@/utils/request'

// ==================== 公开接口 ====================

// 获取分类树
export function getCategoryTree() {
  return request.get('/public/categories')
}

// 根据 ID 获取单个分类
export function getCategoryById(id) {
  return request.get(`/public/categories/detail/${id}`)
}

// ==================== 管理接口 ====================

// 创建分类
export function createCategory(data) {
  return request.post('/categories', data)
}

// 更新分类
export function updateCategory(id, data) {
  return request.put(`/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
