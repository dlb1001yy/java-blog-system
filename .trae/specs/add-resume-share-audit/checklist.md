# Checklist

- [x] sql/08-resume-share-audit.sql 存在且包含 resume_info 新增列与 resume_share 建表语句
- [x] ResumeInfo 实体含 status/auditRemark 字段
- [x] ResumeShare 实体与 Mapper 存在
- [x] mySave 保存后服务端强制 status=0（待审核）
- [x] 未审核通过的简历调用 createShare 返回错误「简历未审核通过，无法分享」
- [x] 分享接口支持 expireMinutes=null 表示永久，token 为 32 位随机串且唯一
- [x] GET /portal/resume/share/{token} 匿名可访问：有效返回简历，过期/撤销返回 404
- [x] GET /portal/resume/{userId} 仅返回 status=1 的用户简历；站长简历仍可匿名查看
- [x] AdminResumeController 提供分页/详情/审核接口且均有 @Admin 注解
- [x] 前台 ProfileResume 显示审核状态与拒绝原因，可生成/复制/撤销分享链接
- [x] 前台 /resume/share/:token 路由可用，失效链接显示空状态提示
- [x] blog-admin 简历管理页支持列表筛选、详情查看、通过/拒绝（含拒绝原因）
- [x] 后端与前端代码无 VSCode 诊断错误（mvn/npm 编译构建由用户手动执行，不由代理运行）
