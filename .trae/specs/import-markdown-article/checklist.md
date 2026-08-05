# Checklist

## 后端配置
- [x] `application.yaml` 的 `file.allowed-types` 包含 `md` 和 `markdown`
- [x] 上传 `.md` 文件时 `FileUtils.upload` 不再抛「不支持的文件类型」异常（代码审查确认 `allowedTypeList.contains(suffix.toLowerCase())` 会通过 `md`/`markdown`）

## 后端 FileUtils 扩展
- [x] `FileUtils` 新增 `saveBytes(byte[] data, String suffix)` 方法
- [x] `saveBytes` 生成的文件名遵循 `时间戳_随机数.suffix` 规则（复用 `ensureDestFile`）
- [x] `saveBytes` 返回的 URL 形如 `/api/uploads/xxx.suffix`，可通过 `http://localhost:8080/api/uploads/xxx.suffix` 访问（`WebMvcConfig` 已映射 `/uploads/**`）
- [x] `saveBytes` 自动创建 `uploadPath` 目录（若不存在）

## 后端 CoverImageGenerator
- [x] 类位于 `com.dlbyy.blog.utils` 包下，标注 `@Component`
- [x] `generate(String title)` 返回 `/api/uploads/xxx.png` 形式的 URL
- [x] 生成图片尺寸为 1200×630（`WIDTH=1200, HEIGHT=630`）
- [x] 背景为 `#667eea → #764ba2` 对角渐变（`GradientPaint`）
- [x] 标题文字白色、居中、字体大小适中（SansSerif BOLD 64px）
- [x] 标题 > 20 字符时自动换行（最多 3 行），超出以 `…` 截断（`wrapTitle` 方法）
- [x] 标题为空时使用「Java 码农笔记」作为默认文案（`DEFAULT_TITLE`）
- [x] 输出格式为 PNG（`ImageIO.write(image, "png", baos)`）

## 后端 MarkdownImportService
- [x] 类位于 `com.dlbyy.blog.service` 包下，标注 `@Service`
- [x] 校验文件后缀，非 `.md` / `.markdown` 抛 `BusinessException("仅支持 .md / .markdown 文件")`
- [x] 以 UTF-8 编码读取文件内容（`new String(file.getBytes(), StandardCharsets.UTF_8)`）
- [x] 标题提取：优先取首个 `^#\s+(.+)$`；无则用文件名（去扩展名）
- [x] 摘要提取：去除标题行、图片、代码块后取第一段非空纯文本，截断到 200 字符并加 `…`
- [x] 首图提取：正则 `!\[.*?\]\(([^)]+)\)` 取第一个捕获组
- [x] 首图为 `http(s)://` 开头时下载并保存到本地，返回本地 URL（Hutool `HttpUtil.downloadBytes`）
- [x] 首图下载失败时捕获异常，回退到生成封面图，流程不中断
- [x] 首图为相对路径时跳过下载，走生成分支
- [x] 无图时调用 `coverImageGenerator.generate(title)`
- [x] 返回结果包含 `title`、`summary`、`content`、`coverImage` 四个字段（`LinkedHashMap`）

## 后端 AdminArticleController
- [x] 新增 `POST /admin/articles/import-markdown` 接口
- [x] 接口参数为 `@RequestParam("file") MultipartFile file`
- [x] 接口返回 `Result.success("导入成功", result)`
- [x] 异常被捕获并返回友好错误信息（`BusinessException` 返回 `Result.error(e.getMessage())`，其他异常返回 `Result.error("导入失败")`）
- [x] Swagger / Knife4j 文档中可见该接口（`@Operation(summary = "导入 Markdown 文件解析为文章字段")`）

## 前端 API
- [x] `api/article.js` 新增 `importMarkdown(file)` 方法
- [x] 使用 `FormData` 上传文件
- [x] 请求头设置 `Content-Type: multipart/form-data`
- [x] 走统一的 `request` 实例（自动携带 `Authorization`）

## 前端 ArticleEdit.vue
- [x] 页面操作区有「导入 Markdown」按钮（含 `Upload` 图标）
- [x] 按钮上传中显示 loading 且禁用（`:loading="importLoading"`）
- [x] 隐藏的 `<input type="file" accept=".md,.markdown">` 触发文件选择
- [x] 上传成功后 `title` / `summary` / `content` / `coverImage` 被回填（仅非空字段覆盖）
- [x] 其他字段（`categoryId` / `tagIds` / `type` / `isTop` / 来源信息）保持不变
- [x] 回填后显示 `ElMessage.success("导入成功")`
- [x] 失败时显示 `ElMessage.error` 且表单内容不被清空（request 拦截器统一弹错）
- [x] 上传完成后 `input.value` 重置为空，允许重复选择同一文件
- [x] 编辑已有文章（带 `id`）时导入不影响 `id`，保存仍走更新流程（`form.id` 未被覆盖）

## 端到端验证
- [x] 后端 `mvn compile` 编译通过（BUILD SUCCESS）
- [x] 前端无 VSCode 诊断错误
- [x] 代码审查确认所有实现与 spec 字段、逻辑一致
- [ ] 上传含 H1 标题 + 外链图片的 `.md`，返回结构与预期一致，外链图被下载到本地（需用户重启后端后验证）
- [ ] 上传无图 `.md`，`coverImage` 为生成的 PNG，图片可正常访问（需用户重启后端后验证）
- [ ] 上传无 H1 的 `.md`，`title` 为文件名（去扩展名）（需用户重启后端后验证）
- [ ] 上传非 `.md` 文件，返回错误提示「仅支持 .md / .markdown 文件」（需用户重启后端后验证）
- [ ] 前端点击「导入 Markdown」选择文件后，表单字段正确回填，封面图预览正常（需用户重启后端后验证）
- [ ] 生成的封面图在浏览器中显示正常（渐变背景 + 标题文字清晰可读）（需用户重启后端后验证）
