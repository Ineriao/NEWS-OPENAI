<template>
  <div class="news-detail" v-loading="loading">
    <template v-if="news">
      <!-- 新闻内容 -->
      <el-card class="news-card">
        <h1 class="news-title">{{ news.title }}</h1>
        <div class="news-meta">
          <span><el-icon><FolderOpened /></el-icon> {{ news.categoryName }}</span>
          <span><el-icon><User /></el-icon> {{ news.authorName }}</span>
          <span><el-icon><Clock /></el-icon> {{ formatTime(news.publishTime) }}</span>
          <span><el-icon><View /></el-icon> {{ news.viewCount }} 阅读</span>
        </div>
        <div class="news-actions">
          <el-button
            :type="liked ? 'primary' : 'default'"
            :class="{ 'is-liked': liked }"
            @click="handleLike"
          >
            <el-icon><Pointer /></el-icon>
            <span>{{ likeCount }}</span>
          </el-button>
          <el-button
            :type="collected ? 'warning' : 'default'"
            :class="{ 'is-collected': collected }"
            @click="handleCollect"
          >
            <el-icon><component :is="collected ? 'StarFilled' : 'Star'" /></el-icon>
            <span>{{ collectionCount }}</span>
          </el-button>
        </div>
        <el-divider />
        <div class="news-content" v-html="news.content"></div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card">
        <template #header>
          <span>评论 ({{ comments.length }})</span>
        </template>

        <!-- 发表评论 -->
        <div class="comment-form" v-if="userStore.isLoggedIn">
          <!-- 回复提示条 -->
          <div v-if="replyTo" class="reply-hint">
            <span>回复 @{{ replyTo.username }}</span>
            <el-button type="primary" link size="small" @click="cancelReply">取消</el-button>
          </div>
          <el-input
            ref="commentInputRef"
            v-model="commentContent"
            type="textarea"
            :rows="3"
            :placeholder="replyTo ? `回复 @${replyTo.username}...` : '发表你的评论...'"
          />
          <div class="comment-toolbar">
            <!-- 表情选择器 -->
            <el-popover
              placement="bottom-start"
              :width="320"
              trigger="click"
              :teleported="false"
            >
              <template #reference>
                <el-button type="default" size="small" class="emoji-btn" title="插入表情" aria-label="插入表情">
                  <span class="emoji-icon">😊</span> 表情
                </el-button>
              </template>
              <div class="emoji-picker">
                <span
                  v-for="emoji in emojiList"
                  :key="emoji"
                  class="emoji-item"
                  @click="insertEmoji(emoji)"
                >
                  {{ emoji }}
                </span>
              </div>
            </el-popover>
            <span class="tip">支持表情和链接（链接将自动识别）</span>
            <el-button type="primary" :loading="submitting" @click="submitComment">
              {{ replyTo ? '回复' : '发表评论' }}
            </el-button>
          </div>
        </div>
        <div class="login-tip" v-else>
          <router-link to="/login">登录</router-link> 后参与评论
        </div>

        <el-divider />

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <el-avatar :size="36" class="comment-avatar">{{ comment.username?.charAt(0) }}</el-avatar>
              <div class="comment-info">
                <span class="username">{{ comment.username }}</span>
                <span class="time">{{ formatTime(comment.createTime) }}</span>
              </div>
            </div>
            <div class="comment-content" v-html="renderContent(comment.content)"></div>
            <div class="comment-actions">
              <el-button
                type="primary"
                link
                size="small"
                @click="replyTo = comment"
              >
                回复
              </el-button>
              <el-button
                v-if="canDeleteComment(comment)"
                type="danger"
                link
                size="small"
                @click="handleDeleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>

            <!-- 回复列表 -->
            <div v-if="comment.replies?.length" class="replies">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <span class="username">{{ reply.username }}</span>
                <span v-if="reply.replyToUsername" class="reply-to">
                  回复 <span class="reply-username">@{{ reply.replyToUsername }}</span>
                </span>
                <span class="content" v-html="': ' + renderContent(reply.content)"></span>
              </div>
            </div>
          </div>

          <el-empty v-if="!comments.length" description="暂无评论" />
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNewsDetail, toggleLike, getLikeStatus, toggleCollection, getCollectionStatus } from '@/api/news'
import { getComments, createComment, deleteComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const news = ref(null)
const comments = ref([])
const commentContent = ref('')
const replyTo = ref(null)
const commentInputRef = ref(null)

// 点赞和收藏状态
const liked = ref(false)
const likeCount = ref(0)
const collected = ref(false)
const collectionCount = ref(0)

// 表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭',
  '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒',
  '🙄', '😬', '🤯', '😳', '🥺', '😢', '😭', '😤',
  '😠', '😡', '🤬', '😈', '👿', '💀', '☠️', '💩',
  '👍', '👎', '👏', '🙌', '🤝', '🙏', '💪', '❤️',
  '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '💯'
]

// 插入表情
const insertEmoji = (emoji) => {
  commentContent.value += emoji
}

// 渲染评论内容（自动识别链接）
const renderContent = (content) => {
  if (!content) return ''
  // 转义 HTML 特殊字符，防止 XSS
  const escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')

  // URL 正则匹配
  const urlRegex = /(https?:\/\/[^\s<]+)/g
  return escaped.replace(urlRegex, '<a href="$1" target="_blank" rel="noopener noreferrer" class="comment-link">$1</a>')
}

