<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h1>REGISTER</h1>
        <p>创建新账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
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

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
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
            class="register-btn"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <router-link to="/login">已有账号</router-link>
        <span class="divider">+</span>
        <router-link to="/">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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

.register-container {
  width: 420px;
  padding: 50px 40px;
  background: rgba(0, 0, 0, 0.9);
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h1 {
  font-size: 32px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.5rem;
  margin-bottom: 10px;
}

.register-header p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  letter-spacing: 0.1rem;
}

.register-form {
  margin-bottom: 30px;
}

.register-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: none;
}

.register-form :deep(.el-input__inner) {
  color: #fff;
}

.register-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.register-form :deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.5);
}

.register-btn {
  width: 100%;
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
  font-weight: 600;
  letter-spacing: 0.2rem;
  text-transform: uppercase;
}

.register-btn:hover {
  opacity: 0.9;
}

.register-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

.register-footer a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 13px;
  letter-spacing: 0.05rem;
  transition: color 0.3s;
}

.register-footer a:hover {
  color: #FEE9A1;
}

.divider {
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
}
</style>
