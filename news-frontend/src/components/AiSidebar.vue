<template>
  <!-- 右侧悬浮提示标签 - 深色风格 -->
  <div
    class="ai-trigger-tab"
    v-show="!isOpen"
    @mouseenter="showSidebar"
    @click="showSidebar"
  >
    <el-icon :size="16"><ChatDotRound /></el-icon>
    <span class="tab-text">AI</span>
  </div>

  <!-- 右侧触发区域 -->
  <div class="ai-trigger-zone" @mouseenter="showSidebar"></div>

  <!-- AI 侧边栏 - 玻璃拟态风格 -->
  <transition name="ai-slide">
    <div class="ai-sidebar" v-show="isOpen" @mouseenter="cancelLeaveTimer" @mouseleave="handleMouseLeave">
      <!-- 深色头部 -->
      <div class="sidebar-header">
        <span class="header-title">AI 助手</span>
        <el-button @click="hideSidebar" text circle size="small">
          <el-icon :size="14"><Close /></el-icon>
        </el-button>
      </div>

      <!-- 未登录提示 -->
      <div v-if="!userStore.isLoggedIn" class="login-prompt">
        <el-icon :size="40" color="rgba(0,0,0,0.3)"><Lock /></el-icon>
        <p class="prompt-title">登录后使用</p>
        <p class="prompt-sub">AI 助手可帮你搜索、总结新闻</p>
        <el-button type="primary" class="login-btn" @click="goLogin">去登录</el-button>
      </div>

      <!-- 已登录：聊天界面 -->
      <template v-else>
        <!-- 消息列表 -->
        <div class="sidebar-messages" ref="messagesRef">
          <div v-if="messages.length === 0" class="empty-hint">
            <el-icon :size="28" color="rgba(0,0,0,0.25)"><ChatLineSquare /></el-icon>
            <p class="hint-title">AI 新闻助手</p>
            <p class="hint-sub">可以帮你搜索、总结、解读新闻内容</p>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-content">
              <div class="message-bubble">{{ msg.content }}</div>
              <div v-if="msg.tokens" class="message-meta">
                {{ msg.tokens }} tokens
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="message ai">
            <div class="message-content">
              <div class="message-bubble loading">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="sidebar-input">
          <el-input
            v-model="inputText"
            placeholder="输入问题..."
            @keyup.enter="sendMessage"
            :disabled="loading"
            size="default"
          >
            <template #append>
              <el-button @click="sendMessage" :loading="loading">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </template>
    </div>
  </transition>
</template>

