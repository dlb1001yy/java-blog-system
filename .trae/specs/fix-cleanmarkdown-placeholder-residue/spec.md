# 修复简历换行占位符残留 Spec

## Why
`cleanMarkdown` 使用 `\u0000NL\u0000` 作为换行占位符，但 Jsoup 清洗输出时会把 NUL 控制字符实体转义为 `&#x0;`，导致还原失败，数据库与前端回显中出现字面量 `&#x0;NL&#x0;`（换行仍未生效且内容被污染）。

## What Changes
- `blog-backend/src/main/java/com/dlbyy/blog/utils/JsoupXssUtil.java` 的 `cleanMarkdown`：
  1. 占位符改为纯字母数字、不会被 Jsoup 转义的随机 token（如 `Zq7nLZq`，控制字符一律不用）。
  2. 清洗后先用 `Parser.unescapeEntities(cleaned, false)` 反转义，再还原占位符为 `\n`。
  3. 自愈处理：入口先将已污染的旧数据中的 `\u0000NL\u0000`、`&#x0;NL&#x0;`、`&amp;#x0;NL&amp;#x0;`、`&#0;NL&#0;` 等变体统一替换回 `\n`，用户下次保存即自动修复存量脏数据。

## Impact
- Affected specs: 相关简历 Markdown 系列（`add-resume-markdown-fields`、`add-resume-markdown-editor`）
- Affected code: 仅 `JsoupXssUtil.java`

## MODIFIED Requirements

### Requirement: cleanMarkdown 保留换行且无占位符残留
`cleanMarkdown` SHALL 在剥离 HTML 标签的同时保留换行，输出中不得出现任何占位符字面量或实体转义残留。

#### Scenario: 多行 Markdown 保存
- **WHEN** 保存含多行 `- xxx;` 的简介
- **THEN** 存库内容保留 `\n`，无 `&#x0;NL&#x0;` 等字面量

#### Scenario: 存量脏数据自愈
- **WHEN** 保存的内容中已包含 `&#x0;NL&#x0;` 等旧占位符残留
- **THEN** 保存后被还原为换行

#### Scenario: XSS 防护不降级
- **WHEN** 输入含 `<script>` 等 HTML 标签
- **THEN** 标签仍被 `Safelist.none()` 剥离
