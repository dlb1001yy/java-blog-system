<template>
  <PageContainer title="题库管理" description="考试题库维护：题目增删改查与题型统计">
    <template #action>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增题目</el-button>
    </template>

    <!-- 题型统计卡 -->
    <div class="stats-row">
      <div v-for="item in statCards" :key="item.type" class="stat-card">
        <div class="stat-count">{{ stats[item.type] || 0 }}</div>
        <div class="stat-label">{{ item.label }}</div>
        <el-tag :type="item.judge ? 'success' : 'warning'" size="small" effect="plain">
          {{ item.judge ? '自动判分' : '人工批改' }}
        </el-tag>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="题干/分类关键词" clearable />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="(item, key) in typeMap" :key="key" :label="item.label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" placeholder="请选择" clearable style="width: 120px">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
            <el-option label="待审核" :value="2" />
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
        <el-table-column prop="stem" label="题干" min-width="240" show-overflow-tooltip />
        <el-table-column prop="type" label="题型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type]?.type || 'info'" size="small">{{ typeMap[row.type]?.label || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="difficultyType(row.difficulty)" size="small" effect="plain">{{ row.difficulty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="usageCount" label="使用次数" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">{{ statusMap[row.status]?.label || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="题干" prop="stem">
          <el-input v-model="form.stem" type="textarea" :rows="3" placeholder="请输入题干" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="题型" prop="type">
              <el-select v-model="form.type" @change="handleTypeChange">
                <el-option v-for="(item, key) in typeMap" :key="key" :label="item.label" :value="Number(key)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如：Java 基础" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty">
                <el-option label="简单" value="简单" />
                <el-option label="中等" value="中等" />
                <el-option label="困难" value="困难" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
            <el-option label="待审核" :value="2" />
          </el-select>
        </el-form-item>

        <!-- 单选/多选：动态选项 + 正确答案 -->
        <template v-if="form.type === 1 || form.type === 2">
          <el-form-item label="选项">
            <div class="options-wrap">
              <div v-for="(opt, index) in form.optionList" :key="index" class="option-item">
                <span class="option-letter">{{ letters[index] }}</span>
                <el-input v-model="form.optionList[index]" :placeholder="`选项 ${letters[index]} 内容`" />
                <el-button
                  v-if="form.type === 2 || form.optionList.length > 2"
                  :icon="Delete"
                  circle
                  size="small"
                  @click="removeOption(index)"
                />
              </div>
              <el-button
                v-if="form.optionList.length < 10"
                :icon="Plus"
                size="small"
                @click="form.optionList.push('')"
              >添加选项</el-button>
            </div>
          </el-form-item>
          <el-form-item v-if="form.type === 1" label="正确答案" required>
            <el-radio-group v-model="form.correctSingle">
              <el-radio v-for="(opt, index) in form.optionList" :key="index" :value="index">
                {{ letters[index] }}{{ opt ? `. ${truncate(opt, 12)}` : '' }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-else label="正确答案" required>
            <el-checkbox-group v-model="form.correctMulti">
              <el-checkbox v-for="(opt, index) in form.optionList" :key="index" :value="index">
                {{ letters[index] }}{{ opt ? `. ${truncate(opt, 12)}` : '' }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </template>

        <!-- 判断题 -->
        <el-form-item v-if="form.type === 3" label="正确答案" required>
          <el-radio-group v-model="form.correctBool">
            <el-radio :value="true">对</el-radio>
            <el-radio :value="false">错</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 填空题：多个空 -->
        <el-form-item v-if="form.type === 4" label="填空答案" required>
          <div class="options-wrap">
            <div v-for="(blank, index) in form.blankList" :key="index" class="option-item">
              <span class="option-letter">空{{ index + 1 }}</span>
              <el-input v-model="form.blankList[index]" :placeholder="`第 ${index + 1} 空答案`" />
              <el-button
                v-if="form.blankList.length > 1"
                :icon="Delete"
                circle
                size="small"
                @click="form.blankList.splice(index, 1)"
              />
            </div>
            <el-button :icon="Plus" size="small" @click="form.blankList.push('')">添加空</el-button>
          </div>
        </el-form-item>

        <!-- 简答/编程：参考答案 -->
        <el-form-item v-if="form.type === 5 || form.type === 6" label="参考答案">
          <el-input v-model="form.referenceAnswer" type="textarea" :rows="4" placeholder="参考答案/解析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import examQuestionApi from '@/api/examQuestion'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref({})

const letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']

const typeMap = {
  1: { label: '单选', type: 'primary' },
  2: { label: '多选', type: 'success' },
  3: { label: '判断', type: 'warning' },
  4: { label: '填空', type: 'info' },
  5: { label: '简答', type: 'danger' },
  6: { label: '编程', type: 'danger' }
}

// 客观题 1-4 自动判分，5-6 人工批改
const statCards = [
  { type: 1, label: '单选题', judge: true },
  { type: 2, label: '多选题', judge: true },
  { type: 3, label: '判断题', judge: true },
  { type: 4, label: '填空题', judge: true },
  { type: 5, label: '简答题', judge: false },
  { type: 6, label: '编程题', judge: false }
]

const statusMap = {
  0: { label: '停用', type: 'info' },
  1: { label: '启用', type: 'success' },
  2: { label: '待审核', type: 'warning' }
}

const searchForm = reactive({
  keyword: '',
  type: null,
  difficulty: '',
  status: null
})

const dialogVisible = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  id: null,
  stem: '',
  type: 1,
  category: '',
  difficulty: '简单',
  score: 5,
  status: 1,
  referenceAnswer: '',
  // 编辑态辅助字段
  optionList: ['', '', '', ''],
  correctSingle: null,
  correctMulti: [],
  correctBool: true,
  blankList: ['']
})

const form = reactive(defaultForm())

const rules = {
  stem: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const truncate = (text, len) => (text.length > len ? text.slice(0, len) + '…' : text)

const difficultyType = (d) => (d === '简单' ? 'success' : d === '中等' ? 'warning' : 'danger')

const parseJsonSafe = (str, fallback) => {
  if (!str) return fallback
  try {
    const v = JSON.parse(str)
    return v ?? fallback
  } catch {
    return fallback
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await examQuestionApi.getPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  const res = await examQuestionApi.getStats()
  stats.value = res.data || {}
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.type = null
  searchForm.difficulty = ''
  searchForm.status = null
  handleSearch()
}

const handleCreate = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  const res = await examQuestionApi.getDetail(row.id)
  const q = res.data
  Object.assign(form, defaultForm(), {
    id: q.id,
    stem: q.stem,
    type: q.type,
    category: q.category,
    difficulty: q.difficulty,
    score: Number(q.score),
    status: q.status,
    referenceAnswer: q.referenceAnswer || ''
  })
  if (q.type === 1 || q.type === 2) {
    form.optionList = parseJsonSafe(q.options, []).length ? parseJsonSafe(q.options, []) : ['', '', '', '']
    if (q.type === 1) {
      form.correctSingle = parseJsonSafe(q.correct, 0)
    } else {
      form.correctMulti = parseJsonSafe(q.correct, [])
    }
  } else if (q.type === 3) {
    form.correctBool = parseJsonSafe(q.correct, true)
  } else if (q.type === 4) {
    form.blankList = parseJsonSafe(q.correct, []).length ? parseJsonSafe(q.correct, []) : ['']
  }
  dialogVisible.value = true
}

const handleTypeChange = () => {
  form.correctSingle = null
  form.correctMulti = []
  form.correctBool = true
  form.blankList = ['']
  if ((form.type === 1 || form.type === 2) && form.optionList.length < 2) {
    form.optionList = ['', '', '', '']
  }
}

const removeOption = (index) => {
  form.optionList.splice(index, 1)
  if (form.correctSingle === index) form.correctSingle = null
  else if (form.correctSingle > index) form.correctSingle--
  form.correctMulti = form.correctMulti
    .filter(i => i !== index)
    .map(i => (i > index ? i - 1 : i))
}

const buildPayload = () => {
  const payload = {
    id: form.id,
    stem: form.stem,
    type: form.type,
    category: form.category,
    difficulty: form.difficulty,
    score: form.score,
    status: form.status,
    referenceAnswer: form.referenceAnswer || null,
    options: null,
    correct: null
  }
  if (form.type === 1) {
    payload.options = JSON.stringify(form.optionList)
    payload.correct = JSON.stringify(form.correctSingle)
  } else if (form.type === 2) {
    payload.options = JSON.stringify(form.optionList)
    payload.correct = JSON.stringify([...form.correctMulti].sort((a, b) => a - b))
  } else if (form.type === 3) {
    payload.correct = JSON.stringify(form.correctBool)
  } else if (form.type === 4) {
    payload.correct = JSON.stringify(form.blankList)
  } else {
    payload.referenceAnswer = form.referenceAnswer
  }
  return payload
}

const validateAnswer = () => {
  if (form.type === 1 && form.correctSingle === null) {
    ElMessage.warning('请设置单选题正确答案')
    return false
  }
  if (form.type === 2 && !form.correctMulti.length) {
    ElMessage.warning('请设置多选题正确答案')
    return false
  }
  if (form.type === 4 && form.blankList.some(b => !b || !b.trim())) {
    ElMessage.warning('请填写所有空的答案')
    return false
  }
  return true
}

const handleSave = async () => {
  await formRef.value.validate()
  if (!validateAnswer()) return
  saving.value = true
  try {
    await examQuestionApi.save(buildPayload())
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
    fetchStats()
  } finally {
    saving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该题目吗？删除后不可恢复', '提示', { type: 'warning' })
    .then(async () => {
      await examQuestionApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
      fetchStats()
    }).catch(() => {})
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-4);
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
}

.stat-count {
  font-size: var(--font-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

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

.options-wrap {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  align-items: flex-start;
}

.option-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
}

.option-letter {
  min-width: 36px;
  font-weight: 600;
  color: var(--text-secondary);
}
</style>
