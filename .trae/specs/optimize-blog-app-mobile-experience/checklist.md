# Checklist

## 样式与 UI/UX
- [x] App.vue 定义 page 级 `--app-*` CSS 变量与 `.theme-dark` 覆盖，含 `--app-safe-bottom`，与 theme.js 值一致
- [x] manifest.json 开启 darkmode，theme.json 存在且 pages.json 引用 themeLocation，globalStyle 颜色使用 `@` 变量
- [x] theme.js 提供暗色色板、响应式主题状态、initTheme/setTheme/toggleTheme（storage 持久化）、onThemeChange 跟随系统
- [x] 全部页面根节点绑定主题 class，surface 颜色使用 `var(--app-*)`，暗色下无白底/黑字不可读区域
- [x] 我的页提供外观设置（跟随系统/亮色/暗色），切换即时生效且重启保持
- [x] ArticleItem、相关文章、菜单项具备 `:active` 缩放触控反馈
- [x] 首页 scroll-view refresher 展示品牌脉冲下拉动画，触底展示三点跳动加载动画，原生 onPullDownRefresh 已移除
- [x] NavBar 组件按 statusBarHeight 适配刘海屏，登录页已接入

## 功能与离线体验
- [x] common/offline.js 提供网络监听、列表/详情缓存（带时间戳）、稍后阅读增删查
- [x] 断网打开首页：降级读缓存并提示"已进入离线阅读模式"
- [x] 断网打开已缓存详情：正文可读，加载成功后自动写缓存
- [x] 详情页提供"稍后阅读"按钮，去重添加、状态高亮
- [x] 稍后阅读页（分包）离线可读，SwipeCell 侧滑删除正常，其余条目自动收起
- [x] SharePoster 绘制渐变头部 + 标题/摘要换行 + 二维码海报；App/MP 保存相册（含权限拒绝引导），H5 长按保存，复制链接可用
- [x] utils/qrcode.js 零外部依赖，可生成文章链接二维码

## 图片与性能
- [x] imageUrl.js 仅对配置的 CDN host 追加 resize/WebP 参数，默认原样返回
- [x] 列表封面图 lazy-load + @load 渐显（软 LQIP）
- [x] 详情正文按段渲染图片，点击 uni.previewImage 可预览并滑动切换本文图片
- [x] pages.json 配置 subPackages，mine/login/resume/readlater 位于 subpkg 分包，主包仅保留 index 与 article/detail
- [x] 全项目无残留 `/pages/mine`、`/pages/resume` 旧路径引用，分包页面均可正常打开
- [x] 输出 HBuilderX H5 手动验证清单，覆盖暗黑、离线、海报、侧滑、分包可达性
