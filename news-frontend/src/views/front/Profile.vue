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
    </div>

    <div class="profile-content">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>账户信息</span>
          </div>
        </template>

        <el-descriptions :column="1" border>
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
      </el-card>

      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>修改密码</span>
          </div>
        </template>

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
      </el-card>

      <el-card class="profile-card quick-actions">
        <template #header>
          <div class="card-header">
            <span>快捷操作</span>
          </div>
        </template>

        <div class="action-buttons">
          <el-button v-if="userStore.hasAdminAccess" type="primary" @click="router.push('/admin')">
            <el-icon><Setting /></el-icon>
            进入后台管理
          </el-button>
          <el-button type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const passwordFormRef = ref(null)
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

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
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  background: linear-gradient(135deg, rgba(167, 149, 191, 0.3), rgba(108, 93, 171, 0.3));
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
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
  font-size: 28px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
  margin: 0;
}

.role-tag {
  width: fit-content;
  font-weight: 600;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.profile-card :deep(.el-card__header) {
  background: rgba(0, 0, 0, 0.95);
  color: #fff;
  border-radius: 12px 12px 0 0;
  padding: 15px 20px;
  border-bottom: none;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.password-form {
  max-width: 400px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-card :deep(.el-descriptions__label) {
  font-weight: 600;
  background: rgba(0, 0, 0, 0.02);
}

.profile-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
}

.profile-card :deep(.el-button--primary:hover) {
  opacity: 0.9;
}
</style>
