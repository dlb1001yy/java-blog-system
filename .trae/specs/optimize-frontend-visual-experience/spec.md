# blog-frontend 视觉与交互体验优化 Spec

## Why
blog-frontend（读者门户）当前样式较为基础：纯色不透明导航、固定宽度容器（1400px 硬编码）、加载无骨架屏、封面图无比例占位、代码块为默认 github-dark 样式、无暗色模式。需按现代内容平台标准从排版、微交互、流式布局、首屏性能、动效五个维度优化，并与 blog-admin 的 Design Tokens 架构（语义化变量 + `--el-*` 变量）保持统一。

## 技术方案

### 1. Typography（排版与阅读体验）
- **字号阶梯**：在 `variables.css` 建立标题/正文字号与行高令牌（H1-H3 基于 1.25 Major Third 比例，正文 16px / line-height 1.75）。
- **正文阅读性**：`ArticleDetail.vue` 的 `.article-content` 强化：`max-width: 68ch`（中文场景以行宽约 38em 兜底适配）、`line-height: 1.75`、`letter-spacing: 0.02em`、`font-feature-settings: "kern" 1, "liga" 1`。
- **代码块增强**：`markdown.js` 渲染时为 `<pre>` 包裹带 Mac 三色圆点顶部栏 + 复制按钮的容器（DOM 结构由 renderer 生成，复制按钮事件在 `ArticleDetail.vue` 渲染后委托绑定）；保持现有 `github-dark`（Atom One Dark 风格）高亮主题。

### 2. 微交互与视觉质感
- **毛玻璃吸顶导航**：`AppHeader.vue` 的 `.app-header` 改为 `backdrop-filter: blur(12px) saturate(180%)` + 半透明白底（新增令牌 `--header-bg`），配 `border-bottom`；`@supports not (backdrop-filter)` 降级为不透明白。
- **卡片悬浮**：`ArticleCard.vue` hover 提升为 `translateY(-4px)` + 柔和大阴影（`0 12px 24px -10px rgba(0,0,0,0.08)`），过渡曲线 `cubic-bezier(0.4, 0, 0.2, 1)` 0.25s。

### 3. Design Tokens 与流式布局
- **令牌体系对齐**：`global.css`/`variables.css` 变量重命名对齐 blog-admin 语义（`--color-primary`、`--bg-page`、`--bg-card`、`--text-primary/regular/secondary`、`--border-color`、阴影/圆角/间距令牌），并同步覆盖 Element Plus 变量（`--el-color-primary` 等，复用与 admin 一致的浅色值体系但保留现有 `#409eff` 品牌主色不变——仅做架构对齐，不改品牌色）。
- **流式尺寸**：容器与外边距用 `clamp()`——`--container-max-width: 1400px`（保持现宽）、`--container-padding: clamp(16px, 5vw, 24px)`、H1 标题字号 `clamp(1.5rem, 4vw + 0.5rem, 2.25rem)`；正文标题随断点平滑缩放，减少 Media Query 硬切换。

### 4. 骨架屏与首屏优化（CLS）
- **骨架屏组件**：新建 `components/SkeletonCard.vue`（shimmer 脉冲动画，结构对齐 ArticleCard：封面块 + 标题条 + 摘要两行 + meta 条），替换 `Home.vue`/`ArticleList.vue` 等文章列表加载态（如现有 v-loading/空白则替换为 3 个骨架卡）。
- **封面图比例占位**：`ArticleCard.vue` 封面容器改 `aspect-ratio: 20 / 13`（贴近现 200×130）并去固定高度，图片未加载时不跳动。

### 5. 动效与过渡细节
- **路由过渡升级**：`App.vue` 的 `<transition name="fade">` 升级为 `fade-slide`（淡入 + Y 轴 10px 平移，0.25s ease，out-in），样式加 `prefers-reduced-motion: reduce` 降级为无位移。

