# 修复：文章已删除但删除标签时仍提示"已被文章使用"

## 问题现象

blog-admin 文章管理中的文章数据已全部删除，但删除标签时报错：
`标签[xxx]已被 N 篇文章使用，无法删除`

## 根因分析

1. 文章删除是**逻辑删除**：`Article` 继承 `BaseEntity`（`@TableLogic isDeleted`），[AdminArticleController.delete/batchDelete](file:///d:\my-project\java-blog-system\blog-backend\src\main\java\com\dlbyy\blog\controller\admin\AdminArticleController.java#L135-L157) 调 `articleService.removeById(id)` 只把 `blog_article.is_deleted` 置 1，**未清理 `blog_article_tag` 关联记录**。
2. 标签删除校验 [TagServiceImpl.checkBeforeDelete](file:///d:\my-project\java-blog-system\blog-backend\src\main\java\com\dlbyy\blog\service\impl\TagServiceImpl.java#L43-L62) 直接 `selectList` 统计 `blog_article_tag` 中该标签的关联数，不校验文章是否已被逻辑删除 → 已删文章遗留的孤儿关联仍被计入 → 误报。

## 修复方案（两处，标本兼治）

### 改动 1：删除文章时同步清理标签关联（治本）

文件：`blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminArticleController.java`

- 在 `delete(id)` 与 `batchDelete(batchIds)` 中，`removeById` 之后删除 `blog_article_tag` 中该文章的关联：
  - 注入 `ArticleTagMapper`（已注入 `TagService`，也可在 TagService 加方法；为最小改动直接注入 `ArticleTagMapper`，项目中 Controller 直接用 Mapper 无先例，故采用在 `ArticleService` 侧处理更符合分层——检查后 `ArticleService` 为 MyBatis-Plus IService 接口，控制器直接调 `removeById`。**决策：在 `TagService` 中新增 `removeRelationsByArticleIds(List<Long>)` 方法**（TagServiceImpl 已持有 `ArticleTagMapper`），Controller 调用之。
- 同时发布 `ArticlePublishedEvent(DELETED)` 的逻辑保持不变。

### 改动 2：checkBeforeDelete 过滤已删文章的孤儿关联（治标 + 清理存量脏数据）

文件：`blog-backend/src/main/java/com/dlbyy/blog/service/impl/TagServiceImpl.java`

- 注入 `ArticleMapper`，在 `checkBeforeDelete` 中：
  1. 查出关联记录后，收集 `articleId` 集合；
  2. `articleMapper.selectBatchIds(articleIds)` 查仍存在的文章（MyBatis-Plus 逻辑删除自动过滤已删）；
  3. 仅统计文章仍存在的关联数，用于报错提示；
  4. 顺手删除指向已删文章的孤儿关联记录（`articleTagMapper.delete(wrapper.in(articleId, orphanArticleIds))`），实现存量脏数据自愈。
- 若过滤后无有效关联 → 直接放行删除。

## 改动文件清单

1. `blog-backend/src/main/java/com/dlbyy/blog/service/TagService.java` — 新增 `removeRelationsByArticleIds(List<Long> articleIds)` 接口方法
2. `blog-backend/src/main/java/com/dlbyy/blog/service/impl/TagServiceImpl.java` — 实现该方法；`checkBeforeDelete` 过滤已删文章并清理孤儿关联
3. `blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminArticleController.java` — delete/batchDelete 删除文章时调用 `tagService.removeRelationsByArticleIds`

## 验证

1. `mvn compile` 通过。
2. 造数：创建文章并绑定标签 → 删除文章 → 删除该标签应成功（不再提示关联文章）。
3. 库中确认 `blog_article_tag` 无孤儿记录（article 已逻辑删除的关联被清理）。
4. 存在有效关联时删除标签仍正常拦截并提示正确篇数。
