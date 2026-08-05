# 导入 Markdown 文件创建文章 Spec

## Why
当前 `blog-admin` 的文章编辑页（`ArticleEdit.vue`）只能手动逐项填写标题、摘要、正文和封面图，无法直接复用已写好的 `.md` 文件。用户希望在写文章时上传一个 Markdown 文件，系统自动解析并回填标题、摘要、正文，同时智能获取封面图（取正文第一张图；若无图则根据内容自动生成一张），以减少重复录入工作。

## What Changes
- **后端 `blog-backend`**
  - 在 `application.yaml` 的 `file.allowed-types` 中追加 `md` 类型，允许上传 Markdown 文件。
  - 在 `AdminArticleController` 新增 `POST /admin/articles/import-markdown` 接口，接收 `MultipartFile`，解析后返回 `{ title, summary, content, coverImage }`。
  - 新增 `MarkdownImportService`（位于 `service` 包）负责：读取 UTF-8 文本、提取标题/摘要/正文/首图、下载外链首图到本地、无图时调用 `CoverImageGenerator` 生成封面。
  - 新增 `CoverImageGenerator`（位于 `utils` 包），使用 JDK 内置 `BufferedImage + Graphics2D` 绘制 1200×630 渐变背景 + 标题文字封面，保存为 PNG 并返回 `/api/uploads/xxx.png` 访问路径。
  - 复用 `FileUtils` 现有上传逻辑（保存目录、URL 规则），不修改其签名。
- **前端 `blog-admin`**
  - 在 `ArticleEdit.vue` 顶部操作区新增「导入 Markdown」按钮，点击后弹出文件选择器（`accept=".md,.markdown"`）。
  - 将所选文件 `FormData` 上传到 `/api/admin/articles/import-markdown`，调用方式通过 `api/article.js` 新增的 `importMarkdown(file)` 方法。
  - 接口返回后，将 `title`、`summary`、`content`、`coverImage` 回填到 `form`（保留用户已填的 `categoryId`、`tagIds`、`type` 等其他字段，仅当返回字段非空时覆盖）。
  - 上传与解析期间按钮 loading，失败时 `ElMessage.error` 提示并保留原表单内容。

## Impact
- Affected specs: 无（首次新增「导入 Markdown」能力）。
- Affected code:
  - 后端：`blog-backend/src/main/resources/application.yaml`、`controller/admin/AdminArticleController.java`、新增 `service/MarkdownImportService.java`、新增 `utils/CoverImageGenerator.java`、可能扩展 `utils/FileUtils.java`（增加保存本地字节数组的重载）。
  - 前端：`blog-admin/src/views/ArticleEdit.vue`、`blog-admin/src/api/article.js`。
- 兼容性：纯新增能力，不改动现有文章 CRUD 与上传接口；`allowed-types` 追加 `md` 不影响已有图片上传。

## ADDED Requirements

### Requirement: Markdown 文件导入接口
系统 SHALL 提供一个 `POST /admin/articles/import-markdown` 接口，接收单个 `.md` / `.markdown` 文件（UTF-8），解析后返回结构化字段供前端回填。

#### Scenario: 正常解析含标题和首图
- **WHEN** 用户上传一个内容包含 `# 标题` 和 `![alt](https://example.com/a.png)` 的 Markdown 文件
- **THEN** 接口返回 `{ code: 200, data: { title: "标题", summary: "<首段摘要≤200字>", content: "<完整 markdown 原文>", coverImage: "/api/uploads/xxx.png" } }`，其中外链首图已被下载并保存到本地 `uploads` 目录，`coverImage` 为本地可访问路径。

#### Scenario: 文件无 H1 标题
- **WHEN** 上传的 Markdown 文件没有 `# ` 一级标题
- **THEN** 系统使用文件名（去除 `.md`/`.markdown` 扩展名）作为 `title` 返回。

#### Scenario: 文件无任何图片
- **WHEN** 上传的 Markdown 文件内容中未匹配到 `![...](...)` 图片语法
- **THEN** 系统调用 `CoverImageGenerator` 根据解析出的标题生成一张 1200×630 的 PNG 封面图，保存到 `uploads` 目录，`coverImage` 返回 `/api/uploads/xxx.png`。

#### Scenario: 首图为本地相对路径
- **WHEN** 首图地址为相对路径（如 `./images/a.png` 或 `images/a.png`）
- **THEN** 系统跳过下载（无法解析远程资源），改走「无图」分支生成封面图，并在日志中记录跳过原因。

#### Scenario: 文件类型不合法
- **WHEN** 用户上传非 `.md`/`.markdown` 文件
- **THEN** 接口返回 `{ code: 500, message: "仅支持 .md / .markdown 文件" }`，不进行解析。

#### Scenario: 外链图片下载失败
- **WHEN** 首图为外链但下载超时或返回非 2xx
- **THEN** 系统捕获异常，回退到「无图」分支生成封面图，流程不中断。

### Requirement: 封面图自动生成
系统 SHALL 在 Markdown 文件无可用首图时，基于解析出的标题生成一张带渐变背景 + 标题文字的 PNG 封面图。

#### Scenario: 标题长度适中
- **WHEN** 标题长度 ≤ 20 个字符
- **THEN** 生成的封面图在 1200×630 画布上以单行居中显示标题，背景为品牌色渐变（如 `#667eea` → `#764ba2`）。

#### Scenario: 标题超长
- **WHEN** 标题长度 > 20 个字符
- **THEN** 系统按字符宽度自动换行（最多 3 行），超出部分以 `…` 截断，保证文字不溢出画布。

#### Scenario: 标题为空
- **WHEN** 解析出的标题为空字符串
- **THEN** 系统使用默认文案「Java 码农笔记」作为封面文字。

### Requirement: 前端导入交互
`ArticleEdit.vue` SHALL 在页面操作区提供「导入 Markdown」按钮，点击后选择本地 `.md` 文件并上传解析，结果回填到表单。

#### Scenario: 成功回填
- **WHEN** 用户点击「导入 Markdown」选择文件，接口返回成功
- **THEN** 表单的 `title`、`summary`、`content`、`coverImage` 被对应返回值覆盖（仅当返回字段非空时覆盖），其他字段（分类、标签、类型、置顶、来源）保持不变，并显示 `ElMessage.success("导入成功")`。

#### Scenario: 上传中禁用
- **WHEN** 文件正在上传与解析
- **THEN** 按钮 `loading=true` 且禁用，防止重复点击。

#### Scenario: 导入失败
- **WHEN** 接口返回非 200 或网络异常
- **THEN** 显示 `ElMessage.error(返回的 message 或 "导入失败")`，表单内容保持原样不被清空。

#### Scenario: 编辑已有文章时导入
- **WHEN** 用户在编辑已存在文章（路由含 `id`）时点击「导入 Markdown」
- **THEN** 同样回填 `title`/`summary`/`content`/`coverImage`，但 `id` 不变，后续保存走更新流程。
