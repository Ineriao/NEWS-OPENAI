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
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="发表你的评论..."
          />
          <el-button type="primary" :loading="submitting" @click="submitComment">
            发表评论
          </el-button>
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
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-actions">
              <el-button
                type="primary"
                link
                size="small"
                @click="replyTo = comment"
              >
                回复
              </el-button>
            </div>

            <!-- 回复列表 -->
            <div v-if="comment.replies?.length" class="replies">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <span class="username">{{ reply.username }}</span>
                <span v-if="reply.replyToUsername" class="reply-to">
                  回复 <span class="reply-username">@{{ reply.replyToUsername }}</span>
                </span>
                <span class="content">：{{ reply.content }}</span>
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
import { getNewsDetail } from '@/api/news'
import { getComments, createComment } from '@/api/comment'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const news = ref(null)
const comments = ref([])
const commentContent = ref('')
const replyTo = ref(null)

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
    ElMessage.success('评论成功')
    commentContent.value = ''
    replyTo.value = null
    fetchComments()
  } catch (error) {
    console.error('评论失败', error)
  } finally {
    submitting.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchNews()
  fetchComments()
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

.news-content {
  line-height: 1.8;
  font-size: 16px;
  color: rgba(0, 0, 0, 0.75);
}

.comment-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
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
</style>
