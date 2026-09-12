<template>
  <PageContainer title="定时任务" description="后台定时任务调度管理">
    <template #action>
      <el-button type="primary" @click="handleAdd">新增任务</el-button>
    </template>

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
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 新增/编辑任务弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="任务类型">
          <el-select
            v-model="form.spiKey"
            :disabled="isEdit"
            placeholder="请选择任务类型"
            style="width: 100%"
            @change="handleSpiChange"
          >
            <el-option
              v-for="spi in spiList"
              :key="spi.taskKey"
              :label="`${spi.taskName}(${spi.taskKey})`"
              :value="spi.taskKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="任务标识" prop="taskKey">
          <el-input v-model="form.taskKey" placeholder="如 databaseBackup-daily（类型-后缀，同类型可建多个）" />
        </el-form-item>
        <el-form-item label="任务名" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述（可选）" />
        </el-form-item>
        <el-form-item label="执行频率" required>
          <div class="cron-editor">
            <Cron
              :key="cronKey"
              v-model="form.cron"
              format="spring"
              locale="zh"
              :periods="cronPeriods"
              @error="handleCronError"
            />
            <el-input :model-value="form.cron" readonly class="cron-echo" placeholder="请选择执行频率" />
            <div class="cron-tip">格式：秒 分 时 日 月 周（Spring 6 段）</div>
          </div>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="暂停" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!cronValid" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CronElementPlus as Cron } from '@vue-js-cron/element-plus'
import '@vue-js-cron/element-plus/dist/element-plus.css'
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

// 任务 SPI 选项（懒加载一次）
const spiList = ref([])
const spiLoaded = ref(false)
const fetchSpis = async () => {
  const res = await taskApi.spiOptions()
  spiList.value = res.data || []
  spiLoaded.value = true
}

// 新增/编辑任务弹窗（isEdit 区分）
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const cronKey = ref(0)
const cronError = ref('')
const form = reactive({ id: null, spiKey: '', taskKey: '', taskName: '', description: '', cron: '', status: 1 })

// cron 组件 periods：限定 6 段（秒/分/时/日/月/周，不含年），对齐后端 Spring CronExpression
const cronPeriods = [
  { id: 'q-second', value: [] },
  { id: 'q-minute', value: ['second'] },
  { id: 'q-hour', value: ['minute', 'second'] },
  { id: 'day', value: ['hour', 'minute', 'second'] },
  { id: 'week', value: ['dayOfWeek', 'hour', 'minute', 'second'] },
  { id: 'month', value: ['day', 'dayOfWeek', 'hour', 'minute', 'second'] }
]

const rules = {
  taskName: [{ required: true, message: '请输入任务名', trigger: 'blur' }],
  taskKey: [
    { required: true, message: '请输入任务标识', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9-]*$/,
      message: '仅支持字母开头的字母/数字/中划线',
      trigger: 'blur'
    }
  ]
}

// 表达式有值且组件未报错才允许保存
const cronValid = computed(() => !!form.cron && !cronError.value)

const handleCronError = (err) => {
  cronError.value = err
}

const openDialog = (edit, row) => {
  isEdit.value = edit
  if (edit) {
    form.id = row.id
    form.taskKey = row.taskKey || ''
    form.taskName = row.taskName
    form.description = row.description || ''
    form.cron = row.cronExpression || ''
    form.status = row.status ?? 1
  } else {
    Object.assign(form, { id: null, spiKey: '', taskKey: '', taskName: '', description: '', cron: '', status: 1 })
  }
  cronError.value = ''
  cronKey.value++ // 重挂载 cron 组件以同步初始表达式
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
  if (!spiLoaded.value) fetchSpis().catch(() => {})
}

const handleAdd = () => openDialog(false)

const handleEdit = (row) => openDialog(true, row)

// 选中任务类型后自动填充默认信息（任务标识默认取类型 key，可追加后缀建多实例）
const handleSpiChange = (spiKey) => {
  const spi = spiList.value.find(s => s.taskKey === spiKey)
  if (!spi) return
  form.taskKey = spi.taskKey || ''
  form.taskName = spi.taskName || ''
  form.description = spi.description || ''
  form.cron = spi.defaultCron || ''
  cronError.value = ''
  cronKey.value++
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!isEdit.value && !form.spiKey) {
    ElMessage.warning('请选择任务类型')
    return
  }
  if (!cronValid.value) {
    ElMessage.warning('请设置合法的 cron 表达式')
    return
  }
  saving.value = true
  try {
    const payload = {
      taskName: form.taskName.trim(),
      description: (form.description || '').trim(),
      cronExpression: form.cron.trim()
    }
    if (isEdit.value) {
      await taskApi.updateTask(form.id, payload)
    } else {
      await taskApi.createTask({ taskKey: form.taskKey.trim(), status: form.status, ...payload })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
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

.cron-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  line-height: normal;
}

.cron-echo {
  font-family: monospace;
}

.cron-tip {
  font-size: 12px;
  color: var(--text-secondary, #909399);
}
</style>
