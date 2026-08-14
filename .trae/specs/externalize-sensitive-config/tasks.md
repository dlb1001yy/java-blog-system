# Tasks
- [x] Task 1: 后端 yaml 占位化与 SignatureProperties 收敛
  - [x] SubTask 1.1: `application.yaml`：datasource/redis 账密 → `${MYSQL_USERNAME:root}`/`${MYSQL_PASSWORD:123456}`/`${REDIS_PASSWORD:123456}`；OSS 五项 → `${OSS_*:占位}`；jwt/signing 注释强调生产必换
  - [x] SubTask 1.2: `application-docker.yaml`：账密同样占位化
  - [x] SubTask 1.3: `SignatureProperties.java` 默认密钥改空串（确认无测试依赖该默认值）
- [x] Task 2: docker-compose 与 .env 机制
  - [x] SubTask 2.1: compose 密码/healthcheck 全部 `${VAR:-默认}` 插值；backend 注入 5 个敏感变量；顶部注释说明 .env 覆盖
  - [x] SubTask 2.2: 新增 `.env.example`（13 项变量 + 中文注释，含 COMPOSE_PROFILES/BLOG_SEARCH_ENABLED/OSS）
  - [x] SubTask 2.3: 根 `.gitignore` 增加 `.env`；额外发现 .env 此前已被 git 跟踪，已 `git rm --cached .env` 使规则真正生效（本地文件保留）
- [x] Task 3: 前端签名密钥环境变量化
  - [x] SubTask 3.1: blog-admin：`.env.development`/`.env.production` + signing.js 读 `import.meta.env.VITE_API_SIGNING_SECRET || 默认`；.gitignore 忽略 .env.local
  - [x] SubTask 3.2: blog-app：新增 `common/env.js`（ES module 导出 SIGNING_SECRET），signing.js 改 import
- [x] Task 4: 验证
  - [x] SubTask 4.1: `mvn test` 全部通过（56 用例，BUILD SUCCESS）
  - [x] SubTask 4.2: `npm run build` 成功（16.5s）；产物含默认密钥 1 处（来自 .env.production，生产由 CI 覆盖，符合设计）
  - [x] SubTask 4.3: 本机无 docker，改为全文 Read 复核 compose YAML（插值位置/数组语法/注释均正确）
- [x] Task 5: 文档同步
  - [x] SubTask 5.1: README.md：启动后端节新增环境变量说明段；配置表扩为 4 列（环境变量列），新增 signing/oss 两行
  - [x] SubTask 5.2: 部署操作手册.md：新增「1.5 环境变量与 .env 配置（首次部署必读）」小节（13 项变量表 + ★生产必改 + MySQL 首次建库密码语义说明）；5.1 节 BLOG_SEARCH_ENABLED 去重指向 1.5

# Task Dependencies
- Task 1、2、3 相互独立，可并行
- Task 4 依赖 1-3；Task 5 依赖 1-3（可与其并行编写）
