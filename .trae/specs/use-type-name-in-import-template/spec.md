# 题目导入模板题型使用名称 Spec

## Why
题目导入模板中"题型"列下拉为纯数字 1-6，用户不直观。应直接显示题型名称（单选题/多选题/判断题/填空题/简答题/编程题），导入时由后端将名称解析为对应数字。同时需排查批量导入链路上所有受题型值影响的逻辑并同步兼容。

## What Changes
- 模板下载接口（`GET /admin/exam-questions/template`）：
  - "题型"列下拉选项改为名称：`单选题, 多选题, 判断题, 填空题, 简答题, 编程题`（与前端 typeMap 名称一致）
  - 表头说明改为"题型(单选题/多选题/判断题/填空题/简答题/编程题)"
  - 示例行题型值由 "1"/"5" 改为 "单选题"/"简答题"
- 后端导入解析（`ExamQuestionServiceImpl.importFromExcel` / `validateRow`）：
  - 新增题型名称→数字映射（单选题→1、多选题→2、判断题→3、填空题→4、简答题→5、编程题→6），兼容不带"题"字简写（单选/多选/判断/填空/简答/编程）与原数字 1-6
  - 解析结果用于 `q.setType`，避免重复 parseInt（名称会抛异常）
  - 错误提示更新为"题型必须为：单选题/多选题/判断题/填空题/简答题/编程题（或 1-6）"
- 批量导入链路影响面排查：题型仅出现在模板与导入解析（validateRow/setType），其余（难度、分类、JSON 校验）不受影响，无需改动。

## Impact
- Affected code:
  - `blog-backend/.../controller/admin/AdminExamQuestionController.java`
  - `blog-backend/.../service/impl/ExamQuestionServiceImpl.java`
- 修改 spec：add-template-type-difficulty-dropdown（题型下拉值由数字改为名称）

## MODIFIED Requirements
### Requirement: 模板题型/难度列为下拉选项
系统 SHALL 在题目导入模板中为"题型"列提供以题型名称为选项的下拉（单选题/多选题/判断题/填空题/简答题/编程题），"难度"列下拉为 简单/中等/困难。

#### Scenario: 下载模板
- **WHEN** 用户下载题目导入模板
- **THEN** "题型"列下拉选项为题型名称，示例行题型值为名称

#### Scenario: 导入按名称解析题型
- **WHEN** 导入文件某行题型为"单选题"（或数字 1，或简写"单选"）
- **THEN** 该行题型正确解析为 1 并正常入库
- **WHEN** 题型为无法识别的名称/数字
- **THEN** 该行报错"题型必须为：单选题/多选题/判断题/填空题/简答题/编程题（或 1-6）"且不落库
