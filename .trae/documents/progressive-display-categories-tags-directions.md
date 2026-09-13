# 分类/标签/技术方向列表：初始显示部分，点击"更多"逐步加载

## 概述

目前分类、标签、刷题技术方向均一次性全部渲染，数量多时页面过长。改为：初始显示 8 个，点击"更多"每次追加 8 个，全部展示完之后"更多"按钮消失（不会一次点开全部）。

用户已确认：4 处全部改造；初始 8 个，每次 +8。

## 现状分析

一次性 `v-for` 全量渲染的 4 处：

1. [AppSidebar.vue#L27-L60](d:\my-project\java-blog-system\blog-frontend\src\components\AppSidebar.vue) — 侧边栏"分类"、"标签"两个 tag-cloud 模块（文章列表等页面均引用）
2. [Category.vue#L6-L12](d:\my-project\java-blog-system\blog-frontend\src\views\Category.vue) — 分类页 category-grid
3. [Tags.vue#L6-L12](d:\my-project\java-blog-system\blog-frontend\src\views\Tags.vue) — 标签云页 tag-cloud-container
4. [Interview.vue#L10-L12](d:\my-project\java-blog-system\blog-frontend\src\views\Interview.vue) — 刷题页左侧"技术方向" el-checkbox-group

统一模式（各组件内联实现，不抽公共组件——每处仅 3 行逻辑）：

```js
const STEP = 8
const visibleCount = ref(STEP)
const visibleItems = computed(() => items.value.slice(0, visibleCount.value))
const showMore = () => { visibleCount.value += STEP }
```

```html
<div v-if="items.length > visibleCount" @click="showMore">更多</div>
```

## 修改方案

### 1. AppSidebar.vue（侧边栏分类 + 标签）

script：
- `ref` 改从 vue 引入 `computed`
- 新增：`catCount`/`tagCount` 两个 ref（初始 8）、`visibleCategories`/`visibleTags` 两个 computed、`showMoreCats`/`showMoreTags` 两个方法（各 +8）

template：
- 分类模块 `v-for` 改遍历 `visibleCategories`；标签模块改遍历 `visibleTags`
- 两个 tag-cloud 之后各加一行"更多"文字按钮：`更多`（剩余数 `{{ categories.length - catCount }}` 可不展示，保持简洁）
  - 形如：`<div v-if="categories.length > catCount" class="more-btn" @click="showMoreCats">更多</div>`

style：新增 `.more-btn`（小号文字按钮样式：13px、主题色、居中、cursor: pointer、上边距 8px）

### 2. Category.vue（分类页）

script：新增 `STEP=8`、`visibleCount` ref、`visibleCategories` computed、`showMore` 方法。

template：
- grid 的 `v-for` 改遍历 `visibleCategories`
- grid 之后新增居中"加载更多"按钮（`el-button text type="primary"`），条件 `categories.length > visibleCount`，显示 `加载更多 ({{ visibleCount }}/{{ categories.length }})`
- `el-empty` 判断保持基于完整 `categories.length` 不变

### 3. Tags.vue（标签云页）

与 Category.vue 完全同构：`visibleTags` computed + "加载更多"按钮，`el-empty` 判断不变。

### 4. Interview.vue（刷题技术方向）

script：新增 `visibleCatCount` ref（初始 8）、`visibleCategories` computed、`showMoreCategories` 方法（+8）。

template：
- el-checkbox-group 内 `v-for` 改遍历 `visibleCategories`（勾选状态绑定的 `selectedCategories` 保存的是 id，与分步展示无关，不影响筛选逻辑）
- checkbox-group 之后加"更多"文字按钮（条件 `categories.length > visibleCatCount`），样式复用 `.more-btn` 小号文字按钮

## 假设与决策

- 每处均为组件内联 ref/computed，不抽公共组合式函数（4 处 × 3 行，重复成本低于抽象成本）
- "更多"点击仅追加、无"收起"功能（用户未要求）
- 筛选、跳转等既有交互逻辑均不变，仅改渲染数量

## 验证步骤

1. `npm run dev` 启动前端
2. 文章列表页侧边栏：分类/标签初始最多 8 个，点"更多"每次追加 8 个，展示完按钮消失
3. 分类页 `/category`、标签页 `/tags`：同样分步加载，空数据时仍显示 el-empty
4. 刷题页 `/interview`：技术方向初始 8 个，逐步追加；勾选任意（含"更多"展开项）后筛选结果正确
5. 确认各处点击"更多"次数足够多时最终能展示全部条目
