<template>
  <PageContainer title="定时任务" description="后台定时任务调度管理">
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" :border="false" stripe>
        <el-table-column prop="taskName" label="任务名" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="cronExpression" label="cron 表达式" width="140">
          <template #default="{ row }">
            <span class="cron-text">{{ row.cronExpression }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextTriggerTime" label="下次触发时间" width="170">
          <template #default="{ row }">{{ row.nextTriggerTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="lastExecuteTime" label="最近执行时间" width="170">
          <template #default="{ row }">{{ row.lastExecuteTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="lastExecuteStatus" label="执行结果" width="100">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.lastExecuteStatus === 'failed' && row.lastExecuteError"
              :content="row.lastExecuteError"
              placement="top"
            >
              <el-tag type="danger" size="small">失败</el-tag>
            </el-tooltip>
            <el-tag v-else-if="row.lastExecuteStatus === 'success'" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="row.lastExecuteStatus === 'failed'" type="danger" size="small">失败</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditCron(row)">编辑cron</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="handleToggle(row, false)">暂停</el-button>
            <el-button v-else link type="success" @click="handleToggle(row, true)">启用</el-button>
            <el-button link type="primary" @click="handleRun(row)">立即执行</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog v-model="cronVisible" title="编辑 cron 表达式" width="480px">
      <el-form label-width="100px">
        <el-form-item label="任务名">
          <span>{{ cronForm.taskName }}</span>
        </el-form-item>
        <el-form-item label="cron 表达式">
          <el-input v-model="cronForm.cron" placeholder="如：0 0 4 * * ?" style="width: 240px; font-family: monospace" />
        </el-form-item>
      </el-form>
      <div class="cron-tip">
        格式：秒 分 时 日 月 周（6 段，可选第 7 段年），如「0 0 4 * * ?」表示每天 04:00:00 执行
      </div>
      <template #footer>
        <el-button @click="cronVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!cronValid" @click="handleSaveCron">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import taskApi from '@/api/task'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await taskApi.pageTasks({
      current: currentPage.value,
      size: pageSize.value
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// 编辑 cron
const cronVisible = ref(false)
const cronForm = reactive({ id: null, taskName: '', cron: '' })

// 前端正则校验：6-7 段（秒 分 时 日 月 周 [年]），每段仅允许常规 cron 字符
const cronValid = computed(() => {
  const parts = cronForm.cron.trim().split(/\s+/)
  return parts.length >= 6 && parts.length <= 7 &&
    parts.every(p => /^[\d*,/\-?LW#A-Za-z]+$/.test(p))
})

const handleEditCron = (row) => {
  cronForm.id = row.id
  cronForm.taskName = row.taskName
  cronForm.cron = row.cronExpression
  cronVisible.value = true
}

const handleSaveCron = async () => {
  saving.value = true
  try {
    await taskApi.updateTaskCron(cronForm.id, { cron: cronForm.cron.trim() })
    ElMessage.success('更新成功')
    cronVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

// 暂停/启用
const handleToggle = (row, enable) => {
  const action = enable ? '启用' : '暂停'
  ElMessageBox.confirm(`确定要${action}任务「${row.taskName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await (enable ? taskApi.resumeTask(row.id) : taskApi.pauseTask(row.id))
      ElMessage.success(`已${action}`)
      fetchData()
    }).catch(() => {})
}

// 立即执行
const handleRun = (row) => {
  ElMessageBox.confirm('将异步触发一次执行，结果稍后可在列表查看。确定继续吗？', '提示', { type: 'warning' })
    .then(async () => {
      await taskApi.runTask(row.id)
      ElMessage.success('已触发')
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

.cron-text {
  font-family: monospace;
  font-size: 13px;
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

.cron-tip {
  font-size: 12px;
  color: var(--text-secondary, #909399);
  padding-left: 100px;
}
</style>
