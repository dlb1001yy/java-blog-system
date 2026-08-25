# Tasks
- [x] Task 1: 后端分类删除校验
  - [x] 1.1 在 CategoryService 中新增删除校验方法：根据分类 ID 集合查询 `blog_article.category_id` 引用计数
  - [x] 1.2 修改 AdminCategoryController 的单个删除与批量删除，有引用时抛业务异常（返回错误信息含分类名与引用文章数），无引用时执行删除
- [x] Task 2: 后端标签删除校验
  - [x] 2.1 在 TagService 中新增删除校验方法：根据标签 ID 集合查询 `blog_article_tag.tag_id` 关联计数
  - [x] 2.2 修改 AdminTagController 的单个删除与批量删除，有关联时抛业务异常，无关联时执行删除
- [x] Task 3: 前端错误提示
  - [x] 3.1 确认/完善 CategoryList.vue、TagList.vue 删除失败时展示后端返回的错误信息
- [x] Task 4: 验证
  - [x] 4.1 编译后端、构建前端确认无错误；手工/接口测试删除被引用与未被引用的分类、标签

# Task Dependencies
- Task 1、Task 2 可并行；Task 3 依赖后端错误信息格式确定；Task 4 最后
