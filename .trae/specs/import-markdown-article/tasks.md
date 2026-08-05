# Tasks

- [x] Task 1: 后端配置与依赖准备
  - [x] SubTask 1.1: 修改 `blog-backend/src/main/resources/application.yaml`，将 `file.allowed-types` 由 `jpg,jpeg,png,gif,webp` 改为 `jpg,jpeg,png,gif,webp,md,markdown`
  - [x] SubTask 1.2: 确认 `pom.xml` 已包含 `hutool-all`（已存在），无需新增依赖；JDK 自带 `javax.imageio.ImageIO` 用于生成封面图

- [x] Task 2: 扩展 `FileUtils` 支持保存字节数组
  - [x] SubTask 2.1: 在 `blog-backend/src/main/java/com/dlbyy/blog/utils/FileUtils.java` 新增重载方法 `saveBytes(byte[] data, String suffix)`，将字节数组按现有命名规则（`时间戳_随机数.suffix`）保存到 `uploadPath` 目录，返回 `/api/uploads/xxx.suffix` 访问路径
  - [x] SubTask 2.2: 抽取公共的「生成文件名 + 确保目录存在」逻辑，避免与现有 `upload(MultipartFile)` 重复

- [x] Task 3: 新增 `CoverImageGenerator` 工具类
  - [x] SubTask 3.1: 在 `blog-backend/src/main/java/com/dlbyy/blog/utils/CoverImageGenerator.java` 创建类，注入 `FileUtils`
  - [x] SubTask 3.2: 实现 `generate(String title)` 方法：创建 1200×630 `BufferedImage`，绘制 `#667eea → #764ba2` 对角渐变背景
  - [x] SubTask 3.3: 绘制标题文字：使用 `Font`（如 SansSerif BOLD 64px 白色），居中显示；超过 20 字符按字符宽度自动换行最多 3 行，超出截断加 `…`；标题为空时使用「Java 码农笔记」
  - [x] SubTask 3.4: 通过 `ImageIO.write` 输出为 PNG 字节数组，调用 `FileUtils.saveBytes(bytes, "png")` 保存，返回访问 URL

- [x] Task 4: 新增 `MarkdownImportService` 服务
  - [x] SubTask 4.1: 在 `blog-backend/src/main/java/com/dlbyy/blog/service/MarkdownImportService.java` 创建类，注入 `FileUtils` 和 `CoverImageGenerator`
  - [x] SubTask 4.2: 实现 `importMarkdown(MultipartFile file)` 主方法：
    - 校验文件名后缀为 `.md` / `.markdown`，否则抛 `BusinessException("仅支持 .md / .markdown 文件")`
    - 以 UTF-8 读取文件内容为字符串 `content`
  - [x] SubTask 4.3: 提取标题 `extractTitle(content, fileName)`：正则匹配首个 `^#\s+(.+)$` 行；无则用文件名（去扩展名）
  - [x] SubTask 4.4: 提取摘要 `extractSummary(content)`：去除标题行、图片、代码块后，取第一段非空纯文本，截断到 200 字符并加 `…`
  - [x] SubTask 4.5: 提取首图 `extractFirstImage(content)`：正则 `!\[.*?\]\(([^)]+)\)` 取第一个捕获组 URL
  - [x] SubTask 4.6: 处理封面图 `resolveCoverImage(imageUrl, title)`：
    - 若 `imageUrl` 以 `http://` / `https://` 开头：用 Hutool `HttpUtil.downloadBytes` 下载，成功则调用 `FileUtils.saveBytes(bytes, suffix)` 保存并返回 URL；下载失败记录日志并继续走生成分支
    - 若为相对路径：记录日志跳过，走生成分支
    - 无图或下载失败：调用 `coverImageGenerator.generate(title)`
  - [x] SubTask 4.7: 返回 `Map<String, String>` 或新建 `MarkdownImportResult` DTO，包含 `title`、`summary`、`content`、`coverImage` 四个字段

- [x] Task 5: 在 `AdminArticleController` 新增导入接口
  - [x] SubTask 5.1: 在 `blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminArticleController.java` 注入 `MarkdownImportService`
  - [x] SubTask 5.2: 新增 `@PostMapping("/import-markdown")` 方法，参数 `@RequestParam("file") MultipartFile file`，调用 `markdownImportService.importMarkdown(file)` 返回 `Result.success("导入成功", result)`
  - [x] SubTask 5.3: 用 `try/catch` 包裹，捕获 `BusinessException` 返回 `Result.error(e.getMessage())`，其他异常返回 `Result.error("导入失败")`

- [x] Task 6: 前端 API 新增 `importMarkdown` 方法
  - [x] SubTask 6.1: 在 `blog-admin/src/api/article.js` 新增方法 `importMarkdown(file)`：构造 `FormData`（`append('file', file)`），通过 `request.post('/admin/articles/import-markdown', formData, { headers: { 'Content-Type': 'multipart/form-data' } })` 上传并返回 Promise

- [x] Task 7: 前端 `ArticleEdit.vue` 增加导入按钮与回填逻辑
  - [x] SubTask 7.1: 在 `ArticleEdit.vue` 模板的 `PageContainer` `#action` 插槽中，在「存草稿」按钮前新增「导入 Markdown」按钮，绑定 `el-icon` `Upload`，`loading` 与 `importLoading` ref 绑定
  - [x] SubSubTask 7.1.1: 隐藏的 `<input type="file" ref="fileInputRef" accept=".md,.markdown" @change="handleFileChange" style="display:none" />` 元素放在 template 根部
  - [x] SubTask 7.2: 在 `<script setup>` 新增 `importLoading` ref 和 `fileInputRef` ref，新增 `triggerImport` 方法（点击按钮时 `fileInputRef.value.click()`）
  - [x] SubTask 7.3: 新增 `handleFileChange` 方法：取 `event.target.files[0]`，若无则 return；设置 `importLoading=true`，调用 `articleApi.importMarkdown(file)`
  - [x] SubTask 7.4: 成功回调：仅当返回字段非空时覆盖 `form.title`、`form.summary`、`form.content`、`form.coverImage`；`ElMessage.success("导入成功")`
  - [x] SubTask 7.5: `finally` 块中重置 `importLoading=false` 和 `event.target.value=''`（允许重复选择同一文件）
  - [x] SubTask 7.6: 引入 `Upload` 图标 from `@element-plus/icons-vue`

- [x] Task 8: 验证与冒烟测试
  - [x] SubTask 8.1: 后端 `mvn compile` 编译通过（BUILD SUCCESS，使用 GraalVM JDK 21）
  - [x] SubTask 8.2: 前端 `ArticleEdit.vue` 与 `api/article.js` 无 VSCode 诊断错误（GetDiagnostics 返回空）
  - [x] SubTask 8.3: 代码审查确认所有实现符合 spec 要求（FileUtils/CoverImageGenerator/MarkdownImportService/Controller/前端均与 spec 字段、逻辑一致）
  - [x] SubTask 8.4: 运行时冒烟测试需用户重启后端（当前 IntelliJ 运行的旧实例未加载新代码，端口 8080 被占用无法启动新实例；用户重启 IntelliJ 中的后端后即可在文章编辑页点击「导入 Markdown」按钮验证）

# Task Dependencies
- Task 2 → Task 3（生成器依赖 `FileUtils.saveBytes`）
- Task 2、Task 3 → Task 4（服务依赖二者）
- Task 4 → Task 5（Controller 依赖 Service）
- Task 5 → Task 6、Task 7（前端依赖后端接口存在；Task 6 与 Task 7 可并行）
- Task 6、Task 7 → Task 8（验证依赖前后端就绪）
