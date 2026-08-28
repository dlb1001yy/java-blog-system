# 修复 blog-admin 标签页右键菜单（提示框）显示位置不正确

## Summary

`blog-admin` 顶部标签页（TagsView）的右键菜单（刷新/关闭/关闭其他/关闭全部）显示位置错误——无论在哪个标签上右键，菜单始终贴在视口最左侧（截图红色区域内），而非跟随鼠标位置。根因是 `openMenu` 中菜单左边距上限 `maxLeft` 的计算基准用错了：取的是**被右键的 tag 元素自身宽度**（约 100px）而非视口宽度，导致 `maxLeft` 为负数，`menuLeft` 恒被钳制为负值，`position: fixed` 的菜单被推到视口左缘。

## Current State Analysis

问题文件：[TagsView.vue](file:///d:/my-project/java-blog-system/blog-admin/src/layout/TagsView.vue)

### 根因代码（第 86-95 行）

```javascript
const openMenu = (tag, e) => {
  selectedTag.value = tag
  const menuMinWidth = 105
  const offsetLeft = e.clientX
  const offsetWidth = e.target.offsetWidth   // BUG：tag 元素自身宽度（约 100px），且 e.target 可能是内部 span/icon，宽度更小
  const maxLeft = offsetWidth - menuMinWidth // = 100 - 105 ≈ -5（负数）
  menuLeft.value = offsetLeft > maxLeft ? maxLeft : offsetLeft  // e.clientX（如 500）恒大于 -5 → menuLeft 恒为 -5
  menuTop.value = e.clientY
  menuVisible.value = true
}
```

### 菜单定位方式（第 25-27 行 + 第 171-182 行 CSS）

```vue
<ul v-show="menuVisible"
    :style="{ left: menuLeft + 'px', top: menuTop + 'px' }"
    class="context-menu">
```

```css
.context-menu {
  position: fixed;   /* fixed 定位以视口为基准 */
  z-index: 3000;
  ...
}
```

**推导链**：`menuLeft = -5px` + `position: fixed` → 菜单始终贴在视口最左缘，与截图现象（菜单竖排出现在左侧导航/答卷列表区域）完全吻合。

**原代码意图**：本应是"菜单超出视口右缘时向左收"的边界保护，但基准取错了对象。

## Proposed Changes

### 修改文件：`blog-admin/src/layout/TagsView.vue`（仅 1 处）

将 `openMenu` 函数的定位计算基准从 tag 元素宽度改为视口尺寸：

```javascript
const openMenu = (tag, e) => {
  selectedTag.value = tag
  const menuMinWidth = 105
  const menuMinHeight = 150  // 4 个菜单项大致高度，防止底部越界
  const maxLeft = window.innerWidth - menuMinWidth
  const maxTop = window.innerHeight - menuMinHeight
  menuLeft.value = e.clientX > maxLeft ? maxLeft : e.clientX
  menuTop.value = e.clientY > maxTop ? maxTop : e.clientY
  menuVisible.value = true
}
```

**说明**：
- `maxLeft` 基准改为 `window.innerWidth`（视口宽度），与 `position: fixed` 的定位基准一致；
- 菜单正常跟随鼠标右键位置显示，仅当接近视口右缘/底缘时向内收，不再出视口；
- 顺带补充 `menuTop` 的底部越界保护（原代码只保护了水平方向，垂直方向未保护；在页面底部右键时菜单会被截断）；
- 不再引用 `e.target`，规避了 `e.target` 可能命中 tag 内部子元素（span/close-icon）导致宽度取值不稳定的问题。

## Assumptions & Decisions

- 截图中红色区域内的"提示框"确认为 TagsView 的标签右键菜单（内容为"刷新/关闭/关闭其他/关闭全部"），不是 tooltip/popover（阅卷中心页面 MarkingCenter.vue 已排查，无此类组件）；
- 修复仅涉及前端这一处定位计算，不改后端、不改样式、不改菜单功能；
- 不额外引入"滚动标签栏时关闭菜单"等增强功能，保持最小改动。

## Verification

1. 启动/刷新 `blog-admin` 前端（`npm run dev`，端口按项目实际）；
2. 登录后进入任意页面，在顶部标签上**右键**：菜单应在鼠标指针附近弹出（左上角贴近点击位置）；
3. 在视口**右缘附近**的标签上右键：菜单不超出视口右边界（向左收）；
4. 缩小窗口高度后在**底缘附近**右键：菜单不超出视口底部；
5. 菜单项（刷新/关闭/关闭其他/关闭全部）功能不受影响，点击空白处菜单正常关闭。
