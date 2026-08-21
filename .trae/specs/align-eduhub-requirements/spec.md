# Align EduHub-Plus Requirements Spec

## Why
《多功能个人站需求文档》（EduHub-Plus v2.0）要求五大模块全量交付。前期重构（refactor-to-multi-function-site）已覆盖大部分功能，但经差距分析仍有 6 项未达标：面试题缺解题思路字段、试卷缺及格线、答卷缺作弊标记、题库缺 Excel 批量导入、编程题缺 Monaco Editor、简历为单表且无用户中心编辑与个人公开页、音乐缺歌词。

## What Changes
- **面试模块**：`interview_question` 增加 `tips`（解题思路/拓展）字段；实体、SQL、管理端表单、门户详情展示同步支持
- **考试模块-数据**：`exam_paper` 增加 `pass_score`（及格线）；`exam_record` 增加 `cheat_flag`（作弊标记），交卷时服务端根据 switchCount 超限（≥3 次）置位
- **考试模块-导入**：题库增加 `.xlsx` 批量导入（Apache POI），后端 `POST /admin/exam/questions/import` + 模板下载；管理端题库页增加"导入"按钮与上传对话框
- **考试模块-前端**：编程题答题从 textarea 升级为 Monaco Editor（vue 端集成 `@guolao/vue-monaco-editor` 或 monaco-editor），支持 Java/Python/JavaScript 高亮；成绩页显示及格/不及格与作弊标记
- **简历模块**：`resume_info` 保持单表（现结构 JSON 字段已含工作/教育/技能，重构多表收益低、风险高，**不做多表重构**），但补齐：门户用户中心增加简历编辑入口（复用现有编辑组件能力）、公开简历页支持 `/resume/:userId` 动态路由（管理端保留站长简历 `/resume`）
- **音乐模块**：`music_song` 增加 `lyric`（LRC 文本）字段；管理端表单支持填写；门户播放器/音乐页支持歌词滚动展示
- **SQL**：新增 `04-requirement-gap.sql` 增量脚本（ALTER TABLE + 示例数据补充）
- **执行方式**：AI 不执行 mvn/npm 命令；增量 SQL 与构建验证（mvn compile、npm run build）由用户手动执行，AI 仅做静态诊断检查

## Impact
- Affected specs: refactor-to-multi-function-site（考试/面试/简历/音乐模块增强）
- Affected code:
  - 后端：`InterviewQuestion.java`、`ExamPaper.java`、`ExamRecord.java`、`MusicSong.java`、`ResumeInfo.java`（userId 支持查询）、`ExamServiceImpl`（交卷作弊判定）、`AdminExamQuestionController`（导入）、`PortalResumeController`、`pom.xml`（POI 依赖）
  - 前端门户：`ExamTaking.vue`（Monaco）、`ExamResult.vue`（及格线/作弊标记）、`Interview.vue`（tips 展示）、`Music.vue`/`PlayerBar.vue`（歌词）、`Resume.vue` + 新增 `UserProfile.vue`（简历编辑）、`router/index.js`
  - 管理端：`ExamQuestionList.vue`（导入）、`InterviewQuestionList.vue`（tips）、`MusicManage.vue`（lyric）、`ExamPaperList.vue`（及格线）
  - 数据库：`sql/04-requirement-gap.sql`

## ADDED Requirements

### Requirement: 面试题解题思路
面试题 SHALL 支持可选 `tips` 字段（解题思路/拓展），管理端可编辑，门户详情展开时与答案一同展示。

#### Scenario: 查看面试题详情
- **WHEN** 用户在刷题页点击"显示答案"
- **THEN** 展示参考答案，若该题有 tips 则继续展示"解题思路"区块

### Requirement: 试卷及格线
试卷 SHALL 支持 `passScore` 字段，组卷时设置，成绩页据此展示"及格/不及格"。

#### Scenario: 查看成绩
- **WHEN** 考生查看已发布成绩且总分 ≥ 及格线
- **THEN** 得分卡显示"及格"标识，反之显示"不及格"

### Requirement: 作弊标记
交卷接口 SHALL 在 `switchCount >= 3` 时将答卷 `cheatFlag` 置 1；成绩与阅卷中心显示作弊标记。

#### Scenario: 切屏超限强制交卷
- **WHEN** 前端检测切屏满 3 次触发强制交卷，提交的 switchCount 为 3
- **THEN** 服务端保存答卷时置 cheatFlag=1，成绩页与阅卷中心显示"作弊嫌疑"标记

### Requirement: 题库 Excel 批量导入
管理端题库 SHALL 支持上传 `.xlsx` 批量导入（POI 解析，逐行校验题型/分值合法性，错误行返回明细），并提供模板下载。

#### Scenario: 批量导入成功
- **WHEN** 管理员上传符合模板的 xlsx（含 10 道单选题）
- **THEN** 导入成功提示新增 10 条，题库列表可查

#### Scenario: 导入部分失败
- **WHEN** 上传文件中第 3 行题型非法
- **THEN** 返回错误明细（第 3 行原因），合法行不落库（整体事务回滚或全量校验后拒绝）

### Requirement: 编程题 Monaco Editor
考试答题页编程题 SHALL 使用 Monaco Editor 展示与编辑代码，支持 Java/Python/JavaScript 语法高亮。

#### Scenario: 作答编程题
- **WHEN** 考生切换到编程题
- **THEN** 显示 Monaco 编辑器，代码高亮、可编辑，答案随交卷一并提交

### Requirement: 用户简历编辑与个人公开页
门户用户中心 SHALL 提供简历编辑入口（基本信息/工作/教育/技能动态增删）；公开简历页 SHALL 支持 `/resume/:userId` 访问指定用户简历。

#### Scenario: 编辑简历
- **WHEN** 登录用户在用户中心填写工作经历并保存
- **THEN** 保存成功，访问 `/resume/{自己的id}` 可见最新内容

#### Scenario: 查看他人简历
- **WHEN** 访问 `/resume/123` 且该用户已填写简历
- **THEN** 渲染该用户的简历卡片页；未填写则显示空态提示

### Requirement: 歌词展示
歌曲 SHALL 支持可选 `lyric`（LRC 文本）字段；门户音乐页 SHALL 支持查看歌词、播放时高亮当前行。

#### Scenario: 播放带歌词的歌曲
- **WHEN** 用户播放一首含 LRC 歌词的歌曲
- **THEN** 音乐页展示歌词列表，随播放进度高亮滚动当前行；无歌词则不显示歌词区块

## MODIFIED Requirements

### Requirement: 组卷表单（既有）
组卷抽屉增加"及格线"输入（默认 60，不超过总分），保存写入 passScore。

### Requirement: 音乐管理表单（既有）
上传/编辑歌曲表单增加可选"歌词（LRC）"多行文本框。
