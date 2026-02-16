<template>
  <div class="book-list-container">
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索书名或作者..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button type="success" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增书目
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="bookList"
        stripe
        class="book-table"
      >
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" min-width="120" />
        <el-table-column prop="edition" label="版本" min-width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listBooks, deleteBook, type Book } from '@/api/book'

const router = useRouter()

const loading = ref(false)
const bookList = ref<Book[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'info' }> = {
  draft: { label: '草稿', type: 'info' },
  published: { label: '已发布', type: 'success' },
  archived: { label: '已归档', type: 'warning' }
}

function getStatusLabel(status: string) {
  return statusMap[status]?.label || status
}

function getStatusType(status: string) {
  return statusMap[status]?.type || 'info'
}

async function fetchBooks() {
  loading.value = true
  try {
    const res = await listBooks({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    bookList.value = res.items
    total.value = res.total
  } catch (error) {
    console.error('Failed to fetch books:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchBooks()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchBooks()
}

function handlePageChange() {
  fetchBooks()
}

function handleCreate() {
  router.push('/books/new')
}

function handleEdit(row: Book) {
  router.push(`/books/${row.id}/edit`)
}

async function handleDelete(row: Book) {
  try {
    await ElMessageBox.confirm(
      `确定要删除《${row.title}》吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteBook(row.id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
    }
  }
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.book-list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  background-color: #fcf9f2;
  border: 1px solid #e6e0d5;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.table-card {
  background-color: #fcf9f2;
  border: 1px solid #e6e0d5;
}

.book-table {
  font-family: "Noto Serif SC", serif;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-button--primary) {
  background-color: #cfb078;
  border-color: #cfb078;
}

:deep(.el-button--primary:hover) {
  background-color: #e0c28b;
  border-color: #e0c28b;
}

:deep(.el-button--success) {
  background-color: #4a6c6f;
  border-color: #4a6c6f;
}

:deep(.el-button--success:hover) {
  background-color: #5d8285;
  border-color: #5d8285;
}
</style>
