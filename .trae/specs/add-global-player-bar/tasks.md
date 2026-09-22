# Tasks

- [x] Task 1: player.js 支持收起状态
  - [x] 1.1 `state` 新增 `collapsed` 字段（默认 false），从 PREFS 恢复
  - [x] 1.2 新增并导出 `toggleCollapsed()`，切换后随 PREFS 持久化

- [x] Task 2: PlayerBar 双形态改造
  - [x] 2.1 新增 `hasTabBar`（默认 true）与 `bottom`（可选覆盖）props，动态计算展开态定位
  - [x] 2.2 展开态右侧新增收起按钮（chevron-right 图标），点击调用 `toggleCollapsed()`
  - [x] 2.3 收起态悬浮球：44px 圆形，right:10px / top:45%；有封面显示旋转封面（暂停静止），无封面显示渐变 + music 图标；点击展开

- [x] Task 3: 全局挂载 PlayerBar（15 个页面）
  - [x] 3.1 主包：`pages/article/detail.vue` 挂载（无 TabBar）；`pages/index/index.vue` 适配新组件（保持默认即可）
  - [x] 3.2 subpkg：`mine/index`、`resume/index`（有 TabBar）；`mine/login`、`mine/register`、`readlater/index`、`message/index`、`about/index`（无 TabBar）
  - [x] 3.3 subpkg-article：`list`、`category`、`tags`、`archives`（均无 TabBar）
  - [x] 3.4 subpkg-study：`interview/index`（有 TabBar）、`exam/index`、`scores/index`（无 TabBar）、`exam/taking`（传 `bottom` 避开固定交卷栏）
  - [x] 3.5 各挂载页面底部滚动留白按展开态预留（约 80px + 安全区）

# Task Dependencies
- Task 3 依赖 Task 1、2；Task 1 与 Task 2 可并行
