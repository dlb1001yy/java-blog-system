# 修复阅卷中心「待批 0 题」与「选择题答对却判错 0 分」

## Summary

阅卷中心存在两个 Bug：

1. **待批 0 题**：试卷含主观题，左侧答卷卡片却显示「待批 0 题」——主观题批改草稿（exam_marking）从未生成。
2. **答对判错**：单选题考生答案与正确答案显示都是 A，右上角却显示「错误 / 0 分」。

根因（后端两处）：

- **根因一（异步判分竞态）**：`submitPaper` 带 `@Transactional`，在事务提交前就调用 `@Async` 的 `judge()`；异步线程 `selectById(recordId)` 读不到未提交的答卷 → 打日志「答卷不存在」直接 return → **markings 从未插入、客观分从未更新**。
- **根因二（答案格式不匹配）**：考生端提交的是字母 `'A'` / 判断题 `'对'/'错'`，而 `judgeObjective` 的 `toIndex` 只认数字索引（`Integer.valueOf("A")` 抛 NumberFormatException → null → 判错）；且管理端题库把单选正确答案存为裸标量 `"0"`、判断题存为 `"true"`（非 JSON 数组），`readValue(correctJson, List)` 解析抛异常 → 直接 return false。

## Current State Analysis

### 证据链（已验证）

