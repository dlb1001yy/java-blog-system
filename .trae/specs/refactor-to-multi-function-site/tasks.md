# Tasks

## 阶段一：数据库与后端基础

- [x] Task 1: 新增数据库迁移脚本 `03-multi_modules.sql`
  - [x] 1.1 面试题表：interview_question（category/difficulty/title/tags/answer/status）、interview_favorite（user_id/question_id/type 收藏|错题）
  - [x] 1.2 考试表：exam_question（stem/type 6 种/category/difficulty/options JSON/correct JSON/reference_answer/status）、exam_paper（title/duration/total_score/status）+ exam_paper_question 关联、exam_record（user_id/paper_id/answers JSON/objective_score/final_score/switch_count/duration/status）、exam_marking（record_id/question_id/score/comment）
  - [x] 1.3 音乐表：music_song（title/artist/album/duration/cover/file_url/format/size）、music_playlist（name/cover/description/status）+ 关联
  - [x] 1.4 sys_user 补充统计所需字段检查（复用现有字段）

- [x] Task 2: 后端实体/Mapper/Service 骨架（面试刷题 + 考试 + 音乐）
  - [x] 2.1 面试题 InterviewQuestion/InterviewFavorite 实体 + Mapper + Service（分页查询、收藏/错题本）
  - [x] 2.2 考试 ExamQuestion/ExamPaper/ExamPaperQuestion/ExamRecord/ExamMarking 实体 + Mapper + Service
  - [x] 2.3 音乐 MusicSong/MusicPlaylist 实体 + Mapper + Service
  - [x] 2.4 初始化示例数据（SQL 脚本内含示例数据）

- [x] Task 3: 后端考试业务逻辑
  - [x] 3.1 考生端：获取试卷（不含正确答案）、提交答卷、客观题自动判分（单选/多选/判断/填空）、生成 exam_record
  - [x] 3.2 阅卷：待批列表查询、逐题保存评分草稿、提交汇总最终成绩
  - [x] 3.3 成绩：我的成绩列表、成绩详情（题型分布、答题回顾含正确答案与解析）

- [x] Task 4: 后端音乐与用户管理接口
  - [x] 4.1 Portal：歌单列表、歌曲列表、推荐歌单
  - [x] 4.2 Admin：MP3 上传（复用 FileStorageService，校验 ≤20MB 与格式）、歌曲/歌单 CRUD、存储统计
  - [x] 4.3 Admin：用户列表（统计/筛选）、编辑、重置密码、禁用/启用

- [x] Task 5: 后端 Controller 层
  - [x] 5.1 Portal：/portal/interview（题目筛选/收藏/错题本）、/portal/exam（进行中/交卷/成绩）、/portal/music
  - [x] 5.2 Admin：/admin/interview-questions、/admin/exam-questions、/admin/exam-papers、/admin/marking、/admin/music/songs、/admin/music/playlists、/admin/users
  - [x] 5.3 Dashboard 增强接口：待办统计（待审评论/留言/待阅卷）、最近活动（操作日志）、系统状态（Actuator 汇总）
  - [x] 5.4 管理端接口加 @Admin 注解与操作日志

## 阶段二：门户端 blog-frontend

- [x] Task 6: 全局播放器与音乐页
  - [x] 6.1 Pinia player store（播放列表/当前曲/进度/音量/循环/随机，localStorage 持久化偏好）
  - [x] 6.2 PlayerBar 全局组件（固定底部，进度/上一首/播放/下一首/音量）
  - [x] 6.3 音乐页：Now Playing banner（封面、均衡器动画、进度）、歌曲列表（当前行高亮）、推荐歌单卡片

- [x] Task 7: 面试刷题页
  - [x] 7.1 路由 /interview + API 模块
  - [x] 7.2 筛选侧栏（方向/难度/状态）+ 快捷入口（我的收藏/错题本）
  - [x] 7.3 题目卡片列表、展开参考答案（代码高亮）、收藏/错题操作

- [x] Task 8: 考试答题页（exam 沉浸布局）
  - [x] 8.1 路由 /exam/:paperId（全屏无 Header 布局）+ API
  - [x] 8.2 顶栏（考试名/倒计时/退出）+ 防作弊警示条（切屏计数）
  - [x] 8.3 答题卡（4 态按钮 grid）+ 答题区（题干/选项/多选/标记）+ 底部操作栏（上一题/标记/下一题/交卷）
  - [x] 8.4 防作弊逻辑：visibilityState 切屏计数（3 次或单次 >10s 强制交卷）、禁右键/复制、倒计时归零自动交卷、客观题提交判分

- [x] Task 9: 成绩查询页
  - [x] 9.1 路由 /scores + 得分 Hero 卡（分数/用时/切屏/客观分主观分）
  - [x] 9.2 题型得分分布条形图 + 知识领域掌握度（category 已由后端补充，含题型掌握度图）
  - [x] 9.3 历史成绩列表 + 答题回顾（正误/我的答案/解析）

- [x] Task 10: 首页与博客/简历优化
  - [x] 10.1 首页 Hero 双栏 + 核心功能入口卡片（博客/简历/考试/刷题/音乐）+ 统计概览
  - [x] 10.2 博客详情：顶部阅读进度条 + TOC 目录（TOC 已存在，补进度条）
  - [x] 10.3 简历页：经历时间轴 + 技能进度条（已存在）+ @media print 打印样式
  - [x] 10.4 AppHeader 增加导航项（刷题/考试/音乐/成绩）

## 阶段三：管理端 blog-admin

- [x] Task 11: 侧边栏重构与 Dashboard 增强
  - [x] 11.1 侧边栏分组（概览/内容管理/考试管理/系统管理）+ 待办 badge
  - [x] 11.2 Dashboard：统计卡、待办事项、最近活动时间轴、系统状态、访问趋势图

- [x] Task 12: 题库与试卷管理页
  - [x] 12.1 题库管理：6 题型统计卡 + 筛选 + 表格 CRUD（含 6 种题型表单）
  - [x] 12.2 试卷管理：试卷列表 + 组卷（从题库选题）+ 发布/停用

- [x] Task 13: 阅卷中心
  - [x] 13.1 左侧待批列表（考生/客观分/待批数）+ 右侧批改面板
  - [x] 13.2 逐题批改（考生答案 vs 参考答案、评分、评语）、保存草稿/提交评分

- [x] Task 14: 面试题管理与音乐管理与用户管理
  - [x] 14.1 面试题管理：筛选 + 表格 CRUD（含答案编辑）
  - [x] 14.2 音乐管理：拖拽上传 MP3（≤20MB 校验 + 版权提示条）+ 歌曲表格（预览/编辑/删除）+ 歌单管理 + 存储统计卡
  - [x] 14.3 用户管理：统计卡 + 筛选 + 编辑/重置密码/禁用

## 阶段四：验证

- [x] Task 15: 集成验证（构建与测试由用户手动执行，不做自动验证）
  - [x] 15.1 端到端流程走查：组卷→答题→交卷→阅卷→成绩查看；音乐上传→播放；刷题→收藏（静态核查完成：路由/API/DTO/权限/配置全链路对齐，修复了音乐上传链路的 multipart/存储大小配置限制）

# Task Dependencies
- Task 2-5 依赖 Task 1（先有表结构）
- Task 6-10 依赖 Task 5（Portal 接口）；Task 6/7/8/9 之间可并行
- Task 11-14 依赖 Task 5（Admin 接口）；之间可并行
- Task 15 依赖全部
