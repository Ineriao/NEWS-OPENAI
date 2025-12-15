import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 主题状态管理
 * 支持深色/浅色模式切换，并持久化到 localStorage
 */
export const useThemeStore = defineStore('theme', () => {
  // 是否深色模式
  const isDark = ref(false)

  /**
   * 初始化主题
   * 优先级：localStorage > 系统偏好 > 默认浅色
   */
  const init = () => {
    const saved = localStorage.getItem('theme')
    if (saved) {
      isDark.value = saved === 'dark'
    } else {
      // 跟随系统偏好
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()

    // 监听系统主题变化
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem('theme')) {
        isDark.value = e.matches
        applyTheme()
      }
    })
  }

  /**
   * 应用主题到 DOM
   */
  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  /**
   * 切换主题
   */
  const toggleTheme = () => {
    isDark.value = !isDark.value
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  /**
   * 设置主题
   * @param {boolean} dark - 是否深色模式
   */
  const setTheme = (dark) => {
    isDark.value = dark
    localStorage.setItem('theme', dark ? 'dark' : 'light')
    applyTheme()
  }

  return {
    isDark,
    init,
    toggleTheme,
    setTheme
  }
})
