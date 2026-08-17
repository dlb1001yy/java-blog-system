# Checklist

- [x] `global.css`/`variables.css` 存在与 admin 对齐的语义化令牌（--color-primary/--bg-page/--bg-card/--text-strong/--shadow-hover/--header-bg 等），旧变量名保留为别名未删除
- [x] 字号阶梯令牌存在：H1 使用 `clamp(1.5rem, 4vw + 0.5rem, 2.25rem)`，正文 16px/1.75，阅读宽度令牌 68ch
- [x] 容器 padding 使用 `clamp(16px, 5vw, 24px)` 流式令牌
- [x] `.app-header` 为毛玻璃效果（blur 12px + saturate 180% + var(--header-bg) 半透明背景），`@supports not` 降级为不透明白+阴影
- [x] ArticleCard hover 为 `translateY(-4px)` + `var(--shadow-hover)`（0 12px 24px -10px），过渡 0.25s cubic-bezier(0.4,0,0.2,1)
- [x] ArticleCard 封面容器使用 `aspect-ratio: 20/13`（移动端 16/9），无固定像素高度
- [x] `SkeletonCard.vue` 存在且含 shimmer 动画（::after 渐变横扫 1.5s infinite），结构对齐真实卡片（封面+标题+摘要+meta）
- [x] Home/ArticleList 列表加载中显示 3 个骨架卡（loading try/finally 控制），空状态有 !loading 条件，加载完成切换真实列表
- [x] `.article-content` 有 max-width var(--reading-width)（68ch）、line-height 1.75、letter-spacing 0.02em、font-feature-settings "kern"/"liga"；h1/h2/h3 引用阶梯令牌
- [x] markdown 渲染的代码块带 Mac 三色圆点顶部栏与复制按钮；点击复制成功后按钮反馈"已复制"1.5s（失败反馈"复制失败"），事件委托 + __copyBound 防重复绑定
- [x] 代码高亮仍为深色主题（github-dark，hljs 高亮值未变），ArticleDetail 复用共享 md 实例消除重复配置
- [x] 路由过渡为 fade-slide（淡入+±10px 平移 0.25s，out-in），`prefers-reduced-motion: reduce` 时降级为纯淡入
- [x] `npm run build` 构建通过（exit 0，built in 11.84s）