const fetchNews = async () => {
  loading.value = true
  try {
    const res = await getNewsDetail(route.params.id)
    news.value = res.data
  } catch (error) {
    console.error('获取新闻详情失败', error)
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data || []
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

// 获取点赞状态
const fetchLikeStatus = async () => {
  try {
    const res = await getLikeStatus(route.params.id)
    liked.value = res.data?.liked || false
    likeCount.value = res.data?.likeCount || 0
  } catch (error) {
    console.error('获取点赞状态失败', error)
  }
}

// 获取收藏状态
const fetchCollectionStatus = async () => {
  try {
    const res = await getCollectionStatus(route.params.id)
    collected.value = res.data?.collected || false
    collectionCount.value = res.data?.collectionCount || 0
  } catch (error) {
    console.error('获取收藏状态失败', error)
  }
}

// 点赞/取消点赞
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await toggleLike(route.params.id)
    liked.value = res.data?.liked
    likeCount.value = res.data?.likeCount
    ElMessage.success(liked.value ? '点赞成功' : '已取消点赞')
  } catch (error) {
    console.error('点赞操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 收藏/取消收藏
const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await toggleCollection(route.params.id)
    collected.value = res.data?.collected
    collectionCount.value = res.data?.collectionCount
    ElMessage.success(collected.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error('收藏操作失败', error)
    ElMessage.error('操作失败')
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment({
      newsId: Number(route.params.id),
      content: commentContent.value,
      parentId: replyTo.value?.id
    })
    ElMessage.success(replyTo.value ? '回复成功' : '评论成功')
    commentContent.value = ''
    replyTo.value = null
    fetchComments()
  } catch (error) {
    console.error('评论失败', error)
  } finally {
    submitting.value = false
  }
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 判断是否可以删除评论
const canDeleteComment = (comment) => {
  if (!userStore.isLoggedIn) return false
  // 作者或管理员可以删除
  return comment.userId === userStore.userId || userStore.role >= 4
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchNews()
  fetchComments()
  fetchLikeStatus()
  fetchCollectionStatus()
})
</script>

<style scoped>
.news-detail {
  max-width: 900px;
  margin: 0 auto;
}

.news-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
}

.news-card :deep(.el-card__body) {
  padding: 30px;
}

.news-title {
  font-size: 28px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.85);
  margin-bottom: 15px;
  letter-spacing: 0.05rem;
}

.news-meta {
  display: flex;
  gap: 20px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.news-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.news-actions {
  display: flex;
  gap: 12px;
  margin-top: 15px;
}

.news-actions .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.news-actions .el-button.is-liked {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border-color: #6C5DAB;
  color: #fff;
}

.news-actions .el-button.is-collected {
  background: linear-gradient(135deg, #F5A623, #E6930D);
  border-color: #E6930D;
  color: #fff;
}

.news-actions .el-button:not(.is-liked):not(.is-collected):hover {
  border-color: #6C5DAB;
  color: #6C5DAB;
}

.news-content {
  line-height: 1.8;
  font-size: 16px;
  color: rgba(0, 0, 0, 0.75);
}

.comment-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
}

.comment-card :deep(.el-card__header) {
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

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

.comment-form .el-input {
  width: 100%;
}

.comment-form .el-button {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
}

.reply-hint {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(108, 93, 171, 0.1);
  border-radius: 6px;
  color: #6C5DAB;
  font-size: 14px;
}

.login-tip {
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  padding: 20px;
}

.login-tip a {
  color: #6C5DAB;
  font-weight: 600;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-avatar {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
}

.comment-info {
  display: flex;
  flex-direction: column;
}

.comment-info .username {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.comment-info .time {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.comment-content {
  color: rgba(0, 0, 0, 0.75);
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-actions .el-button {
  color: #6C5DAB;
}

.replies {
  background: rgba(0, 0, 0, 0.03);
  padding: 15px;
  border-radius: 8px;
  margin-top: 10px;
}

.reply-item {
  padding: 8px 0;
  font-size: 14px;
}

.reply-item .username {
  color: #6C5DAB;
  font-weight: 600;
}

.reply-item .reply-to {
  color: rgba(0, 0, 0, 0.45);
}

.reply-item .reply-username {
  color: #6C5DAB;
}

.reply-item .content {
  color: rgba(0, 0, 0, 0.75);
}

/* 评论工具栏 */
.comment-toolbar {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-toolbar .tip {
  flex: 1;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.emoji-btn {
  background: #fff !important;
  border: 1px solid #dcdfe6 !important;
  color: #606266 !important;
}

.emoji-btn:hover {
  border-color: #6C5DAB !important;
  color: #6C5DAB !important;
}

.emoji-icon {
  font-size: 16px;
  margin-right: 4px;
}

/* 表情选择器 */
.emoji-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background: rgba(108, 93, 171, 0.1);
}

/* 评论中的链接 */
.comment-content :deep(.comment-link),
.reply-item .content :deep(.comment-link) {
  color: #6C5DAB;
  text-decoration: none;
  word-break: break-all;
}

.comment-content :deep(.comment-link):hover,
.reply-item .content :deep(.comment-link):hover {
  text-decoration: underline;
}
</style>
