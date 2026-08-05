# Tasks

- [x] Task 1: 修改工作经历描述显示格式
  - [x] SubTask 1.1: 在 `blog-frontend/src/views/Resume.vue` 第 83 行，把 `<p class="timeline-desc">{{ work.description }}</p>` 改为 `<p class="summary-text" v-html="work.description"></p>`

- [x] Task 2: 修改项目经验描述显示格式
  - [x] SubTask 2.1: 在 `blog-frontend/src/views/Resume.vue` 第 102 行，把 `<p class="project-desc">{{ project.description }}</p>` 改为 `<p class="summary-text" v-html="project.description"></p>`

- [x] Task 3: 验证
  - [x] SubTask 3.1: GetDiagnostics 检查 Resume.vue 无错误
  - [x] SubTask 3.2: 确认自我评价已使用 summary-text + v-html（无需改动）

# Task Dependencies
- Task 1、Task 2 独立，可并行
- Task 3 依赖 Task 1、Task 2 完成
