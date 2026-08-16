# Tasks
- [x] Task 1: 新增 logback-spring.xml 结构化日志配置
  - [x] SubTask 1.1: 创建 blog-backend/src/main/resources/logback-spring.xml：property 定义 LOG_FILE（默认 /app/logs/blog-backend.log，非 docker/目录不可写时降级临时目录）、CONSOLE_LOG_PATTERN 与 FILE_LOG_PATTERN（时间|级别|线程|logger|消息）
  - [x] SubTask 1.2: 配置 CONSOLE（chromatic 可读）与 FILE 两个 appender，FILE 使用 RollingFileAppender（SizeAndTimeBasedRollingPolicy，100MB/天，maxHistory 14，totalSizeCap 30 个文件，.gz 归档）
  - [x] SubTask 1.3: 配置 ERROR 独立 RollingFileAppender（blog-backend-error.log，ThresholdFilter ERROR）与异步包装（AsyncAppender，queueSize 512，discardingThreshold 0 保证不丢 ERROR）
  - [x] SubTask 1.4: root INFO；`springProfile` docker 下不动 root、其余 profile 保持一致；logger name="org.apache.ibatis" WARN 降噪
- [x] Task 2: application.yaml 切换 MyBatis 日志实现
  - [x] SubTask 2.1: `mybatis-plus.configuration.log-impl` 由 StdOutImpl 改为 `org.apache.ibatis.logging.slf4j.Slf4jImpl`
- [x] Task 3: application-docker.yaml 注入 LOG_FILE
  - [x] SubTask 3.1: 顶部 environment 相关处定义 `LOG_FILE: ${LOG_FILE:/app/logs/blog-backend.log}`（yaml 语法，注意文件实际后缀为 .yaml）
- [x] Task 4: docker-compose.yml 日志持久化与轮转
  - [x] SubTask 4.1: blog-backend 服务 volumes 挂载 `backend_logs:/app/logs`
  - [x] SubTask 4.2: blog-backend 服务新增 logging 段：driver json-file，options max-size=50m、max-file=5
  - [x] SubTask 4.3: 顶层 volumes 段新增 `backend_logs: { name: blog_backend_logs }`
- [x] Task 5: 验证（不跑 mvn test）
  - [x] SubTask 5.1: XML/YAML 语法自检（Read 复核缩进与标签闭合）、docker compose config 语义校验（如环境可用）

# Task Dependencies
- Task 1 独立；Task 2、Task 3 独立可并行
- Task 4 依赖 Task 1（文件路径约定一致）
- Task 5 依赖全部完成后执行
