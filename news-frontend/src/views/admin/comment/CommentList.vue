<template>
  <div class="comment-list">
    <el-card>
      <template #header>
        <span>评论管理</span>
      </template>

      <el-table :data="commentList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" min-width="200">
          <template #default="{ row }">
            <div class="comment-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="评论者" width="100" />
        <el-table-column prop="newsTitle" label="所属新闻" width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/news/${row.newsId}`)">
              {{ row.newsTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchComments"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { deleteComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const commentList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await request.get('/comments/admin', {
      params: { page: page.value, size: pageSize.value }
    })
    commentList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取评论失败', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '警告', { type: 'warning' })
    await deleteComment(id)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-content {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