## What Changes
- 修改 `blog-frontend/src/assets/styles/global.css`：令牌对齐重命名（保留旧名别名过渡）、流式 clamp 令牌、shimmer 动画、fade-slide 过渡样式
- 修改 `blog-frontend/src/assets/styles/variables.css`：补充字号阶梯、间距、流式尺寸、header-bg 令牌
- 修改 `blog-frontend/src/components/AppHeader.vue`：毛玻璃 sticky 导航
- 修改 `blog-frontend/src/components/ArticleCard.vue`：hover 光影过渡、封面 aspect-ratio 占位
- 新建 `blog-frontend/src/components/SkeletonCard.vue`：shimmer 骨架卡
- 修改 `blog-frontend/src/views/Home.vue`、`ArticleList.vue`（及有列表加载态的视图）：接入骨架屏
- 修改 `blog-frontend/src/utils/markdown.js`：代码块 Mac 顶部栏 + 复制按钮 DOM
- 修改 `blog-frontend/src/views/ArticleDetail.vue`：正文阅读性强化、复制按钮事件绑定
- 修改 `blog-frontend/src/App.vue`：路由过渡升级 fade-slide
- **BREAKING**：无（纯样式/体验优化，业务逻辑与接口不变；变量重命名通过别名兼容组件内旧引用）

## Impact
- Affected specs: `modernize-blog-app-ui`（不同端，无冲突）；与 admin 侧 `refresh-admin-color-palette`/`add-element-plus-theme-switching` 仅做令牌**语义**对齐（本 spec 不引入暗色模式、不改品牌色 #409eff）
- Affected code: 上述 blog-frontend 文件
- 不影响：blog-backend、blog-admin、blog-app、API 层、路由结构

## ADDED Requirements

### Requirement: 正文阅读性强化
文章正文 SHALL 限制行宽（约 68ch / 中文兜底 38em）、行高 1.75、开启 kern/liga 字体微调，长文阅读不产生扫视疲劳。

#### Scenario: 正文排版生效
- **WHEN** 打开文章详情页
- **THEN** 正文区域行宽受限居中，行高 1.75，标题层级清晰（H1-H3 阶梯）

### Requirement: 代码块现代化
Markdown 代码块 SHALL 渲染带 Mac 三色圆点顶部栏与一键复制按钮，高亮保持深色主题。

#### Scenario: 复制代码
- **WHEN** 用户点击代码块右上角复制按钮
- **THEN** 代码文本写入剪贴板，按钮给出成功反馈（文字短暂变"已复制"）

### Requirement: 毛玻璃吸顶导航
顶部导航 SHALL 在滚动时呈现半透明毛玻璃效果（blur 12px + saturate 180%），不支持 backdrop-filter 的浏览器降级为不透明背景。

### Requirement: 卡片悬浮光影
文章卡片 hover SHALL 有 -4px 上浮与大范围柔和阴影，过渡 0.25s 标准缓动曲线，无生硬跳变。

### Requirement: 流式响应式尺寸
容器内边距与标题字号 SHALL 使用 clamp() 在移动端与桌面端之间平滑缩放，减少断点硬切换。

### Requirement: 骨架屏加载
文章列表加载中 SHALL 显示 shimmer 骨架卡（结构与真实卡片对齐），加载完成骨架与内容同构替换，无布局跳动。

### Requirement: 封面图比例占位
文章封面容器 SHALL 用 aspect-ratio 固定比例，图片加载前后页面高度稳定（CLS ≈ 0）。

### Requirement: 路由淡入平移过渡
路由切换 SHALL 使用 fade-slide 过渡（淡入 + 10px 平移，0.25s），系统开启"减少动态效果"时降级为纯淡入。

## MODIFIED Requirements

### Requirement: 全局样式令牌
`global.css` 变量从 Element 默认命名（--primary-color 等）扩展为与 admin 对齐的语义化令牌体系，旧变量名保留为别名，组件现有引用不破坏。
