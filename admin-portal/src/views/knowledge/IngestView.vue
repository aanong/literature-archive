<template>
  <div class="ingest-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>书籍入库工具</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择书籍" prop="bookId">
              <el-select
                v-model="form.bookId"
                filterable
                remote
                placeholder="请输入关键词搜索书籍"
                :remote-method="searchBooks"
                :loading="loadingBooks"
                @change="handleBookSelect"
                style="width: 100%"
              >
                <el-option
                  v-for="item in bookOptions"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书籍标题" prop="bookTitle">
              <el-input v-model="form.bookTitle" placeholder="若未选择书籍，请手动输入标题" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如 history, philosophy" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="form.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入标签"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in tagOptions"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">拆分策略配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="拆分策略" prop="splitStrategy">
              <el-select v-model="form.splitStrategy" style="width: 100%">
                <el-option label="按段落拆分" value="PARAGRAPH" />
                <el-option label="固定长度拆分" value="FIXED_SIZE" />
                <el-option label="按章节标记拆分" value="CHAPTER_MARKER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.splitStrategy === 'FIXED_SIZE'">
            <el-form-item label="块大小" prop="chunkSize">
              <el-input-number v-model="form.chunkSize" :min="100" :max="2000" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.splitStrategy === 'FIXED_SIZE'">
            <el-form-item label="重叠大小" prop="chunkOverlap">
              <el-input-number v-model="form.chunkOverlap" :min="0" :max="500" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="自动向量化" prop="autoVectorize">
          <el-switch v-model="form.autoVectorize" />
          <span class="tip-text">开启后将自动调用 Embedding 模型并将结果存入 Milvus</span>
        </el-form-item>

        <el-form-item label="书籍内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="在此粘贴书籍文本内容..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="onSubmit" :loading="loading">立即入库</el-button>
          <el-button type="success" @click="onPreview" :loading="loadingPreview">预览拆分</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览结果对话框 -->
    <el-dialog v-model="previewVisible" title="拆分结果预览" width="80%">
      <div v-if="previewResult">
        <el-descriptions title="统计信息" :column="4" border>
          <el-descriptions-item label="总块数">{{ previewResult.totalChunks }}</el-descriptions-item>
          <el-descriptions-item label="书籍ID">{{ previewResult.bookId }}</el-descriptions-item>
        </el-descriptions>
        
        <el-table :data="previewResult.previewChunks" style="width: 100%; margin-top: 20px" height="500">
          <el-table-column type="index" width="50" />
          <el-table-column prop="content" label="文本内容" show-overflow-tooltip />
          <el-table-column prop="tokens" label="预估Tokens" width="120" />
          <el-table-column label="元数据">
            <template #default="scope">
              <pre class="metadata-code">{{ JSON.stringify(scope.row.metadata, null, 2) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { listBooks, type Book } from '@/api/book'
import { ingestBook, previewIngest, getTags, type BookIngestionResult } from '@/api/knowledge'

const formRef = ref<FormInstance>()
const loading = ref(false)
const loadingPreview = ref(false)
const previewVisible = ref(false)
const previewResult = ref<BookIngestionResult | null>(null)

// 书籍搜索相关
const loadingBooks = ref(false)
const bookOptions = ref<Book[]>([])

// 标签选项
const tagOptions = ref<string[]>([])

const form = reactive({
  bookId: undefined as string | undefined,
  bookTitle: '',
  category: '',
  tags: [] as string[],
  content: '',
  splitStrategy: 'PARAGRAPH' as 'PARAGRAPH' | 'FIXED_SIZE' | 'CHAPTER_MARKER',
  chunkSize: 500,
  chunkOverlap: 50,
  autoVectorize: false
})

const rules = reactive<FormRules>({
  bookTitle: [{ required: true, message: '请输入书籍标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入书籍内容', trigger: 'blur' }],
  splitStrategy: [{ required: true, message: '请选择拆分策略', trigger: 'change' }]
})

const searchBooks = async (query: string) => {
  if (query) {
    loadingBooks.value = true
    try {
      const res = await listBooks({ keyword: query, page: 1, pageSize: 20 })
      bookOptions.value = res.items
    } catch (error) {
      console.error(error)
    } finally {
      loadingBooks.value = false
    }
  } else {
    bookOptions.value = []
  }
}

const handleBookSelect = (val: string) => {
  const book = bookOptions.value.find(item => item.id === val)
  if (book) {
    form.bookTitle = book.title
  }
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const res = await ingestBook(form)
        ElMessage.success(`入库成功！共生成 ${res.totalChunks} 个知识块，成功 ${res.successCount} 个`)
        // 清空内容? form.content = ''
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const onPreview = async () => {
  if (!form.content) {
    ElMessage.warning('请先输入内容')
    return
  }
  loadingPreview.value = true
  try {
    const res = await previewIngest(form)
    previewResult.value = res
    previewVisible.value = true
  } catch (error) {
    console.error(error)
  } finally {
    loadingPreview.value = false
  }
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}

onMounted(async () => {
  try {
    tagOptions.value = await getTags()
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
})
</script>

<style scoped>
.ingest-container {
  max-width: 1200px;
  margin: 0 auto;
}

.tip-text {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}

.metadata-code {
  font-size: 12px;
  background-color: #f4f4f5;
  padding: 5px;
  border-radius: 4px;
}
</style>
