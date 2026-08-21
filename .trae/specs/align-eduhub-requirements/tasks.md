# Tasks
- [x] Task 1: 数据库增量脚本：新增 `sql/04-requirement-gap.sql`（interview_question+tips、exam_paper+pass_score、exam_record+cheat_flag、music_song+lyric），并在本地库执行验证
- [x] Task 2: 后端实体与字段透传：4 个实体加字段；管理端实体直传自动生效；Portal 面试题详情/试卷/音乐均实体透传返回新字段
- [x] Task 3: 考试后端逻辑：交卷时 `switchCount >= 3` 置 `cheatFlag=1`；成绩 DTO 增加 cheatFlag/passScore；阅卷列表 SELECT r.* 自动带出
- [x] Task 4: 题库 Excel 导入：pom 引入 POI 5.2.5；`POST /admin/exam-questions/import` 与 `GET /admin/exam-questions/template`；逐行校验+错误行号明细，全量校验通过才落库
- [x] Task 5: 管理端适配：ExamQuestionList（导入+模板+错误明细）、ExamPaperList（及格线）、InterviewQuestionList（tips）、MusicManage（lyric）、MarkingCenter（作弊标记）
- [x] Task 6: 门户考试页：ExamTaking 编程题 Monaco Editor（CDN 加载+15s 超时回退 textarea，JS/Java/Python 切换）；Scores.vue 展示及格/不及格与作弊标记
- [x] Task 7: 门户面试/音乐页：Interview 展开显示 tips；Music 歌词面板（LRC 解析、当前行高亮平滑滚动、点击 seek）
- [x] Task 8: 简历改造：PortalResumeController 增加 /{userId} 与 /mine 接口；sql/05-resume-multi-user.sql（resume_info+user_id）；门户 ProfileResume 编辑页 + /resume/:userId 动态路由 + 导航"我的简历"入口
- [x] Task 9: 静态验证：8 项检查全部通过；修复 ResumeInfoService 接口缺失 getUserByUsername 声明的编译错误。构建命令由用户手动执行

# Task Dependencies
- Task 2 depends on Task 1
- Task 3, 4 depend on Task 2（实体字段）
- Task 5, 6, 7, 8 depend on Task 2/3/4（各自对应后端）
- Task 9 depends on all
