# 修复面试题导入字段丢失并增加导入预览 Spec

## Why
导入解析函数 `parseQuestionsMd` 中 `_field` 存的是中文标记名（题干/解题思路/参考答案），而对象属性是英文键（title/tips/answer），后续行被追加到不存在的属性上，导致导入后"解题思路"和"参考答案"为空。同时用户需要在导入前完整预览每道题各字段内容以核对正确性。

## What Changes
- 修复 `blog-admin/src/views/InterviewQuestionList.vue` 的 `parseQuestionsMd`：建立中文标记 → 属性键映射（题干→title、技术方向→category、难度→difficulty、标签→tags、解题思路→tips、参考答案→answer），多行追加写入正确属性。
- 导入确认改为预览对话框（el-dialog）：列出解析出的每道题的 题干/技术方向/难度/标签/解题思路（Markdown 渲染）/参考答案（Markdown 渲染），及校验错误明细；用户逐题核对后确认导入。

## Impact
- Affected code: `blog-admin/src/views/InterviewQuestionList.vue`（仅导入相关部分）

## MODIFIED Requirements
### Requirement: 面试题 Markdown 导入
解析结果中 tips/answer SHALL 正确保留 Markdown 原文；导入确认 SHALL 以可滚动的预览对话框逐题展示全部字段内容（思路/答案渲染 Markdown），确认后才提交。
