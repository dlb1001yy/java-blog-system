# 三端品牌样式统一 Spec

## Why
blog-admin（翡翠绿 #059669）、blog-frontend（Element 默认蓝 #409eff）、blog-app（靛蓝 #4F46E5）三端主色、圆角、阴影、字体令牌各不相同，同一产品线视觉割裂。需要以 admin 现有设计令牌为基准做令牌级统一，保持各端布局与组件结构不变。

## What Changes
- 以 blog-admin 的 `tokens.css` 为唯一品牌基准（翡翠绿主色体系）
- **blog-frontend**：`global.css` 主色/圆角/阴影/字体栈对齐 admin；补齐 Element Plus 主色阶梯（light-3~9 / dark-2）；清理硬编码旧蓝色（LogoIcon、Home、a:hover、各组件 fallback 值）
- **blog-app**：`common/theme.js`、`uni.scss`、`App.vue` CSS 变量、`theme.json` 全部切换为绿色体系；清理页面/组件中硬编码的靛蓝/青/紫（SharePoster、detail、mine、readlater 等）
- **blog-admin**：作为基准不动（零改动）
- 状态色（success/warning/danger）保持各端现状，不在本次范围
- 暗色模式：仅统一 blog-app 暗色令牌为 admin 的深绿夜色系；blog-frontend 暗色模式不在本次范围（用户选择"令牌级统一"）

### 统一后的品牌令牌基准（源自 blog-admin）
| 令牌 | 值 |
|---|---|
| primary | `#059669` |
| primary-light | `#10B981` |
| primary-dark | `#047857` |
| secondary | `#0D9488` |
| accent | `#14B8A6` |
| gradient-primary | `linear-gradient(135deg, #059669 0%, #14B8A6 100%)` |
| 圆角 sm/md/lg/xl | `8/12/16/20px` |
| 暗色 bg/card | `#0B0F0E` / `#131A18` |
| 暗色 text 主/次 | `#F5F7F6` / `#C3CDC9` |
| 字体栈 | `'Inter', 'Plus Jakarta Sans', -apple-system, ... sans-serif` |

## Impact
- Affected code:
  - `blog-frontend/src/assets/styles/global.css`（核心）
  - `blog-frontend/src/components/LogoIcon.vue`、`Upload.vue`
  - `blog-frontend/src/views/Home.vue`、`ExamTaking.vue`
  - `blog-app/common/theme.js`（核心）、`blog-app/uni.scss`、`blog-app/App.vue`、`blog-app/theme.json`
  - `blog-app` 中含硬编码靛蓝/青/紫的页面（SharePoster.vue、pages/article/detail.vue、subpkg/pages/mine/index.vue、subpkg/pages/readlater/index.vue 等，以 grep 结果为准）
- blog-admin 零改动
- 各端布局、组件结构、业务逻辑均不变

## ADDED Requirements

### Requirement: 统一品牌令牌
三端（blog-admin / blog-frontend / blog-app）的 CSS/SCSS/JS 设计令牌 SHALL 使用同一套品牌基准：主色 `#059669`、辅色 `#0D9488`、强调色 `#14B8A6`、渐变 `135deg #059669→#14B8A6`、圆角阶梯 8/12/16/20px。

#### Scenario: 令牌一致
- **WHEN** 检查三端令牌定义文件
- **THEN** 主色相关令牌值与基准表完全一致

#### Scenario: 硬编码色清理
- **WHEN** 在三端源码中搜索 `#409eff`、`#66b1ff`、`#4F46E5`、`#6366F1`、`#4338CA`、`#818CF8`
- **THEN** 零匹配（fallback 值同步更新为对应新色）

#### Scenario: Element Plus 组件换肤
- **WHEN** blog-frontend 中使用 el-button / el-tag / el-pagination 等组件
- **THEN** 组件主色呈现翡翠绿（hover/active 等状态使用补齐的 light/dark 阶梯色）

#### Scenario: blog-app 暗色模式协调
- **WHEN** blog-app 切换到暗色主题
- **THEN** 主色呈现 `#34D399`（emerald-400），背景/卡片为深绿夜色系 `#0B0F0E`/`#131A18`

#### Scenario: 构建可用
- **WHEN** 执行 blog-admin 与 blog-frontend 的生产构建
- **THEN** 构建成功无错误
