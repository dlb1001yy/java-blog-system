# 统一所有页面容器宽度

## 问题分析

首页已修改 `.home .container { width: 1400px; }`，其他页面也需要统一修改。

## 修改清单

### 1. ArticleList.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/ArticleList.vue`
- 修改：在 `.article-list-page` 内添加 `.container` 包裹 `.layout`

### 2. ArticleDetail.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/ArticleDetail.vue`
- 修改：在 `.article-detail` 内添加 `.container` 包裹 `.layout`

### 3. Archives.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/Archives.vue`
- 修改：在 `.archives-page` 内添加 `.container` 包裹内容

### 4. Category.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/Category.vue`
- 修改：在 `.category-page` 内添加 `.container` 包裹内容

### 5. Tags.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/Tags.vue`
- 修改：在 `.tags-page` 内添加 `.container` 包裹内容

### 6. MessageBoard.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/MessageBoard.vue`
- 修改：在 `.message-board` 内添加 `.container` 包裹内容

### 7. About.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/About.vue`
- 修改：在 `.about-page` 内添加 `.container` 包裹内容

### 8. Resume.vue - 添加 container 包裹
- 文件：`blog-admin/src/views/Resume.vue`
- 修改：在 `.resume-page` 内添加 `.container` 包裹内容

## 样式添加

每个页面添加对应的样式：
```css
.xxx-page .container {
  width: 1400px;
}
```

## 验证步骤

1. 所有页面宽度统一为 1400px
2. 响应式布局正常
