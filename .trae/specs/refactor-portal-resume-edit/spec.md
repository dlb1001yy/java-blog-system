# 重构前台用户简历编辑页面 Spec

## Why
前台用户简历编辑页 `blog-frontend/src/views/ProfileResume.vue` 中动态列表（技能、工作、项目、教育、证书）的增删逻辑直接内联在模板中（如 `skillList.splice(i, 1)`、`workList.push({...})`），与 admin 端 `blog-admin/src/views/ResumeEdit.vue` 已重构后的命名方法风格不一致，可读性和可维护性差；照片字段目前是手填链接输入框，体验不佳。

## What Changes
- 重构 [ProfileResume.vue](file:///d:/my-project/java-blog-system/blog-frontend/src/views/ProfileResume.vue)：
  - 动态列表的增删改为独立命名方法（addSkill/removeSkill、addWork/removeWork、addProject/removeProject、addEducation/removeEducation、addCertificate/removeCertificate、removeTech），与 admin 端 ResumeEdit.vue 保持一致。
  - 新增图片上传组件 `blog-frontend/src/components/Upload.vue`（仿 admin 端 Upload.vue：点击上传、预览、悬停删除、大小校验），替换"照片"字段的手填链接输入框。
  - 上传调用后端已有接口 `POST /api/v1/storage/upload`（multipart 参数 `file`），通过前台现有 request 实例携带 token，上传成功后取 `res.data` 作为返回值填入 `form.avatar`。
  - 保留页面原有功能不变：审核状态展示、分享链接管理、登录校验、保存逻辑（含 JSON 序列化动态列表）。
- API 层在 `blog-frontend/src/api/resume.js` 中新增 `uploadFile(formData)` 方法（或直接在 Upload 组件内通过 request 实例调用）。

## Impact
- Affected specs: 无（纯前端页面重构，接口不变）
- Affected code:
  - blog-frontend/src/views/ProfileResume.vue（重构）
  - blog-frontend/src/components/Upload.vue（新增）
  - blog-frontend/src/api/resume.js（新增上传方法，可选）

## ADDED Requirements
### Requirement: 简历照片上传
系统 SHALL 在前台简历编辑页提供照片上传组件，用户选择本地图片后上传至 `/api/v1/storage/upload`，成功后回显预览并可删除，头像 URL 存入 `form.avatar`。

#### Scenario: 上传成功
- **WHEN** 用户选择一张 ≤10MB 的图片
- **THEN** 调用上传接口成功，组件显示图片预览，`form.avatar` 为返回的 URL

#### Scenario: 超出大小限制
- **WHEN** 用户选择超过 maxSize 的图片
- **THEN** 提示"图片大小不能超过 10MB"，不发起请求

## MODIFIED Requirements
### Requirement: 简历动态列表编辑
前台简历编辑页的技能/工作经历/项目经验/教育背景/证书荣誉的增删操作 SHALL 通过独立命名方法实现，模板不再内联 splice/push 逻辑。

#### Scenario: 操作动态列表
- **WHEN** 用户点击"新增技能"或删除按钮
- **THEN** 对应列表项被正确添加/移除，页面正常渲染
