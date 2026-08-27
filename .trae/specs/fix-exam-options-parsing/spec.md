# 修复前台考试选择题选项展示错误 Spec

## Why
后端 `/portal/exam/papers/{id}` 返回的 `options` 字段是 JSON 字符串（如 `["final","const","static","define"]`），前台 `ExamTaking.vue` 未做解析直接用 `v-for` 遍历该字符串，JavaScript 对字符串逐字符迭代，导致单选题渲染出几十个碎片"选项"（`A. = "`、`B. = [`…），考生无法正常作答。管理端（blog-admin）已通过 `parseJsonSafe` 正确解析并展示 A/B/C/D 四个选项，可作为对照基准。

## What Changes
- blog-frontend `ExamTaking.vue`：在 `loadPaper` 中对每道题的 `options` 字段做安全 JSON 解析（`JSON.parse` + try/catch），解析结果非数组或失败时回退为空数组
- 单选题（type=1）/多选题（type=2）基于解析后的数组渲染，与管理端展示一致（A/B/C/D 四个选项）
- 不改动后端接口与管理端代码

## Impact
- Affected code: `blog-frontend/src/views/ExamTaking.vue`（仅 `loadPaper` 方法）
- 后端返回格式不变；管理端已正常，无需改动；`Scores.vue` 成绩详情不展示选项列表，不受影响

## ADDED Requirements
### Requirement: 前台考试选项解析
前台考试答题页在加载试卷后，系统 SHALL 将每道题的 `options` JSON 字符串解析为数组供单选/多选题渲染。

#### Scenario: 正常解析展示
- **WHEN** 用户进入考试答题页且当前题为单选/多选题
- **THEN** 按 A/B/C/D 顺序展示选项文本，内容与管理端题库中维护的选项一致

#### Scenario: 容错处理
- **WHEN** 某题 `options` 为空、非合法 JSON 或解析结果不是数组
- **THEN** 该题选项区渲染为空，页面不抛错、不影响其他题目展示

#### Scenario: 其他题型不受影响
- **WHEN** 当前题为判断/填空/简答/编程题
- **THEN** 各题型渲染逻辑与修复前一致
