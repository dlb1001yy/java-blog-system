# 后端新增 admin 接口修复 tags/resume/links 404

## 问题概述

前端 blog-admin 调用三个后台管理接口返回 404：
- `GET/POST/PUT/DELETE /api/admin/tags`
- `GET/POST/PUT /api/admin/resume`
- `GET/POST/PUT/DELETE /api/admin/links`

（经 Vite 代理转发 + 后端 context-path `/api`，对应后端路径 `/admin/tags`、`/admin/resume`、`/admin/links`）

## 根因分析

后端 `controller/admin` 目录已有 AdminArticleController、AdminCategoryController、AdminCommentController、AdminMessageController、AdminDashboardController，但**缺少** AdminTagController、AdminResumeController、AdminLinkController。

而 service 层、mapper、entity 均已就绪：
- [TagService](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/TagService.java) extends IService<Tag>（空实现，用 MyBatis-Plus 默认方法）
- [ResumeInfoService](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/ResumeInfoService.java) extends IService<ResumeInfo>
- [LinkService](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/service/LinkService.java) extends IService<Link>
- 实体：[Tag](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/entity/Tag.java)(blog_tag)、[ResumeInfo](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/entity/ResumeInfo.java)(resume_info)、[Link](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/entity/Link.java)(blog_link)

因此只需新增 3 个 Controller，直接调用既有 service 方法即可。

## 参考模式

