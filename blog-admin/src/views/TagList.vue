<template>
  <PageContainer title="标签管理" description="管理文章标签">
    <template #action>
      <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增标签</el-button>
    </template>
    <div class="table-card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item>
            <el-input
              v-model="searchForm.name"
              placeholder="请输入标签名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" :border="false" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import tagApi from '@/api/tag'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = reactive({ id: null, name: '' })
const rules = { name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }

const searchForm = reactive({ name: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      name: searchForm.name
    }
    const res = await tagApi.getPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增标签'
  form.id = null
  form.name = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑标签'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.id) {
      await tagApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await tagApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该标签吗？', '提示', { type: 'warning' })
    .then(async () => {
      await tagApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {})
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个标签吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await tagApi.batchDelete(selectedRows.value.map(row => row.id))
      ElMessage.success('批量删除成功')
      fetchData()
    }).catch(() => {})
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

onMounted(() => fetchData())
</script>

<style scoped>
.search-bar {
  margin-bottom: var(--space-5);
}

:deep(.el-form--inline .el-form-item) {
  margin-bottom: 0;
}

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}
:deep(.el-table) {
  border-radius: var(--radius-md);
}
:deep(.el-table th.el-table__cell) {
  background: var(--bg-subtle);
  color: var(--text-regular);
  font-weight: 600;
}
:deep(.el-table tr) {
  transition: background var(--transition-base);
}
:deep(.el-table__row:hover > td.el-table__cell) {
  background: var(--el-color-primary-light-9) !important;
}
:deep(.el-table .el-table__cell) {
  border-bottom: 1px solid var(--border-color);
}
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: var(--bg-subtle);
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-5);
}
:deep(.el-dialog) {
  border-radius: var(--radius-lg);
  overflow: hidden;
}
:deep(.el-dialog__header) {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-color);
  margin-right: 0;
}
:deep(.el-dialog__body) {
  padding: var(--space-5);
}
:deep(.el-dialog__footer) {
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-color);
}
</style>