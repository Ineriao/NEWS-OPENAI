import request from '@/utils/request'

/**
 * 通用文件上传
 * @param {File} file 文件对象
 * @returns {Promise} 上传结果
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取上传接口 URL（供编辑器使用）
 * @returns {string} 上传接口完整 URL
 */
export function getEditorUploadUrl() {
  return import.meta.env.VITE_API_BASE_URL + '/files/upload/editor'
}

/**
 * 获取编辑器上传请求头
 * @returns {Object} 请求头对象
 */
export function getEditorUploadHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}
