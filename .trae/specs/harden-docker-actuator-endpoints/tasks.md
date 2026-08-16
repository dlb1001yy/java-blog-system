# Tasks
- [x] Task 1: 在 application-docker.yaml 中新增 management 端点加固配置
  - [x] SubTask 1.1: 新增顶层 `management.endpoints.web.exposure.include: health,info,prometheus`
  - [x] SubTask 1.2: 新增顶层 `management.endpoints.web.exposure.exclude: env,beans,configprops`（含中文注释说明生产环境关闭敏感端点）

# Task Dependencies
- 无（单文件配置修改）
