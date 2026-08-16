<template>
  <PageContainer title="操作日志" description="后台操作审计记录">
    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="操作人" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="操作描述">
          <el-input v-model="searchForm.operation" placeholder="操作描述关键字" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 160px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作描述" min-width="140" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="100">
          <template #default="{ row }">
            <el-tag :type="methodTypeMap[row.method] || 'info'" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uri" label="请求路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时" width="100">
          <template #default="{ row }">{{ row.costMs + ' ms' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentDetail.operation }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentDetail.method }}</el-descriptions-item>
        <el-descriptions-item label="客户端IP">{{ currentDetail.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ currentDetail.costMs }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentDetail.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求路径" :span="2">{{ currentDetail.uri }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <div class="detail-section-title">请求参数</div>
        <pre class="detail-pre">{{ currentDetail?.params || '-' }}</pre>
      </div>

      <div class="detail-section" v-if="currentDetail && (currentDetail.status === 0 || currentDetail.errorMsg)">
        <div class="detail-section-title">错误信息</div>
        <pre class="detail-pre">{{ currentDetail.errorMsg || '-' }}</pre>
      </div>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import operationLogApi from '@/api/operationLog'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentDetail = ref(null)

const methodTypeMap = {
  POST: 'success',
  PUT: 'warning',
  DELETE: 'danger'
}

const searchForm = reactive({ username: '', operation: '', status: null })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await operationLogApi.getPage({
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
  searchForm.username = ''
  searchForm.operation = ''
  searchForm.status = null
  currentPage.value = 1
  fetchData()
}

const handleSizeChange = () => { currentPage.value = 1; fetchData() }

const handleDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
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
:deep(.el-table__row:hover > td.el-table__cell) { background: var(--el-color-primary-light-9) !important; }
:deep(.el-table .el-table__cell) { border-bottom: 1px solid var(--border-color); }
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) { background: var(--bg-subtle); }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: var(--space-5); }

:deep(.el-dialog) { border-radius: var(--radius-lg); overflow: hidden; }
:deep(.el-dialog__header) { padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-color); margin-right: 0; }
:deep(.el-dialog__body) { padding: var(--space-5); }
:deep(.el-dialog__footer) { padding: var(--space-4) var(--space-5); border-top: 1px solid var(--border-color); }

.detail-section { margin-top: var(--space-4); }
.detail-section-title { font-weight: 600; color: var(--text-regular); margin-bottom: var(--space-2); }

.detail-pre {
  margin: 0;
  padding: var(--space-3);
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}
</style>
