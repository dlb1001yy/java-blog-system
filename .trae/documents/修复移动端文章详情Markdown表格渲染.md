# 修复移动端文章详情 Markdown 表格渲染

## Summary

移动端 blog-app（uni-app）文章详情页中，Markdown 表格以源码形式（`|` 竖线原文）显示，未渲染为表格格式。

根因：`blog-app/utils/markdown.js` 是自写的轻量 Markdown 解析器（为规避 HBuilderX 无 node_modules 的问题替换了 marked），**没有实现 GFM 表格语法解析分支**，表格行落入"普通段落"分支，被 `<p>` 原样输出。同时 `detail.vue` 的 `.markdown-body` 样式没有任何 `table/th/td` 规则。

后端（原样返回 Markdown 源文本）与 PC 端（markdown-it 默认支持表格）均正常，**只需修改移动端 blog-app 两个文件**。

## Current State Analysis

1. **解析器无表格分支**：`blog-app/utils/markdown.js` 主循环（68-157 行）依次匹配：空行、代码块、标题、分割线、引用、无序列表、有序列表、普通段落。`| a | b |` 与 `| --- | --- |` 都落入普通段落分支（142-156 行），输出 `<p>| a | b |<br>...</p>`。
2. **段落吞行风险**：普通段落的 while 条件（143-152 行）没有排除表格行，若"表格前一行是普通文本"，表格首行会被吞进前一段落。
3. **无表格样式**：`blog-app/pages/article/detail.vue` 样式区（484-566 行）无 `:deep(table)` 系列规则，即使解析出 `<table>` 也无边框无表头样式。
4. **共用方自动受益**：`parseMarkdown` 另被 3 个学习模块页面引用（`subpkg-study/pages/scores/index.vue`、`exam/taking.vue`、`interview/index.vue`），解析器修复后这些页面的表格解析自动生效。
5. **渲染层无障碍**：uni-app `rich-text` 组件支持 table 节点。

## Proposed Changes

### 1. `blog-app/utils/markdown.js` — 增加表格解析分支

**位置**：主循环中，在"有序列表"分支之后、"普通段落"分支之前插入表格分支；并同步修改普通段落 while 条件。

**解析逻辑**（GFM 表格）：
- 新增辅助函数 `isTableRow(line)`：trim 后以 `|` 结尾或包含 `|`（用于识别候选行）
- 新增辅助函数 `isTableDivider(line)`：匹配分隔行 `| --- | :---: | ---: |`（正则 `/^\s*\|?(\s*:?-{1,}:?\s*\|)+\s*:?-{0,}:?\s*$/` 或等价实现，容忍首尾竖线省略、冒号对齐标记）
- 新增辅助函数 `splitTableRow(line)`：去掉首尾 `|` 后按 `|` 分割，各单元格 trim
- 主循环新增分支：当前行 `isTableRow(line)` 且下一行 `isTableDivider(lines[i+1])` 时：
  - 表头行 → `<thead><tr><th>parseInline(cell)</th>...</tr></thead>`
  - 分隔行 → 解析每列 `:---` / `:---:` / `---:` 得到对齐方式，应用到 `<th>` 的 `style="text-align:..."`
  - 后续连续 `isTableRow` 行 → `<tbody><tr><td>parseInline(cell)</td>...</tr></tbody>`
  - 输出 `<table><thead>...</thead><tbody>...</tbody></table>`，消费所有行
- 修改普通段落 while 条件：追加 `!isTableRow(lines[i]) || !isTableDivider(lines[i+1])` 类似的排除条件（精确写法：排除"是表格起始行"的情况，即 `isTableRow(lines[i]) && i+1 < lines.length && isTableDivider(lines[i+1])` 时不进入段落），避免表格前的普通文本吞掉表格首行
- 更新文件头注释：支持列表中加入"表格"
- 单元格内容复用现有 `parseInline`（保留粗体/行内代码/链接等行内能力与转义安全性）
- 不处理 `\|` 转义（保持轻量，与现有解析器风格一致）

### 2. `blog-app/pages/article/detail.vue` — 补充表格样式

**位置**：`.markdown-body` 样式区（约 555 行 `ul/ol` 规则附近），追加：

```scss
.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0 0 16px 0;
  font-size: 13px;
  display: block; /* 小程序 rich-text 兼容，允许宽表纵向自然展开 */
}
```

> 注：`display: block` 的取舍在实现时验证——H5 端保持默认 `table` 布局更自然；若小程序端 table 无法自适应宽度再降级为 block。实现时以 H5 + 微信小程序双端目测为准，优先不写 display，仅 `width: 100%`。

```scss
.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--app-border, #E7E5E4);
  padding: 8px 10px;
  word-break: break-word; /* 宽单元格换行而非溢出（rich-text 内无法横向滚动） */
}
.markdown-body :deep(th) {
  background: var(--app-bg, #FAFAF9);
  font-weight: 600;
}
```

- 颜色全部使用现有 CSS 变量（`--app-border`、`--app-bg`），亮暗主题自动适配（与文件内现有写法一致）
- 不使用 `nth-child` 斑马纹（小程序 rich-text 选择器支持不稳定，保守起见）

## Assumptions & Decisions

1. **不引入 markdown-it/marked**：项目刻意保持 blog-app 无 node_modules（文件头注释说明），继续走自写解析器路线，只补表格分支。
2. **只改移动端**：PC 端 blog-frontend / blog-admin 用 markdown-it 渲染正常，后端无需改动。
3. **学习模块页面不改样式**：3 个学习页面共用 `parseMarkdown`，解析修复自动生效；它们的表格样式属超出本次请求范围，如显示朴素（无边框）可后续单独处理。
4. **对齐语法支持**：实现 `:---`/`:---:`/`---:` → `text-align`，成本低、GFM 标配。
5. 表格在 rich-text 内不做横向滚动容器（rich-text 是字符串节点渲染，无法内嵌 scroll-view），用 `word-break` 换行兜底宽表。

## Verification

1. **单元验证（快速自测）**：在 HBuilderX 中临时（或用 console）调用 `parseMarkdown`，输入：
   ```
   | 列A | 列B |
   | --- | :---: |
   | 1 | **粗体** |
   | 2 | `code` |
   ```
   预期输出含 `<table><thead><th>列A</th>...<tbody><td><strong>粗体</strong></td>`，对齐 style 正确。
2. **页面验证**：HBuilderX 运行 blog-app 到浏览器/模拟器，打开一篇含表格的文章详情：
   - 表格以带边框表格显示，不再是竖线原文
   - 表头有浅灰背景、加粗
   - 切换暗黑主题，表格边框/背景随主题变化
3. **回归验证**：打开含标题/代码块/列表/引用/图片的文章，确认原有渲染不回归；重点确认"普通段落紧跟表格"场景（段落不会被吞进表格、表格首行不被吞进段落）。
4. 学习模块抽查（interview 题目答案含表格时）确认解析正常。