<script setup>
import { ref, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { aiChat } from '@/api/ai'
import { Close, Lock, Promotion, ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isOpen = ref(false)
const inputText = ref('')
const loading = ref(false)
const messages = ref([])
const messagesRef = ref(null)

// 离开定时器
let leaveTimer = null

// 取消离开定时器
const cancelLeaveTimer = () => {
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
}

// 显示侧边栏
const showSidebar = () => {
  cancelLeaveTimer()  // 显示时取消任何待执行的关闭定时器
  isOpen.value = true
}

// 隐藏侧边栏
const hideSidebar = () => {
  cancelLeaveTimer()
  isOpen.value = false
}

// 鼠标离开处理（延迟关闭）
const handleMouseLeave = () => {
  cancelLeaveTimer()  // 先清除之前的定时器
  leaveTimer = setTimeout(() => {
    hideSidebar()
  }, 500)  // 增加延迟到 500ms，减少误触
}

// 去登录
const goLogin = () => {
  hideSidebar()
  router.push('/login')
}

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || loading.value) return

  const userMessage = inputText.value.trim()
  inputText.value = ''

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage
  })

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 调用 AI API
  loading.value = true
  try {
    const res = await aiChat({ message: userMessage })
    if (res.code === 200 && res.data) {
      messages.value.push({
        role: 'ai',
        content: res.data.reply || '抱歉，我无法回答这个问题。',
        tokens: res.data.tokens
      })
    } else {
      messages.value.push({
        role: 'ai',
        content: res.message || '服务暂时不可用，请稍后再试。'
      })
    }
  } catch (error) {
    messages.value.push({
      role: 'ai',
      content: '网络错误，请检查网络连接后重试。'
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 清除定时器
onUnmounted(() => {
  cancelLeaveTimer()
})
</script>

<style scoped>
/* ========== 触发标签 - 深浅色适配 ========== */
.ai-trigger-tab {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: var(--bg-nav);
  color: var(--text-primary);
  padding: 14px 10px;
  border-radius: 8px 0 0 8px;
  cursor: pointer;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  box-shadow: -2px 0 12px var(--shadow-color);
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
  border-right: none;
}

.ai-trigger-tab:hover {
  background: var(--bg-secondary);
  padding-right: 14px;
}

.ai-trigger-tab:hover .tab-text {
  color: var(--color-primary);
}

.ai-trigger-tab .tab-text {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1rem;
  text-transform: uppercase;
  transition: color 0.3s;
}

/* ========== 触发区域 ========== */
.ai-trigger-zone {
  position: fixed;
  top: 0;
  right: 0;
  width: 20px;
  height: 100vh;
  z-index: 998;
}

/* ========== 侧边栏 - 深浅色适配 ========== */
.ai-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  width: 360px;
  height: 100vh;
  background: var(--bg-card);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  box-shadow: -4px 0 20px var(--shadow-color);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  border-left: 1px solid var(--border-color);
}

/* ========== 滑入动画 ========== */
.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: transform 0.3s ease;
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  transform: translateX(100%);
}

/* ========== 头部 - 深浅色适配 ========== */
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background: var(--bg-nav);
  border-bottom: 1px solid var(--border-color);
}

.header-title {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.1rem;
  text-transform: uppercase;
}

.sidebar-header .el-button {
  color: var(--text-secondary);
}

.sidebar-header .el-button:hover {
  color: var(--color-primary);
  background: transparent;
}

/* ========== 未登录提示 ========== */
.login-prompt {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
}

.login-prompt .el-icon {
  color: var(--text-muted) !important;
}

.prompt-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.prompt-sub {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

.login-btn {
  margin-top: 8px;
  background: var(--color-primary);
  border: none;
  padding: 10px 28px;
  font-weight: 600;
  letter-spacing: 0.05rem;
}

.login-btn:hover {
  opacity: 0.9;
}

/* ========== 消息列表 ========== */
.sidebar-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ========== 空提示 ========== */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.empty-hint .el-icon {
  color: var(--text-muted) !important;
}

.hint-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 12px 0 4px 0;
}

.hint-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
}

/* ========== 消息样式 ========== */
.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.ai {
  justify-content: flex-start;
}

.message-content {
  max-width: 85%;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.message.user .message-bubble {
  background: var(--color-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message.ai .message-bubble {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
}

.message-meta {
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 4px;
  text-align: right;
}

/* ========== 加载动画 ========== */
.message-bubble.loading {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
}

.dot {
  width: 6px;
  height: 6px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* ========== 输入区域 ========== */
.sidebar-input {
  padding: 14px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.sidebar-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: var(--bg-secondary);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

.sidebar-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.sidebar-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.sidebar-input :deep(.el-input__inner) {
  color: var(--text-primary);
}

.sidebar-input :deep(.el-input-group__append) {
  background: var(--color-primary);
  border: none;
  box-shadow: none;
}

.sidebar-input :deep(.el-input-group__append .el-button) {
  color: #fff;
}

.sidebar-input :deep(.el-input-group__append .el-button:hover) {
  color: var(--color-accent);
}

/* ========== 自定义滚动条 ========== */
.sidebar-messages::-webkit-scrollbar {
  width: 4px;
}

.sidebar-messages::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-messages::-webkit-scrollbar-thumb {
  background: var(--scrollbar-thumb);
  border-radius: 2px;
}

.sidebar-messages::-webkit-scrollbar-thumb:hover {
  background: var(--scrollbar-thumb-hover);
}
</style>
