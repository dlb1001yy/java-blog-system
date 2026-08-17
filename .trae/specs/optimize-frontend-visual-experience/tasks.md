# Tasks

- [x] Task 1: Design Tokens 对齐与流式布局令牌
  - [x] SubTask 1.1: 修改 `blog-frontend/src/assets/styles/global.css`：在 `:root` 新增对齐 admin 的语义化令牌（`--color-primary: var(--primary-color)`、`--bg-page`、`--bg-card`、`--text-primary/regular/secondary`、`--border-color`、阴影/圆角/间距令牌、`--header-bg: rgba(255,255,255,0.75)`）；旧变量名保留为别名，不删除
  - [x] SubTask 1.2: 修改 `blog-frontend/src/assets/styles/variables.css`：新增字号阶梯（H1 `clamp(1.5rem, 4vw + 0.5rem, 2.25rem)`、H2/H3 按 1.25 比例、正文 16px/1.75）、流式间距（`--container-padding: clamp(16px, 5vw, 24px)`）、阅读行宽（`--reading-width: 68ch`）
  - [x] SubTask 1.3: `global.css` 中 `.container` 等容器 padding 引用流式令牌；追加 shimmer keyframes 与 fade-slide 过渡样式（含 `prefers-reduced-motion: reduce` 降级）
- [x] Task 2: 毛玻璃吸顶导航
  - [x] SubTask 2.1: 修改 `blog-frontend/src/components/AppHeader.vue`：`.app-header` 改 `background: var(--header-bg)` + `backdrop-filter: blur(12px) saturate(180%)` + `border-bottom: 1px solid var(--border-color)`；用 `@supports not (backdrop-filter: blur(1px))` 降级为 `background: #fff`
- [x] Task 3: 卡片悬浮与封面占位
  - [x] SubTask 3.1: 修改 `blog-frontend/src/components/ArticleCard.vue`：hover 改 `translateY(-4px)` + `box-shadow: 0 12px 24px -10px rgba(0,0,0,0.08)`，`transition: transform 0.25s cubic-bezier(0.4,0,0.2,1), box-shadow 0.25s cubic-bezier(0.4,0,0.2,1)`；封面容器改 `aspect-ratio: 20 / 13` 去固定高度（移动端 16/9），img 保持 object-fit
- [x] Task 4: 骨架屏组件与接入
  - [x] SubTask 4.1: 新建 `blog-frontend/src/components/SkeletonCard.vue`：结构对齐 ArticleCard（封面灰块 + 标题条 + 摘要两条 + meta 条），使用 `--bg-color`/浅灰 + shimmer 动画（::after 渐变横扫 1.5s infinite）
  - [x] SubTask 4.2: 修改 `blog-frontend/src/views/Home.vue` 与 `ArticleList.vue`：两视图原本均无加载态，新增 `loading` ref（try/finally 控制），加载中渲染 3 个 SkeletonCard，`v-else` 渲染真实列表；空状态加 `!loading` 条件；分页/路由 watch 逻辑不变
- [x] Task 5: 正文阅读性与代码块增强
  - [x] SubTask 5.1: 修改 `blog-frontend/src/views/ArticleDetail.vue`：`.article-content` 强化 `max-width: var(--reading-width)`、`line-height: 1.75`、`letter-spacing: 0.02em`、`font-feature-settings: "kern" 1, "liga" 1`；标题引用字号阶梯令牌
  - [x] SubTask 5.2: 修改 `blog-frontend/src/utils/markdown.js`：`highlight` 返回值包裹 `wrapCodeBlock()`（Mac 三色圆点 + 复制按钮 + pre）结构；ArticleDetail 同步改为复用该共享 md 实例（删除组件内重复的 MarkdownIt 配置约 40 行）
  - [x] SubTask 5.3: `ArticleDetail.vue` 渲染后对 `.copy-btn` 事件委托绑定（`__copyBound` 防重复绑定），复制成功反馈"已复制"1.5s；非 scoped style 块补充 `.code-block`/`.dot`/`.copy-btn` 样式
- [x] Task 6: 路由过渡升级
  - [x] SubTask 6.1: 修改 `blog-frontend/src/App.vue`：`<transition name="fade-slide" mode="out-in">`；fade-slide 样式已在 global.css（Task 1.3）
- [x] Task 7: 构建验证
  - [x] SubTask 7.1: 执行 `npm run build` 确认无编译错误（vite 5 构建 exit 0，built in 11.84s）

# Task Dependencies
- Task 2、3、4、5、6 依赖 Task 1（令牌与全局样式先行），彼此可并行
- Task 7 依赖 Task 1 ~ Task 6 全部完成
