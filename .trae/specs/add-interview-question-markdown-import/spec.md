# 面试题 Markdown 批量导入 Spec

## Why
管理后台逐条新增面试题效率低，需要支持从 Markdown 文件批量导入（以 `D:\学习资料\study-notes\面试积累\gemini\2026最新的Java程序员面试题.md` 为格式样例），且"解题思路"和"参考答案"的 Markdown 原文必须完整保留。

## 导入文件格式约定
文件由多道题目顺序组成，每道题包含以下字段（以粗体标记开头，字段顺序固定）：

```
**题干**：<多行文本，至下一字段前>
**技术方向**：后端
**难度**：中等
**标签**：`Java` `并发` `JVM`（反引号包裹，空格分隔）
**解题思路**：
<Markdown 原文>
**参考答案**：
<Markdown 原文>
```

## What Changes
- 后端 `AdminInterviewQuestionController` 新增 `POST /admin/interview-questions/import`：接收题目 JSON 数组，批量校验并插入，返回 `{count, errors}`；有错误时不落库该批（全量校验通过才保存）。
- 前端 `blog-admin/src/api/interviewQuestion.js` 新增 `importQuestions(data)`。
- 前端 `blog-admin/src/views/InterviewQuestionList.vue` 新增"导入面试题"按钮：选择 .md 文件 → 前端按上述格式解析（正则切分 `**题干**` 边界，保留 tips/answer 的 Markdown 原文）→ 弹窗预览解析出的题目数量与错误列表 → 确认后调用导入接口 → 成功后刷新列表。
- 前端 `blog-admin/src/views/InterviewQuestionList.vue` 新增"下载导入模板"能力：生成含 1 道示例题的 .md 模板文件下载（纯前端 Blob 下载，无需后端接口）。

## Impact
- Affected code:
  - `blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminInterviewQuestionController.java`
  - `blog-backend/src/main/java/com/dlbyy/blog/service/InterviewQuestionService.java`（或直接在 Controller 批量循环 save，取最小实现）
  - `blog-admin/src/api/interviewQuestion.js`
  - `blog-admin/src/views/InterviewQuestionList.vue`
- 不修改既有新增/编辑/删除逻辑；数据表无需变更。

## ADDED Requirements
### Requirement: Markdown 面试题批量导入
管理端 SHALL 支持上传 .md 文件批量导入面试题，字段按"题干/技术方向/难度/标签/解题思路/参考答案"粗体标记解析，tips 与 answer 保存原始 Markdown 文本。

#### Scenario: 正常导入
- **WHEN** 管理员上传符合格式的 .md 文件并确认
- **THEN** 系统解析出 N 道题，逐条校验必填字段（title/category/difficulty），全部通过则批量入库并提示成功数量

#### Scenario: 解析失败
- **WHEN** 文件格式不符合约定或缺少必填字段
- **THEN** 弹窗展示错误明细（第几题、缺什么字段），不落库

#### Scenario: 模板下载
- **WHEN** 管理员点击"下载模板"
- **THEN** 下载包含一道完整示例题的 .md 文件

### Requirement: Markdown 原文保留
- **WHEN** 导入的解题思路/参考答案含代码块、列表、粗体等 Markdown 语法
- **THEN** 数据库存储与前台展示均保留原始 Markdown 并正常渲染（复用既有渲染链路）
