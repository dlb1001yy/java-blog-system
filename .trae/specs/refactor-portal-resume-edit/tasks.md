# Tasks

- [x] Task 1: 新增前台图片上传组件
  - [x] 创建 `blog-frontend/src/components/Upload.vue`，仿 admin 端 Upload.vue（占位/预览/悬停删除/大小校验）
  - [x] 上传通过 `@/api/request` 实例 POST `/v1/storage/upload`（FormData 字段 `file`），成功后 emit 返回的 URL
- [x] Task 2: 重构 ProfileResume.vue
  - [x] 照片字段从 `el-input` 改为使用新的 Upload 组件
  - [x] 动态列表增删改为命名方法：addSkill/removeSkill、addWork/removeWork、addProject/removeProject、addEducation/removeEducation、addCertificate/removeCertificate、removeTech
  - [x] 保留审核状态展示、分享链接管理、登录校验、保存逻辑不变
- [x] Task 3: 验证
  - [x] 确认前端构建无报错（npm run build 通过）

# Task Dependencies
- Task 2 depends on Task 1
- Task 3 depends on Task 2
