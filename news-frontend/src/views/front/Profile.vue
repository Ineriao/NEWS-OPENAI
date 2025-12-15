<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-section">
        <el-avatar :size="100" class="user-avatar">
          {{ userStore.username.charAt(0).toUpperCase() }}
        </el-avatar>
        <div class="user-meta">
          <h1 class="username">{{ userStore.username }}</h1>
          <el-tag :type="roleTagType" class="role-tag">{{ userStore.roleName }}</el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="userStore.hasAdminAccess" type="primary" @click="router.push('/admin')">
          <el-icon><Setting /></el-icon>
          进入后台
        </el-button>
        <el-button type="danger" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>

    <div class="profile-content">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 账户信息 -->
        <el-tab-pane label="账户信息" name="account">
          <div class="tab-content">
            <el-descriptions :column="1" border class="account-info">
              <el-descriptions-item label="用户ID">{{ userStore.userId }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ userStore.roleName }}</el-descriptions-item>
              <el-descriptions-item label="权限">
                <template v-if="userStore.hasAdminAccess">
                  <el-tag type="success" size="small">后台管理</el-tag>
                </template>
                <template v-else>
                  <el-tag type="info" size="small">普通用户</el-tag>
                </template>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <!-- 我的收藏 -->
        <el-tab-pane label="我的收藏" name="collections">
          <div class="tab-content" v-loading="collectionsLoading">
            <div v-if="collections.length > 0" class="list-container">
              <div
                v-for="item in collections"
                :key="item.id"
                class="list-item"
                @click="router.push(`/home/news/${item.id}`)"
              >
                <div class="item-content">
                  <h4 class="item-title">{{ item.title }}</h4>
                  <p class="item-summary">{{ item.summary }}</p>
                  <div class="item-meta">
                    <span><el-icon><Clock /></el-icon> {{ formatTime(item.publishTime) }}</span>
                    <span><el-icon><View /></el-icon> {{ item.viewCount }} 阅读</span>
                  </div>
                </div>
              </div>
              <div class="pagination" v-if="collectionsTotal > collectionsPageSize">
                <el-pagination
                  v-model:current-page="collectionsPage"
                  :page-size="collectionsPageSize"
                  :total="collectionsTotal"
                  layout="prev, pager, next"
                  @current-change="fetchCollections"
                />
              </div>
            </div>
            <el-empty v-else description="暂无收藏" />
          </div>
        </el-tab-pane>

        <!-- 我的评论 -->
        <el-tab-pane label="我的评论" name="comments">
          <div class="tab-content" v-loading="commentsLoading">
            <div v-if="comments.length > 0" class="list-container">
              <div
                v-for="item in comments"
                :key="item.id"
                class="list-item comment-item"
                @click="router.push(`/home/news/${item.newsId}`)"
              >
                <div class="item-content">
                  <div class="comment-header">
                    <span class="news-link">{{ item.newsTitle }}</span>
                    <span class="comment-time">{{ formatTime(item.createTime) }}</span>
                  </div>
                  <p class="comment-text">{{ item.content }}</p>
                </div>
              </div>
              <div class="pagination" v-if="commentsTotal > commentsPageSize">
                <el-pagination
                  v-model:current-page="commentsPage"
                  :page-size="commentsPageSize"
                  :total="commentsTotal"
                  layout="prev, pager, next"
                  @current-change="fetchComments"
                />
              </div>
            </div>
            <el-empty v-else description="暂无评论" />
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <div class="tab-content">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="password-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                  placeholder="请输入当前密码"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入新密码"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入新密码"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="passwordLoading"
                  @click="handleChangePassword"
                >
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { getUserCollections } from '@/api/news'
import { getUserComments } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('account')

// 密码相关
const passwordFormRef = ref(null)
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 收藏列表
const collectionsLoading = ref(false)
const collections = ref([])
const collectionsPage = ref(1)
const collectionsPageSize = ref(10)
const collectionsTotal = ref(0)

// 评论列表
const commentsLoading = ref(false)
const comments = ref([])
const commentsPage = ref(1)
const commentsPageSize = ref(10)
const commentsTotal = ref(0)

const roleTagType = computed(() => {
  const typeMap = {
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'danger'
  }
  return typeMap[userStore.role] || 'info'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

// 获取收藏列表
const fetchCollections = async () => {
  collectionsLoading.value = true
  try {
    const res = await getUserCollections({
      page: collectionsPage.value,
      size: collectionsPageSize.value
    })
    collections.value = res.data?.records || []
    collectionsTotal.value = res.data?.total || 0
  } catch (error) {
    console.error('获取收藏失败', error)
  } finally {
    collectionsLoading.value = false
  }
}

// 获取评论历史
const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const res = await getUserComments({
      pageNum: commentsPage.value,
      pageSize: commentsPageSize.value
    })
    comments.value = res.data?.list || []
    commentsTotal.value = res.data?.total || 0
  } catch (error) {
    console.error('获取评论失败', error)
  } finally {
    commentsLoading.value = false
  }
}

// 切换 Tab 时加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'collections' && collections.value.length === 0) {
    fetchCollections()
  } else if (newTab === 'comments' && comments.value.length === 0) {
    fetchComments()
  }
})

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await changePassword({
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      userStore.logout()
      router.push('/login')
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      passwordLoading.value = false
    }
  })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/home')
  }).catch(() => {})
}
</script>

<style scoped>
.profile-page {
  max-width: 900px;
  margin: 0 auto;
}

.profile-header {
  background: linear-gradient(135deg, rgba(167, 149, 191, 0.3), rgba(108, 93, 171, 0.3));
  border-radius: 16px;
  padding: 30px 40px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  font-size: 36px;
  font-weight: bold;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.role-tag {
  width: fit-content;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
}

.profile-content {
  background: var(--bg-card);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px var(--shadow-color);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.profile-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-weight: 500;
  padding: 0 24px;
  height: 50px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tab-content {
  padding: 24px;
  min-height: 300px;
}

.account-info :deep(.el-descriptions__label) {
  font-weight: 600;
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.account-info :deep(.el-descriptions__content) {
  color: var(--text-primary);
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.list-item:hover {
  border-color: var(--color-primary);
  transform: translateX(4px);
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.item-summary {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 10px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 16px;
  color: var(--text-muted);
  font-size: 13px;
}

.item-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-item .comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-item .news-link {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}

.comment-item .comment-time {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-item .comment-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.password-form {
  max-width: 400px;
}

.password-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
}

.password-form :deep(.el-button--primary:hover) {
  opacity: 0.9;
}
</style>
