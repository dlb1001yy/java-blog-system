---
name: blog-app-env-config
overview: 在 blog-app 的 config.js 中使用 uni-app 条件编译，让本地调试使用 localhost，APK 打包使用生产域名。
todos:
  - id: update-config-js
    content: "修改 blog-app/common/config.js，使用 #ifdef H5 / #ifdef APP-PLUS 条件编译区分 BASE_URL"
    status: completed
---

## 用户需求
blog-app（uni-app 项目）需要实现双环境 API 地址切换：
- **本地调试（H5 模式）**：连接 `http://localhost:8080/api`
- **APK 打包（APP-PLUS 模式）**：连接 `http://gz.aeert.com:19612/api`

通过 uni-app 条件编译在编译阶段静态区分，无需运行时判断，零性能开销。

## 核心功能
- 在 `config.js` 中使用 `// #ifdef H5` / `// #ifdef APP-PLUS` 条件编译，为两个平台导出不同的 `BASE_URL`
- `SERVER_ORIGIN` 和 `resolveFileUrl` 等下游逻辑自动跟随 `BASE_URL` 切换，无需额外修改

## 技术方案

### 实现方式
利用 uni-app 条件编译指令，在编译阶段决定 `BASE_URL` 常量值：
- H5 编译时：`BASE_URL = 'http://localhost:8080/api'`
- APP-PLUS（Android APK）编译时：`BASE_URL = 'http://gz.aeert.com:19612/api'`

`SERVER_ORIGIN` 通过 `BASE_URL.replace(/\/api$/, '')` 自动派生，无需分开定义。

### 修改范围
仅修改 **1 个文件**：`blog-app/common/config.js`

### 修改后代码结构

```js
// #ifdef H5
export const BASE_URL = 'http://localhost:8080/api'
// #endif

// #ifdef APP-PLUS
export const BASE_URL = 'http://gz.aeert.com:19612/api'
// #endif

export const TOKEN_KEY = 'uni_app_token'
export const REFRESH_TOKEN_KEY = 'uni_app_refresh_token'

// ... 其余代码不变
```

### 实现注意事项
- **条件编译是编译阶段行为**：不同平台打出的包中只包含对应分支代码，APK 中不会残留 localhost 字符串
- **SERVER_ORIGIN 自动适配**：`BASE_URL.replace(/\/api$/, '')` 会将两个 BASE_URL 正确派生为本平台对应的 origin
- **无需修改 request.js**：`BASE_URL` 是具名导出，`import { BASE_URL } from './config.js'` 在编译后自动指向对应平台的值
- **向下兼容**：H5 模式行为完全不变，仅 APP-PLUS 模式新增了生产环境地址
