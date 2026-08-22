# 简历分享页描述字段 Markdown 渲染修复计划

## Summary
用户反馈分享页 `/resume/share/:token` 的个人简介、自我评价、工作描述、项目描述、教育描述显示为纯文本。经排查，分享页与预览共用 `ResumePreview.vue`，该组件已通过 `renderMd` + `v-html` 渲染 Markdown，但共享的 `@/utils/markdown` 实例未开启 `breaks: true`，旧数据中单个换行符 `\n` 不会被转成 `<br>`，多段纯文本被挤成一个段落，视觉上"格式丢失像纯文本"；另外若线上未重新构建部署，也会展示旧 bundle。

## Current State Analysis
- `blog-frontend/src/views/Resume.vue`：分享页，直接渲染 `<ResumePreview :resume=... />`，无自身文本渲染。
- `blog-frontend/src/components/ResumePreview.vue`：五种描述字段（summary / selfEvaluation / w.description / p.description / e.description）四种风格下均为 `<div class="md-text" v-html="renderMd(...)">`，`renderMd` 使用 `import md from '@/utils/markdown'`。
- `blog-frontend/src/utils/markdown.js`：markdown-it 实例，`html: true, linkify: true, typographer: true`，**未设置 `breaks`（默认 false）**——单个换行不产生 `<br>`。该实例同时被文章详情页使用，不能直接全局改。

## Proposed Changes
1. **ResumePreview.vue（唯一代码改动）**：不再复用全局 md 实例，改为在组件内创建局部 markdown-it 实例并开启 `breaks: true`：
   - `import MarkdownIt from 'markdown-it'`
   - `const md = new MarkdownIt({ html: true, linkify: true, breaks: true })`
   - `renderMd` 保持不变。
   - 效果：单换行即换行，列表/加粗等 Markdown 语法正常渲染，且不影响文章页渲染行为。
2. **重新构建部署**：分享页若为线上环境，需重新 `npm run build` 并部署前端（代码在 dev 已正确，线上旧 bundle 也可能是原因之一）。

## Assumptions & Decisions
- 不修改全局 `utils/markdown.js`（文章渲染行为不应被改变）。
- 分享页无需单独改动（复用同一组件）。
- 教育描述为空时不渲染（现有 `v-if="e.description"` 保持）。

## Verification
1. GetDiagnostics 检查 ResumePreview.vue 无错误。
2. 本地 `npm run dev`，在"我的简历"页填入含单换行、列表、加粗的描述 → 保存 → 打开分享链接，确认描述按 Markdown 渲染、换行生效。
3. （若线上）重新构建部署后再次访问分享链接验证。
