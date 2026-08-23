<template>
  <PageContainer title="试卷管理" description="试卷维护：组卷、发布与停用">
    <template #action>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增试卷</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入试卷名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已停用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格卡 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" :border="false" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="试卷名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="duration" label="时长(分)" width="90" />
        <el-table-column prop="questionCount" label="题目数" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleCompose(row)">组卷</el-button>
            <el-button
              v-if="row.status !== 1"
              link
              type="success"
              @click="handlePublish(row, true)"
            >发布</el-button>
            <el-button v-else link type="warning" @click="handlePublish(row, false)">停用</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑试卷' : '新增试卷'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="试卷说明" />
        </el-form-item>
        <el-form-item label="时长(分)" prop="duration">
          <el-input-number v-model="form.duration" :min="10" :max="300" :step="10" />
        </el-form-item>
        <el-form-item label="及格线" prop="passScore">
          <el-input-number v-model="form.passScore" :min="0" :max="form.totalScore || 100" :step="5" />
          <span class="form-tip">总分：{{ form.totalScore || '—' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 组卷抽屉 -->
    <el-drawer v-model="composeVisible" :title="`组卷：${composePaper?.title || ''}`" size="65%" destroy-on-close>
      <div class="compose-wrap">
        <!-- 左：题目选择 -->
        <div class="compose-left">
          <div class="compose-filter">
            <el-select v-model="questionFilter.type" placeholder="题型" clearable size="small" style="width: 100px" @change="handleQuestionSearch">
              <el-option v-for="(item, key) in typeMap" :key="key" :label="item.label" :value="Number(key)" />
            </el-select>
            <el-select v-model="questionFilter.category" placeholder="分类" clearable size="small" style="width: 110px" @change="handleQuestionSearch">
              <el-option v-for="item in categories" :key="item.name" :label="item.name" :value="item.name" />
            </el-select>
            <el-button type="primary" size="small" @click="handleQuestionSearch">查询</el-button>
          </div>
          <div v-loading="questionLoading" class="question-list">
            <div v-for="q in questionList" :key="q.id" class="question-row">
              <el-checkbox
                :model-value="selectedIds.includes(q.id)"
                @change="toggleQuestion(q)"
              />
              <div class="question-info">
                <div class="question-stem" :title="q.stem">{{ truncate(q.stem, 30) }}</div>
                <div class="question-meta">
                  <el-tag :type="typeMap[q.type]?.type || 'info'" size="small">{{ typeMap[q.type]?.label || q.type }}</el-tag>
                  <span class="meta-text">{{ q.category }}</span>
                  <span class="meta-text">{{ q.score }} 分</span>
                </div>
              </div>
            </div>
            <el-empty v-if="!questionLoading && !questionList.length" description="暂无题目" :image-size="60" />
          </div>
          <el-pagination
            v-model:current-page="questionPage"
            :page-size="questionSize"
            :total="questionTotal"
            layout="total, prev, pager, next"
            small
            @current-change="fetchQuestions"
          />
        </div>

        <!-- 右：已选题目 -->
        <div class="compose-right">
          <div class="compose-right-header">
            <span>已选题目（{{ selectedQuestions.length }} 题）</span>
            <el-tag type="primary" effect="dark">总分：{{ composeTotalScore }}</el-tag>
          </div>
          <div class="selected-list">
            <div v-for="(q, index) in selectedQuestions" :key="q.id" class="selected-row">
              <span class="selected-index">{{ index + 1 }}</span>
              <div class="question-info">
                <div class="question-stem" :title="q.stem">{{ truncate(q.stem, 24) }}</div>
                <div class="question-meta">
                  <el-tag :type="typeMap[q.type]?.type || 'info'" size="small">{{ typeMap[q.type]?.label }}</el-tag>
                  <span class="meta-text">{{ q.score }} 分</span>
                </div>
              </div>
              <div class="selected-actions">
                <el-button link :disabled="index === 0" @click="moveQuestion(index, -1)">上移</el-button>
                <el-button link :disabled="index === selectedQuestions.length - 1" @click="moveQuestion(index, 1)">下移</el-button>
                <el-button link type="danger" @click="removeSelected(q.id)">移除</el-button>
              </div>
            </div>
            <el-empty v-if="!selectedQuestions.length" description="请从左侧选择题目" :image-size="60" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="composeVisible = false">取消</el-button>
        <el-button type="primary" :loading="composing" @click="handleComposeSave">保存组卷</el-button>
      </template>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import examPaperApi from '@/api/examPaper'
import examQuestionApi from '@/api/examQuestion'
import categoryApi from '@/api/category'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const categories = ref([])

const typeMap = {
  1: { label: '单选', type: 'primary' },
  2: { label: '多选', type: 'success' },
  3: { label: '判断', type: 'warning' },
  4: { label: '填空', type: 'info' },
  5: { label: '简答', type: 'danger' },
  6: { label: '编程', type: 'danger' }
}

const statusMap = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已停用', type: 'danger' }
}

const searchForm = reactive({
  keyword: '',
  status: null
})

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  description: '',
  duration: 60,
  passScore: 60,
  totalScore: 100
})

