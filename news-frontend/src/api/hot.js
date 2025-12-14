import request from '@/utils/request'

// 获取微博热搜
export function getWeiboHot() {
  return request.get('/public/hot/weibo')
}

// 获取抖音热搜
export function getDouyinHot() {
  return request.get('/public/hot/douyin')
}

// 获取知乎热搜
export function getZhihuHot() {
  return request.get('/public/hot/zhihu')
}

// 获取百度热搜
export function getBaiduHot() {
  return request.get('/public/hot/baidu')
}

// 获取所有平台热搜
export function getAllHot() {
  return request.get('/public/hot/all')
}
