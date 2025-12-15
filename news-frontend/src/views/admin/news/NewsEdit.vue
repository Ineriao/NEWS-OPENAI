<template>
  <div class="news-edit">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑新闻' : '发布新闻' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 900px;"
      >
        <el-form-item label="新闻标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入新闻标题" />
        </el-form-item>

        <el-form-item label="新闻分类" prop="categoryId">
          <el-cascader
            v-model="form.categoryId"
            :options="categories"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
            placeholder="请选择分类"
            clearable
          />
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload
            class="cover-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <img v-if="form.coverImage" :src="getImageUrl(form.coverImage)" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">支持 JPG、PNG、GIF 格式，最大 10MB</div>
        </el-form-item>

        <el-form-item label="新闻摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入新闻摘要"
          />
        </el-form-item>

        <el-form-item label="新闻内容" prop="content">
          <div class="editor-container">
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
              class="editor-toolbar"
            />
            <Editor
              v-model="form.content"
              :defaultConfig="editorConfig"
              mode="default"
              class="editor-content"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave(0)">
            保存草稿
          </el-button>
          <el-button type="success" :loading="loading" @click="handleSave(1)">
            保存并提交审核
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryTree } from '@/api/category'
import { getNewsDetail, createNews, updateNews, submitForReview } from '@/api/news'
import { getEditorUploadUrl, getEditorUploadHeaders } from '@/api/file'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// Wangeditor 导入
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const categories = ref([])

// 编辑器实例
const editorRef = shallowRef()

const isEdit = computed(() => !!route.params.id)

// 上传配置
const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/files/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const form = reactive({
  title: '',
  categoryId: null,
  summary: '',
  content: '',
  coverImage: ''
})

const rules = {
  title: [{ required: true, message: '请输入新闻标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入新闻内容', trigger: 'blur' }]
}

// 工具栏配置
const toolbarConfig = {
  excludeKeys: [
    'group-video', // 排除视频上传
    'fullScreen'   // 排除全屏
  ]
}

// 编辑器配置 - 使用服务器上传
const editorConfig = {
  placeholder: '请输入新闻内容...',
  MENU_CONF: {
    // 图片上传配置 - 服务器上传
    uploadImage: {
      server: import.meta.env.VITE_API_BASE_URL + '/files/upload/editor',
      fieldName: 'file',
      maxFileSize: 10 * 1024 * 1024, // 10MB
      maxNumberOfFiles: 20,
      allowedFileTypes: ['image/*'],
      headers: () => {
        const token = localStorage.getItem('token')
        return token ? { Authorization: `Bearer ${token}` } : {}
      },
      // 自定义插入图片
      customInsert(res, insertFn) {
        if (res.errno === 0) {
          const url = res.data.url
          insertFn(url)
        } else {
          ElMessage.error(res.message || '图片上传失败')
        }
      },
      // 上传错误处理
      onError(file, err) {
        console.error('图片上传失败', err)
        ElMessage.error('图片上传失败')
      }
    }
  }
}

// 获取图片完整 URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return import.meta.env.VITE_API_BASE_URL.replace('/api', '') + path
}

// 封面上传成功
const handleCoverSuccess = (response) => {
  if (response.code === 200) {
    form.coverImage = response.data.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 封面上传前验证
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

// 编辑器创建完成回调
const handleCreated = (editor) => {
  editorRef.value = editor
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchNews = async () => {
  if (!isEdit.value) return

  try {
    const res = await getNewsDetail(route.params.id)
    const data = res.data
    form.title = data.title
    form.categoryId = data.categoryId
    form.summary = data.summary
    form.content = data.content
    form.coverImage = data.coverImage || ''
  } catch (error) {
    console.error('获取新闻失败', error)
  }
}

const handleSave = async (submitStatus) => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 额外验证内容是否为空
  if (!form.content || form.content === '<p><br></p>') {
    ElMessage.warning('请输入新闻内容')
    return
  }

  loading.value = true
  try {
    let newsId

    if (isEdit.value) {
      await updateNews(route.params.id, form)
      newsId = route.params.id
      ElMessage.success('更新成功')
    } else {
      const res = await createNews(form)
      newsId = res.data?.id
      ElMessage.success('保存成功')
    }

    // 提交审核
    if (submitStatus === 1 && newsId) {
      await submitForReview(newsId)
      ElMessage.success('已提交审核')
    }

    router.push('/admin/news')
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchNews()
})

// 组件销毁时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.news-edit {
  max-width: 1000px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
}

.editor-toolbar {
  border-bottom: 1px solid #dcdfe6;
}

.editor-content {
  height: 400px;
  overflow-y: auto;
}

/* 封面上传样式 */
.cover-uploader {
  display: block;
}

.cover-uploader :deep(.el-upload) {
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.cover-image {
  width: 200px;
  height: 120px;
  object-fit: cover;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