- 简单 CRUD 模板：[AdminCategoryController.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminCategoryController.java)（@GetMapping list / @PostMapping create / @PutMapping update / @DeleteMapping/{id} delete）
- 分页模板：[AdminArticleController.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminArticleController.java#L26-L50)（Page + LambdaQueryWrapper + @RequestParam）
- 响应封装：[Result.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/common/Result.java)（success(data) / success(message,data) / error(message)）

## 前端调用对照（决定后端需实现的方法）

| 前端 api 方法 | 实际被 view 调用 | 后端需实现 |
|---|---|---|
| tag.getAll() GET /admin/tags | TagList.vue:53, ArticleEdit.vue:279 | ✅ list |
| tag.getPage() GET /admin/tags/page | 未被调用 | ✅ page（api 暴露，补全） |
| tag.getDetail(id) GET /admin/tags/{id} | 未被调用 | ✅ detail（补全） |
| tag.create() POST /admin/tags | TagList.vue:80 | ✅ create |
| tag.update() PUT /admin/tags | TagList.vue:77 | ✅ update |
| tag.delete(id) DELETE /admin/tags/{id} | TagList.vue:91 | ✅ delete |
| resume.getResume() GET /admin/resume | ResumeEdit.vue:95 | ✅ get |
| resume.save() POST /admin/resume | ResumeEdit.vue:103 | ✅ save（insert or update） |
| resume.update() PUT /admin/resume | 未被调用 | ✅ update |
| link.getAll() GET /admin/links | LinkList.vue:80 | ✅ list |
| link.getPage() GET /admin/links/page | 未被调用 | ✅ page（补全） |
| link.create() POST /admin/links | LinkList.vue:106 | ✅ create |
| link.update() PUT /admin/links | LinkList.vue:103 | ✅ update |
| link.delete(id) DELETE /admin/links/{id} | LinkList.vue:117 | ✅ delete |

## 修改方案

在 `d:\my-project\java-blog-system\blog-backend\src\main\java\com\dlbyy\blog\controller\admin\` 目录下新增 3 个文件。无需改动 service/mapper/entity（已存在）。

### 新增文件 1：AdminTagController.java

路径：`.../controller/admin/AdminTagController.java`

- `@RestController` `@RequestMapping("/admin/tags")` `@RequiredArgsConstructor` `@Tag(name="后台标签管理")`
- 注入 `TagService`
- `GET ""` → `Result<List<Tag>> list()` 调 `tagService.list()`，按 createTime 倒序
- `GET "/page"` → `Result<Page<Tag>> page(current,size,name)` 用 Page + LambdaQueryWrapper，name 模糊查询
- `GET "/{id}"` → `Result<Tag> detail(id)` 调 `tagService.getById(id)`
- `POST ""` → `Result<?> create(@RequestBody Tag)` 设 createTime，调 `tagService.save()`
- `PUT ""` → `Result<?> update(@RequestBody Tag)` 调 `tagService.updateById()`
- `DELETE "/{id}"` → `Result<?> delete(id)` 调 `tagService.removeById()`

### 新增文件 2：AdminResumeController.java

路径：`.../controller/admin/AdminResumeController.java`

- `@RestController` `@RequestMapping("/admin/resume")` `@RequiredArgsConstructor` `@Tag(name="后台简历管理")`
- 注入 `ResumeInfoService`
- `GET ""` → `Result<ResumeInfo> get()` 调 `resumeInfoService.getOne(null)`（与 PortalResumeController 一致，取唯一记录）
- `POST ""` → `Result<?> save(@RequestBody ResumeInfo)` **insert-or-update 逻辑**：先 `getOne(null)` 查是否已有记录，有则 set id 后 updateById，无则 save；设 createTime/updateTime
- `PUT ""` → `Result<?> update(@RequestBody ResumeInfo)` 调 `resumeInfoService.updateById()`，设 updateTime

注：resume_info 是单条记录表（前台用 getOne(null) 取数），save 需处理「已存在则更新」语义，因前端表单不含 id。

### 新增文件 3：AdminLinkController.java

路径：`.../controller/admin/AdminLinkController.java`

- `@RestController` `@RequestMapping("/admin/links")` `@RequiredArgsConstructor` `@Tag(name="后台友链管理")`
- 注入 `LinkService`
- `GET ""` → `Result<List<Link>> list()` 调 `linkService.list()`，按 sort 升序
- `GET "/page"` → `Result<Page<Link>> page(current,size,name,status)` 用 Page + LambdaQueryWrapper
- `POST ""` → `Result<?> create(@RequestBody Link)` 设 createTime，调 `linkService.save()`
- `PUT ""` → `Result<?> update(@RequestBody Link)` 调 `linkService.updateById()`
- `DELETE "/{id}"` → `Result<?> delete(id)` 调 `linkService.removeById()`

## 前提条件与决策

- **决策**：仅新增后端 Controller，复用既有 service/mapper/entity，不改动前端（前端调用路径已正确）。
- **分页端点**：tag.getPage / link.getPage 当前未被 view 调用，但 api 模块已暴露，一并实现避免将来 404；分页实现遵循 AdminArticleController 的 Page+LambdaQueryWrapper 模式。
- **resume save 语义**：因 resume_info 为单记录表且前端表单无 id 字段，save 端点采用 insert-or-update 逻辑（getOne 判空）。
- **时间字段**：create 时设 createTime，update/save 时设 updateTime，与 AdminArticleController 一致。
- **安全**：/admin/** 路径已由现有 admin 控制器使用且登录后可访问（AdminCategoryController 正常工作），无需改动安全配置。

## 验证步骤

1. 后端重新编译启动（`mvn spring-boot:run` 或 IDE 重启），确认无编译错误、应用启动在 8080。
2. 前端 TagList 页面（侧边栏「标签管理」）：加载列表不 404；新增/编辑/删除标签均成功。
3. 前端 ResumeEdit 页面（侧边栏「简历管理」）：加载简历数据；保存后刷新数据持久化。
4. 前端 LinkList 页面（侧边栏「友情链接」）：加载/新增/编辑/删除链接均成功。
5. 前端 ArticleEdit 页面（写文章/编辑文章）：标签下拉能正常加载（tagApi.getAll() 调 /admin/tags 返回 200）。
6. 浏览器 Network 面板确认上述请求均返回 200，控制台无 404。
