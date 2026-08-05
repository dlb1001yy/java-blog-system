<template>
  <div class="editor-wrapper">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <el-button-group>
        <el-button size="small" @click="insertText('# ', '')" title="标题1">H1</el-button>
        <el-button size="small" @click="insertText('## ', '')" title="标题2">H2</el-button>
        <el-button size="small" @click="insertText('### ', '')" title="标题3">H3</el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @click="insertText('**', '**', '粗体')" title="粗体">
          <el-icon><Bold /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('*', '*', '斜体')" title="斜体">
          <el-icon><Italic /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('~~', '~~', '删除线')" title="删除线">
          <el-icon><StrikeThrough /></el-icon>
        </el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @click="insertText('- ', '', '列表项')" title="无序列表">
          <el-icon><List /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('1. ', '', '列表项')" title="有序列表">
          <el-icon><ListNumbered /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('> ', '', '引用')" title="引用">
          <el-icon><Quote /></el-icon>
        </el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @click="insertText('`', '`', '代码')" title="行内代码">
          <el-icon><Code /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('\n```\n', '\n```\n', '代码块')" title="代码块">
          <el-icon><CodeBlock /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('| ', ' |', '表格')" title="表格">表格</el-button>
      </el-button-group>
      <el-button-group style="margin-left: 8px;">
        <el-button size="small" @click="insertText('[', '](url)', '链接')" title="链接">
          <el-icon><Link /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('![', '](url)', '图片')" title="图片">
          <el-icon><Picture /></el-icon>
        </el-button>
        <el-button size="small" @click="insertText('---\n', '')" title="分割线">---</el-button>
      </el-button-group>
      <el-button 
        size="small" 
        style="margin-left: 8px;" 
        :type="previewMode ? 'primary' : 'default'"
        @click="previewMode = !previewMode"
      >
        {{ previewMode ? '编辑' : '预览' }}
      </el-button>
    </div>
    
    <!-- 编辑区域 -->
    <div class="editor-content">
      <textarea
        v-show="!previewMode"
        ref="textareaRef"
        :value="modelValue"
        @input="handleInput"
        @scroll="handleScroll"
        class="editor-textarea"
        placeholder="请输入内容，支持 Markdown 语法"
      ></textarea>
      <div 
        v-show="previewMode"
        ref="previewRef"
        class="editor-preview markdown-body"
        v-html="renderedContent"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { Bold, Italic, List, Link, Picture, Code } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: String,
    default: '400px'
  }
})

const emit = defineEmits(['update:modelValue'])

const textareaRef = ref(null)
const previewRef = ref(null)
const previewMode = ref(false)

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`
      } catch (_) {}
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

const renderedContent = computed(() => {
  return md.render(props.modelValue || '')
})

const handleInput = (e) => {
  emit('update:modelValue', e.target.value)
}

const handleScroll = (e) => {
  if (previewRef.value) {
    const scrollTop = e.target.scrollTop
    const scrollHeight = e.target.scrollHeight
    const previewHeight = previewRef.value.scrollHeight
    previewRef.value.scrollTop = (scrollTop / scrollHeight) * previewHeight
  }
}

const insertText = (before, after = '', placeholder = '') => {
  const textarea = textareaRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selected = props.modelValue.substring(start, end) || placeholder
  
  const newText = 
    props.modelValue.substring(0, start) + 
    before + selected + after + 
    props.modelValue.substring(end)
  
  emit('update:modelValue', newText)
  
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selected.length)
  })
}

onMounted(() => {
  if (textareaRef.value) {
    textareaRef.value.style.height = props.height
  }
})
</script>

<style scoped>
.editor-wrapper {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px;
  border-bottom: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.editor-content {
  display: flex;
  position: relative;
}

.editor-textarea {
  flex: 1;
  padding: 16px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: 'Fira Code', Consolas, Monaco, monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  background: #fff;
}

.editor-preview {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #fff;
  min-height: 400px;
}

.editor-preview :deep(h1),
.editor-preview :deep(h2),
.editor-preview :deep(h3) {
  margin: 16px 0 8px;
  font-weight: 600;
}

.editor-preview :deep(p) {
  margin-bottom: 12px;
}

.editor-preview :deep(pre) {
  border-radius: 4px;
  margin: 12px 0;
  overflow-x: auto;
}

.editor-preview :deep(code) {
  font-family: 'Fira Code', Consolas, Monaco, monospace;
  font-size: 14px;
}

.editor-preview :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 8px 16px;
  margin: 12px 0;
  background: #f5f7fa;
  border-radius: 0 4px 4px 0;
}
</style>