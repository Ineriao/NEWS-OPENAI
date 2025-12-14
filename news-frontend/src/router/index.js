import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // ==================== 落地页 ====================
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/front/Landing.vue'),
    meta: { title: '欢迎' }
  },

  // ==================== 前台路由 ====================
  {
    path: '/home',
    component: () => import('@/views/front/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/front/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'category/:id',
        name: 'Category',
        component: () => import('@/views/front/Category.vue'),
        meta: { title: '分类' }
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('@/views/front/NewsDetail.vue'),
        meta: { title: '新闻详情' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/front/Search.vue'),
        meta: { title: '搜索结果' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/front/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/front/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/front/Register.vue'),
    meta: { title: '注册' }
  },

  // ==================== 后台路由 ====================
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'news',
        name: 'NewsList',
        component: () => import('@/views/admin/news/NewsList.vue'),
        meta: { title: '新闻列表', roles: [2, 3, 4] }
      },
      {
        path: 'news/create',
        name: 'NewsCreate',
        component: () => import('@/views/admin/news/NewsEdit.vue'),
        meta: { title: '发布新闻', roles: [2, 3, 4] }
      },
      {
        path: 'news/edit/:id',
        name: 'NewsEdit',
        component: () => import('@/views/admin/news/NewsEdit.vue'),
        meta: { title: '编辑新闻', roles: [2, 3, 4] }
      },
      {
        path: 'news/review',
        name: 'NewsReview',
        component: () => import('@/views/admin/news/NewsReview.vue'),
        meta: { title: '新闻审核', roles: [3, 4] }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/admin/category/CategoryList.vue'),
        meta: { title: '分类管理', roles: [4] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/admin/user/UserList.vue'),
        meta: { title: '用户管理', roles: [4] }
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('@/views/admin/comment/CommentList.vue'),
        meta: { title: '评论管理', roles: [4] }
      }
    ]
  },

  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/front/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 新闻发布系统` : '新闻发布系统'

  const userStore = useUserStore()

  // 需要登录但未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限但没有
  if (to.meta.requiresAdmin && !userStore.hasAdminAccess) {
    next({ name: 'Home' })
    return
  }

  // 特定角色限制
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    next({ name: 'Dashboard' })
    return
  }

  next()
})

export default router
