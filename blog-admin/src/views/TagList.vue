<template>
  <PageContainer title="标签管理" description="管理文章标签">
    <template #action>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增标签</el-button>
    </template>
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" :border="false" stripe>
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
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = reactive({ id: null, name: '' })
const rules = { name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await tagApi.getAll()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
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

onMounted(() => fetchData())
</script>

<style scoped>
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