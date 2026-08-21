# Tasks
- [x] Task 1: 后端注册与邮箱登录：POST /auth/register（唯一性/强度/两次一致/BCrypt/user 角色，/auth/** 已放行）；登录含 @ 账号按邮箱映射
- [x] Task 2: SecurityConfig /admin/** 收紧为 hasRole("admin")；新增 JwtAccessDeniedHandler 返回 403 JSON
- [x] Task 3: XSS 闭环：Safelist 去 style；文章 create/update 清洗 content；简历 mySave 纯文本字段 cleanText；考试主观题答案 cleanHtml
- [x] Task 4: 门户注册页：Register.vue/Login.vue/auth.js/路由；Login 用 userStore.setUser(accessToken) 闭环；互跳入口
- [x] Task 5: 静态验证：全部通过；修复简历字段 cleanHtml→cleanText 口径问题。mvn/npm 由用户手动执行

# Task Dependencies
- Task 4 depends on Task 1
- Task 2, 3 独立
- Task 5 depends on all
