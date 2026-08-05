# Tasks

- [x] Task 1: 建立设计令牌系统与全局基础样式
  - [x] SubTask 1.1: 新增 `src/assets/styles/tokens.css`，定义颜色/渐变/阴影/圆角/间距/字号/过渡 CSS 变量，并覆盖 Element Plus 主题变量（`--el-color-primary` 等）
  - [x] SubTask 1.2: 修改 `src/main.js`，在 `global.css` 之前引入 `tokens.css`，并通过 CDN link 引入 Inter 字体
  - [x] SubTask 1.3: 修改 `src/assets/styles/global.css`，升级字体栈、滚动条样式（4px 圆角半透明）、路由切换动画（slide-fade）、全局背景

- [x] Task 2: 重构 Layout 主框架
  - [x] SubTask 2.1: 修改 `src/layout/index.vue`，侧边栏渐变深色背景，主内容区加柔和渐变光斑背景，调整 padding 与圆角
  - [x] SubTask 2.2: 修改 `src/layout/Sidebar.vue`，渐变背景 + 激活态发光指示条 + 悬浮微动效 + logo 圆角胶囊
  - [x] SubTask 2.3: 修改 `src/layout/Header.vue`，玻璃模糊背景（backdrop-filter）+ 图标圆角胶囊 hover + 用户头像渐变描边
  - [x] SubTask 2.4: 修改 `src/layout/TagsView.vue`，圆润胶囊样式 + 激活态渐变填充 + 关闭按钮缩放动效

- [x] Task 3: 重构登录页
  - [x] SubTask 3.1: 修改 `src/views/Login.vue`，左右分屏布局：左侧品牌区（动态渐变 + 网格 + 标语），右侧玻璃拟态登录卡，移动端自适应隐藏左侧

- [x] Task 4: 重构 Dashboard
  - [x] SubTask 4.1: 修改 `src/views/Dashboard.vue`，统计卡改为渐变背景 + 大号白字 + 微光斑，Bento 网格布局，图表卡大圆角柔和阴影，待办列表现代卡片化

- [x] Task 5: 新建 PageContainer 组件
  - [x] SubTask 5.1: 新增 `src/components/PageContainer.vue`，提供 title/description props 与 default/action 插槽，统一页头视觉

- [x] Task 6: 统一重构列表页视觉（文章/分类/标签/评论/留言/友链）
  - [x] SubTask 6.1: 修改 `src/views/ArticleList.vue`，使用 PageContainer，搜索栏圆角卡片，表格去硬边框 + 行 hover 高亮，分页卡片包裹
  - [x] SubTask 6.2: 修改 `src/views/CategoryList.vue`、`src/views/TagList.vue`，套用 PageContainer 与统一表格/分页视觉
  - [x] SubTask 6.3: 修改 `src/views/CommentList.vue`、`src/views/MessageList.vue`、`src/views/LinkList.vue`，套用 PageContainer 与统一视觉

- [x] Task 7: 优化表单/编辑页视觉
  - [x] SubTask 7.1: 修改 `src/views/Settings.vue`、`src/views/ResumeEdit.vue`、`src/views/ArticleEdit.vue`，套用 PageContainer 与现代卡片/表单样式

- [x] Task 8: 整体走查与微调
  - [x] SubTask 8.1: 启动 dev server，逐页检查视觉一致性、响应式、动效，修复细节

# Task Dependencies
- Task 2 依赖 Task 1（令牌先就位）
- Task 3、Task 4、Task 6、Task 7 依赖 Task 1 与 Task 5
- Task 8 依赖 Task 2 ~ Task 7 全部完成
