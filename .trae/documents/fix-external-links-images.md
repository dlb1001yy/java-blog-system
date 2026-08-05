# 修复三端博客详情页外链/图片无法查看

## Summary
三个前端项目（blog-frontend、blog-admin、blog-app）在查看博客详情时，正文中的外链图片无法显示、外链点击体验差。根因有二：
1. **图片防盗链**：图床（如 gitee raw、码云图床等）基于 `Referer` 头做防盗链，浏览器请求图片时携带了本站 Referer → 返回 403/占位图，图片加载失败。
2. **链接未新窗口打开**：markdown 渲染出的 `<a href="外链">` 没有 `target="_blank"`，点击后在当前 SPA 内跳转 → 离开博客站点或触发路由 404。

修复策略：全局 `<meta name="referrer" content="no-referrer">` 让所有外链图片请求不带 Referer（绕过防盗链）+ 在 markdown 渲染层为外链加 `target="_blank" rel="noopener noreferrer"`、为图片加 `referrerpolicy="no-referrer" loading="lazy"`。

## Current State Analysis（基于 Phase 1 探查）

### blog-frontend（Vue3 + markdown-it，用户截图中带 `data-v-a5ebea5b` 的项目）
- [src/views/ArticleDetail.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/ArticleDetail.vue#L115-L127)：第 115 行自建 `new MarkdownIt({...})`，第 139 行 `md.render(content)` → 第 22 行 `v-html="renderedContent"`。markdown-it 默认 `<a>` 无 target、`<img>` 无 referrerpolicy。
- [src/utils/markdown.js](file:///d:/my-project/java-blog-system/blog-frontend/src/utils/markdown.js)：导出共享 md 实例，当前无人 import（ArticleDetail 自建实例），但应一并改造以保持一致。
- [index.html](file:///d:/my-project/java-blog-system/blog-frontend/index.html)：无 referrer meta。
- [src/views/Resume.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/Resume.vue#L28)：`v-html="resume.summary"`（纯 HTML，非 markdown），全局 meta 同样覆盖。

### blog-admin（Vue3 + markdown-it，后台预览）
- [src/components/Editor.vue](file:///d:/my-project/java-blog-system/blog-admin/src/components/Editor.vue#L106-L118)：第 106 行 `new MarkdownIt`，第 121 行 `md.render` → 第 75 行 `v-html` 预览。
- [src/views/ArticleEdit.vue](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L203-L210)：第 203 行 `new MarkdownIt`，第 209 行 `md.render` → 第 146 行 `v-html` 预览。
- [index.html](file:///d:/my-project/java-blog-system/blog-admin/index.html)：无 referrer meta。

### blog-app（uni-app + 自研解析器，H5 跑在 5173）
- [pages/article/detail.vue](file:///d:/my-project/java-blog-system/blog-app/pages/article/detail.vue#L108)：`import { parseMarkdown } from '@/utils/markdown.js'`，第 123 行 `parseMarkdown(content)` → 第 26 行 `<rich-text :nodes="htmlContent">`。
- [utils/markdown.js](file:///d:/my-project/java-blog-system/blog-app/utils/markdown.js)：自研解析器（上一轮已替换 marked）。当前输出 `<a href="url">` 无 target、`<img src="url">` 无 referrerpolicy。
- [index.html](file:///d:/my-project/java-blog-system/blog-app/index.html)：无 referrer meta。
- [manifest.json](file:///d:/my-project/java-blog-system/blog-app/manifest.json)：vueVersion 3，无 H5 独立 head 配置；uni-app H5 直接用 index.html 的 head，故改 index.html 即可。

## Proposed Changes

### 改动 1：三端 index.html 加 referrer meta（核心修复，解决图片防盗链）
对三个文件，在 `<meta name="viewport"...>` 之后插入：
```html
<meta name="referrer" content="no-referrer" />
```
- 文件：[blog-frontend/index.html](file:///d:/my-project/java-blog-system/blog-frontend/index.html)
- 文件：[blog-admin/index.html](file:///d:/my-project/java-blog-system/blog-admin/index.html)
- 文件：[blog-app/index.html](file:///d:/my-project/java-blog-system/blog-app/index.html)

**Why**：让浏览器对所有外链图片/媒体请求**不发送 Referer 头**，从而绕过 gitee/码云等图床的 Referer 防盗链。这一个改动即可让外链图片恢复显示。`no-referrer` 对 SEO 影响可忽略（博客正文图片为主），且更保护用户隐私。

### 改动 2：blog-frontend 渲染层为外链加 target、为图片加 referrerpolicy（链接体验 + 双保险）
在 [src/views/ArticleDetail.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/ArticleDetail.vue) 的 `const md = new MarkdownIt({...})` 之后（第 127 行后）追加 renderer 覆盖：
```js
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
```
同样追加到 [src/utils/markdown.js](file:///d:/my-project/java-blog-system/blog-frontend/src/utils/markdown.js)（在 `const md = new MarkdownIt({...})` 之后），保持共享实例一致。

### 改动 3：blog-admin 渲染层覆盖（预览页同样生效）
在以下两处 `new MarkdownIt({...})` 之后追加与改动 2 完全相同的 renderer 覆盖代码块：
- [src/components/Editor.vue](file:///d:/my-project/java-blog-system/blog-admin/src/components/Editor.vue) 第 118 行后
- [src/views/ArticleEdit.vue](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue) 第 206 行后

### 改动 4：blog-app 自研解析器输出加属性
在 [utils/markdown.js](file:///d:/my-project/java-blog-system/blog-app/utils/markdown.js) 的 `parseInline` 中，修改两处输出：
- 图片：`<img src="${url}" alt="${alt}" referrerpolicy="no-referrer" loading="lazy" />`
- 链接：`<a href="${url}" target="_blank" rel="noopener noreferrer">${label}</a>`

**Why**：rich-text 渲染的节点属性由解析器输出决定；uni-app H5 下 `<a target="_blank">` 可正常新窗口打开，`<img referrerpolicy>` 与全局 meta 共同保证图片加载。小程序端 `loading`/`referrerpolicy` 会被忽略但不报错。

## Assumptions & Decisions
- **假设**：用户截图中的 gitee 图片失败主因是 Referer 防盗链（而非 CORS/混合内容）。gitee raw 图片本身无 CORS 限制，`no-referrer` 即可加载；若个别图床仍强制 Referer 白名单，则需后端代理，本次不涉及。
- **决定**：不改后端、不加图片代理接口，仅在前端通过 referrer 策略 + 链接属性修复，零后端改动、风险最低。
- **决定**：不引入新依赖。markdown-it renderer 覆盖为内置 API，blog-app 沿用自研解析器。
- **决定**：锚点链接（`#heading-x`，ArticleDetail 目录用）不加 `target="_blank"`，仅对 `http(s)://` 外链加，避免破坏站内锚点滚动。
- **不处理**：`Resume.vue` 的 `v-html="resume.summary"` 为纯 HTML 输入，全局 meta 已覆盖其图片；不单独改其渲染逻辑。
- **不处理**：小程序端 rich-text `<a>` 无法触发导航（平台限制），本次面向 H5（5173）场景。

## Verification
1. blog-frontend：`npm run dev`（端口见 vite.config），打开含 gitee 外链图片的文章详情 → 图片正常显示；点击正文外链 → 新标签打开。
2. blog-admin：`npm run dev`，编辑/预览含外链图片的文章 → 预览区图片显示；外链新窗口打开。
3. blog-app：HBuilderX 跑 H5（5173），进文章详情 → 外链图片显示；点外链新窗口打开。
4. 检查浏览器 Network：外链图片请求 Headers 中 `Referer` 应为空或不存在 → 确认 meta 生效。
5. 回归：站内锚点目录点击仍在本页滚动（未被加 target）。
