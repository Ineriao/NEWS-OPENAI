<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>LOGIN</h1>
        <p>欢迎回来</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <router-link to="/register">注册账号</router-link>
        <span class="divider">+</span>
        <router-link to="/home">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    userStore.login(res.data)
    ElMessage.success('登录成功')

    // 根据角色自动跳转
    if (route.query.redirect) {
      // 有指定跳转地址，使用指定地址
      router.push(route.query.redirect)
    } else if (res.data.role >= 2) {
      // 管理人员（编辑及以上）跳转后台
      router.push('/admin/dashboard')
    } else {
      // 普通用户跳转首页
      router.push('/home')
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #CBD4E5 0%, #A795BF 25%, #FEE9A1 50%, #6C5DAB 75%, #CCA2A7 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-container {
  width: 420px;
  padding: 50px 40px;
  background: rgba(0, 0, 0, 0.9);
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 32px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.5rem;
  margin-bottom: 10px;
}

.login-header p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  letter-spacing: 0.1rem;
}

.login-form {
  margin-bottom: 30px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: none;
}

.login-form :deep(.el-input__inner) {
  color: #fff;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.login-form :deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.5);
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
  font-weight: 600;
  letter-spacing: 0.2rem;
  text-transform: uppercase;
}

.login-btn:hover {
  opacity: 0.9;
}

.login-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

.login-footer a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 13px;
  letter-spacing: 0.05rem;
  transition: color 0.3s;
}

.login-footer a:hover {
  color: #FEE9A1;
}

.divider {
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
}
</style>
