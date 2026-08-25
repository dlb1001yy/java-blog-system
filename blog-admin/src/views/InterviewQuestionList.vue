<template>
  <PageContainer title="面试题管理" description="面试题库维护">
    <template #action>
      <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增面试题</el-button>
      <el-button :icon="Upload" @click="triggerImport">导入面试题</el-button>
      <el-button :icon="Download" @click="downloadTemplate">下载模板</el-button>
    </template>
    <input
      ref="fileInputRef"
      type="file"
      accept=".md,.markdown"
      style="display: none"
      @change="handleFileChange"
    />

    <!-- 筛选栏 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="题干/标签关键词" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="searchForm.categoryId" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" placeholder="请选择" clearable style="width: 120px">
            <el-option v-for="d in difficultyOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" :border="false" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="方向" width="100" />
        <el-table-column prop="difficulty" label="难度" width="90">
          <template #default="{ row }">
            <el-tag :type="difficultyTypeMap[row.difficulty] || 'info'" size="small">{{ row.difficulty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              v-for="tag in splitTags(row.tags)"
              :key="tag"
              size="small"
              effect="plain"
              class="tag-item"
            >{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'" size="small">
              {{ row.status ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="题干" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="2" placeholder="请输入题目标题/题干" />
        </el-form-item>
        <el-form-item label="技术方向" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择技术方向" style="width: 240px">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 240px">
            <el-option v-for="d in difficultyOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tagIds">
          <el-select
            v-model="form.tagIds"
            multiple
            filterable
            default-first-option
            placeholder="选择标签，如：Java/并发/JVM"
            style="width: 100%"
          >
            <el-option v-for="t in tagOptions" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="解题思路" prop="tips">
          <div class="editor-wrapper">
            <div class="editor-toolbar">
              <el-button-group>
                <el-button size="small" @click="insertText('tips', '# ', '标题')">H1</el-button>
                <el-button size="small" @click="insertText('tips', '## ', '标题')">H2</el-button>
                <el-button size="small" @click="insertText('tips', '### ', '标题')">H3</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('tips', '**', '**', '粗体')">B</el-button>
                <el-button size="small" @click="insertText('tips', '*', '*', '斜体')">I</el-button>
                <el-button size="small" @click="insertText('tips', '`', '`', '代码')">&lt;/&gt;</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('tips', '- ', '', '列表项')">列表</el-button>
                <el-button size="small" @click="insertText('tips', '> ', '', '引用')">引用</el-button>
                <el-button size="small" @click="insertText('tips', '\n```\n', '\n```\n', '代码块')">代码块</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('tips', '[', '](url)', '链接')">链接</el-button>
                <el-button size="small" @click="insertText('tips', '![', '](url)', '图片')">图片</el-button>
              </el-button-group>
              <el-button size="small" style="margin-left: 8px;" @click="tipsPreview = !tipsPreview">
                {{ tipsPreview ? '编辑' : '预览' }}
              </el-button>
            </div>
            <div class="editor-content editor-content-sm">
              <el-input
                v-show="!tipsPreview"
                v-model="form.tips"
                type="textarea"
                :rows="4"
                placeholder="请输入解题思路/分析要点（可选，支持 Markdown）"
                class="md-editor-tips"
                :input-style="{ fontFamily: 'monospace', fontSize: '13px' }"
              />
              <div v-show="tipsPreview" class="markdown-body preview preview-sm" v-html="renderedTips"></div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="参考答案" prop="answer">
          <div class="editor-wrapper">
            <div class="editor-toolbar">
              <el-button-group>
                <el-button size="small" @click="insertText('answer', '# ', '标题')">H1</el-button>
                <el-button size="small" @click="insertText('answer', '## ', '标题')">H2</el-button>
                <el-button size="small" @click="insertText('answer', '### ', '标题')">H3</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('answer', '**', '**', '粗体')">B</el-button>
                <el-button size="small" @click="insertText('answer', '*', '*', '斜体')">I</el-button>
                <el-button size="small" @click="insertText('answer', '`', '`', '代码')">&lt;/&gt;</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('answer', '- ', '', '列表项')">列表</el-button>
                <el-button size="small" @click="insertText('answer', '> ', '', '引用')">引用</el-button>
                <el-button size="small" @click="insertText('answer', '\n```\n', '\n```\n', '代码块')">代码块</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('answer', '[', '](url)', '链接')">链接</el-button>
                <el-button size="small" @click="insertText('answer', '![', '](url)', '图片')">图片</el-button>
              </el-button-group>
              <el-button size="small" style="margin-left: 8px;" @click="answerPreview = !answerPreview">
                {{ answerPreview ? '编辑' : '预览' }}
              </el-button>
            </div>
            <div class="editor-content">
              <el-input
                v-show="!answerPreview"
                v-model="form.answer"
                type="textarea"
                :rows="10"
                placeholder="支持 Markdown 语法，代码块请使用 ``` 包裹"
                class="md-editor-answer"
                :input-style="{ fontFamily: 'monospace', fontSize: '13px' }"
              />
              <div v-show="answerPreview" class="markdown-body preview" v-html="renderedAnswer"></div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入预览对话框 -->
    <el-dialog v-model="importPreviewVisible" title="导入预览" width="800px" top="6vh">
      <div class="import-preview-body">
        <div class="import-summary">
          共解析出 <b>{{ importQuestionsList.length }}</b> 道题<template v-if="importErrors.length">，<span class="import-error-text">{{ importErrors.length }} 道题校验失败将被跳过</span></template>
        </div>
        <div v-for="(err, i) in importErrors" :key="'err-' + i" class="import-error-item">{{ err }}</div>
        <div v-for="(q, idx) in importQuestionsList" :key="idx" class="import-question-card">
          <div class="q-title">{{ q.title }}</div>
          <div class="q-meta">
            <span>技术方向：{{ q.category }}</span>
            <span>难度：{{ q.difficulty }}</span>
            <span v-if="q.tags">标签：{{ q.tags }}</span>
          </div>
          <div class="q-section-label">解题思路</div>
          <div v-if="q.tips" class="markdown-body q-md" v-html="md.render(q.tips)"></div>
          <div v-else class="q-empty">（空）</div>
          <div class="q-section-label">参考答案</div>
          <div v-if="q.answer" class="markdown-body q-md" v-html="md.render(q.answer)"></div>
          <div v-else class="q-empty">（空）</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="importPreviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">
          确认导入 {{ importQuestionsList.length }} 道题
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css'
import interviewQuestionApi from '@/api/interviewQuestion'
import categoryApi from '@/api/category'
import tagApi from '@/api/tag'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryOptions = ref([])
const tagOptions = ref([])
const difficultyOptions = ['简单', '中等', '困难']
const difficultyTypeMap = { 简单: 'success', 中等: 'warning', 困难: 'danger' }

const searchForm = reactive({ keyword: '', categoryId: null, difficulty: '', status: null })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  category: '',
  difficulty: '',
  tags: [],
  tips: '',
  answer: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择技术方向', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const splitTags = (tags) => (tags ? tags.split(',').map(t => t.trim()).filter(Boolean) : [])

const tipsPreview = ref(false)
const answerPreview = ref(false)

const md = new MarkdownIt({
  html: true,
  linkify: true
})

const renderedTips = computed(() => md.render(form.tips || ''))
const renderedAnswer = computed(() => md.render(form.answer || ''))

const insertText = (field, before, after = '', placeholder = '') => {
  const textarea = document.querySelector(
    field === 'tips' ? '.md-editor-tips textarea' : '.md-editor-answer textarea'
  )
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selected = form[field].substring(start, end) || placeholder

  form[field] =
    form[field].substring(0, start) +
    before + selected + after +
    form[field].substring(end)

  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selected.length)
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId ?? undefined,
      difficulty: searchForm.difficulty || undefined,
      status: searchForm.status ?? undefined
    }
    const res = await interviewQuestionApi.getPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, { keyword: '', categoryId: null, difficulty: '', status: null })
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = () => fetchData()

const handleAdd = () => {
  dialogTitle.value = '新增面试题'
  Object.assign(form, { id: null, title: '', categoryId: null, difficulty: '', tagIds: [], tips: '', answer: '', status: 1 })
  tipsPreview.value = false
  answerPreview.value = false
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑面试题'
  const res = await interviewQuestionApi.getDetail(row.id)
  Object.assign(form, {
    id: res.data.id,
    title: res.data.title,
    categoryId: res.data.categoryId ?? null,
    difficulty: res.data.difficulty,
    tagIds: res.data.tagIds || [],
    tips: res.data.tips || '',
    answer: res.data.answer || '',
    status: res.data.status ?? 1
  })
  tipsPreview.value = false
  answerPreview.value = false
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data = { ...form }
      await interviewQuestionApi.save(data)
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchData()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该面试题吗？', '提示', { type: 'warning' })
    .then(async () => {
      await interviewQuestionApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {})
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 道面试题吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await interviewQuestionApi.batchDelete(selectedRows.value.map(row => row.id))
      ElMessage.success('批量删除成功')
      fetchData()
    }).catch(() => {})
}

// ============ Markdown 批量导入 ============
const fileInputRef = ref()
const importing = ref(false)
const importPreviewVisible = ref(false)
const importQuestionsList = ref([])
const importErrors = ref([])

// 字段标记行：如 **题干**： / **解题思路**: （兼容中英文冒号与尾随空格）
const FIELD_LINE = /^\*\*(题干|技术方向|难度|标签|解题思路|参考答案)\*\*\s*[:：]\s*(.*)$/

// 中文标记 -> 英文属性键
const FIELD_KEY = {
  题干: 'title',
  技术方向: 'category',
  难度: 'difficulty',
  标签: 'tags',
  解题思路: 'tips',
  参考答案: 'answer'
}

const parseQuestionsMd = (text) => {
  const lines = text.replace(/\r\n/g, '\n').split('\n')
  const questions = []
  const errors = []
  let current = null

  const finishBlock = () => {
    if (!current) return
    const no = questions.length + 1
    const missing = []
    if (!current.title.trim()) missing.push('题干')
    if (!current.category) missing.push('技术方向')
    if (!current.difficulty) missing.push('难度')
    if (missing.length) errors.push(`第${no}题：缺少${missing.join('/')}`)
    else questions.push({
      title: current.title.trim(),
      category: current.category,
      difficulty: current.difficulty,
      tags: current.tags,
      tips: current.tips.trim(),
      answer: current.answer.trim(),
      status: 1
    })
  }

  for (const line of lines) {
    const m = line.match(FIELD_LINE)
    if (m && m[1] === '题干') {
      finishBlock()
      current = { title: m[2].replace(/\s+$/, ''), category: '', difficulty: '', tags: '', tips: '', answer: '', _field: FIELD_KEY[m[1]] }
      continue
    }
    if (!current) continue
    if (m) {
      current._field = FIELD_KEY[m[1]]
      const value = m[2].replace(/\s+$/, '')
      if (m[1] === '技术方向') current.category = value.trim()
      else if (m[1] === '难度') current.difficulty = value.trim()
      else if (m[1] === '标签') {
        current.tags = value.replace(/`/g, '').split(/\s+/).map(t => t.trim()).filter(Boolean).join(',')
      }
      // 题干/解题思路/参考答案为多行文本，标记行本身无内容
      continue
    }
    current[current._field] += (current[current._field] ? '\n' : '') + line
  }
  finishBlock()
  return { questions, errors }
}

const triggerImport = () => fileInputRef.value.click()

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  e.target.value = ''
  if (!file) return
  const text = await file.text()
  const { questions, errors } = parseQuestionsMd(text)
  if (!questions.length) {
    ElMessage.warning('未解析到有效面试题，请检查文件格式是否符合模板要求')
    return
  }
  importQuestionsList.value = questions
  importErrors.value = errors
  importPreviewVisible.value = true
}

const confirmImport = async () => {
  importing.value = true
  try {
    const payload = importQuestionsList.value.map(q => ({
      title: q.title,
      categoryName: q.category,
      difficulty: q.difficulty,
      tagNameList: splitTags(q.tags),
      tips: q.tips,
      answer: q.answer,
      status: q.status
    }))
    await interviewQuestionApi.importQuestions(payload)
    ElMessage.success(`成功导入 ${importQuestionsList.value.length} 道面试题`)
    importPreviewVisible.value = false
    fetchData()
  } finally {
    importing.value = false
  }
}

const downloadTemplate = () => {
  const template = [
    '# 面试题导入模板',
    '',
    '每个字段以 `**字段名**：` 开头，题目之间用 `**题干**：` 分隔。',
    '',
    '**题干**：',
    '请描述面试题的题干内容，支持多行。',
    '',
    '**技术方向**：后端  ',
    '**难度**：中等',
    '**标签**：`Java` `并发`',
    '',
    '**解题思路**：',
    '1. 先说明 HashMap 的底层数据结构；',
    '2. 分析链表转红黑树的阈值；',
    '3. 结合源码给出结论。',
    '',
    '- 数组 + 链表 + 红黑树',
    '- 链表长度超过 8 且数组长度 >= 64 时树化',
    '',
    '**参考答案**：',
    'HashMap 在 JDK 1.8 中采用数组 + 链表 + 红黑树结构：',
    '',
    '```java',
    'int threshold = 8; // 链表树化阈值',
    '```',
    '',
    '> 注意：本题答案保留 Markdown 原文格式。'
  ].join('\n')
  const blob = new Blob([template], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '面试题导入模板.md'
  a.click()
  URL.revokeObjectURL(url)
}

const fetchOptions = async () => {
  try {
    const [catRes, tagRes] = await Promise.all([categoryApi.getAll(), tagApi.getAll()])
    categoryOptions.value = catRes.data || []
    tagOptions.value = tagRes.data || []
  } catch (e) {
    // 选项加载失败不影响主流程
  }
}

onMounted(() => {
  fetchData()
  fetchOptions()
})
</script>

<style scoped>
.search-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  margin-bottom: var(--space-5);
}
.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-5);
}
.tag-item {
  margin-right: 4px;
}
.editor-wrapper {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}
.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: var(--space-2);
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-subtle);
}
.editor-content :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  resize: vertical;
}
.preview {
  padding: var(--space-4);
  min-height: 240px;
  max-height: 420px;
  overflow: auto;
  font-size: 14px;
}
.editor-content-sm .preview,
.preview-sm {
  min-height: 120px;
  max-height: 260px;
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
.import-preview-body {
  max-height: 60vh;
  overflow: auto;
}
.import-summary {
  font-size: 14px;
  color: var(--text-regular);
  margin-bottom: var(--space-3);
}
.import-error-text,
.import-error-item {
  color: var(--el-color-danger);
}
.import-error-item {
  font-size: 13px;
  margin-bottom: 4px;
}
.import-question-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}
.q-title {
  font-weight: 600;
  white-space: pre-wrap;
  margin-bottom: var(--space-2);
}
.q-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--text-regular);
  margin-bottom: var(--space-3);
}
.q-section-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-regular);
  margin: var(--space-3) 0 var(--space-1);
}
.q-md {
  font-size: 14px;
}
.q-md :deep(pre) {
  white-space: pre-wrap;
}
.q-empty {
  font-size: 13px;
  color: var(--el-text-info, #909399);
}
</style>
