<template>
  <div class="md-editor">
    <div class="md-editor__toolbar">
      <el-button size="small" text @mousedown.prevent @click="wrapSelection('**', '**')">加粗</el-button>
      <el-button size="small" text @mousedown.prevent @click="wrapSelection('*', '*')">斜体</el-button>
      <el-button size="small" text @mousedown.prevent @click="insertLinePrefix('## ')">标题</el-button>
      <el-button size="small" text @mousedown.prevent @click="insertLinePrefix('- ')">无序列表</el-button>
      <el-button size="small" text @mousedown.prevent @click="insertLinePrefix('1. ')">有序列表</el-button>
      <el-button size="small" text @mousedown.prevent @click="insertLink">链接</el-button>
      <el-button size="small" text @mousedown.prevent @click="wrapSelection('\n```\n', '\n```\n')">代码块</el-button>
      <div class="md-editor__preview-switch">
        <span>预览</span>
        <el-switch v-model="showPreview" size="small" />
      </div>
    </div>
    <div class="md-editor__body" :class="{ 'is-split': showPreview }">
      <textarea
        ref="textareaRef"
        class="md-editor__textarea"
        :value="modelValue"
        :placeholder="placeholder"
        :style="{ height: height + 'px' }"
        @input="onInput"
        @keydown.tab="onTab"
      ></textarea>
      <div v-if="showPreview" class="md-editor__preview md-text" v-html="html"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import md from '@/utils/markdown'

const props = defineProps({
  modelValue: { type: String, default: '' },
  height: { type: Number, default: 200 },
  placeholder: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const textareaRef = ref(null)
const showPreview = ref(true)

const html = computed(() => md.render(props.modelValue || ''))

function onInput(e) {
  emit('update:modelValue', e.target.value)
}

function onTab(e) {
  e.preventDefault()
  insertText('  ')
}

function update(value, selStart, selEnd) {
  emit('update:modelValue', value)
  requestAnimationFrame(() => {
    const ta = textareaRef.value
    if (!ta) return
    ta.focus()
    ta.setSelectionRange(selStart, selEnd)
  })
}

function insertText(text, selectStartOffset = 0, selectEndOffset = 0) {
  const ta = textareaRef.value
  if (!ta) return
  const { selectionStart: start, selectionEnd: end, value } = ta
  const next = value.slice(0, start) + text + value.slice(end)
  update(next, start + selectStartOffset, start + selectEndOffset || start + text.length + selectEndOffset)
}

// 包裹选中文本
function wrapSelection(before, after) {
  const ta = textareaRef.value
  if (!ta) return
  const { selectionStart: start, selectionEnd: end, value } = ta
  const selected = value.slice(start, end)
  const next = value.slice(0, start) + before + selected + after + value.slice(end)
  update(next, start + before.length, start + before.length + selected.length)
}

// 行前缀（作用于选区涉及的所有行）
function insertLinePrefix(prefix) {
  const ta = textareaRef.value
  if (!ta) return
  const { selectionStart: start, selectionEnd: end, value } = ta
  const lineStart = value.lastIndexOf('\n', start - 1) + 1
  const lineEndIdx = value.indexOf('\n', end)
  const lineEnd = lineEndIdx === -1 ? value.length : lineEndIdx
  const lines = value.slice(lineStart, lineEnd).split('\n')
  const prefixed = lines.map(l => (l.startsWith(prefix) ? l : prefix + l)).join('\n')
  const next = value.slice(0, lineStart) + prefixed + value.slice(lineEnd)
  update(next, lineStart, lineStart + prefixed.length)
}

function insertLink() {
  const ta = textareaRef.value
  if (!ta) return
  const { selectionStart: start, selectionEnd: end, value } = ta
  const selected = value.slice(start, end) || '链接文字'
  const text = `[${selected}](https://)`
  const next = value.slice(0, start) + text + value.slice(end)
  update(next, start + text.length, start + text.length)
}
</script>

<style scoped>
.md-editor {
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-radius: 6px;
  overflow: hidden;
}

.md-editor__toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 8px;
  background: #f5f7fa;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.md-editor__preview-switch {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.md-editor__body {
  display: flex;
}

.md-editor__textarea {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  resize: vertical;
  padding: 10px 12px;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}

.md-editor__body.is-split .md-editor__textarea {
  width: 50%;
  flex: none;
}

.md-editor__preview {
  width: 50%;
  flex: none;
  overflow: auto;
  padding: 10px 14px;
  border-left: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.md-editor__preview.md-text :deep(p) {
  margin: 4px 0;
}

.md-editor__preview.md-text :deep(ul),
.md-editor__preview.md-text :deep(ol) {
  padding-left: 20px;
}
</style>
