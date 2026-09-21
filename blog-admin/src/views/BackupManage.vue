<template>
  <PageContainer title="数据备份" description="数据库备份与还原">
    <template #action>
      <el-button type="primary" :loading="backing" :disabled="restoring" @click="handleCreate">立即备份</el-button>
    </template>

    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 160px">
            <el-option label="手动" value="manual" />
            <el-option label="自动" value="auto" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" :border="false" stripe>
        <el-table-column prop="fileName" label="文件名" min-width="240" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'manual' ? 'primary' : 'info'" size="small">
              {{ row.type === 'manual' ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.status === 'failed' && row.errorMsg"
              :content="row.errorMsg"
              placement="top"
            >
              <el-tag type="danger" size="small">失败</el-tag>
            </el-tooltip>
            <el-tag v-else-if="row.status === 'success'" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="row.status === 'failed'" type="danger" size="small">失败</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="backupTime" label="备份时间" width="180" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" :disabled="restoring" @click="handleRestore(row)">还原</el-button>
            <el-button link type="danger" :disabled="restoring" @click="handleDelete(row)">删除</el-button>
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
          @current-change="fetchData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import backupApi from '@/api/backup'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const backing = ref(false)
// 还原进行中：禁用还原/删除/立即备份按钮，防止并发还原交错执行损坏数据
const restoring = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ type: null })

// 文件大小格式化 B/KB/MB
const formatSize = (size) => {
  if (size === null || size === undefined) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await backupApi.pageBackups({
      current: currentPage.value,
      size: pageSize.value,
      ...searchForm
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; fetchData() }

const handleReset = () => {
  searchForm.type = null
  currentPage.value = 1
  fetchData()
}

const handleSizeChange = () => { currentPage.value = 1; fetchData() }

// 立即备份（同步接口，按钮 loading 直到返回）
const handleCreate = async () => {
  backing.value = true
  try {
    await backupApi.createBackup()
    ElMessage.success('备份完成')
    fetchData()
  } finally {
    backing.value = false
  }
}

// 还原（危险操作，红色确认按钮）
const handleRestore = (row) => {
  ElMessageBox.confirm('还原将用该备份【覆盖当前全部数据】，且不可恢复，确定继续？', '危险操作', {
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
    confirmButtonText: '确定还原',
    cancelButtonText: '取消'
  })
    .then(async () => {
      restoring.value = true
      try {
        await backupApi.restoreBackup(row.id)
        ElMessage.success('还原成功')
        fetchData()
      } finally {
        restoring.value = false
      }
    }).catch(() => {})
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除备份「${row.fileName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await backupApi.deleteBackup(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {})
}

onMounted(() => fetchData())
</script>

<style scoped>
.search-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}
:deep(.el-form--inline .el-form-item) { margin-bottom: 0; }

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

:deep(.el-table) { border-radius: var(--radius-md); }
:deep(.el-table th.el-table__cell) { background: var(--bg-subtle); color: var(--text-regular); font-weight: 600; }
:deep(.el-table tr) { transition: background var(--transition-base); }
:deep(.el-table .el-table__cell) { border-bottom: 1px solid var(--border-color); }
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) { background: var(--bg-subtle); }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: var(--space-5); }
</style>
