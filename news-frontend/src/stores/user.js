import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || 0)
  const username = computed(() => userInfo.value?.username || '')
  const userId = computed(() => userInfo.value?.id || null)

  // 角色名称映射
  const roleName = computed(() => {
    const roleMap = {
      1: '普通用户',
      2: '编辑',
      3: '总编',
      4: '管理员'
    }
    return roleMap[role.value] || '游客'
  })

  // 是否有管理权限（编辑及以上）
  const hasAdminAccess = computed(() => role.value >= 2)

  // Actions
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function login(data) {
    setToken(data.token)
    setUserInfo({
      id: data.userId,
      username: data.username,
      role: data.role
    })
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    role,
    username,
    userId,
    roleName,
    hasAdminAccess,
    setToken,
    setUserInfo,
    login,
    logout
  }
})
