# 修复考试页面编程题代码编辑器显示异常

## Summary

`blog-frontend` 考试页面（ExamTaking.vue）编程题答案填写区域显示为一条黑色横条，无法编写代码。根因是 `@guolao/vue-monaco-editor` 组件未显式传 `height` prop，组件 wrapper 使用 inline style `height: 100%`（prop 默认值）覆盖了 CSS class 中的 `height: 360px`；而父级容器高度为 auto，导致 `100%` 失效、编辑器容器高度塌陷。修复为按官方用法显式传入 `height="360px"`。

## Current State Analysis

### 现象
- 编程题（type 6）语言切换按钮（JavaScript/Java/Python）正常显示
- 编辑器区域只渲染出一条黑色横条（vs-dark 主题背景 #1e1e1e，说明 Monaco 实际已从 CDN 加载成功并创建了编辑器实例）
- 无法看到完整编辑器 UI，无法输入代码

### 排查结论（已验证）

1. **组件注册正常**：[main.js](../../../d:/my-project/java-blog-system/blog-frontend/src/main.js#L3) 通过 `app.use(VueMonacoEditorPlugin)` 全局注册（组件 name 为 `VueMonacoEditor`），Vue 3 自动将 kebab-case 标签 `<vue-monaco-editor>` 解析到该全局组件，`<script setup>` 无需 import。
2. **CDN 配置正常**：`loader.config({ paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.45.0/min/vs' } })`。截图显示黑色主题说明 Monaco 已成功加载。
3. **根因：高度塌陷**。组件源码（`node_modules/@guolao/vue-monaco-editor/lib/es/index.js`）中：
   - wrapper div 渲染 inline style `{ display: flex, position: relative, textAlign: initial, width: props.width, height: props.height }`
   - `height` prop 默认值为 `"100%"`
   - **inline style 优先级高于 CSS class**，因此 [ExamTaking.vue:690-695](../../../d:/my-project/java-blog-system/blog-frontend/src/views/ExamTaking.vue#L690-L695) 的 `.code-editor { height: 360px }` 被 inline `height: 100%` 覆盖，成为死代码
   - 父级 `.blank-area`（第 677-679 行）仅设置 `max-width: 800px`，高度为 auto → `height: 100%` 无参照、失效 → wrapper 按内容计算高度
   - 编辑器 container div 是 flex 子项且无内容 → 高度趋近 0 → Monaco 渲染在塌陷容器中 → 显示为黑色横条
4. **回退机制未触发**：`@mount` 已正常触发（编辑器创建成功），`monacoReady` 保持 true，因此不会回退 textarea，而是一直显示塌陷的编辑器。

## Proposed Changes

### 修改文件：`blog-frontend/src/views/ExamTaking.vue`

**改动 1：模板第 145-154 行，给 `<vue-monaco-editor>` 显式传 `height` prop**

```vue
<vue-monaco-editor
  v-if="monacoReady"
  height="360px"
  :value="textAnswer"
  :language="codeLang"
  theme="vs-dark"
  :options="monacoOptions"
  class="code-editor"
  @change="v => setText(v)"
  @mount="onMonacoMount"
/>
```

- 依据：`@guolao/vue-monaco-editor` 官方用法——要么设置编辑器父容器的显式高度，要么直接传 `height`/`width` prop。显式传 prop 后 wrapper inline style 变为 `height: 360px`，编辑器 container 作为 flex 子项 stretch 撑满，配合 `automaticLayout: true` 正常渲染。

**改动 2：CSS 第 690-695 行，删除被覆盖的死代码 `height: 360px`**

```css
.code-editor {
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--border-color, #e4e7ed);
}
```

- 删除 `height: 360px`（已由 prop 提供，保留会误导后续维护者以为高度由 CSS 控制）。
- 其余属性（圆角、边框、overflow hidden）作用于 wrapper 根元素，保留继续生效。

### 不改动的部分（确认无问题）

- `main.js` 的全局注册与 CDN `loader.config` —— 正常
- `monacoOptions`、`onMonacoMount`、15 秒超时回退 textarea 机制 —— 正常
- `textAnswer` / `setText` 的 join/split 逻辑 —— 正常
- 语言切换 `codeLang` —— 正常

## Assumptions & Decisions

- 不更换 CDN 源、不本地打包 Monaco：截图证明 jsdelivr CDN 在用户环境加载成功，本次问题与 CDN 无关；且已有 15 秒超时回退 textarea 的兜底。
- 不改 `.blank-area` 布局：显式传 prop 是官方推荐做法，改动最小。
- 编辑器高度沿用原 CSS 设计值 360px。

## Verification steps

1. 启动前端：`cd blog-frontend && npm run dev`
2. 登录后进入"在线考试"→ 选择含编程题的试卷 → 开始考试
3. 切换到编程题，验证：
   - 编辑器以 360px 高度完整显示（vs-dark 深色主题、行号、可点击光标输入）
   - 输入代码有语法高亮；切换 JavaScript/Java/Python 高亮随之变化
   - 切到其他题再切回，已输入代码保留
4. 交卷后到成绩/阅卷侧确认编程题答案内容完整提交
5. 断网或屏蔽 CDN 复测（可选）：15 秒后应回退为 textarea 仍可作答
