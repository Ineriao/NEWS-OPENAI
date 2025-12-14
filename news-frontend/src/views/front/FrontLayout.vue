<template>
  <div class="front-layout">
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <span class="logo-text">NEWS</span>
          <span class="logo-divider">+</span>
          <span class="logo-sub">发布系统</span>
        </div>

        <nav class="nav-menu">
          <router-link to="/home" class="nav-item">首页</router-link>
          <el-dropdown v-if="categories.length" trigger="hover">
            <span class="nav-item">
              新闻分类 <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="cat in categories" :key="cat.id">
                  <el-dropdown-item @click="router.push(`/home/category/${cat.id}`)">
                    {{ cat.name }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-for="child in cat.children"
                    :key="child.id"
                    @click="router.push(`/home/category/${child.id}`)"
                    style="padding-left: 30px; color: #999;"
                  >
                    {{ child.name }}
                  </el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索新闻..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>

          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="32" class="user-avatar">{{ userStore.username.charAt(0) }}</el-avatar>
                <span class="username">{{ userStore.username }}</span>
                <el-tag size="small" class="user-role-tag" :type="roleTagType">{{ userStore.roleName }}</el-tag>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/home/profile')">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.hasAdminAccess" @click="router.push('/admin')">
                    <el-icon><Setting /></el-icon> 管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="auth-link">登录</router-link>
            <router-link to="/register" class="auth-link auth-link--primary">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-slogan">媒介即讯息</div>
        <p class="copyright">NEWS PUBLISHING SYSTEM © 2025</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCategoryTree } from '@/api/category'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const searchKeyword = ref('')

// 角色标签类型
const roleTagType = computed(() => {
  const typeMap = {
    1: 'info',
    2: 'warning',
    3: 'success',
    4: 'danger'
  }
  return typeMap[userStore.role] || 'info'
})

const fetchCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { keyword: searchKeyword.value.trim() } })
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/home')
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: rgba(0, 0, 0, 0.95);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover { opacity: 0.8; }

.logo-text {
  font-size: 24px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.2rem;
}

.logo-divider {
  color: #FEE9A1;
  font-size: 18px;
  font-weight: 300;
}

.logo-sub {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.1rem;
}

.nav-menu {
  display: flex;
  gap: 40px;
}

.nav-item {
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  transition: color 0.3s;
  text-decoration: none;
}

.nav-item:hover { color: #FEE9A1; }

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-input { width: 200px; }

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: none;
}

.search-input :deep(.el-input__inner) { color: #fff; }
.search-input :deep(.el-input__inner::placeholder) { color: rgba(255, 255, 255, 0.5); }

.search-input :deep(.el-input-group__append) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-left: none;
}

.search-input :deep(.el-input-group__append .el-button) { color: rgba(255, 255, 255, 0.8); }

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-avatar { background: linear-gradient(135deg, #A795BF, #6C5DAB); }
.username { color: rgba(255, 255, 255, 0.9); font-weight: 500; }

.user-role-tag {
  font-size: 10px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
}

.auth-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.05rem;
  padding: 8px 16px;
  transition: all 0.3s;
}

.auth-link:hover { color: #FEE9A1; }

.auth-link--primary {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border-radius: 4px;
  color: #fff;
}

.auth-link--primary:hover { color: #fff; opacity: 0.9; }

.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
  width: 100%;
}

.footer {
  background: rgba(0, 0, 0, 0.95);
  color: #fff;
  padding: 40px 30px;
  margin-top: auto;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.footer-slogan {
  font-size: 18px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.3rem;
  margin-bottom: 20px;
  font-style: italic;
}

.copyright {
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
  letter-spacing: 0.2rem;
}
</style>
