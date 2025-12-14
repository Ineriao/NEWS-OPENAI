<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <span>用户管理</span>
      </template>

      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="openRoleDialog(row)">
              修改角色
            </el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="修改角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">{{ currentUser?.username }}</el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newRole" style="width: 100%;">
            <el-option label="普通用户" :value="1" />
            <el-option label="编辑" :value="2" />
            <el-option label="总编" :value="3" />
            <el-option label="管理员" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const roleDialogVisible = ref(false)
const currentUser = ref(null)
const newRole = ref(1)

const roleText = (role) => {
  const map = { 1: '普通用户', 2: '编辑', 3: '总编', 4: '管理员' }
  return map[role] || '未知'
}

const roleType = (role) => {
  const map = { 1: '', 2: 'warning', 3: 'success', 4: 'danger' }
  return map[role] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/users', { params: { page: page.value, size: pageSize.value } })
    userList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取用户失败', error)
  } finally {
    loading.value = false
  }
}

const openRoleDialog = (user) => {
  currentUser.value = user
  newRole.value = user.role
  roleDialogVisible.value = true
}

const updateRole = async () => {
  try {
    await request.put(`/users/${currentUser.value.id}/role`, { role: newRole.value })
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error('修改失败', error)
  }
}

const toggleStatus = async (user) => {
  const action = user.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}该用户吗？`, '提示')
    await request.put(`/users/${user.id}/status`, { status: user.status === 1 ? 0 : 1 })
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败', error)
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
