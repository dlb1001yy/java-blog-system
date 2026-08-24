# Tasks
- [x] Task 1: 后端批量导入接口
  - [x] 1.1 AdminInterviewQuestionController 新增 POST /admin/interview-questions/import，接收 List<InterviewQuestion>，加 @Admin("批量导入面试题") 权限注解
  - [x] 1.2 校验每条 title/category/difficulty 非空，收集错误（序号+原因）；全部通过则批量 save，返回 {count, errors}
- [x] Task 2: 前端导入功能
  - [x] 2.1 interviewQuestion.js 新增 importQuestions(data)
  - [x] 2.2 InterviewQuestionList.vue 新增"导入面试题"按钮 + 隐藏 file input（accept=".md,.markdown"）
  - [x] 2.3 实现 md 解析函数：按题干标记切分，tips/answer 保留 Markdown 原文；标签去反引号转逗号分隔
  - [x] 2.4 导入前弹窗预览解析数量与错误明细，确认后调用接口，成功刷新列表
  - [x] 2.5 新增"下载模板"按钮：前端 Blob 生成含一道示例题的 .md 并下载
- [x] Task 3: 验证
  - [x] 3.1 用样例文件核对解析规则（发现题干同行内容被丢弃的 bug，已修复并复验）
  - [x] 3.2 后端 mvn compile、前端 npm run build 通过

# Task Dependencies
- Task 1 与 Task 2 可并行；Task 3 依赖两者。
