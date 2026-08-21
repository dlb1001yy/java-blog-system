# 首页最新博客区块 + 播放器增强计划

## Summary
1. 首页「最新文章」区块改造为与原型一致的 **3 列纵向网格卡片**，展示最新 **3 篇**博客（去掉 Tabs/分页/侧边栏，改用 `getLatestArticles` API）。
2. 底部播放器 PlayerBar 补充 **播放列表弹层** 和 **音量图标点击静音**。
3. 音乐页与播放器联动已实现（Music.vue `playSong` → `player.setPlaylist`），无需改动，仅验证。

## Current State
- [Home.vue](d:/my-project/java-blog-system/blog-frontend/src/views/Home.vue) L80-133：「最新文章」为 pageSize=10 分页列表 + Tabs + AppSidebar，走 `articleApi.getArticles`；`getLatestArticles`（api/article.js L30-32，返回前 N 篇）未被使用。
- [PlayerBar.vue](d:/my-project/java-blog-system/blog-frontend/src/components/PlayerBar.vue)：已有封面/歌名/上下曲/播放/进度条/循环/随机/音量滑块；音量图标为纯展示 Headset 图标；无播放列表入口。
- [player store](d:/my-project/java-blog-system/blog-frontend/src/stores/player.js)：state 含 playlist/currentIndex/volume，actions 有 playAt/setVolume；无静音状态，可直接用 volume=0 实现静音切换（记住静音前音量存于组件内局部变量）。
- ArticleCard 为横向左图右文列表形态，不适合 3 列网格，需新组件。

## Changes

### 1. 新建 `src/components/LatestBlogCard.vue`
纵向卡片（原型形态）：
- 上：封面图 coverImage（16:9，object-fit cover，无图时显示渐变占位）
- 中：标签行（首个分类名或 tag 的 el-tag 小标签 + YYYY-MM-DD 日期）、标题（1-2 行省略）、摘要 summary（2 行省略）
- 底：阅读数 viewCount（View 图标）+ 点赞数 likeCount；无阅读时长字段则不显示
- 整卡 hover 阴影上浮，点击跳 `/articles/:id`
- 样式使用现有 CSS 变量（--card-bg、--border-color、--primary-color 等）

### 2. 修改 `src/views/Home.vue`
- L80-133 区块替换：
  - 保留 section-header（标题「最新文章」+ 副标题 + 查看全部 →）
  - 移除 layout/主内容/侧边栏结构、Tabs、分页、AppSidebar 引用
  - 新增 `latestArticles = ref([])`，`fetchLatest()` 调 `articleApi.getLatestArticles(3)`（若 API 参数签名不同按实际调整，多取则 slice(0,3)）
  - 网格容器 `.latest-grid`（grid-template-columns: repeat(3, 1fr); gap: 20px; 响应式 2 列/1 列）
  - loading 时显示 3 个 SkeletonCard
  - 空数据时 el-empty
- script 中清理不再使用的：activeTab/handleTabChange/handlePageChange/currentPage/pageSize/total/typeMap/fetchArticles、AppSidebar/ArticleCard import（若仍用于别处则保留）
- onMounted 改调 fetchLatest()

### 3. 修改 `src/components/PlayerBar.vue`
- **音量图标**：改为可点击按钮，静音时显示 `Mute` 图标、正常时 `Headset`（或 Volume 相关图标），点击在 0 与上次音量（组件 ref `lastVolume`，默认 0.8）间切换，并调 `player.setVolume`
- **播放列表入口**：右侧新增列表按钮（`List` 图标），点击弹出 `el-popover`（placement top-end, width 320）：
  - 内含可滚动列表（max-height 300px）：序号/当前曲目高亮 + 歌名 + 歌手，点击项调 `player.playAt(index)` 并不关闭弹层
  - 底部显示「共 N 首」
  - 当前播放项歌名用 --primary-color 高亮并加播放中小图标

### 4. 联动验证（不改代码）
确认 Music.vue playSong → setPlaylist → PlayerBar 出现并播放；播放器切歌后 Music.vue「正在播放」面板（watch currentIndex）同步。若实测有脱节再补 watch 同步。

## Verification
1. `cd blog-frontend && npm run build` 通过
2. 首页：最新 3 篇纵向卡片网格展示、点击进详情、查看全部跳 /articles、无文章时空态
3. 音乐页点歌 → 底部播放器出现并播放同一首；播放器播放列表弹层点其他歌正常切歌且音乐页同步
4. 音量图标点击静音/恢复，滑块联动

## Assumptions
- 后端 `/portal/articles/latest` 已存在（getLatestArticles 已定义）
- 阅读时长字段不存在，卡片底部用 viewCount/likeCount 代替