const rules = {
  title: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'change' }]
}

// ---- 组卷 ----
const composeVisible = ref(false)
const composing = ref(false)
const composePaper = ref(null)
const questionLoading = ref(false)
const questionList = ref([])
const questionPage = ref(1)
const questionSize = ref(10)
const questionTotal = ref(0)
const questionFilter = reactive({ type: null, category: '' })
const selectedQuestions = ref([])

const selectedIds = computed(() => selectedQuestions.value.map(q => q.id))
// 后端 compose 只接收 questionIds，使用题目默认分值汇总
const composeTotalScore = computed(() =>
  selectedQuestions.value.reduce((sum, q) => sum + Number(q.score || 0), 0)
)

const truncate = (text, len) => {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await examPaperApi.getPage(params)
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
  searchForm.keyword = ''
  searchForm.status = null
  handleSearch()
}

const handleCreate = () => {
  Object.assign(form, { id: null, title: '', description: '', duration: 60, passScore: 60, totalScore: 100 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    description: row.description || '',
    duration: row.duration,
    passScore: row.passScore != null ? Number(row.passScore) : 60,
    totalScore: Number(row.totalScore) || 100
  })
  if (form.passScore > form.totalScore) form.passScore = form.totalScore
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    // totalScore 由后端按组卷题目分值自动汇总，不随表单提交
    const { totalScore: _ts, ...payload } = form
    await examPaperApi.save(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

const fetchQuestions = async () => {
  questionLoading.value = true
  try {
    const params = {
      page: questionPage.value,
      size: questionSize.value,
      type: questionFilter.type,
      category: questionFilter.category || undefined
    }
    const res = await examQuestionApi.getPage(params)
    questionList.value = res.data.records
    questionTotal.value = res.data.total
  } finally {
    questionLoading.value = false
  }
}

const handleQuestionSearch = () => {
  questionPage.value = 1
  fetchQuestions()
}

const handleCompose = async (row) => {
  composePaper.value = row
  selectedQuestions.value = []
  questionPage.value = 1
  questionFilter.type = null
  questionFilter.category = ''
  composeVisible.value = true
  fetchQuestions()
  // 加载已有组卷（按顺序）
  const res = await examPaperApi.getDetail(row.id)
  const ids = res.data.questionIds || []
  if (ids.length) {
    // 逐题取详情以保持顺序（题目数量有限，可接受）
    const list = []
    for (const id of ids) {
      try {
        const q = await examQuestionApi.getDetail(id)
        list.push(q.data)
      } catch { /* 题目可能已删除 */ }
    }
    selectedQuestions.value = list
  }
}

const toggleQuestion = (q) => {
  const index = selectedQuestions.value.findIndex(item => item.id === q.id)
  if (index >= 0) {
    selectedQuestions.value.splice(index, 1)
  } else {
    selectedQuestions.value.push(q)
  }
}

const removeSelected = (id) => {
  selectedQuestions.value = selectedQuestions.value.filter(q => q.id !== id)
}

const moveQuestion = (index, offset) => {
  const target = index + offset
  const list = selectedQuestions.value
  ;[list[index], list[target]] = [list[target], list[index]]
}

const handleComposeSave = async () => {
  if (!selectedQuestions.value.length) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  composing.value = true
  try {
    await examPaperApi.compose(composePaper.value.id, selectedQuestions.value.map(q => q.id))
    ElMessage.success('组卷成功')
    composeVisible.value = false
    fetchData()
  } finally {
    composing.value = false
  }
}

const handlePublish = (row, enable) => {
  ElMessageBox.confirm(
    `确定要${enable ? '发布' : '停用'}试卷「${row.title}」吗？`,
    '提示',
    { type: 'warning' }
  ).then(async () => {
    await examPaperApi.publish(row.id, enable)
    ElMessage.success(enable ? '已发布' : '已停用')
    fetchData()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该试卷吗？删除后不可恢复', '提示', { type: 'warning' })
    .then(async () => {
      await examPaperApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {})
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

const fetchCategories = async () => {
  const res = await categoryApi.getAll()
  categories.value = res.data || []
}

onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<style scoped>
.search-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
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
  --el-table-border-color: var(--border-color);
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

.compose-wrap {
  display: flex;
  gap: var(--space-4);
  height: 100%;
}

.compose-left,
.compose-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  min-width: 0;
}

.compose-filter {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.question-list,
.selected-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-2);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.question-row,
.selected-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2);
  border-bottom: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.question-row:hover,
.selected-row:hover {
  background: var(--bg-subtle);
}

.question-info {
  flex: 1;
  min-width: 0;
}

.question-stem {
  font-size: var(--font-sm);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: 2px;
}

.meta-text {
  font-size: var(--font-xs, 12px);
  color: var(--text-secondary);
}

.selected-index {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
  color: var(--text-secondary);
}

.selected-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.compose-right-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: var(--text-primary);
}

.form-tip {
  margin-left: var(--space-3);
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
