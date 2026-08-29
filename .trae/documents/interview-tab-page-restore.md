# 计划：刷题页还原为 tab 主页面（恢复原生导航 + TabBar，移除返回按钮）

## Summary

上一轮按"补返回按钮"的方案给刷题页加了自定义 NavBar 并改 `navigationStyle: custom`，但实际带来三个问题（用户本次反馈）：
1. **头部变白**：自定义导航下页面失去原生导航栏，`applyNavBarTheme()`（原本同步原生导航栏配色）失去作用，状态栏区域直接露出页面背景/白色
2. **底部导航栏消失**：刷题页从未挂载 `<TabBar>`，之前靠原生导航栏撑着头部，现在头部白、底部空，页面看起来像"跳转走了"
3. **不应有返回按钮**：刷题是 4-tab 之一的主页面（同首页/音乐/我的），不应呈现为二级页面

正确做法：还原为 tab 主页面形态——恢复原生导航栏、删除 NavBar、补挂 TabBar（与音乐/我的页同款模式）。

## Current State Analysis

- [pages.json L110-117](d:/my-project/java-blog-system/blog-app/pages.json#L110-L117)：`subpkg-study/pages/interview/index` 当前带 `"navigationStyle": "custom"`（上一轮加入）
- [interview/index.vue L4-5](d:/my-project/java-blog-system/blog-app/subpkg-study/pages/interview/index.vue#L4-L5)：模板第 4-5 行是 `<NavBar title="面试刷题" />`；脚本 L171 有 `import NavBar from '@/components/NavBar.vue'`
- [interview/index.vue L455-469](d:/my-project/java-blog-system/blog-app/subpkg-study/pages/interview/index.vue#L455-L469)：`.interview-page` 当前 `display:flex; flex-direction:column`，`.container` 用 `flex:1; min-height:0`（上一轮为 NavBar 改的）；页面末尾（L157-158）`</scroll-view></view>` 直接结束，无 TabBar
- 对照组 1 [mine/index.vue L86](d:/my-project/java-blog-system/blog-app/subpkg/pages/mine/index.vue#L86)：tab 主页面模式 = 原生导航 + 页面末尾 `<TabBar current="..." />`
- 对照组 2 [subpkg-music/pages/index.vue L162-165](d:/my-project/java-blog-system/blog-app/subpkg-music/pages/index.vue#L162-L165)：同为 tab 页（原生子包导航 + flex 根布局 + scroll-view 中部 + TabBar 固定底部），是最佳参照结构
- 全项目 `<TabBar` 挂载 4 处：首页/音乐/简历预览/我的，刷题页缺失
- `applyNavBarTheme()`（theme.js）依赖页面存在原生导航栏，`custom` 样式下无效——这是"头部变白"的根因

## Proposed Changes

**文件 1：[pages.json](d:/my-project/java-blog-system/blog-app/pages.json)**
- `subpkg-study/pages/interview/index` 的 style 删除 `"navigationStyle": "custom"`，保留 `navigationBarTitleText: "面试刷题"` 与 `backgroundColor: @bgColor`（恢复原生导航栏，`applyNavBarTheme()` 重新生效、标题/配色随主题）

**文件 2：[subpkg-study/pages/interview/index.vue](d:/my-project/java-blog-system/blog-app/subpkg-study/pages/interview/index.vue)**
1. 模板：删除 `<NavBar title="面试刷题" />` 及其注释行（L4-5）
2. 模板：`</scroll-view>` 之后、根节点 `</view>` 之前插入 `<TabBar current="/subpkg-study/pages/interview/index" />`
3. 脚本：删除 `import NavBar from '@/components/NavBar.vue'`（L171），新增 `import TabBar from '@/components/TabBar.vue'`
4. 样式：`.interview-page` 移除 `display:flex; flex-direction:column`（保留 `height:100vh` 与背景）；`.container` 由 `flex:1; min-height:0` 改回 `height:100%`；确认 `.container` 底部 padding ≥ `calc(56px + 安全区 + 间距)`，避免列表最后一项被固定 TabBar 遮挡（当前是 `calc(24px + env(safe-area-inset-bottom))`，需加大到 `calc(56px + 24px + env(safe-area-inset-bottom))` 量级，与音乐页列表底部留白策略一致）
5. TabBar 激活态：TabBar.vue 的 list 中"刷题"path 即 `/subpkg-study/pages/interview/index`，传入相同 `current` 即正确高亮

## Assumptions & Decisions

- "不应该跳转的"理解为：点击底部 tab"刷题"后应停留在 tab 主页面语境（有原生标题栏 + 底部 tab 栏常驻），而非像 push 二级页那样头部白板、底部 tab 消失；不改变 TabBar 的 `reLaunch` 切换机制
- 首页网格"刷题"入口（`navigateTo`）进入时，原生导航栏会自动显示返回箭头（uni-app 默认行为，栈深 >1 时出现），无需额外处理——tab 页身份与"从网格进入可返回"天然兼容
- 不动音乐页/我的页等已正确的 tab 页
- 上一轮计划文件不修改、不删除（历史记录）

## Verification steps

1. Read interview/index.vue 确认：无 NavBar 引用、`import TabBar` 存在、模板末尾有 `<TabBar current="/subpkg-study/pages/interview/index" />`
2. Read pages.json 确认 interview 页无 `navigationStyle: custom`
3. GetDiagnostics 两文件无错误
4. H5 运行验证：
   - 底部 TabBar 点"刷题"：顶部显示原生标题"面试刷题"（配色随主题），无返回按钮，底部 tab 栏常驻且"刷题"高亮
   - 暗黑模式下头部/页面背景正常（applyNavBarTheme 生效）
   - 列表滚动触底加载正常，最后一项不被 TabBar 遮挡
   - 首页网格"刷题"进入：原生导航自动带返回箭头，可返回首页
