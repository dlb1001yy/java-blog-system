# 修复 blog-frontend 成绩查询页面"答案/正确答案"显示格式不统一

## Summary

`blog-frontend` 成绩查询页面（答题回顾）中，红色区域圈出的"我的答案"与"正确答案"显示格式不统一：

- 单选题：我的答案显示字母 `A`，正确答案却显示数字索引 `0`
- 多选题：正确答案显示 `0、2` 而非 `A、C`
- 判断题：我的答案显示汉字 `对`，正确答案却显示英文 `true`

考生无法对照判断自己选的到底是哪个选项。根因是成绩详情返回的两类答案数据源格式不同，而前端 `formatAnswer` 未按题型做归一化转换。

## Current State Analysis

### 数据流（已探索确认）

1. **考生提交答案**（存入 `exam_record.answers`，[ExamTaking.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/ExamTaking.vue)）：
   - 单选：`["A"]`（字母，`optionKey = String.fromCharCode(65 + i)`，第 229/244 行）
   - 多选：`["A","C"]`（字母数组）
   - 判断：`["对"]` / `["错"]`（汉字，第 105/109 行 `setSingle('对')`）
   - 填空/简答/编程：文本
2. **题库正确答案**（`exam_question.correct`，题库管理端保存格式）：
   - 单选：`"0"`（数字索引裸标量）
   - 多选：`"[0,2]"`（数字索引数组）
   - 判断：`"true"`（布尔裸标量）
   - 填空：文本数组
3. **成绩详情接口**（[ExamServiceImpl.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java#L307-L312)）原样透传：
   - `item.setMyAnswer(toJsonSafe(...))` → `["A"]`
   - `item.setCorrectAnswer(question.getCorrect())` → `"0"`
4. **前端格式化**（[Scores.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/Scores.vue#L317-L328)）：
   ```javascript
   const formatAnswer = (ans) => {
     ...
     const v = JSON.parse(ans)
     if (Array.isArray(v)) return v.join('、')   // ["A"] → "A"；[0,2] → "0、2"
     ...
     return String(v)                            // "0" → 0 → "0"；"true" → true → "true"
   }
   ```
   仅做 JSON 解析与拼接，无题型感知 → 单选/多选/判断两类答案格式不一致。

### 排除项

- 管理端阅卷中心（MarkingCenter.vue）有自己的 `formatObjectiveAnswer` 格式化，不受本问题影响，也不在本次修改范围。

## Proposed Changes

### 修改文件：`blog-frontend/src/views/Scores.vue`（仅 1 处函数 + 2 处模板调用）

**1. 重写 `formatAnswer`（第 317-328 行），增加题型感知的单值转换 `toDisplayValue`：**

```javascript
// 单值 → 展示文本：单选/多选数字索引转字母，判断转对/错
const toDisplayValue = (v, type) => {
  if (v === null || v === undefined) return ''
  if ((type === 1 || type === 2) && typeof v === 'number' && Number.isInteger(v) && v >= 0 && v < 26) {
    return String.fromCharCode(65 + v)
  }
  if (type === 3) {
    if (typeof v === 'boolean') return v ? '对' : '错'
    if (typeof v === 'number') return v !== 0 ? '对' : '错'
    if (typeof v === 'string') {
      const t = v.trim().toLowerCase()
      if (['对', '正确', '√', '是', 'true', 't', 'yes', 'y', '1'].includes(t)) return '对'
      if (['错', '错误', '×', 'x', '否', 'false', 'f', 'no', 'n', '0'].includes(t)) return '错'
    }
  }
  if (typeof v === 'object') return JSON.stringify(v)
  return String(v)
}

// 答案格式化：后端为 JSON 字符串（字符串或数组）
const formatAnswer = (ans, type) => {
  if (ans === null || ans === undefined || ans === '') return ''
  if (typeof ans !== 'string') return toDisplayValue(ans, type)
  try {
    const v = JSON.parse(ans)
    if (Array.isArray(v)) return v.map(x => toDisplayValue(x, type)).filter(Boolean).join('、')
    return toDisplayValue(v, type)
  } catch (_) {
    return toDisplayValue(ans, type)
  }
}
```

**2. 模板调用处传入题型（2 处）：**

- 第 179 行：`formatAnswer(item.myAnswer)` → `formatAnswer(item.myAnswer, item.type)`
- 第 183 行：`formatAnswer(item.type <= 4 ? item.correctAnswer : item.referenceAnswer)` → 同样追加 `, item.type`

### 转换效果对照

| 题型 | 原显示（我的/正确） | 修复后 |
|---|---|---|
| 单选 | `A` / `0` | `A` / `A` |
| 多选 | `A、C` / `0、2` | `A、C` / `A、C` |
| 判断 | `对` / `true` | `对` / `对` |
| 填空/简答/编程 | 文本原样 | 文本原样（type 4/5/6 不转换） |

### 为什么在前端修而不在后端修

- `correctAnswer` 字段在 DTO（ExamRecordDetailItemDTO）中语义为"题库 correct JSON 原文"，阅卷中心等消费方可能依赖原始格式；前端 Scores.vue 已有 `formatAnswer` 展示层格式化职责，在此归一化影响面最小（仅成绩查询页面）。
- 考生答案本身已是字母/汉字格式，只需把题库数字索引/布尔转换为同一展示口径。

## Assumptions & Decisions

- 题库单选/多选 correct 为数字索引（0-based，与选项下标对应），判断为布尔/汉字语义——与后端判分 `normalizeToInt`/`toBoolean`（ExamJudgeAsyncService，Bug 5 修复成果）的容错口径一致；
- 考生多选答案不额外排序（保持提交顺序），最小改动；
- 不改后端、不改 ExamTaking、不改阅卷中心。

## Verification

1. 刷新 `blog-frontend` 成绩查询页，进入一条已判分记录的详情；
2. 单选题："我的答案"与"正确答案"同为字母（如 `A` / `A`），不再出现数字 `0`；
3. 多选题：正确答案显示字母组合（如 `A、C`）；
4. 判断题：两侧同为 `对`/`错`，不再出现 `true`；
5. 填空/简答/编程题文本答案显示不受影响；未作答仍显示"未作答"。
