# 题目导入模板题型与难度下拉化 Spec

## Why
题目导入模板（`GET /admin/exam-questions/template`）中"题型"、"难度"仍需手工填写数字/文本，易出错。分类列已支持下拉（上个变更 use-category-data-for-exam-questions），题型与难度也应改为 Excel 下拉选项，手填非法值时导入报错。

## What Changes
- 模板下载接口中，"题型"列（索引1）增加 Excel 数据验证下拉，选项为：`1单选, 2多选, 3判断, 4填空, 5简答, 6编程`（显示为带说明的文本，导入时解析数字；或直接用数字 1-6，取决于现有解析——现解析按 `Integer.parseInt(typeText)` 处理，需兼容）。
- "难度"列（索引3）增加下拉，选项为：`简单, 中等, 困难`。
- 后端导入校验已有：题型 1-6 整数、难度枚举、分类存在性——保持不变，模板下拉与之一致。

## Impact
- Affected code: `blog-backend/.../controller/admin/AdminExamQuestionController.java`（模板生成）
- 前端无改动。

## ADDED Requirements
### Requirement: 模板题型/难度列为下拉选项
系统 SHALL 在题目导入模板中为"题型"和"难度"列提供 Excel 数据验证下拉。

#### Scenario: 下载模板
- **WHEN** 用户下载题目导入模板
- **THEN** "题型"列单元格下拉可选项为 1-6 对应题型，"难度"列下拉可选项为 简单/中等/困难
- **WHEN** 用户手填非法题型或难度后导入
- **THEN** 该行导入失败并返回现有格式的错误提示
