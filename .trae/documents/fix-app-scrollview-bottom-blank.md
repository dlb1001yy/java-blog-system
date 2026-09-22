# 修复：App 打包后页面底部空白遮挡内容

## 问题概述

打包成 App 后（H5 端不易察觉），使用 `scroll-view` 做整页滚动的页面（截图为「面试刷题」页），内容滚到底部时最后一行文字被切断，底部 TabBar 上方出现一块约 1/5 屏高的常驻空白（红框区域），内容永远无法滚进该区域，表现为"空白遮挡内容"。

## 根因分析

涉及页面均采用此模式（以刷题页 [blog-app/subpkg-study/pages/interview/index.vue](d:\my-project\java-blog-system\blog-app\subpkg-study\pages\interview\index.vue#L486-L490) 为例）：

```scss
.interview-page { height: 100vh; }
.container {              /* scroll-view */
  height: 100%;
  box-sizing: border-box;
  padding: $spacing-md $spacing-lg calc(144px + env(safe-area-inset-bottom));
}
```

App 端（app-vue，webview 渲染）uni-app 将 `scroll-view` 编译为「外层宿主元素（用户 class/padding 打在这里）+ 内层滚动 div（实际产生滚动）」的双层结构。**打在外层宿主元素上的 `padding-bottom` 只会压缩内层滚动视口高度，不会计入滚动内容高度（scrollHeight）**，导致：

1. 滚动底界被钉死在 padding 区上缘 → 最后一行文字下半部分被切（截图"1. mmap + write 方式："处截断）；
2. 底部 padding 区（144px + 安全区 ≈ 1/5 屏）永远空白、不可滚入 → 即红框空白；
3. 该留白本意是避开 fixed 的 TabBar（56px）+ PlayerBar（56px），结构失效后变成"遮挡"。

项目内佐证：[blog-app/subpkg-study/pages/scores/index.vue](d:\my-project\java-blog-system\blog-app\subpkg-study\pages\scores\index.vue#L423-L426) 把 padding 放在 scroll-view 内层的 `.list-body` 上，无此问题——说明"padding 放内层包裹 view"才是跨端正确写法。

页面级滚动的页面（`min-height: 100vh` + padding-bottom，webview 页面滚动，CSS 标准行为正常）不受影响，如 `pages/article/detail.vue`、`exam/taking.vue`、`tags.vue`、`category.vue`、`mine/index.vue`、`resume/index.vue` 等，**无需修改**。

## 修改方案

统一修法：scroll-view 自身只保留高度约束，去掉其 padding；在 scroll-view 内部新增一层包裹 view，把原 padding（含底部留白数值，不变）移到该内层 view 上。内层 view 的高度参与滚动内容计算，底部留白即可正常滚入，内容不再被切。

涉及 5 个文件：

### 1. blog-app/subpkg-study/pages/interview/index.vue（刷题页，截图问题页）

- template：`<scroll-view class="container">` 内部，将 `.filter-card` 与 `.list`（第 8~169 行的全部内容）整体包进 `<view class="scroll-content">...</view>`。
- style（第 486-490 行）：

```scss
.container {
  height: 100%;
}

/* 内容包裹层：padding 放内层参与滚动高度计算，App 端 scroll-view 宿主 padding 不撑开滚动区 */
.scroll-content {
  box-sizing: border-box;
  padding: $spacing-md $spacing-lg calc(144px + env(safe-area-inset-bottom));
}
```

### 2. blog-app/pages/index/index.vue（首页）

- `.container` 现状：`height: 100%; box-sizing: border-box; padding-bottom: calc(140px + env(safe-area-inset-bottom));`（第 285-289 行）。
- template：scroll-view 内（refresher 具名插槽保持原位不动），将 Hero、stats-card、modules 等默认插槽内容整体包进 `<view class="scroll-content">`。
- style：`.container` 去掉 `padding-bottom` 与 `box-sizing`，新增：

```scss
.scroll-content {
  box-sizing: border-box;
  padding-bottom: calc(140px + env(safe-area-inset-bottom));
}
```

### 3. blog-app/subpkg-article/pages/list.vue（文章列表）

- `.container` 现状：`height: 100%; box-sizing: border-box; padding-bottom: calc(88px + env(safe-area-inset-bottom));`（第 318-322 行）。
- template：同上包一层 `.scroll-content`（refresher 插槽位置不动）。
- style：同模式迁移 padding-bottom 到 `.scroll-content`。

### 4. blog-app/subpkg-article/pages/archives.vue（归档）

- `.container` 现状：`height: 100%; box-sizing: border-box; padding-bottom: calc(88px + env(safe-area-inset-bottom));`（第 119-123 行）。
- template：timeline/骨架/空态整体包一层 `.scroll-content`。
- style：同模式迁移。

### 5. blog-app/subpkg-music/pages/index.vue（音乐页）

- `.scroll-area` 现状：`flex: 1; min-height: 0; box-sizing: border-box; padding-bottom: calc(64px + env(safe-area-inset-bottom));`（第 512-518 行，flex 子项滚动区）。
- template：`<scroll-view class="scroll-area">` 内的 section 们整体包进 `<view class="scroll-content">`。
- style：`.scroll-area` 去掉 padding-bottom（保留 flex: 1 / min-height: 0 / box-sizing），新增 `.scroll-content { padding-bottom: calc(64px + env(safe-area-inset-bottom)); }`。

## 不改动的部分（及原因）

- `subpkg-study/pages/scores/index.vue`：padding 已在内层 `.list-body`，写法正确。
- `pages/article/detail.vue`、`exam/taking.vue`、`tags.vue`、`category.vue`、`mine/index.vue`、`resume/index.vue` 等：页面级滚动（非 scroll-view），padding-bottom 为标准 CSS 行为，App 端正常。
- `components/CategoryChips.vue`：横向 scroll-view，无底部 padding 问题。
- 底部留白数值（144/140/88/64px）与 TabBar/PlayerBar 组件本身：保持不变，本次只修滚动结构。

## 假设与决策

- 假设项目通过 HBuilderX 打 App 包（app-vue，webview 渲染），非 nvue。
- 决策：采用「内层包裹 view 承接 padding」的官方推荐跨端写法，而非给每页列表尾部加占位空 view（后者侵入列表结构、需多处重复）。
- 不做"PlayerBar 未播放时动态缩小留白"等增强，聚焦本次遮挡 bug。

## 验证步骤

1. HBuilderX 运行到手机或重新云打包自定义基座，进入「面试刷题」页：
   - 展开任一题目答案，滚动到底 → 最后一行文字完整可见，不再被切断；
   - 底部留白区可随内容滚入视野（滚到底时 TabBar 上方无"死区"空白遮挡）；
   - 触底加载更多（scrolltolower）正常触发。
2. 依次检查首页、文章列表、归档、音乐页滚动到底的表现：内容不被切、无异常空白死区。
3. H5 端回归：以上 5 页滚动、下拉刷新（首页/列表页 refresher）、触底分页均正常。
