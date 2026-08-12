---
name: 部署手册补充Elasticsearch配置
overview: 在《部署操作手册.md》中补充 Elasticsearch 全文检索服务（可选 profile "search"）相关配置与说明，使文档与实际 docker-compose.yml 一致。
todos:
  - id: update-toc
    content: 更新目录，新增 Elasticsearch 启用与排查章节条目
    status: completed
  - id: add-file-check
    content: 在第1.2节补充 elasticsearch/Dockerfile 存在性检查
    status: completed
    dependencies:
      - update-toc
  - id: add-image-prep
    content: 在第3节说明 ES 镜像经 Dockerfile 构建而非 docker pull
    status: completed
    dependencies:
      - update-toc
  - id: add-startup-section
    content: 新增 Elasticsearch 启用章节：--profile search 与 BLOG_SEARCH_ENABLED=true
    status: completed
    dependencies:
      - update-toc
  - id: add-status-check
    content: 在第5节补充 ES 状态、健康检查、日志查看内容
    status: completed
    dependencies:
      - update-toc
  - id: add-access-test
    content: 在第6节补充 curl localhost:9200 访问测试
    status: completed
    dependencies:
      - update-toc
  - id: add-troubleshoot
    content: 新增第7节 ES 排查（连接失败/内存/开关）与第8.6、8.7 数据卷和容器进入
    status: completed
    dependencies:
      - update-toc
---

## 用户需求
用户要求检查《部署操作手册.md》中是否包含 Elasticsearch 的配置说明，没有则补充，不正确则修正。

## 产品概述
《部署操作手册.md》是 Java 博客系统的 Docker 部署文档。经核查，该文档当前完全没有任何 Elasticsearch 相关内容，而项目实际通过 `docker-compose.yml` 提供了基于 `profiles: ["search"]` 的可选 Elasticsearch 全文检索服务（8.11.1 + IK 中文分词）。需要在文档中补充该服务的配置与启用说明，使文档与代码现状一致。

## 核心功能
- 在文档目录、关键文件检查、镜像准备等章节补充 Elasticsearch 相关内容
- 说明 Elasticsearch 为可选服务（profile=search），默认不随主命令启动
- 说明启用方式：`docker compose --profile search up -d --build` 且后端需设置 `BLOG_SEARCH_ENABLED=true`
- 补充 ES 服务状态检查、健康检查、日志查看、访问测试命令
- 补充 ES 数据卷说明与常见排查（连接失败、内存不足、开关未开等）
- 明确 ES 镜像经本地 Dockerfile 构建（`blog-elasticsearch:8.11.1-ik`），非直接 `docker pull`

## 技术栈
- 文档格式：Markdown（与现有《部署操作手册.md》风格一致）
- 目标文件：`d:\my-project\java-blog-system\部署操作手册.md`
- 不涉及代码改动，仅文档补充；所有内容需与 `docker-compose.yml`、`elasticsearch/Dockerfile`、`blog-backend/.../application-docker.yaml` 实际配置严格对应

## 实现方案
采用"就地补充、保持原有章节结构与行文风格"的策略，在现有 8 个章节的对应位置插入 Elasticsearch 相关内容，并新增可选服务的专门说明。所有新增命令与参数均直接引用已核实的 compose/后端配置，避免杜撰路径或指令。

关键技术决策：
1. ES 为可选服务，通过 `profiles: ["search"]` 控制，因此新增强调"默认不启动、需显式 `--profile search` 并配置 `BLOG_SEARCH_ENABLED=true`"双条件，避免用户以为加完 compose 就自动可用。
2. 镜像准备章节（第 3 节）原为 `docker pull` 基础镜像列表，而 ES 服务使用 `build`（本地 Dockerfile），需明确说明 ES 镜像是构建生成、基础镜像在 `docker compose --profile search up --build` 时自动拉取，不要求用户手动 `docker pull blog-elasticsearch`，防止误导。
3. 后端开关 `blog.search.enabled: ${BLOG_SEARCH_ENABLED:false}` 默认关闭，需提示用户通过环境变量开启，否则即使 ES 启动，全文检索也不生效。

## 实现说明（执行要点）
- 仅在 `部署操作手册.md` 单文件内修改，不触碰任何代码/配置文件，保持向后兼容。
- 新增命令必须可复制执行，且与实际 service 名（`elasticsearch`、`blog-elasticsearch`）、端口（`9200`）、数据卷（`blog_es_data`）一致。
- 健康检查描述对齐 compose：`curl -s http://localhost:9200/_cluster/health` 检查 green/yellow。
- 注意第 8.6 数据卷当前只列 mysql/redis/uploads 三项，需补 `blog_es_data`。

## 架构设计
文档型修改，无系统架构变更。修改点在单文件内，按现有 8 章节结构插入，不新增独立文件。

## 目录结构
```
d:\my-project\java-blog-system\
└── 部署操作手册.md   # [MODIFY] 补充 Elasticsearch 可选全文检索服务的配置与启用说明，覆盖目录、关键文件检查、镜像准备、启动、状态检查、访问测试、问题排查、数据卷等章节
```
