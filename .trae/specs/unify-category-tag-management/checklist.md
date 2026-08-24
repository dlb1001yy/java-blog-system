# Checklist

- [x] 初始化 SQL 将 5 个技术方向插入 blog_category（不存在时）
- [x] 面试题页技术方向下拉来自分类接口，无硬编码选项
- [x] 面试题页标签为多选下拉（allow-create），提交逗号分隔名称
- [x] 试题 Excel 导入/保存遇到新分类自动新增到 blog_category，不再报错
- [x] 面试题保存/Markdown 导入的技术方向与标签自动新增到对应表
- [x] 文章保存时 allow-create 的新标签由后端 getOrCreate 落库并正确关联
- [x] 文章 Markdown 导入支持分类/标签并自动新增
- [x] 后端编译通过（mvn compile）
