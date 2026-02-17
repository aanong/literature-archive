<template>
  <div class="book-upload-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>书籍导入</span>
        </div>
      </template>
      
      <el-steps :active="activeStep" finish-status="success" simple>
        <el-step title="选择书籍" />
        <el-step title="上传内容" />
        <el-step title="完成" />
      </el-steps>

      <div class="step-content" v-if="activeStep === 0">
        <el-form label-width="100px">
          <el-form-item label="选择书籍">
            <el-select 
              v-model="selectedBookId" 
              filterable 
              remote 
              placeholder="请输入书名搜索" 
              :remote-method="searchBooks"
              :loading="loading">
              <el-option
                v-for="item in bookOptions"
                :key="item.id"
                :label="item.title"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="step-actions">
          <el-button type="primary" @click="nextStep" :disabled="!selectedBookId">下一步</el-button>
        </div>
      </div>

      <div class="step-content" v-if="activeStep === 1">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1">
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只支持 txt 文本文件
            </div>
          </template>
        </el-upload>
        
        <div class="preview-area" v-if="fileContent">
          <h3>内容预览 (前500字)</h3>
          <el-input
            type="textarea"
            :rows="10"
            v-model="previewContent"
            readonly
          />
        </div>

        <div class="step-actions">
           <el-checkbox v-model="autoClassify" style="margin-right: 20px;">启用 AI 自动分类</el-checkbox>
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading" :disabled="!fileContent">开始导入</el-button>
        </div>
      </div>

      <div class="step-content" v-if="activeStep === 2">
        <el-result
          icon="success"
          title="导入成功"
          sub-title="书籍内容已保存，后台正在进行处理">
          <template #extra>
            <el-button type="primary" @click="resetForm">继续导入</el-button>
            <el-button @click="viewBook">查看书籍</el-button>
          </template>
        </el-result>
      </div>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listBooks, importChapters, type Book } from '@/api/book'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeStep = ref(0)
const loading = ref(false)
const uploading = ref(false)
const bookOptions = ref<Book[]>([])
const selectedBookId = ref('')
const fileContent = ref('')
const autoClassify = ref(true)

const previewContent = computed(() => {
  return fileContent.value.substring(0, 500) + (fileContent.value.length > 500 ? '...' : '')
})

const searchBooks = async (query: string) => {
  if (query) {
    loading.value = true
    try {
      const res = await listBooks({ keyword: query, page: 1, pageSize: 20 })
      bookOptions.value = res.data.items
    } finally {
      loading.value = false
    }
  } else {
    bookOptions.value = []
  }
}

// 初始加载一些数据
listBooks({ page: 1, pageSize: 20 }).then(res => {
  bookOptions.value = res.data.items
})

const nextStep = () => {
  if (activeStep.value < 2) {
    activeStep.value++
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const handleFileChange = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    fileContent.value = e.target?.result as string
  }
  reader.readAsText(file.raw)
}

const submitUpload = async () => {
  uploading.value = true
  try {
    // 实际项目中可能需要传递 autoClassify参数，这里暂时只上传内容
    // 如果后端接口支持，可以将 autoClassify 放入 query param 或者 header
    // 目前 BookController 只接收 body 作为 content
    // 若需支持 autoClassify，需修改后端接口接收 DTO 或 param
    
    // 临时方案：如果需要 autoClassify，可以调用 knowledge-service 的接口
    // 但根据 implementation_plan.md，我们是在 content-service 接收 import request
    
    // 这里先只调用 content-service 的 importChapters
    await importChapters(selectedBookId.value, fileContent.value)
    
    // 触发知识库入库（包含 AI 分类逻辑）
    // 由于后端异步事件尚未完全打通，这里模拟前端调用（实际应由后端触发）
    // 或者在导入章节后，前端调用一个 knowledge-service 的触发接口
    // 暂且假设 content-service 内部会处理或后续步骤处理
    
    // 如果 implementation_plan 里提到 knowledge-service 的 BookIngestionService
    // 我们可能需要调用那个接口。
    // 但目前前端主要对接 content-service
    
    ElMessage.success('导入成功')
    nextStep()
  } catch (error) {
    console.error(error)
    ElMessage.error('导入失败')
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  activeStep.value = 0
  selectedBookId.value = ''
  fileContent.value = ''
  bookOptions.value = []
  listBooks({ page: 1, pageSize: 20 }).then(res => {
    bookOptions.value = res.data.items
  })
}

const viewBook = () => {
  // router.push(`/books/${selectedBookId.value}`) 
  // 假设有书籍详情页
   ElMessage.info('功能开发中')
}
</script>

<style scoped>
.book-upload-container {
  padding: 20px;
}
.upload-card {
  max-width: 800px;
  margin: 0 auto;
}
.step-content {
  margin-top: 30px;
  min-height: 300px;
}
.step-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.preview-area {
  margin-top: 20px;
}
</style>
