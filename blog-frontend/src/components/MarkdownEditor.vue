<template>
  <div class="editor-wrapper">
    <!-- 工具栏（借鉴 blog-admin ArticleEdit） -->
    <div class="editor-toolbar">
      <el-button-group>
        <el-button size="small" @mousedown.prevent @click="insertText('# ', '', '标题')">H1</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('## ', '', '标题')">H2</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('### ', '', '标题')">H3</el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @mousedown.prevent @click="insertText('**', '**', '粗体')">B</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('*', '*', '斜体')">I</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('`', '`', '代码')">&lt;/&gt;</el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @mousedown.prevent @click="insertText('- ', '', '列表项')">列表</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('> ', '', '引用')">引用</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('\n```\n', '\n```\n', '代码块')">代码块</el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @mousedown.prevent @click="insertText('[', '](url)', '链接')">链接</el-button>
        <el-button size="small" @mousedown.prevent @click="insertText('![', '](url)', '图片')">图片</el-button>
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
        ref="inputRef"
        v-model="text"
        type="textarea"
        :rows="rows"
        :placeholder="placeholder"
        class="markdown-editor"
        :input-style="{ fontFamily: 'Consolas, Monaco, monospace', fontSize: '14px' }"
        @keydown.tab="onTab"
      />
      <div
        v-show="previewMode"
        class="markdown-body preview"
        v-html="renderedContent"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { nextTick } from 'vue'
import MarkdownIt from 'markdown-it'
import 'github-markdown-css'

const props = defineProps({
  modelValue: { type: String, default: '' },
  height: { type: Number, default: 200 },
  placeholder: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const inputRef = ref(null)
const previewMode = ref(false)

// 行数按高度估算（约 28px/行），与 height prop 兼容
const rows = computed(() => Math.max(3, Math.round(props.height / 28)))

const text = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

// 简历描述多为单换行文本，开启 breaks 使单个换行即时生效
const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true
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
// 图片：防盗链 + 懒加载
const defaultImage = md.renderer.rules.image || function (tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}
md.renderer.rules.image = function (tokens, idx, options, env, self) {
  tokens[idx].attrSet('referrerpolicy', 'no-referrer')
  tokens[idx].attrSet('loading', 'lazy')
  return defaultImage(tokens, idx, options, env, self)
}

const renderedContent = computed(() => md.render(props.modelValue || ''))

// 作用于光标选区的插入（借鉴 admin，通过 ref 获取自身 textarea，多实例隔离）
function insertText(before, after = '', placeholder = '') {
  const textarea = inputRef.value?.textarea ?? inputRef.value?.$el?.querySelector('textarea')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = props.modelValue || ''
  const selected = value.substring(start, end) || placeholder

  emit('update:modelValue',
    value.substring(0, start) + before + selected + after + value.substring(end)
  )

  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selected.length)
  })
}

function onTab(e) {
  e.preventDefault()
  const textarea = inputRef.value?.textarea ?? inputRef.value?.$el?.querySelector('textarea')
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = props.modelValue || ''
  emit('update:modelValue', value.substring(0, start) + '  ' + value.substring(end))
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + 2, start + 2)
  })
}
</script>

<style scoped>
.editor-wrapper {
  width: 100%;
  border: 1px solid var(--border-color, #dcdfe6);
  border-radius: 6px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-bottom: 1px solid var(--border-color, #ebeef5);
  background: #f5f7fa;
}

.editor-content :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  box-shadow: none;
  resize: vertical;
}

.preview {
  padding: 16px 20px;
  min-height: 120px;
  background: #fff;
}
</style>
