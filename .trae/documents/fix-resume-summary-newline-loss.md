# 简历 Markdown 编辑器借鉴 blog-admin 实现计划（v3）

## Summary
用户要求 blog-frontend 的 Markdown 组件（`MarkdownEditor.vue`）借鉴 blog-admin 项目（`ArticleEdit.vue`）的 Markdown 编辑实现。同时保留后端修复：简介保存后换行丢失的根因是后端 `cleanText` 用 Jsoup 折叠了换行，需改为保留换行的清洗方式，否则任何编辑器保存后换行仍会失效。

## Current State Analysis
- blog-admin 参考实现 `blog-admin/src/views/ArticleEdit.vue`（L105-153、L216-264）：
  - 工具栏：H1/H2/H3、B/I/`</>`、列表/引用/代码块、链接/图片 分组的 el-button-group；末尾"编辑/预览"切换按钮（`previewMode`，v-show 整块切换，非分栏）。
  - 编辑区：`el-input type="textarea"` + `:input-style` monospace；预览区 `markdown-body`（github-markdown-css）+ `v-html`。
  - 插入逻辑 `insertText(before, after, placeholder)`：作用于光标选区，`nextTick` 后恢复焦点与选区。
  - md 实例：组件内 `new MarkdownIt({ html: true, linkify: true })` + 外链新窗/图片防盗链渲染规则。
- blog-frontend 现状 `blog-frontend/src/components/MarkdownEditor.vue`：原生 textarea、左右分栏预览、复用全局 `@/utils/markdown`（无 breaks）。
- blog-frontend 已有依赖：`markdown-it`、`github-markdown-css`（package.json 已含，无需新增依赖）。
- 后端：`ResumeInfoServiceImpl.mySave` L39-41 对 summary/selfEvaluation/interests 调 `JsoupXssUtil.cleanText`（换行被折叠，主因）。

## Proposed Changes

### 1. 重写 `blog-frontend/src/components/MarkdownEditor.vue`（借鉴 admin 模式）
保持组件对外接口不变（props: `modelValue` / `height` / `placeholder`；emit `update:modelValue`），内部改为：
- **工具栏**（照搬 admin 分组）：H1/H2/H3、B/I/`</>`、列表/引用/代码块、链接/图片 + 右侧"编辑/预览"切换按钮（替换现有 el-switch）。
- **编辑区**：`el-input type="textarea"`（`:autosize` 由 `height` 换算或 `:rows`，`input-style` monospace 14px），v-model 直接绑定（借助 computed get/set 转发 props/emit）。
- **预览**：`v-show="previewMode"` 整块切换（与 admin 一致，非分栏），class `markdown-body`（引入 `github-markdown-css`，组件内 import 一次）。
- **md 实例**：组件内局部 `new MarkdownIt({ html: true, linkify: true, breaks: true })`，并复制 admin 的 link_open（外链新窗）与 image（no-referrer + lazy）渲染规则；`breaks: true` 保证单换行预览生效。
- **插入逻辑**：仿 admin `insertText(before, after, placeholder)`，但 textarea 通过组件 ref 获取（不用 `document.querySelector`，简历页有多个编辑器实例会串）。工具栏按钮加 `@mousedown.prevent` 防失焦。
- Tab 插入两空格行为保留。
- 移除旧分栏样式，新增 `editor-wrapper`/`editor-toolbar`/`editor-content`/`preview` 样式（参考 admin，变量用 blog-frontend 现有 CSS 变量 `--border-color` 等）。

### 2. 后端换行保留（必要，否则换行仍丢）
- `blog-backend/.../utils/JsoupXssUtil.java` 新增 `cleanMarkdown(String)`：换行占位 → `Jsoup.clean(_, Safelist.none())` 剥离标签 → 还原换行（XSS 防护不降级）。
- `ResumeInfoServiceImpl.java` L39-41：summary/selfEvaluation/interests 改用 `cleanMarkdown`。

### 3. 不改动
- `ProfileResume.vue`（接口不变，无需改）；`ResumePreview.vue`（展示端已修复）。
- 旧已折叠数据需用户重新编辑保存一次。

## Assumptions & Decisions
- 编辑/预览采用 admin 的整块切换而非左右分栏（按用户"借鉴 admin"的要求）。
- 预览启用 `breaks: true`（admin 未开，但简历场景需要单换行生效；不改变 admin 项目本身）。

## Verification
1. GetDiagnostics：MarkdownEditor.vue 无错误。
2. 后端编译通过。
3. 手动：简介输入多行 `- Linux...` → 预览切换正常渲染列表 → 保存 → 重新进入页面换行保留、预览/分享页正常。
4. 工具栏各按钮在多编辑器实例页面上只作用于自身 textarea（ref 隔离）。
