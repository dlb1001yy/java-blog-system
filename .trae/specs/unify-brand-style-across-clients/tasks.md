# Tasks

- [x] Task 1: blog-frontend 令牌对齐 admin 基准
  - [x] 1.1 修改 `global.css`：`--primary-color` 改为 `#059669`，`a:hover` 色改 `#10B981`，圆角阶梯升级为 8/12/16px，字体栈与阴影对齐 admin
  - [x] 1.2 补齐 Element Plus 主色阶梯：`--el-color-primary-light-3/5/7/8/9`、`--el-color-primary-dark-2`（取值与 admin tokens.css 一致）
  - [x] 1.3 清理 `LogoIcon.vue` 中硬编码的 `#409eff`（stroke 与渐变 stop 改用品牌绿）
  - [x] 1.4 清理 `Home.vue` 快捷入口的 `color: '#409eff'` 与 `rgba(64,158,255,0.1)` 背景
  - [x] 1.5 清理 `Upload.vue`、`ExamTaking.vue` 中 fallback 值 `#409eff` → `#059669`
- [x] Task 2: blog-app 令牌切换为绿色体系
  - [x] 2.1 修改 `common/theme.js`：colors 与 darkColors 全套替换为绿色基准（暗色 primary 用 `#34D399`，bg/card/text 对齐 admin 暗色值），同步更新 gradientPrimary/gradientHero/gradientMesh 与类型徽章配色
  - [x] 2.2 同步修改 `uni.scss` 的 `$color-*` 系列变量
  - [x] 2.3 修改 `App.vue` 中 `--app-primary/--app-primary-light/--app-secondary` 及暗色主题对应变量
  - [x] 2.4 修改 `theme.json`：light/dark 的 navBgColor（`#059669` / `#0B0F0E`）
- [x] Task 3: blog-app 硬编码颜色清理
  - [x] 3.1 `components/SharePoster.vue`：canvas 渐变 `#4F46E5→#06B6D4` 改为 `#059669→#14B8A6`
  - [x] 3.2 `pages/article/detail.vue`：fallback `#4F46E5` 与硬编码渐变改绿色
  - [x] 3.3 `subpkg/pages/mine/index.vue`：hero 渐变与 fallback 色改绿色
  - [x] 3.4 `subpkg/pages/readlater/index.vue`：type 徽章配色对齐（primary→`#059669`）
  - [x] 3.5 全局 grep 复查 `#4F46E5|#6366F1|#4338CA|#06B6D4|#8B5CF6|#818CF8` 清零（排除注释说明性文字）
- [x] Task 4: 验证
  - [x] 4.1 blog-frontend 执行 `npm run build`，构建成功
  - [x] 4.2 blog-admin 执行 `npm run build`，构建成功（确认零改动无回归）
  - [x] 4.3 三端 grep 验证旧主色清零，新令牌一致

# Task Dependencies
- Task 2、Task 3 顺序执行（同一项目内先改令牌再清硬编码）
- Task 1 与 Task 2/3 无依赖，可并行
- Task 4 依赖 Task 1、2、3 全部完成
