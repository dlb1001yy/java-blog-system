# 计划：刷题页添加返回按钮 + 简历仅保留预览（移除编辑）

## Summary

两个独立改动：
1. 刷题页（TabBar tab 之一，同时可从首页网格 navigateTo 进入）当前使用原生导航栏，从 TabBar `reLaunch` 进入时页面栈仅 1 层，原生导航栏不渲染返回箭头，用户无法返回首页。改为自定义导航栏（NavBar 组件，自带 navigateBack 失败兜底 reLaunch 首页），保证任何入口进入都有可用的返回按钮。
2. 用户不需要简历编辑功能，仅保留预览。删除编辑页与其注册、删除"我的"页菜单中的编辑入口，并清理预览页中仅服务于编辑页的 `preview=1` 死代码分支。

## Current State Analysis

- [pages.json](d:/my-project/java-blog-system/blog-app/pages.json#L115-L122)：`subpkg-study/pages/interview/index` 用默认原生导航栏（仅 `navigationBarTitleText`）；`subpkg/pages/resume/edit` 注册于 L72-78（`navigationStyle: custom`）
- [interview/index.vue](d:/my-project/java-blog-system/blog-app/subpkg-study/pages/interview/index.vue#L3-L5)：根节点 `.interview-page`（`height: 100vh`）内第一个子元素是 `scroll-view.container`（`height: 100%`），无任何自定义导航
- [NavBar.vue](d:/my-project/java-blog-system/blog-app/components/NavBar.vue#L47-L53)：已有 `goBack` 实现——`uni.navigateBack` 失败时 `uni.reLaunch('/pages/index/index')`；普通流式布局（非 fixed），含状态栏占位 + 44px 导航行
- [TabBar.vue](d:/my-project/java-blog-system/blog-app/components/TabBar.vue#L58-L61)：tab 切换用 `uni.reLaunch`，这是刷题页页面栈为 1 的原因
- [mine/index.vue](d:/my-project/java-blog-system/blog-app/subpkg/pages/mine/index.vue#L38-L51)：菜单前两项为"我的简历"（→ `resume/edit`）与"简历预览"（→ `resume/index`）
- [resume/index.vue](d:/my-project/java-blog-system/blog-app/subpkg/pages/resume/index.vue#L4)：`isPreview` 分支（L4 NavBar、L169 TabBar 条件、L187/L258-268 onLoad storage 读取）仅被 edit.vue 的 `preview=1` 跳转触发；`app_resume_preview` storage key 只在 edit.vue 写入
- 全局 grep `resume/edit` 仅 2 处引用：pages.json 注册 + mine/index.vue 菜单项，无其他依赖
- 首页模块网格"简历"入口（[index.vue L192](d:/my-project/java-blog-system/blog-app/pages/index/index.vue#L192)）指向的是预览页 `resume/index`，不受影响，保留
- api.js 中的 saveMyResume/分享系列函数与 upload.js 为通用 API 层导出，本次不清理（避免连带风险）

## Proposed Changes

### 变更 1：刷题页添加自定义导航（返回按钮）

**文件：[pages.json](d:/my-project/java-blog-system/blog-app/pages.json)**
- `subpkg-study/pages/interview/index` 的 style 增加 `"navigationStyle": "custom"`

**文件：[subpkg-study/pages/interview/index.vue](d:/my-project/java-blog-system/blog-app/subpkg-study/pages/interview/index.vue)**
- 模板：在 `<scroll-view class="container">` 之前插入 `<NavBar title="面试刷题" />`（`showBack` 默认 true，无需传）
- 脚本：`import NavBar from '@/components/NavBar.vue'`
- 样式：
  - `.interview-page` 增加 `display: flex; flex-direction: column;`（保留 `height: 100vh`）
  - `.container` 由 `height: 100%` 改为 `flex: 1; min-height: 0;`（其余 padding 不变），保证 NavBar 占位后滚动区自适应
- 行为验证点：
  - 首页网格 `navigateTo` 进入 → 返回按钮 `navigateBack` 回首页
  - TabBar `reLaunch` 进入（栈深 1）→ `navigateBack` 失败 → 兜底 `reLaunch` 回首页

### 变更 2：移除简历编辑，仅保留预览

1. **删除文件** `blog-app/subpkg/pages/resume/edit.vue`
2. **[pages.json](d:/my-project/java-blog-system/blog-app/pages.json)**：删除 `subpkg` 分包中 `pages/resume/edit` 注册块（L72-78）
3. **[mine/index.vue](d:/my-project/java-blog-system/blog-app/subpkg/pages/mine/index.vue)**：删除"我的简历"菜单项（L38-44，icon 为 edit 的那一项）；"简历预览"项保留
4. **[resume/index.vue](d:/my-project/java-blog-system/blog-app/subpkg/pages/resume/index.vue)** 清理死代码：
   - 模板：删除 `<NavBar v-if="isPreview" title="简历预览" />`（L4）；`<TabBar v-if="!isPreview" ...>` 改为 `<TabBar current="/subpkg/pages/resume/index" />`（L169）
   - 脚本：删除 `isPreview` ref、NavBar import、onLoad 中 `options.preview === '1'` 分支（storage key `app_resume_preview` 读取逻辑）

## Assumptions & Decisions

- 刷题页虽是 tab 页，但按用户明确要求提供返回按钮；NavBar 的 reLaunch 兜底保证栈深 1 时也能回首页，不出现"点了没反应"
- 音乐页同为 tab 页但用户未提及，不动
- `resume/index.vue` 预览页仍走 `api.getResume()` 接口（需登录），未登录时维持现有"简历加载失败" toast 行为，不加守卫（用户未要求）
- api.js/upload.js 中仅被 edit.vue 使用过的导出函数保留不删（通用 API 层，删除属过度清理）

## Verification steps

1. Grep 全项目 `resume/edit` → 应仅剩 0 处引用（pages.json 与 mine 菜单均已移除）
2. Grep `app_resume_preview`、`isPreview` → resume/index.vue 中应为 0 处
3. H5 运行验证：
   - 首页 → 模块网格"刷题" → 顶部出现"面试刷题"标题 + 左侧返回箭头 → 点击返回首页
   - 底部 TabBar 点"刷题" → 返回箭头点击 → 回首页（reLaunch 兜底）
   - 刷题页筛选面板展开/收起、列表滚动、触底分页不受 NavBar 插入影响（滚动区高度正确）
   - "我的"页菜单无"我的简历"项；点"简历预览"正常渲染简历数据；首页网格"简历"入口同样正常
   - 暗黑模式下 NavBar 配色正常（组件本身已用 `var(--app-*)`）