1. 考生端 [ExamTaking.vue:77](../../../d:/my-project/java-blog-system/blog-frontend/src/views/ExamTaking.vue#L77) `setSingle(optionKey(i))` → `optionKey = i => String.fromCharCode(65+i)` → 单选存 `['A']`，判断题存 `['对']`/`['错']`（第 105/109 行），多选存 `['A','C']`。
2. 管理端题库 [ExamQuestionList.vue:452-457](../../../d:/my-project/java-blog-system/blog-admin/src/views/ExamQuestionList.vue#L452-L457) 保存：单选 `correct = JSON.stringify(form.correctSingle)` → `"0"`（裸数字）；判断 `JSON.stringify(form.correctBool)` → `"true"`（裸布尔）；多选 `[0,2]`、填空 `["文本"]` 为数组。
3. 后端 [ExamJudgeAsyncService.java:114](../../../d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamJudgeAsyncService.java#L114) `readValue(correctJson, new TypeReference<List<Object>>(){})`：`"0"`/`"true"` 非 JSON 数组 → JsonProcessingException → catch 后 return false（判错）。
4. [ExamJudgeAsyncService.java:151-163](../../../d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamJudgeAsyncService.java#L151-L163) `toIndex`：`'A'` → `Integer.valueOf("A")` 抛 NumberFormatException → null → 判错。即使 correct 恰好是数组 `[0]`，考生答案 'A' 也判错。
5. 竞态：[ExamServiceImpl.java:62](../../../d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java#L62) `@Transactional` + 第 128 行事务内调用 `@Async judge()`；MySQL 默认 READ_COMMITTED，异步线程 `selectById` 返回 null → 第 57-60 行 early return → markings（主观题草稿）不生成 → 待批 0 题；`objectiveScore` 保持交卷时写入的 0。
6. 阅卷中心展示「考生答案 A / 正确答案 A」是因为 [MarkingCenter.vue:223-235](../../../d:/my-project/java-blog-system/blog-admin/src/views/MarkingCenter.vue#L223-L235) `formatObjectiveAnswer` 把两侧都归一化为字母显示（`"0"` → 'A'，`'A'` → 'A'），看起来相同；但后端 `buildDetail`（[ExamServiceImpl.java:303](../../../d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java#L303)）实时调 `judgeObjective` 判错 → 显示「错误 / 0 分」。

### 波及范围

- `judgeObjective` 被**异步判分**与**详情组装（buildDetail）**两处复用 → 后端归一化修复后，新交卷的判分与历史答卷的详情页判定同时修正。
- 判断题（type 3）同样是裸布尔 + '对'/'错' 文本，需一并修。

## Proposed Changes

### 文件 1：`blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamJudgeAsyncService.java`

**改动：`judgeObjective` 及辅助方法做归一化容错**（保持方法签名不变，两处调用方无需改动）

1. **correct 解析容错**：先 `readValue(correctJson, Object.class)`；结果若是 `List` 用之，否则（裸数字/布尔/字符串）包装为单元素列表。
2. **新增 `normalizeToInt(Object)`**：Number → intValue；Boolean → 1/0；字符串先尝试数字解析，再尝试单字母 A-Z/a-z → 0-25 索引；失败返回 null。correct 侧与考生侧统一走它。
3. **`toIndex` 增加字母支持**：数字解析失败后，若为单字母则 `Character.toUpperCase(c) - 'A'` 返回索引（多选题 `toIndexSet` 自动受益）。
4. **判断题（type 3）改按布尔语义比较**：新增 `toBoolean(Object)` —— Boolean 直接返回；Number → `!= 0`；字符串映射 `'对'/'正确'/'√'/'是'/'true'/'t'/'yes'/'1'` → true、`'错'/'错误'/'×'/'x'/'否'/'false'/'f'/'no'/'0'` → false、其余 null；correct 取列表首元素、考生答案直接转换，两者非 null 且相等才判对。
5. 单选（type 1）：`normalizeToInt(correct.get(0))` 与 `toIndex(myAnswer)` 比较，仍要求 correct 仅 1 个元素。
6. 多选（type 2）：correct 各元素走 `normalizeToInt`；`mySet` 判空 + 集合相等逻辑保留。
7. 填空（type 4）逻辑保留不变。

### 文件 2：`blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java`

**改动：异步判分改为事务提交后触发，消除竞态**

[第 126-128 行](../../../d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java#L126-L128) 直接调用改为注册 `TransactionSynchronization.afterCommit` 回调，事务提交后再执行 `examJudgeAsyncService.judge(record.getId(), paperId, rawAnswerMap)`：

```java
// 异步判分：客观题判分 + 主观题批改草稿批量插入
// 事务提交后才触发，避免异步线程读不到未提交的答卷记录
Map<Long, Object> rawAnswerMap = answerMap;
Long recordId = record.getId();
TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override
    public void afterCommit() {
        examJudgeAsyncService.judge(recordId, paperId, rawAnswerMap);
    }
});
```

需新增 import：`org.springframework.transaction.support.TransactionSynchronization`、`TransactionSynchronizationManager`。`judge()` 自身带 `@Transactional`，提交后异步开启自身事务，无嵌套问题。

### 不改动的部分

- 考生端 ExamTaking.vue 提交格式（字母）——后端容错后兼容，且历史已存答卷也是字母格式，后端归一化能同时修正其详情页判定。
- 管理端题库保存格式——`"0"`/`"true"` 为存量格式，由判分侧容错消化。
- 阅卷中心/成绩页展示逻辑——`formatObjectiveAnswer` 已能正确显示，判定修正后「错误/0 分」自然变「正确/满分」。
- `selectPendingPage` SQL——本身正确，markings 生成后「待批 N 题」自然恢复。

## Assumptions & Decisions

- 修复放在**后端判分侧**而非前端改提交格式：一处修复同时覆盖异步判分、详情页实时判定、历史存量答卷（答案已按字母格式入库）。
- 交卷时同步写入的 `objectiveScore=0` 占位逻辑保留；提交后异步更新。
- 历史已交、markings 缺失的答卷（如截图这条）：教师仍可在详情页对主观题直接评分保存（saveMarking 不依赖草稿存在），「待批 0 题」对旧记录不再变化；新交卷记录恢复正常。不做存量数据订正脚本（超范围）。
- 判断题对错文案以考生端 `'对'/'错'`、管理端 `true/false` 为准，`toBoolean` 映射表覆盖常见同义词。

## Verification steps

1. 编译验证：`cd blog-backend`，PowerShell 下 `$env:JAVA_HOME='C:\Users\dlb\.jdks\graalvm-jdk-21.0.7'; mvn -q compile -DskipTests`，exit 0。
2. 重启后端，用考生账号（blog-frontend）进入同一份「Java基础考题」：
   - 单选题选 A（正确答案 A）→ 交卷。
   - 简答题/编程题随便作答 → 交卷。
3. 管理端阅卷中心验证：
   - 新答卷卡片显示「待批 N 题」（N=主观题数）而非 0。
   - 打开详情：单选题右上角显示「正确 / 满分分值」；考生答案与正确答案均显示 A。
   - 判断题、多选题各验一题（对/错、AB 多选）确认判分正确。
4. 打开截图中的**旧答卷**详情：单选题现在显示「正确」且得分非 0（buildDetail 实时判定即时生效）；主观题仍可手动评分后发布。
5. 后端日志确认无「异步判分失败：答卷不存在」。
