<template>
  <PageContainer title="文章编辑" description="创作或修改文章内容">
    <template #action>
      <el-button @click="$router.back()">返回</el-button>
      <el-button :loading="importLoading" @click="triggerImport">
        <el-icon v-if="!importLoading"><Upload /></el-icon>
        导入 Markdown
      </el-button>
      <el-button type="info" @click="handleSave(0)">存草稿</el-button>
      <el-button type="primary" @click="handleSave(1)">发布</el-button>
    </template>

    <div class="content-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类型">
              <el-radio-group v-model="form.type">
                <el-radio :value="0">原创</el-radio>
                <el-radio :value="1">转载</el-radio>
                <el-radio :value="2">翻译</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置顶">
              <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签">
          <el-select
            v-model="form.tagIds"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面图">
          <el-upload
            class="cover-uploader"
            action="/api/admin/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            accept="image/*"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <div v-else class="cover-placeholder">
              <el-icon :size="28"><Plus /></el-icon>
              <span>上传封面</span>
            </div>
          </el-upload>
          <el-button text @click="form.coverImage = ''" v-if="form.coverImage">移除</el-button>
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 转载信息 -->
        <template v-if="form.type === 1">
          <el-form-item label="来源名称">
            <el-input v-model="form.sourceName" placeholder="请输入来源名称" />
          </el-form-item>
          <el-form-item label="来源链接">
            <el-input v-model="form.sourceUrl" placeholder="请输入原文链接" />
          </el-form-item>
        </template>

        <el-form-item label="正文" prop="content">
          <div class="editor-wrapper">
            <!-- Markdown编辑器 -->
            <div class="editor-toolbar">
              <el-button-group>
                <el-button size="small" @click="insertText('# ', '标题')">H1</el-button>
                <el-button size="small" @click="insertText('## ', '标题')">H2</el-button>
                <el-button size="small" @click="insertText('### ', '标题')">H3</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('**', '**', '粗体')">B</el-button>
                <el-button size="small" @click="insertText('*', '*', '斜体')">I</el-button>
                <el-button size="small" @click="insertText('`', '`', '代码')">&lt;/&gt;</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('- ', '', '列表项')">列表</el-button>
                <el-button size="small" @click="insertText('> ', '', '引用')">引用</el-button>
                <el-button size="small" @click="insertText('\n```\n', '\n```\n', '代码块')">代码块</el-button>
              </el-button-group>
              <el-button-group style="margin-left: 8px;">
                <el-button size="small" @click="insertText('[', '](url)', '链接')">链接</el-button>
                <el-button size="small" @click="insertText('![', '](url)', '图片')">图片</el-button>
              </el-button-group>
              <el-button
                size="small"
                style="margin-left: 8px;"
                @click="previewMode = !previewMode"
              >
                {{ previewMode ? '编辑' : '预览' }}
              </el-button>
            </div>

            <div class="editor-content">
              <el-input
                v-show="!previewMode"
                v-model="form.content"
                type="textarea"
                :rows="20"
                placeholder="请输入文章内容，支持Markdown语法"
                class="markdown-editor"
                :input-style="{ fontFamily: 'monospace', fontSize: '14px' }"
              />
              <div
                v-show="previewMode"
                class="markdown-body preview"
                v-html="renderedContent"
              ></div>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <input
      ref="fileInputRef"
      type="file"
      accept=".md,.markdown"
      style="display:none"
      @change="handleFileChange"
    />
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css'
import articleApi from '@/api/article'
import categoryApi from '@/api/category'
import tagApi from '@/api/tag'
import PageContainer from '@/components/PageContainer.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const categories = ref([])
const tags = ref([])
const previewMode = ref(false)
const importLoading = ref(false)
const fileInputRef = ref()

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  categoryId: null,
  type: 0,
  tagIds: [],
  coverImage: '',
  summary: '',
  content: '',
  sourceName: '',
  sourceUrl: '',
  isTop: 0,
  isPublish: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('admin_token')}`
}))

const md = new MarkdownIt({
  html: true,
  linkify: true
})

// 外链新窗口打开（锚点链接除外）
const defaultLinkOpen = md.renderer.rules.link_open || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const href = tokens[idx].attrGet('href') || ''
  if (/^https?:\/\//i.test(href)) {
    tokens[idx].attrSet('target', '_blank')
    tokens[idx].attrSet('rel', 'noopener noreferrer')
  }
  return defaultLinkOpen(tokens, idx, options, env, self)
}
// 图片：防盗链 + 懒加载（与 meta 双保险）
const defaultImage = md.renderer.rules.image || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.image = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('referrerpolicy', 'no-referrer')
  tokens[idx].attrSet('loading', 'lazy')
  return defaultImage(tokens, idx, options, env, self)
}

const renderedContent = computed(() => {
  return md.render(form.content || '')
})

const insertText = (before, after = '', placeholder = '') => {
  const textarea = document.querySelector('.markdown-editor textarea')
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selected = form.content.substring(start, end) || placeholder
  
  form.content = 
    form.content.substring(0, start) + 
    before + selected + after + 
    form.content.substring(end)
  
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selected.length)
  })
}

const triggerImport = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  importLoading.value = true
  try {
    const res = await articleApi.importMarkdown(file)
    const data = res.data || {}
    if (data.title) form.title = data.title
    if (data.summary) form.summary = data.summary
    if (data.content) form.content = data.content
    if (data.coverImage) form.coverImage = data.coverImage
    ElMessage.success('导入成功')
  } catch (e) {
    // request 拦截器已统一弹出错误提示
  } finally {
    importLoading.value = false
    event.target.value = ''
  }
}

const handleCoverSuccess = (response) => {
  if (response.code === 200) {
    form.coverImage = response.data
  } else {
    ElMessage.error('上传失败')
  }
}

const handleSave = async (isPublish) => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    form.isPublish = isPublish
    
    try {
      if (isEdit.value) {
        await articleApi.update(form)
      } else {
        await articleApi.create(form)
      }
      ElMessage.success(isPublish ? '发布成功' : '保存成功')
      router.push('/article')
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

const fetchArticle = async () => {
  if (!route.params.id) return
  
  const res = await articleApi.getDetail(route.params.id)
  Object.assign(form, res.data)
  if (res.data.tags) {
    form.tagIds = res.data.tags.map(t => t.id)
  }
}

const fetchCategories = async () => {
  const res = await categoryApi.getAll()
  categories.value = res.data
}

const fetchTags = async () => {
  const res = await tagApi.getAll()
  tags.value = res.data
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  fetchArticle()
})
</script>

<style scoped>
.content-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-base);
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(5, 150, 105, 0.1);
}
:deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-base);
}
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(5, 150, 105, 0.1);
}

:deep(.el-tabs__item) {
  font-size: var(--font-base);
  font-weight: 500;
}
:deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: var(--radius-full);
}
:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--color-primary-light);
}

.cover-uploader {
  display: inline-block;
}

.cover-image {
  width: 200px;
  height: 120px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.cover-placeholder {
  width: 200px;
  height: 120px;
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: border-color var(--transition-base), color var(--transition-base);
}

.cover-placeholder:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
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

.editor-content {
  min-height: 400px;
}

.editor-content :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  resize: vertical;
}

.preview {
  padding: var(--space-5);
  min-height: 400px;
}
</style>