# 修复 API 请求 404 问题

## 问题分析

### 现象
访问 `http://localhost:3000/api/portal/articles/hot` 返回 404

### 根本原因
Vite 代理配置与后端 context-path 冲突，导致请求路径被错误转发。

### 请求流程分析

1. **前端请求**: `http://localhost:3000/api/portal/articles/hot`

2. **Vite 代理配置** (`vite.config.js`):
   ```js
   proxy: {
     '/api': {
       target: 'http://localhost:8080',
       changeOrigin: true,
       rewrite: (path) => path.replace(/^\/api/, '')  // 问题在这里
     }
   }
   ```
   - 匹配 `/api` 前缀
   - **rewrite 移除 `/api`** → 路径变为 `/portal/articles/hot`
   - 转发到 `http://localhost:8080/portal/articles/hot`

3. **后端配置** (`application.yaml`):
   ```yaml
   server:
     port: 8080
     servlet:
       context-path: /api
   ```
   - 后端应用上下文路径: `/api`
   - Controller 映射: `@RequestMapping("/portal/articles")` + `@GetMapping("/hot")`
   - **后端实际监听路径**: `/api/portal/articles/hot`

4. **问题**:
   - 代理转发路径: `/portal/articles/hot`
   - 后端期望路径: `/api/portal/articles/hot`
   - **路径不匹配 → 404**

## 修复方案

### 修改文件
- `blog-admin/vite.config.js`

### 修改内容
移除 `rewrite` 配置，保留 `/api` 前缀直接转发给后端：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
    // 移除 rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

### 原理
- 前端请求 `/api/portal/articles/hot`
- 代理直接转发到 `http://localhost:8080/api/portal/articles/hot`
- 后端 context-path `/api` + controller `/portal/articles/hot` 正确匹配

## 验证步骤

1. 修改 `vite.config.js`
2. 重启前端开发服务器 `npm run dev`
3. 访问 `http://localhost:3000/api/portal/articles/hot`
4. 确认返回正确数据而非 404

## 相关配置参考

| 层级 | 配置项 | 值 |
|------|--------|-----|
| 前端代理 | 匹配路径 | `/api` |
| 前端代理 | 目标地址 | `http://localhost:8080` |
| 后端 | 端口 | `8080` |
| 后端 | context-path | `/api` |
| Controller | 类级映射 | `/portal/articles` |
| Controller | 方法级映射 | `/hot` |
| 完整后端路径 | | `/api/portal/articles/hot` |
