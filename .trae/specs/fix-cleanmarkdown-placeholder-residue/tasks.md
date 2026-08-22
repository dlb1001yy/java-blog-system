# Tasks

- [x] Task 1: 修复 JsoupXssUtil.cleanMarkdown
  - [x] SubTask 1.1: 占位符改为 `Zq7nLZq`（无控制字符），清洗后 `Parser.unescapeEntities` 再还原 `\n`
  - [x] SubTask 1.2: 入口自愈：把 `\u0000NL\u0000` / `&#x0;NL&#x0;` / `&amp;#x0;NL&amp;#x0;` / `&#0;NL&#0;` 变体替换回 `\n`
- [x] Task 2: 验证
  - [x] SubTask 2.1: IDE 诊断无错误；逻辑走查多行/脏数据/XSS 输入三种场景

# Task Dependencies
- Task 2 依赖 Task 1
