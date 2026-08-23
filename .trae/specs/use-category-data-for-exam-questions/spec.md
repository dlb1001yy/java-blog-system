# 题库管理分类对接分类管理数据 Spec

## Why
当前题库管理（ExamQuestionList）中的"分类"字段为自由文本输入（含表单、试卷筛选、Excel 模板与批量导入），与分类管理（CategoryList）维护的分类数据脱节，导致分类命名不一致。应统一使用分类管理的数据源。

## What Changes
- 题库管理新增/编辑题目表单中，"分类"由文本输入改为下拉选择（数据来源于 `GET /admin/categories`）。
- 保存题目时，将所选分类的 `name` 写入题目的 `category` 字段（后端字段不变，仍为字符串）。
- 试卷管理（ExamPaperList）题目筛选中的"分类"输入框同步改为可清空下拉选择。
- 后端模板下载接口（`GET /admin/exam-questions/template`）在"分类"列增加 Excel 数据验证（下拉列表），选项为分类管理中的全部分类名称，示例行的分类值改为有效分类。
- 后端批量导入接口（`POST /admin/exam-questions/import`）校验分类必填且必须存在于分类管理数据中，不匹配的行返回明确错误提示。
- 表单校验规则由"请输入分类"（blur）调整为"请选择分类"（change）。

## Impact
- Affected code:
  - `blog-admin/src/views/ExamQuestionList.vue`（表单分类下拉）
  - `blog-admin/src/views/ExamPaperList.vue`（筛选分类下拉）
  - `blog-backend/.../controller/admin/AdminExamQuestionController.java`（模板分类列数据验证）
  - `blog-backend/.../service/impl/ExamQuestionServiceImpl.java`（导入分类校验）
- 数据兼容：已存在的历史题目 category 值不受影响，仅新增/导入时约束。

## ADDED Requirements
### Requirement: 题目分类使用分类管理数据
系统 SHALL 在题库管理的题目表单中以下拉选择方式展示分类管理维护的全部分类，并在提交时使用所选分类名称。

#### Scenario: 新增/编辑题目选择分类
- **WHEN** 用户打开新增/编辑题目弹窗
- **THEN** "分类"字段为下拉框，选项来自 `GET /admin/categories` 返回的分类列表
- **WHEN** 用户未选择分类提交
- **THEN** 表单校验提示"请选择分类"

#### Scenario: 试卷组卷筛选题目按分类
- **WHEN** 用户在试卷管理的题目筛选中使用分类条件
- **THEN** 分类条件为可清空下拉选择，选项来自分类管理数据

### Requirement: 导入模板分类列受分类数据约束
系统 SHALL 在下载的题目导入模板中，为"分类"列提供以下拉形式展示的全部分类名称（Excel 数据验证）。

#### Scenario: 下载模板
- **WHEN** 用户下载题目导入模板
- **THEN** 模板"分类"列单元格带下拉选项，选项为分类管理的全部分类名称

### Requirement: 批量导入校验分类有效性
系统 SHALL 在批量导入时校验每行分类非空且存在于分类管理数据中。

#### Scenario: 导入分类无效
- **WHEN** 导入文件中某行分类为空或不在分类管理列表中
- **THEN** 该行导入失败并返回错误明细（如"第N行: 分类必须为分类管理中已有的分类"）
