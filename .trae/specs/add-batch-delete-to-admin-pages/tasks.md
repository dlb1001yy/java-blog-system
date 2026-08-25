# Tasks
- [x] Task 1: 后端新增批量删除接口
  - [x] 1.1 AdminCategoryController、AdminTagController、AdminLinkController 新增 `DELETE /batch`
  - [x] 1.2 AdminInterviewQuestionController、AdminExamQuestionController、AdminExamPaperController 新增 `DELETE /batch`
  - [x] 1.3 AdminMessageController、AdminArticleController 新增 `DELETE /batch`（文章删除需沿用单条删除的清理逻辑，如标签关联/ES）
  - [x] 1.4 AdminMusicController 新增 `DELETE /songs/batch` 与 `DELETE /playlists/batch`（歌单删除需清理歌曲关联）
  - [x] 1.5 补齐 AdminCommentController `DELETE /batch`（前端已调用但后端缺失）
- [x] Task 2: 前端 API 模块新增 batchDelete
  - [x] category.js、tag.js、link.js、interviewQuestion.js、examQuestion.js、examPaper.js、music.js（歌曲/歌单）新增 batchDelete(ids)；校对 article.js/comment.js/message.js 已有方法路径与后端一致
- [x] Task 3: 前端列表页添加批量删除功能
  - [x] CategoryList.vue、TagList.vue、LinkList.vue、MessageList.vue
  - [x] InterviewQuestionList.vue、ExamQuestionList.vue、ExamPaperList.vue
  - [x] MusicManage.vue（歌曲表格 + 歌单表格，已补齐歌单表格 selection 绑定）
  - [x] 每页：selection 列、批量删除按钮（未选中禁用）、ElMessageBox 二次确认、成功提示与刷新
- [ ] Task 4: 验证（不执行 mvn/npm 命令，由用户手动执行）
  - [ ] 提示用户手动执行 `mvn compile` 与 `npm run build` 验证
  - [x] 按 checklist.md 核对全部页面

# Task Dependencies
- Task 2、Task 3 依赖 Task 1 的接口路径确定（可并行开发，路径按 spec 约定）
