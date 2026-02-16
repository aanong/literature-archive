<template>
  <div class="book-form-container">
    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑书目' : '新增书目' }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="book-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="书名" prop="title">
          <el-input v-model="formData.title" placeholder="请输入书名" maxlength="100" />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <el-input v-model="formData.author" placeholder="请输入作者" maxlength="50" />
        </el-form-item>

        <el-form-item label="版本" prop="edition">
          <el-input v-model="formData.edition" placeholder="如：初版、校订本" maxlength="50" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 200px">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建书目' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getBook, createBook, updateBook, type Book } from '@/api/book'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(false)

const bookId = computed(() => {
  const id = route.params.id
  return id ? String(id) : null
})

const isEdit = computed(() => bookId.value !== null)

const formData = reactive<Partial<Book>>({
  title: '',
  author: '',
  edition: '',
  status: 'draft'
})

const formRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '书名长度在 1-100 个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '作者长度在 1-50 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

async function fetchBook() {
  if (!bookId.value) return
  
  loading.value = true
  try {
    const book = await getBook(bookId.value)
    formData.title = book.title
    formData.author = book.author
    formData.edition = book.edition || ''
    formData.status = book.status || 'draft'
  } catch (error) {
    ElMessage.error('获取书目信息失败')
    router.push('/books')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value && bookId.value) {
        await updateBook(bookId.value, formData)
        ElMessage.success('修改成功')
      } else {
        await createBook(formData)
        ElMessage.success('创建成功')
      }
      router.push('/books')
    } catch (error) {
      ElMessage.error(isEdit.value ? '修改失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

function handleCancel() {
  router.push('/books')
}

onMounted(() => {
  if (isEdit.value) {
    fetchBook()
  }
})
</script>

<style scoped>
.book-form-container {
  max-width: 600px;
}

.form-card {
  background-color: #fcf9f2;
  border: 1px solid #e6e0d5;
}

.card-header {
  font-family: "KaiTi", serif;
  font-size: 18px;
  font-weight: bold;
  color: #2c2521;
}

.book-form {
  padding: 20px 20px 0 0;
}

:deep(.el-form-item__label) {
  font-family: "Noto Serif SC", serif;
  color: #5d5d5d;
}

:deep(.el-input__wrapper) {
  font-family: "Noto Serif SC", serif;
}

:deep(.el-button--primary) {
  background-color: #cfb078;
  border-color: #cfb078;
}

:deep(.el-button--primary:hover) {
  background-color: #e0c28b;
  border-color: #e0c28b;
}
</style>
