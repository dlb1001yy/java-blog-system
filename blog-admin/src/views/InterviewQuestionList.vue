<template>
  <PageContainer title="面试题管理" description="面试题库维护">
    <template #action>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增面试题</el-button>
    </template>

    <!-- 筛选栏 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="题干/标签关键词" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="searchForm.category" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
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
      <el-table :data="tableData" v-loading="loading" :border="false" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="方向" width="100" />
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
        <el-form-item label="技术方向" prop="category">
          <el-select v-model="form.category" placeholder="请选择技术方向" style="width: 240px">
            <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 240px">
            <el-option v-for="d in difficultyOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用英文逗号分隔，如：Java,并发,JVM" />
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
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css'
import interviewQuestionApi from '@/api/interviewQuestion'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryOptions = ['后端', '前端', '数据库', 'DevOps', '算法']
const difficultyOptions = ['简单', '中等', '困难']
const difficultyTypeMap = { 简单: 'success', 中等: 'warning', 困难: 'danger' }

const searchForm = reactive({ keyword: '', category: '', difficulty: '', status: null })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  category: '',
  difficulty: '',
  tags: '',
  tips: '',
  answer: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  category: [{ required: true, message: '请选择技术方向', trigger: 'change' }],
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
      category: searchForm.category || undefined,
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
  Object.assign(searchForm, { keyword: '', category: '', difficulty: '', status: null })
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = () => fetchData()

const handleAdd = () => {
  dialogTitle.value = '新增面试题'
  Object.assign(form, { id: null, title: '', category: '', difficulty: '', tags: '', tips: '', answer: '', status: 1 })
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
    category: res.data.category,
    difficulty: res.data.difficulty,
    tags: res.data.tags || '',
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
      await interviewQuestionApi.save({ ...form })
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

onMounted(() => fetchData())
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
</style>
