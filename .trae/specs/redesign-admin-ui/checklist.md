# Checklist

- [x] `src/assets/styles/tokens.css` 存在并定义了颜色/渐变/阴影/圆角/间距/字号/过渡 CSS 变量，且覆盖了 `--el-color-primary` 为 `#6366F1`
- [x] `src/main.js` 在 `global.css` 之前引入 `tokens.css`，并引入了 Inter 字体（通过 index.html Google Fonts link）
- [x] `global.css` 字体栈优先使用 Inter，滚动条为 4px 圆角半透明，路由切换为 slide-fade 动画
- [x] `layout/index.vue` 侧边栏为渐变深色背景，主内容区有柔和渐变光斑
- [x] `Sidebar.vue` 激活菜单项有发光指示条，悬浮有微动效，logo 为圆角胶囊
- [x] `Header.vue` 背景使用 backdrop-filter 玻璃模糊，图标 hover 为圆角胶囊背景，用户头像有渐变描边
- [x] `TagsView.vue` 标签为圆润胶囊，激活态渐变填充白字，关闭按钮 hover 缩放
- [x] `Login.vue` 为左右分屏：左侧品牌渐变区 + 标语，右侧玻璃拟态登录卡，移动端 < 768px 隐藏左侧
- [x] `Dashboard.vue` 统计卡为渐变背景 + 大号白字，使用 Bento 网格，图表卡大圆角柔和阴影
- [x] `PageContainer.vue` 组件存在，提供 title/description props 与 default/action 插槽
- [x] ArticleList/CategoryList/TagList/CommentList/MessageList/LinkList 均使用 PageContainer
- [x] 所有列表页表格无硬外边框，行 hover 有主色浅色背景，分页有卡片包裹
- [x] Settings/ResumeEdit/ArticleEdit 套用 PageContainer 与现代卡片样式
- [x] 全站主色统一为靛蓝紫渐变（#6366F1 → #8B5CF6），无残留默认 Element Plus 蓝 (#409eff) 作为主色
- [x] 后端接口、路由结构、业务逻辑未被修改
- [x] dev server 启动无报错，各页面视觉一致、动效流畅
