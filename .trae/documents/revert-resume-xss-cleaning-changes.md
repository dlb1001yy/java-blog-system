# 回退简历相关 XSS 清洗修改计划

## Summary
用户要求废弃此前为 blog-frontend"我的简历"功能对 `ResumeInfoServiceImpl` 和 `JsoupXssUtil` 的修改（cleanMarkdown 相关），恢复到修改前的状态。

## Current State Analysis（本次会话中这两个类的全部改动）
- `blog-backend/.../utils/JsoupXssUtil.java`：新增了 `cleanMarkdown(String)` 方法（L61-85，含占位符与自愈逻辑）。`cleanHtml`/`cleanText` 未被改动。
- `blog-backend/.../service/impl/ResumeInfoServiceImpl.java` L38-41：summary/selfEvaluation/interests 从 `cleanText` 改成了 `cleanMarkdown`，注释同步改过。

## Proposed Changes（纯回退，不新增任何逻辑）
1. `ResumeInfoServiceImpl.java`：
   - 三处 `JsoupXssUtil.cleanMarkdown(...)` 改回 `JsoupXssUtil.cleanText(...)`
   - 注释恢复为："// XSS 清洗：这些为纯文本段落字段，用 cleanText 保留纯文本；JSON 数组字段（skills/workExperience 等）跳过"
2. `JsoupXssUtil.java`：整体删除 `cleanMarkdown` 方法及其 javadoc（L61-85），类恢复为只有 `cleanHtml` 与 `cleanText`。

## Assumptions & Decisions
- 用户明确接受回退后"我的简历"描述字段的换行在保存时会被 `cleanText` 折叠（回到最初行为），前端简历 Markdown 编辑/展示能力保留（前端不回退）。
- `AdminArticleController` 等其他调用方未涉及这两个改动点，不受影响。

## Verification
1. IDE 诊断两个 Java 文件无错误。
2. Grep 确认 `cleanMarkdown` 在整个后端已无任何引用与定义。
