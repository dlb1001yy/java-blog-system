# 操作日志后台查看页面 Spec

## Why
后端已实现操作日志记录（@Admin 注解 + OperationLogAspect 异步落库到 sys_operation_log）并提供 `GET /admin/operation-logs/page` 分页查询接口，但管理后台无任何查看入口，审计数据"存而不见"，无法完成审计闭环。

## What Changes
- blog-admin 新增"操作日志"页面（OperationLogList.vue）：分页表格展示日志，支持按操作人/操作描述/操作状态筛选
- blog-admin 新增 `api/operationLog.js` API 模块，对接后端已有分页接口
- blog-admin 路由新增 `/operation-log` 菜单项（icon: List / Clock），自动出现在侧边栏
- 表格"参数"列点击可弹窗查看完整脱敏参数 JSON 与错误信息（审计排障必需）

## Impact
- Affected specs: add-operation-log-audit（消费其查询接口，属自然延伸，不修改后端）
- Affected code:
  - `blog-admin/src/api/operationLog.js`（新增）
  - `blog-admin/src/views/OperationLogList.vue`（新增）
  - `blog-admin/src/router/index.js`（新增路由，置于"系统设置"之前）
  - 后端无改动

## ADDED Requirements

### Requirement: 操作日志查看页面
blog-admin SHALL 提供"操作日志"管理页面，分页展示 sys_operation_log 记录，字段列：ID、操作人、操作描述、请求方式（method，带 tag 样式区分 GET/POST/PUT/DELETE 颜色）、请求路径（uri，超长省略）、客户端 IP、状态（status=1 成功绿 tag / 0 失败红 tag）、耗时（cost_ms）、操作时间（create_time 倒序）。

#### Scenario: 进入页面
- **WHEN** 管理员点击侧边栏"操作日志"菜单
- **THEN** 页面加载默认第 1 页（每页 10 条），表格按操作时间倒序展示

#### Scenario: 分页浏览
- **WHEN** 切换页码或修改每页条数
- **THEN** 表格重新加载对应页数据，分页条显示总条数

### Requirement: 筛选功能
页面 SHALL 提供筛选表单：操作人（username，输入框，后端精确匹配）、操作描述（operation，输入框，后端模糊匹配）、状态（下拉：全部/成功/失败）。点击"查询"重置到第 1 页并加载，点击"重置"清空条件重新加载。

#### Scenario: 条件筛选
- **WHEN** 输入操作人 "admin" 并点击查询
- **THEN** 仅展示 username=admin 的日志记录

### Requirement: 详情弹窗
表格操作列 SHALL 提供"详情"按钮，点击后弹窗展示该条日志全部字段，其中"请求参数"与"错误信息"以等宽字体 `<pre>` 块完整展示（不截断、保留换行），便于审计排障。

#### Scenario: 查看失败日志详情
- **WHEN** 管理员点击某条失败日志的"详情"
- **THEN** 弹窗完整展示操作人、描述、method、uri、ip、耗时、状态及完整错误信息与请求参数
