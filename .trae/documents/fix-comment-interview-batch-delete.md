# 修复评论管理与面试题管理批量删除问题

## Summary
评论管理批量删除/批量审核因前后端请求体格式不匹配而失败；面试题管理代码链路正确（若仍异常，应为旧构建/未重启，重新构建即可）。

## Current State Analysis
1. **评论管理（真正 Bug）**
   - 前端 `blog-admin/src/api/comment.js`：
     - `batchApprove`: `PUT /admin/comments/batch-approve`，请求体 `{ ids }`
     - `batchDelete`: `DELETE /admin/comments/batch`，请求体 `{ data: { ids } }`
   - 后端 `AdminCommentController.java`：
     - `batchApprove(@RequestBody List<Long> ids)`（第 60 行）——期望**裸数组** `[1,2]`，与前端 `{ids}` 不匹配 → 400
     - `batchDelete(@RequestBody List<Long> ids)`（第 85 行）——同样不匹配 → 400
   - 项目其余 10 个批量接口统一使用 `BatchIds` DTO（`{ ids: [...] }`），仅 Comment 控制器用 `List<Long>`，属遗留不一致。

2. **面试题管理（无代码 Bug）**
   - 前端 `InterviewQuestionList.vue`：selection 列 + `@selection-change` 已于上一轮补齐（第 48-49 行），JS 逻辑完整
   - 前端 `interviewQuestion.js#batchDelete` 发送 `{ data: { ids } }`；后端 `AdminInterviewQuestionController#batchDelete` 接收 `BatchIds` —— 格式一致，链路正确
   - 若用户仍见问题，大概率是旧前端构建/浏览器缓存，重建即可

## Proposed Changes
1. **后端 `blog-backend/.../controller/admin/AdminCommentController.java`**
   - `batchApprove` 参数改为 `@RequestBody BatchIds batchIds`，从 `batchIds.getIds()` 取 id 列表（import `com.dlbyy.blog.dto.BatchIds`）
   - `batchDelete` 同样改为 `BatchIds`，保持空列表防御逻辑
   - 原因：与前端 `{ids}` 请求体及项目其他批量接口统一，改动最小（只动后端 2 个方法）
2. **面试题管理**：无代码改动；验证时提醒用户重新 `npm run build` / 重启后端

## Assumptions & Decisions
- 选择改后端而非改前端：前端 `{ids}` 格式是项目统一约定（article/message 等均如此），后端 Comment 控制器是异类
- 不改动 `batch-approve` 的语义，仅修参数绑定

## Verification
- 检查 `AdminCommentController` 两方法签名均使用 `BatchIds`
- 确认无其他控制器存在 `List<Long>` 裸数组绑定的批量端点（本次已排查：仅 Comment）
- 用户手动执行：后端 `mvn compile` 并重启，前端 `npm run build`；页面实测评论批量删除/批量审核、面试题批量删除
