import request from '@/utils/request'

/**
 * AI 对话
 * @param {Object} data - { message: string, newsContent?: string, newsId?: number }
 */
export function aiChat(data) {
  return request.post('/ai/chat', data)
}

/**
 * AI 总结新闻
 * @param {string} newsContent - 新闻内容
 */
export function aiSummarize(newsContent) {
  return request.post('/ai/summarize', { newsContent })
}
