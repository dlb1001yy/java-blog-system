# 简历页面添加头像展示

## Summary
接口 `api/portal/resume` 已返回 `avatar` 字段（如 `/api/uploads/1785749796261_5534.jpg`），需要在 `blog-app` 的简历页面头像位置展示该图片，并在无头像时回退到现有的「姓名首字母」占位样式。

## Current State Analysis
- 文件：`blog-app/pages/resume/index.vue`
- 当前头像区域（第 4-17 行）只渲染一个圆形背景 + 姓名首字母 `<text>{{ resume.name?.charAt(0) }}</text>`，并未使用任何 `avatar` 字段。
- `blog-app/common/config.js` 中 `BASE_URL = 'http://localhost:8080/api'`。
- 接口返回的 `avatar` 值形如 `/api/uploads/1785749796261_5534.jpg`，已自带 `/api` 前缀，因此完整 URL 应为 `http://localhost:8080` + avatar。
- `blog-app/components/ArticleItem.vue` 中 `<image>` 是 uni-app 跨端图片组件的标准用法，应同样使用 `<image>` 渲染头像。

## Proposed Changes

### 1. `blog-app/pages/resume/index.vue` — `<script setup>` 部分
- 引入 `BASE_URL`：`import { BASE_URL } from '@/common/config.js'`
- 新增 computed `avatarUrl`：
  - 当 `resume.value?.avatar` 存在时，返回 `BASE_URL.replace('/api', '') + resume.value.avatar`（即 `http://localhost:8080` + `/api/uploads/xxx.jpg`）。
  - 否则返回空字符串。
- 处理 avatar 字段可能出现的两种格式：
  - 以 `/api` 开头的相对路径 → 拼接 host。
  - 已是完整 URL（`http`/`https` 开头）→ 直接使用。

### 2. `blog-app/pages/resume/index.vue` — `<template>` 部分
将第 5-7 行的 `.avatar` 节点改为条件渲染：
- 有 `avatarUrl` 时：渲染 `<image :src="avatarUrl" class="avatar-img" mode="aspectFill" />`
- 无 `avatarUrl` 时：保留原 `<text>{{ resume.name?.charAt(0) }}</text>` 占位

### 3. `blog-app/pages/resume/index.vue` — `<style>` 部分
在 `.resume-header .avatar` 选择器内新增 `.avatar-img` 样式：
- `width: 100%; height: 100%; border-radius: 50%;` 保证填满父容器并保持圆形。
- 父级 `.avatar` 已有 `overflow` 控制需求，补加 `overflow: hidden;` 防止图片溢出圆形边界。

## Assumptions & Decisions
- **URL 拼接策略**：通过 `BASE_URL.replace('/api', '')` 推导出 host（`http://localhost:8080`），避免硬编码域名，便于生产环境切换。
- **回退策略**：保留原有「首字母占位」作为无头像时的降级展示，UI 不会出现空白圆形。
- **不修改接口与后端**：仅前端展示层改造。
- **不做错误处理**：图片加载失败时 uni-app `<image>` 默认空白即可，符合最小改动原则。

## Verification Steps
1. 启动后端服务（8080 端口）确保 `/api/portal/resume` 正常返回带 `avatar` 字段的数据。
2. 在 blog-app 中打开简历页面（`pages/resume/index.vue`），确认圆形头像位置显示实际头像图片。
3. 临时将接口返回的 `avatar` 设为空字符串或不存在，确认页面回退到「姓名首字母」展示。
4. 检查 H5 端控制台无图片 404 报错。
