# 修复在线考试“所有题目共享同一个答案”Bug 的实施计划

## Summary
前台在线考试答题页（blog-frontend `ExamTaking.vue`）中，所有题目的答案互相串用：选择题选完后简答题被预填同样内容、答题卡两题同时显示已答、交卷后答案全部丢失（0 分）。根因是后端试卷题目接口返回的题目字段为 `id`，而前端全部使用 `questionId` 读写答案，导致所有题共用 `answers[undefined]` 同一个 key。用户已确认：**在后端改字段名**，将 `ExamPortalQuestionDTO.id` 改名为 `questionId`，与交卷 DTO、成绩详情 DTO 命名对齐。

## Current State Analysis（现状与根因）

### 数据链路
1. 加载试卷：`GET /portal/exam/papers/{id}` → [ExamPortalQuestionDTO.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/dto/ExamPortalQuestionDTO.java)（字段 `id`，无 `questionId`）
2. 前台答题：[ExamTaking.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/ExamTaking.vue#L238-L253) 以 `q.questionId` 为 key 读写 `answers`（`setSingle`/`setBlank`/`setText`/`toggleMulti`），答题卡 `isAnswered(q)`、交卷 payload 也用 `questionId`
3. 交卷：`POST /papers/{id}/submit` → [ExamAnswerDTO.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/dto/ExamAnswerDTO.java)（字段 `questionId`）→ [ExamServiceImpl.submitPaper](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java#L86) `filter(a -> a.getQuestionId() != null)` 过滤
4. 成绩详情：[ExamRecordDetailItemDTO.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/dto/ExamRecordDetailItemDTO.java#L13)（字段 `questionId`）

### 根因链
- 后端返回 `{"id": 123, ...}`，前端取 `q.questionId` → `undefined`
- `answers.value[undefined]` 所有题共用同一 key：
  - 第 1 题（单选）选 A → `answers[undefined] = ['A']`
  - 切到第 2 题（简答）`textAnswer = answers[undefined].join('\n')` = `"A"` → 简答题被预填 "A"（即用户截图所见“简答题填写了答案”的假象来源之一，且答案互串）
  - 答题卡 `isAnswered` 查同一 key → 两题同时“已答”
- 交卷 payload `questionId: undefined` 被 JSON.stringify 丢弃 → 后端 `getQuestionId() == null` 全量过滤 → **答案全丢、0 分**

### 附带说明（不改，仅记录）
- `loadPaper` 中 `if (res.data?.title)` 针对数组取属性恒为 undefined，本就不生效（试卷标题来自路由 query），不在本次修复范围。

## Proposed Changes

### 1. blog-backend：DTO 字段改名（核心修复）
文件：[ExamPortalQuestionDTO.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/dto/ExamPortalQuestionDTO.java)
- 字段 `private Long id;` → `private Long questionId;`
- 静态工厂 `of(Long id, ...)` 的参数与 `dto.setId(...)` → 改为 `questionId` / `dto.setQuestionId(...)`
- 类注释不变，字段注释改为“题目ID”

文件：[ExamPaperServiceImpl.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamPaperServiceImpl.java#L184)（portalDetail 内 `ExamPortalQuestionDTO.of(q.getId(), ...)` 调用处）
- 调用处参数不变（位置参数，仍传 `q.getId()`），无需修改——仅当工厂参数名变化不影响编译。**确认：无需改动此文件。**

### 2. blog-frontend：无需改动
- `ExamTaking.vue` 已全部使用 `questionId`，后端改名后自动对齐生效。
- 检查前台其他引用 `/portal/exam/papers/{id}` 返回结构的页面：仅 `ExamTaking.vue` 使用（`Exam.vue` 列表用 `row.id`，是 `ExamPaper` 实体的 id，不受影响；`Scores.vue` 用 detail items 的 `questionId`，本就是 `questionId`）。

### 3. 管理端 blog-admin：无需改动
- 管理端不调用 `/portal/exam/papers/{id}`（其试卷管理用 `/admin/exam/...`），无引用 `ExamPortalQuestionDTO`。

## Assumptions & Decisions
- 用户已确认选择“后端改字段名”方案。
- Jackson 默认命名策略（未配置 property-naming-strategy，已验证配置文件无相关配置），`questionId` 序列化为 `questionId`，前端直接可用。
- 不做任何兼容旧字段 `id` 的保留（前台仅 ExamTaking.vue 消费该接口，无第三方依赖）。
- 前端 `v-for :key="q.questionId"` 在修复后变为真实唯一 id，列表渲染更稳定。
- 修复需重启后端服务生效。

## Verification steps
1. 编译验证：`cd blog-backend && mvn -q compile`（或 IDE 编译）确认无编译错误。
2. 功能验证（需后端运行 + 前台 `npm run dev`）：
   - 进入在线考试 → 开始考试 → 试卷第 1 题选择题选某项 → 切到第 2 题简答题：**简答题应为空**（不再预填 "A"）
   - 左侧答题卡：仅第 1 题显示已答，第 2 题未答
   - 简答题输入内容 → 两题分别独立记忆答案，来回切换不串
   - 交卷 → 成绩查询详情：两题的“我的答案”正确回显、得分正常（非全错 0 分）
3. 回归：判断/填空/编程题各答一题，确认读写正常；答题卡计数正确。
