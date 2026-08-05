# 扩宽首页宽度

## 问题分析

**当前问题**:
- 首页使用 `.container` 类，`max-width: 1200px`
- 用户觉得首页宽度太窄，需要扩宽

## 修复方案

**修改文件**: `blog-admin/src/views/Home.vue`

### 修改内容

在 `<style scoped>` 中添加 `.home .container` 样式，将最大宽度从 1200px 增加到 1400px：

```css
.home .container {
  padding-top: 24px;
  max-width: 1400px;
}
```

## 验证步骤

1. 首页内容宽度变大
2. 其他页面保持 1200px 不变
3. 响应式布局正常
