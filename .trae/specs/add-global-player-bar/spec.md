# 全局常驻音乐播放条（可收起右侧悬浮球）Spec

## Why
音乐音频实例（`common/player.js`）为模块级单例，切页后持续播放，但迷你播放条仅挂在首页。用户希望：播放音乐时，无论切换到哪个页面（刷题、看博客、考试等），播放条都一直在；不想看时收起为右侧小悬浮球，想看时点开即可控制播放/切歌。

## What Changes
- `common/player.js`：
  - `state` 新增 `collapsed`（播放条收起态，Boolean），默认 `false`
  - 新增 `toggleCollapsed()`，收起/展开状态随 PREFS 持久化（storage key `app_player_prefs` 内新增字段），切页、重启后保持
- `components/PlayerBar.vue` 改造为"展开态 + 收起态"双形态：
  - **展开态**：现有迷你播放条（封面 + 歌名/歌手 + 上一首/播放暂停/下一首 + 底部进度条），右侧新增"收起"按钮（chevron-right 图标），点击收起
  - **收起态**：右侧边缘垂直居中的 44px 圆形悬浮球——有封面显示封面圆图（播放中持续旋转、暂停静止），无封面显示主色渐变 + music 图标；点击展开回播放条
  - 新增 `hasTabBar` prop（默认 `true`）：有 TabBar 页 `bottom: calc(56px + env(safe-area-inset-bottom))`；无 TabBar 页 `bottom: calc(12px + env(safe-area-inset-bottom))`
  - 新增 `bottom` prop（String，可选）：直接覆盖 bottom 定位（特殊页面用）
  - 收起态定位 `right: 10px; top: 45%`，与页面底部任何固定栏（TabBar/交卷栏）均不冲突
- 全局挂载 `PlayerBar` 到以下 15 个页面（未播放时不渲染任何形态，不占空间）：
  - 主包：`pages/index/index.vue`（已有，改用新 prop）、`pages/article/detail.vue`
  - subpkg：`mine/index`、`mine/login`、`mine/register`、`resume/index`、`readlater/index`、`message/index`、`about/index`
  - subpkg-article：`list`、`category`、`tags`、`archives`
  - subpkg-study：`interview/index`、`exam/index`、`exam/taking`（传 `bottom` 避开底部交卷栏）、`scores/index`
- 挂载页面滚动容器/根节点底部 padding 按展开态高度预留（约 80px + 安全区），收起态底部留白略多余属预期
- **不挂载**：`subpkg-music/pages/index.vue`（自带大播放卡，避免重复）
- 不修改后端接口、不修改音频播放/切歌逻辑

## Impact
- Affected code: `blog-app/common/player.js`、`blog-app/components/PlayerBar.vue` + 15 个页面文件（模板挂载 + 底部留白样式）
- 主题兼容：双形态均使用 CSS 变量（`--app-bg-card`/`--app-primary` 等），由各页面根节点 `theme-dark` 类级联，无需额外处理

## ADDED Requirements

### Requirement: 播放条全局常驻
系统 SHALL 在除音乐页外的所有页面显示播放条（播放中且未收起时），使用户在刷题、看博客、考试等任意页面均可控制音乐。

#### Scenario: 刷题时控制音乐
- **WHEN** 用户在音乐页开始播放后进入"刷题"页
- **THEN** 音乐持续播放，TabBar 上方显示播放条，可暂停/播放、上一首/下一首

#### Scenario: 看博客时控制音乐
- **WHEN** 用户正在播放并进入文章详情页（无 TabBar）
- **THEN** 播放条贴合页面底部上方 12px 显示，内容不被永久遮挡（底部留白已预留）

### Requirement: 播放条可收起为右侧悬浮球
系统 SHALL 支持将播放条收起为右侧边缘的圆形悬浮球，并支持点击展开还原。

#### Scenario: 收起播放条
- **WHEN** 用户点击播放条右侧收起按钮
- **THEN** 播放条收起为右侧垂直居中的悬浮球（有封面显示旋转封面，无封面显示渐变音符），音乐播放不受影响

#### Scenario: 展开悬浮球
- **WHEN** 用户点击右侧悬浮球
- **THEN** 悬浮球展开还原为完整播放条，可控制播放与切歌

#### Scenario: 收起状态跨页/重启保持
- **WHEN** 用户收起播放条后切换页面或重启应用（播放中）
- **THEN** 所有页面均显示悬浮球形态（而非播放条），状态从本地存储恢复

### Requirement: 沉浸/特殊页面适配
#### Scenario: 考试答题页
- **WHEN** 用户在答题页（底部有固定交卷栏）且播放中
- **THEN** 播放条通过自定义 bottom 定位悬浮于交卷栏之上，不遮挡交卷按钮；悬浮球形态位于右侧中部，与交卷栏无冲突

#### Scenario: 未播放时不占空间
- **WHEN** 播放列表为空或无当前曲目
- **THEN** 所有页面不渲染播放条与悬浮球，布局与现状一致

#### Scenario: 音乐页不重复
- **WHEN** 用户停留在音乐页
- **THEN** 不显示迷你播放条/悬浮球（页面自带大播放卡），音乐控制由大播放卡承担
