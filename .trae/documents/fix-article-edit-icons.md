# 修复 ArticleEdit.vue 图标导入 404/SyntaxError 错误

## 问题概述

点击 ArticleList.vue「写文章」按钮跳转到 ArticleEdit.vue 时，路由导航报错：
```
SyntaxError: The requested module '@element-plus/icons-vue' does not provide an export named 'Bold'
```
导致 ArticleEdit.vue 无法加载，路由导航失败。

## 根因分析

[ArticleEdit.vue:171](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L171) 导入了三个不存在的图标：

```js
import { Plus, Bold, Italic, Code } from '@element-plus/icons-vue'
```

经核查 `node_modules/@element-plus/icons-vue` (v2.3.2) 的 [dist/index.js](file:///d:/my-project/java-blog-system/blog-admin/node_modules/@element-plus/icons-vue/dist/index.js) 全部导出（共 293 个图标），**确认不存在 `Bold`、`Italic`、`Code` 这三个图标**。Element Plus 图标库是 UI 导向的，不包含富文本格式化图标（粗体/斜体/代码）。

这三个图标在模板中的使用位置（Markdown 编辑器工具栏）：
- [ArticleEdit.vue:117](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L117) `<el-icon><Bold /></el-icon>` — 粗体按钮，插入 `**`
- [ArticleEdit.vue:120](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L120) `<el-icon><Italic /></el-icon>` — 斜体按钮，插入 `*`
- [ArticleEdit.vue:123](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L123) `<el-icon><Code /></el-icon>` — 行内代码按钮，插入 `` ` ``

## 修改方案

将这三个图标按钮改为**文字标签按钮**，与工具栏中已有的 `H1`/`H2`/`H3`/`列表`/`引用` 文字按钮风格保持一致。这是最小改动，无需引入新依赖，也符合该工具栏既有的设计模式。

### 改动 1：[ArticleEdit.vue:171](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L171) 修正 import

移除不存在的 `Bold, Italic, Code`，仅保留 `Plus`：

```js
import { Plus } from '@element-plus/icons-vue'
```

### 改动 2：[ArticleEdit.vue:116-118](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L116-L118) 粗体按钮

```html
<!-- 修改前 -->
<el-button size="small" @click="insertText('**', '**', '粗体')">
  <el-icon><Bold /></el-icon>
</el-button>

<!-- 修改后 -->
<el-button size="small" @click="insertText('**', '**', '粗体')">B</el-button>
```

### 改动 3：[ArticleEdit.vue:119-121](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L119-L121) 斜体按钮

```html
<!-- 修改前 -->
<el-button size="small" @click="insertText('*', '*', '斜体')">
  <el-icon><Italic /></el-icon>
</el-button>

<!-- 修改后 -->
<el-button size="small" @click="insertText('*', '*', '斜体')">I</el-button>
```

### 改动 4：[ArticleEdit.vue:122-124](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L122-L124) 行内代码按钮

```html
<!-- 修改前 -->
<el-button size="small" @click="insertText('`', '`', '代码')">
  <el-icon><Code /></el-icon>
</el-button>

<!-- 修改后 -->
<el-button size="small" @click="insertText('`', '`', '代码')">&lt;/&gt;</el-button>
```

注：`</>` 在 HTML 中需转义为 `&lt;/&gt;`。`Plus` 图标在 [ArticleEdit.vue:78](file:///d:/my-project/java-blog-system/blog-admin/src/views/ArticleEdit.vue#L78) 仍在使用，故保留 import。

## 前提条件与决策

- **决策**：选择「文字标签」而非「寻找替代图标」。原因是 Element Plus 图标库无语义匹配的粗体/斜体/代码图标，强行套用其他图标（如 EditPen/Document）反而语义混乱；而工具栏已大量使用文字按钮（H1/H2/H3/列表/引用），文字方案最一致。
- **不引入新依赖**（如 font-awesome）以保持改动最小化。

## 验证步骤

1. 修改后保存，Vite HMR 自动热更新。
2. 在 ArticleList 页面点击「写文章」按钮，确认能正常跳转到 ArticleEdit 页面，控制台无 `SyntaxError`。
3. 确认 Markdown 工具栏中 B / I / </> 三个按钮显示正常，点击后能正确插入 `**` / `*` / `` ` `` 标记。
4. 确认文章编辑、保存功能正常。
