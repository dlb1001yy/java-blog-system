# Tasks
- [x] Task 1: 服务端作弊判定强化（ExamServiceImpl.submitPaper）
  - [x] 1.1 增加规则：题目数 ≥ 8 且 durationSeconds < 题目数×10 秒 → cheatFlag = 1
  - [x] 1.2 与切屏超限（switchCount >= 3）判定取或
  - [x] 1.3 补充单元测试覆盖两种触发路径与正常路径
- [x] Task 2: 交卷异步判分
  - [x] 2.1 AsyncConfig 新增 examJudgeExecutor 线程池 Bean
  - [x] 2.2 submitPaper 拆分：同步插入占位 ExamRecord（状态 0）并返回 recordId；判分、答案序列化、批改草稿生成放入 @Async("examJudgeExecutor") 方法
  - [x] 2.3 异步方法异常捕获：记录日志，record 状态保持 0 不污染数据
  - [x] 2.4 主观题批改草稿改为批量插入（Db.saveBatch）
- [x] Task 3: RateLimitAspect 日志增强
  - [x] 3.1 拦截日志中补充窗口阈值与限流 key 维度上下文
- [x] Task 4: 前端适配异步判分（blog-frontend 交卷结果页）
  - [x] 4.1 若结果详情中 answers/objectiveScore 尚未就绪，做轮询或"判分中"占位展示
- [x] Task 5: 回归验证
  - [x] 5.1 后端编译与单测通过（mvn test，67/67 通过）
  - [x] 5.2 手动/接口测试交卷 → 异步判分 → 批改 → 发布全流程（以单测覆盖交卷与异步判分调用链）

# Task Dependencies
- Task 2 依赖 Task 1（同一方法内改动，先定作弊规则）
- Task 4 依赖 Task 2
- Task 3 独立，可与 Task 1/2 并行
