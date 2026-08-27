# 修复阅卷中心：未知考生 + 考题显示不完整

## 摘要

`blog-admin` 阅卷中心（MarkingCenter.vue）存在两个显示 Bug：

1. **未知考生**：待批列表考生名显示"未知考生"，应显示考生名字。
2. **考题不完整**：右侧批改面板只显示简答/编程题，选择题等客观题不显示。

## 现状分析

### 问题一根因（后端）

- `blog-backend/src/main/resources/mapper/ExamRecordMapper.xml` 第 10 行：
  ```sql
  u.nickname  AS user_name
  ```
  只取 `sys_user.nickname`，无回退。
- `AuthController.register()`（公开注册）创建用户时**不设置 nickname**（仅 username/email/password/role/status/failCount），自注册考生 `nickname` 为 NULL。
- 前端 `MarkingCenter.vue:23`：`{{ row.userName || '未知考生' }}` → 昵称为空即显示兜底文案。

### 问题二根因（前端过滤）

- `MarkingCenter.vue:139-141`：
  ```js
  const subjectiveItems = computed(() =>
    (detail.value?.items || []).filter(item => item.type === 5 || item.type === 6)
  )
  ```
  右侧渲染循环只遍历 `subjectiveItems`，客观题（type 1 单选/2 多选/3 判断/4 填空）被过滤。
- 后端 `ExamServiceImpl.buildDetail(recordId, true)` 已返回**全部题型** items（`ExamRecordDetailItemDTO` 含 `options`、`myAnswer`、`correctAnswer`、`correct`、`gotScore`、`referenceAnswer` 等），数据完整，纯前端展示问题。

### 附带发现（同页面显示缺陷）

- `MarkingCenter.vue:67`：`v-if="item.category"` / `{{ item.category }}`，但后端字段为 `categoryName` → 题目分类永不显示。
- 待批卡片 `submitTime` 原样渲染 ISO 字符串（如 `2026-08-27T15:36:38`）。

## 修改方案

### 1. 后端：`blog-backend/src/main/resources/mapper/ExamRecordMapper.xml`（未知考生根因）

将第 10 行：

```sql
u.nickname  AS user_name,
```

改为：

```sql
COALESCE(NULLIF(u.nickname, ''), u.username) AS user_name,
```

昵称为空字符串或 NULL 时回退到用户名。搜索条件（nickname/username LIKE）无需改动。右侧信息栏的 `selectedRecord?.userName` 取自待批列表行数据，随之修复。

### 2. 前端：`blog-admin/src/views/MarkingCenter.vue`（考题完整展示 + 小修）

**a. 新增计算属性与工具函数**

- `allItems`：`(detail.value?.items || [])` 全量题目（渲染循环改用它）。
- `TYPE_MAP` + `typeLabel(t)`：`{1:'单选题',2:'多选题',3:'判断题',4:'填空题',5:'简答题',6:'编程题'}`（参考 `blog-frontend/src/views/Scores.vue:209`）。
- `parseOptions(opt)`：JSON 字符串安全解析为数组（参考 `blog-frontend/src/views/ExamTaking.vue` 的 parseOptions / 管理端 parseJsonSafe 模式）。
- `formatTime(t)`：`String(t).replace('T',' ').slice(0, 19)`（参考 Scores.vue:314）。

**b. 渲染循环改为遍历 `allItems`，按题型分两种卡片**

- **客观题（type ≤ 4）— 只读参考卡片**：
  - 头部：题型标签（el-tag，按题型区分颜色）、`categoryName`、`gotScore / score 分`、正误标签（`item.correct` → 正确/错误 el-tag success/danger）。
  - 题干 `<pre>`（沿用现有样式）。
  - 选择题（type 1/2）：解析 `item.options` 渲染选项列表（字母 A/B/C/D 前缀 = 下标），高亮考生所选项与正确项（基于 `myAnswer`/`correctAnswer` 解析出的字母集合；判断/填空不高亮）。
  - "考生答案"与"正确答案"块（沿用现有 `answer-block` 样式与 `formatAnswer`，正确答案块用 `answer-block--ref` 绿色样式）。
  - **不含评分表单**（客观题已自动判分，不改分）。
- **主观题（type 5/6）— 保持现有评分卡片**：题型标签、题干、考生答案、参考答案、评分 `el-input-number` + 评语 `el-input`（绑定 `markForms[item.questionId]`，逻辑不变）。

**c. 逻辑保持不变的部分**

- `subjectiveItems` 保留：评分表单构建（`handleSelect` 中的 markForms 回填、作弊默认零分）、未评分校验（`handleSave`）、保存提交（`doSave` 只提交主观题）均继续基于它，与后端 `saveMarking` 只处理 `exam_marking` 主观题记录的语义一致。

**d. 小修**

- `item.category` → `item.categoryName`。
- 待批卡片 `{{ row.submitTime }}` → `{{ formatTime(row.submitTime) }}`。

## 假设与决策

- 客观题已由 `ExamJudgeAsyncService` 自动判分，阅卷人侧只读参考，不提供改分入口（后端 saveMarking 无需改动）。
- 考生名回退策略：nickname 为空用 username 兜底，不修改注册逻辑、不回填存量数据。
- 选项高亮仅对单选/多选生效（答案为字母）；判断题答案为"对/错"文本、填空为文本，直接在"考生答案/正确答案"块展示不高亮。
- `detail` 接口数据已完整，后端仅需改 SQL 一行；其余全部为前端展示改动。

## 验证步骤

1. 后端编译：
   ```powershell
   $env:JAVA_HOME='C:\Users\dlb\.jdks\graalvm-jdk-21.0.7'; mvn -q compile -DskipTests
   ```
   （在 `blog-backend` 目录；系统默认 JDK 为 1.8，必须显式指定。）
2. 重启后端服务使 SQL 生效。
3. 打开 `blog-admin` → 阅卷中心：
   - 左侧待批卡片显示考生用户名/昵称，不再显示"未知考生"；
   - 交卷时间显示为 `YYYY-MM-DD HH:mm:ss` 格式。
4. 点击待批答卷：
   - 右侧显示全部题目：客观题只读卡片（题干、选项、考生答案、正确答案、正误、得分），主观题评分卡片（评分/评语）；
   - 题目分类正常显示。
5. 功能回归：对主观题打分 → 保存草稿 → 提交评分，成绩正常发布（仅主观题参与人工评分，客观题得分不变）。
