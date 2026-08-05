# 首页样式与头部协调修复

## 问题分析

**当前问题**:
1. **首页内容未包裹在 `container` 中**：头部使用 `container` 类（max-width: 1200px, padding: 0 20px），但首页的 `banner` 和 `layout` 没有容器约束，导致宽度不一致
2. **Banner 圆角不协调**：`banner` 设置了 `border-radius: var(--radius-lg)`，与头部的全宽风格不统一
3. **整体布局未适配**：首页与其他页面（如文章列表）都缺少容器包裹

## 修复方案

**修改文件**: `blog-admin/src/views/Home.vue`

### 修改内容

1. **添加 `container` 类包裹整个内容区域**
   ```html
   <div class="home">
     <div class="container">
       <!-- Banner -->
       <!-- layout -->
     </div>
   </div>
   ```

2. **调整 Banner 样式**
   - 移除 `border-radius`，改为全宽风格与头部衔接
   - 或保留圆角但增加左右 padding 与容器对齐

3. **优化布局结构**
   - 确保 `.layout` 在容器内正确显示
   - 添加响应式适配

### 具体代码修改

**模板修改**：
```html
<template>
  <div class="home">
    <div class="container">
      <!-- Banner -->
      <div class="banner">...</div>
      <!-- 内容区域 -->
      <div class="layout">...</div>
    </div>
  </div>
</template>
```

**样式修改**：
- Banner 保持圆角，但整体在 container 内
- 确保响应式布局正常

## 验证步骤

1. 首页内容与头部宽度一致（最大宽度 1200px）
2. 移动端布局正常适配
3. Banner 与整体风格协调
