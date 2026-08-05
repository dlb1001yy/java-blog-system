# 修复 blog-admin 登录接口 404 问题

## 问题概述

`blog-admin` 项目请求登录接口 `http://localhost:3001/api/auth/login` 返回 404。

## 根因分析

请求链路存在路径前缀不匹配：

### 前端（blog-admin）
- [request.js](file:///d:/my-project/java-blog-system/blog-admin/src/api/request.js#L5-L8)：axios `baseURL: '/api'`
- [auth.js](file:///d:/my-project/java-blog-system/blog-admin/src/api/auth.js#L5-L7)：`request.post('/auth/login')` → 实际请求 `/api/auth/login`
- [vite.config.js](file:///d:/my-project/java-blog-system/blog-admin/vite.config.js#L15-L21)：
  - 代理 `/api` → `http://localhost:8080`
  - **`rewrite: (path) => path.replace(/^\/api/, '')` 移除了 `/api` 前缀**
  - 转发后变成 `http://localhost:8080/auth/login`

### 后端（blog-backend）
- [application.yaml](file:///d:/my-project/java-blog-system/blog-backend/src/main/resources/application.yaml#L1-L4)：`server.port: 8080` + `context-path: /api`
- 所有接口都在 `/api/*` 下提供
- [AuthController.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/controller/portal/AuthController.java#L14-L22)：`@RequestMapping("/auth")` + `@PostMapping("/login")` + context-path `/api` → 完整路径 `http://localhost:8080/api/auth/login`

### 不匹配点
- 代理转发：`http://localhost:8080/auth/login`（少了 `/api`）
- 后端期望：`http://localhost:8080/api/auth/login`（带 `/api`）
- → 返回 404

## 修改方案

修改 [vite.config.js](file:///d:/my-project/java-blog-system/blog-admin/vite.config.js#L15-L21)，**删除 `rewrite` 行**，使 `/api` 前缀保留转发到后端（与后端 `context-path: /api` 匹配）。

### 修改前
```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

### 修改后
```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 前提条件与决策

- **前提**：后端 `blog-backend` 已启动并运行在 8080 端口（404 而非连接错误，说明后端已在运行）。
- **决策**：选择修改前端代理配置而非后端 context-path，因为后端 `context-path: /api` 可能被 blog 前台门户（portal）共同使用，改动后端影响面更大。仅修改 vite.config.js 影响范围最小。

## 验证步骤

1. 修改 vite.config.js 后，重启 blog-admin 开发服务器（`npm run dev`）。
2. 浏览器访问 `http://localhost:3001/admin/login`，输入账号密码登录。
3. 打开浏览器开发者工具 Network 面板，确认 `/api/auth/login` 请求返回 200，响应体含 `token`。
4. 若仍 404，确认后端服务是否正常运行在 8080 端口。
