# Tasks
- [x] Task 1: 新建 ResumePreview.vue 组件：接收 form 数据对象与 style prop，解析 JSON 字段（skills/workExperience/projects/education/certificates），实现 4 种风格（modern/classic/sidebar/bold）的模板与样式
  - [x] SubTask 1.1: 组件骨架 + props/computed 解析
  - [x] SubTask 1.2: 现代极简 + 经典衬线（单栏类）
  - [x] SubTask 1.3: 双栏侧边栏 + 粗体页眉
- [x] Task 2: ProfileResume.vue 集成：顶部加「预览简历」按钮，el-drawer 全屏打开，内部放风格切换 + ResumePreview，传当前 form 与列表数据
- [x] Task 3: 验证：空字段容错、四种风格切换正常、构建通过（vite build 成功）
