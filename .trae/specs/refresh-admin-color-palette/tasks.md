# Tasks

- [x] Task 1: 更新设计令牌文件配色
  - [x] SubTask 1.1: 修改 `src/assets/styles/tokens.css`，将主色/渐变/中性色/阴影/Element Plus 覆盖全部从靛蓝切换到深森林绿+暖石色（按 spec 中的色值映射表）
- [x] Task 2: 清除全局样式中的硬编码靛蓝色
  - [x] SubTask 2.1: 修改 `src/assets/styles/global.css`，将滚动条的 `rgba(99, 102, 241, 0.2/0.4)` 替换为 `rgba(5, 150, 105, 0.2/0.4)`
- [x] Task 3: 清除布局组件中的硬编码靛蓝色
  - [x] SubTask 3.1: 修改 `src/layout/index.vue`，将主内容区渐变光斑的 `rgba(99, 102, 241, 0.06)` 和 `rgba(139, 92, 246, 0.05)` 替换为 `rgba(5, 150, 105, 0.06)` 和 `rgba(20, 184, 166, 0.05)`
  - [x] SubTask 3.2: 修改 `src/layout/Header.vue`，将头像描边 `rgba(99, 102, 241, 0.3)` 替换为 `rgba(5, 150, 105, 0.3)`
- [x] Task 4: 清除登录页中的硬编码靛蓝色
  - [x] SubTask 4.1: 修改 `src/views/Login.vue`，将品牌区渐变（#4F46E5/#6366F1/#8B5CF6）、输入框聚焦阴影、按钮阴影中的硬编码色值替换为森林绿色值
- [x] Task 5: 清除 Dashboard 中的硬编码靛蓝色
  - [x] SubTask 5.1: 修改 `src/views/Dashboard.vue`，将 ECharts 图表色值（#6366F1/#818CF8/rgba(99,102,241,...)）和统计卡渐变（#6366F1→#818CF8 等）替换为森林绿色值；将中性灰色值（#94A3B8/#F1F5F9/#dcdfe6）替换为暖石色对应值
- [x] Task 6: 清除表单页中的硬编码靛蓝色
  - [x] SubTask 6.1: 修改 `src/views/ArticleEdit.vue`、`src/views/ResumeEdit.vue`、`src/views/Settings.vue`，将输入框聚焦阴影 `rgba(99, 102, 241, 0.1)` 替换为 `rgba(5, 150, 105, 0.1)`
- [x] Task 7: 全局验证无靛蓝色残留
  - [x] SubTask 7.1: 在 `src/` 目录搜索 `6366F1`、`818CF8`、`4F46E5`、`8B5CF6`、`rgba(99, 102, 241`、`rgba(139, 92, 246` 确认零结果

# Task Dependencies
- Task 2、3、4、5、6 可与 Task 1 并行（彼此无依赖）
- Task 7 依赖 Task 1 ~ Task 6 全部完成
