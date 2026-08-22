# 修复后台编辑文章 Markdown 失效计划

## Summary
blog-admin 编辑文章保存后 Markdown 格式失效。排查：文章 content 在 `AdminArticleController` create/update（L83/L117）使用 `JsoupXssUtil.cleanHtml`（relaxed 白名单）清洗——该清洗面向"富文本 HTML"，而文章 content 实为 **Markdown 源文本**（admin 编辑器与前台 ArticleDetail 均按 Markdown 渲染）。`cleanHtml` 会把纯文本段落重排为 `<p>` 标签、折叠换行、转义字符，破坏 Markdown 源结构（标题/列表/代码块错乱），导致编辑回显与前台渲染"格式失效"。

## Current State Analysis
- `blog-admin/src/views/ArticleEdit.vue`：编辑器输入的是 Markdown 源文本（textarea + markdown-it 预览）。
- `blog-frontend/src/views/ArticleDetail.vue` L107/L141：前台用 `md.render(article.content)` 渲染，同样假设 content 为 Markdown 源文本。
- `blog-backend/.../controller/admin/AdminArticleController.java` L81-83 / L115-117：`title/summary` 用 `cleanText`，`content` 用 `cleanHtml`（会把 Markdown 源文本 HTML 化、破坏换行）。
- `blog-backend/.../utils/JsoupXssUtil.java`：已具备 `cleanMarkdown`（剥离全部 HTML 标签 + 保留换行，XSS 防护 Safelist.none），正是 Markdown 源文本需要的清洗方式（此前为简历新增）。

## Proposed Changes
1. **AdminArticleController.java**（仅 2 行）：
   - L83：`article.setContent(JsoupXssUtil.cleanHtml(dto.getContent()))` → `cleanMarkdown(dto.getContent())`
   - L117：同上。
   - 注释同步改为"content 为 Markdown 源文本，用 cleanMarkdown 剥离 HTML 标签并保留换行"。
   - `title/summary` 保持 `cleanText` 不变。
2. **无需前端改动**；评论/留言的 `cleanHtml` 用法保持不变（它们本来就是富文本 HTML）。
3. 存量数据说明：已被 cleanHtml HTML 化（含 `<p>` 包裹）的旧文章无法自动还原，严重者需重新编辑保存。

## Assumptions & Decisions
- 文章 content 的安全策略与简历描述一致：剥离所有 HTML 标签 + 保留换行（Markdown 本身不需要内嵌 HTML；markdown-it 渲染的 HTML 能力因此受限，属安全收益）。
- 不修改 `cleanHtml` 本身（评论/留言仍在使用）。

## Verification
1. IDE 诊断 AdminArticleController 无错误。
2. 逻辑走查：`cleanMarkdown("# 标题\n- 列表\n```代码块```")` → 标签剥离、换行保留、Markdown 语法字符（#、-、`）不受影响。
3. 手动：admin 编辑文章输入 Markdown（标题/列表/代码块/链接）→ 保存 → 重新打开编辑页回显为原 Markdown；前台详情页渲染正常。
