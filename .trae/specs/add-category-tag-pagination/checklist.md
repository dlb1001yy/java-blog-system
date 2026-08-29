# Checklist

- [x] 后端存在 `GET /admin/categories/page` 接口，支持 current/size/name 参数，返回 Page 结构（records/total），排序 sort 升序、createTime 降序
- [x] 后端编译通过
- [x] CategoryList.vue 使用 `categoryApi.getPage` 获取数据，展示 records 与 total
- [x] CategoryList.vue 有名称搜索框（支持搜索与重置）
- [x] CategoryList.vue 有分页条（10/20/50/100 可选，可跳页），样式与 ArticleList.vue 一致
- [x] TagList.vue 使用 `tagApi.getPage` 获取数据，展示 records 与 total
- [x] TagList.vue 有名称搜索框（支持搜索与重置）
- [x] TagList.vue 有分页条（10/20/50/100 可选，可跳页），样式与 ArticleList.vue 一致
- [x] 新增/编辑/删除/批量删除后刷新当前页且选中行被清空
- [x] `getAll()` 全量接口与前端 API 方法未被移除（文章编辑等处仍可用）
- [x] blog-admin `npm run build` 构建通过
