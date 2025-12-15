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

          <!-- 主题切换按钮 -->
          <el-button
            class="theme-toggle"
            circle
            @click="themeStore.toggleTheme"
            :title="themeStore.isDark ? '切换到浅色模式' : '切换到深色模式'"
            :aria-label="themeStore.isDark ? '切换到浅色模式' : '切换到深色模式'"
          >
            <el-icon><component :is="themeStore.isDark ? 'Sunny' : 'Moon'" /></el-icon>
          </el-button>

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
import { useThemeStore } from '@/stores/theme'
import { getCategoryTree } from '@/api/category'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
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

/* ========== 页首样式 ========== */
.header {
  background: var(--bg-nav);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 10px var(--shadow-color);
  transition: background 0.3s, border-color 0.3s;
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
  color: var(--text-primary);
  letter-spacing: 0.2rem;
}

.logo-divider {
  color: var(--color-primary);
  font-size: 18px;
  font-weight: 300;
}

.logo-sub {
  font-size: 14px;
  color: var(--text-secondary);
  letter-spacing: 0.1rem;
}

.nav-menu {
  display: flex;
  gap: 40px;
}

.nav-item {
  color: var(--text-secondary);
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

.nav-item:hover { color: var(--color-primary); }

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-input { width: 200px; }

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  box-shadow: none;
}

.search-input :deep(.el-input__inner) { color: var(--text-primary); }
.search-input :deep(.el-input__inner::placeholder) { color: var(--text-muted); }

.search-input :deep(.el-input-group__append) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-left: none;
}

.search-input :deep(.el-input-group__append .el-button) { color: var(--text-secondary); }

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-avatar { background: linear-gradient(135deg, #A795BF, #6C5DAB); }
.username { color: var(--text-primary); font-weight: 500; }

.user-role-tag {
  font-size: 10px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
}

.auth-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.05rem;
  padding: 8px 16px;
  transition: all 0.3s;
}

.auth-link:hover { color: var(--color-primary); }

.auth-link--primary {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border-radius: 4px;
  color: #fff;
}

.auth-link--primary:hover { color: #fff; opacity: 0.9; }

/* ========== 主内容区 ========== */
.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
  width: 100%;
}

/* ========== 页脚样式 ========== */
.footer {
  background: var(--bg-nav);
  color: var(--text-primary);
  padding: 40px 30px;
  margin-top: auto;
  border-top: 1px solid var(--border-color);
  transition: background 0.3s, border-color 0.3s;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.footer-slogan {
  font-size: 18px;
  font-weight: 300;
  color: var(--text-secondary);
  letter-spacing: 0.3rem;
  margin-bottom: 20px;
  font-style: italic;
}

.copyright {
  color: var(--text-muted);
  font-size: 12px;
  letter-spacing: 0.2rem;
}

/* ========== 主题切换按钮 ========== */
.theme-toggle {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  transition: all 0.3s;
}

.theme-toggle:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

/* 深色模式下图标白色发光效果 */
:global(html.dark) .theme-toggle {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.3);
}

:global(html.dark) .theme-toggle:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 25px rgba(255, 255, 255, 0.5);
}
</style>
