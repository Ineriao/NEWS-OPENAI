<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="router.push('/home')">
        <span class="logo-text" v-show="!isCollapse">NEWS</span>
        <span class="logo-divider" v-show="!isCollapse">+</span>
        <span class="logo-sub" v-show="!isCollapse">管理后台</span>
        <el-icon v-show="isCollapse" class="logo-icon"><Document /></el-icon>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <!-- 新闻管理 -->
        <el-sub-menu index="news" v-if="userStore.role >= 2">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>新闻管理</span>
          </template>
          <el-menu-item index="/admin/news">新闻列表</el-menu-item>
          <el-menu-item index="/admin/news/create">发布新闻</el-menu-item>
          <el-menu-item v-if="userStore.role >= 3" index="/admin/news/review">新闻审核</el-menu-item>
        </el-sub-menu>

        <!-- 分类管理 -->
        <el-menu-item index="/admin/categories" v-if="userStore.role >= 4">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>

        <!-- 用户管理 -->
        <el-menu-item index="/admin/users" v-if="userStore.role >= 4">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <!-- 评论管理 -->
        <el-menu-item index="/admin/comments" v-if="userStore.role >= 4">
          <el-icon><ChatLineRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">{{ userStore.username.charAt(0) }}</el-avatar>
              <span class="username">{{ userStore.username }}</span>
              <span class="role-tag">{{ userStore.roleName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/home')">
                  <el-icon><House /></el-icon> 返回首页
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background: rgba(0, 0, 0, 0.95);
  transition: width 0.3s;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: opacity 0.3s;
}

.logo:hover { opacity: 0.8; }

.logo-text {
  font-size: 20px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.15rem;
}

.logo-divider {
  color: #FEE9A1;
  font-size: 16px;
  font-weight: 300;
}

.logo-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.1rem;
}

.logo-icon {
  color: #fff;
  font-size: 24px;
}

.admin-menu {
  border-right: none;
  background: transparent;
}

.admin-menu :deep(.el-menu-item),
.admin-menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  letter-spacing: 0.05rem;
}

.admin-menu :deep(.el-menu-item:hover),
.admin-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #FEE9A1;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  color: #fff;
}

.admin-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.3);
}

.admin-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 50px !important;
  font-size: 13px;
}

.admin-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
}

.main-container {
  background: linear-gradient(135deg, #CBD4E5 0%, #A795BF 25%, #FEE9A1 50%, #6C5DAB 75%, #CCA2A7 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.6);
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #6C5DAB;
}

.header :deep(.el-breadcrumb__item) {
  font-weight: 500;
}

.header :deep(.el-breadcrumb__inner) {
  color: rgba(0, 0, 0, 0.6);
}

.header :deep(.el-breadcrumb__inner.is-link:hover) {
  color: #6C5DAB;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.05);
}

.user-avatar {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
}

.username {
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
}

.role-tag {
  font-size: 12px;
  color: #6C5DAB;
  background: linear-gradient(135deg, rgba(167, 149, 191, 0.2), rgba(108, 93, 171, 0.2));
  padding: 2px 10px;
  border-radius: 4px;
  font-weight: 600;
  letter-spacing: 0.05rem;
}

.main {
  padding: 20px;
  overflow-y: auto;
}

.main :deep(.el-card) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.main :deep(.el-card__header) {
  background: rgba(0, 0, 0, 0.95);
  color: #fff;
  border-radius: 12px 12px 0 0;
  padding: 15px 20px;
  border-bottom: none;
}

.main :deep(.el-button--primary) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
}

.main :deep(.el-button--primary:hover) {
  opacity: 0.9;
}

.main :deep(.el-table) {
  background: transparent;
}

.main :deep(.el-table th) {
  background: rgba(0, 0, 0, 0.03);
  color: rgba(0, 0, 0, 0.85);
  font-weight: 600;
}

.main :deep(.el-table tr) {
  background: transparent;
}

.main :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(0, 0, 0, 0.02);
}
</style>
