# 优化收起态悬浮球可见性计划

## Summary
收起播放条后，右侧悬浮球显示不清晰、用户难以发现。原因是悬浮球仅靠 44px 封面/渐变 + 柔和阴影呈现，缺少边框和强对比，在内容丰富的页面上不显眼。本次只改 `components/PlayerBar.vue` 的收起态样式，让悬浮球"一眼可识别"。

## Current State Analysis（Phase 1 探索结论）
- 悬浮球结构：[PlayerBar.vue](file:///d:/my-project/java-blog-system/blog-app/components/PlayerBar.vue#L46-L58) `.player-fab`（fixed right:10px / top:45% / 44px 圆形 / overflow:hidden / `$shadow-floating`）
  - 有封面：显示封面图（旋转）；无封面：`.fab-placeholder` 主色渐变 + 白色音符图标
  - **问题**：`$shadow-floating` 是 `0 8px 24px rgba(15,23,42,0.12)` 的柔和阴影，无描边；封面图多为浅色/低饱和，与页面背景（`--app-bg` 米白 / 暗色 `#0B0F0E`）对比弱，悬浮球像"半透明"一样融入背景
- 主题体系：`App.vue` 定义 `--app-primary`（亮 #059669 / 暗 #34D399）等 CSS 变量，页面根节点挂 `.theme-dark` 级联；`uni.scss` 提供 `$color-primary`、`$shadow-floating` 等设计令牌
- 无封面悬浮球本身是实色渐变（不算透明），主要不显眼的是**有封面**的浅色封面场景 + 整体尺寸偏小

## Proposed Changes
仅修改 `blog-app/components/PlayerBar.vue`（模板微调 + 样式增强）：

### 1. 尺寸与定位微调
- `.player-fab`：44px → **48px**，`right: 10px → 8px`（更易点、更醒目，仍不挡内容）

### 2. 白色描边 + 边框（核心：物理隔离背景）
- `.player-fab` 增加：
  - `border: 2.5px solid #FFFFFF`（白色实描边，任何背景下都是清晰轮廓；暗色主题下白边同样高对比）
  - `box-shadow` 增强为双层：`0 0 0 1px rgba(15,23,42,0.08), 0 6px 18px rgba(15,23,42,0.35)`（外圈细描边 + 加深投影，浮起感明显）
  - 注意 `overflow: hidden` 保留（裁剪封面圆形），border 在盒模型内侧渲染于裁剪区外，轮廓仍可见

### 3. 播放中呼吸光环（动态可识别）
- 播放中给 `.player-fab` 外层增加主色发光光环动画（`::after` 不可靠于 image 裁剪容器内，改为在 `.player-fab` 上叠加 `box-shadow` 动画或外层包一个光环节点）：
  - 方案：模板中给 `.player-fab` 加 `:class="state.isPlaying ? 'playing' : ''"`，样式 `.player-fab.playing { animation: fab-glow 2s ease-in-out infinite; }`，keyframes 在主色光晕 `0 0 0 6px rgba(5,150,105,0.35)` 与 `0 0 0 10px rgba(5,150,105,0.12)` 之间脉动（暂停时静止无光环）
  - rgba 主色值需固定字面量（CSS 变量不能直接用于 rgba 拼接），亮暗主题均使用 #059669 基色光晕，对比足够
- 暂停态：无光环、无旋转，静止但白边轮廓清晰

### 4. 无封面占位增强（可选低风险，顺带做）
- `.fab-placeholder` 渐变保持，音符图标 20 → 22px

### 具体改动点（executor 直接照做）
```
模板 L47：<view v-else class="player-fab" @click.stop="onToggleCollapsed">
   改为   <view v-else :class="['player-fab', state.isPlaying ? 'playing' : '']" @click.stop="onToggleCollapsed">

样式 .player-fab：
  width/height: 48px; right: 8px;
  border: 2.5px solid #FFFFFF;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08), 0 6px 18px rgba(15, 23, 42, 0.35);

新增样式：
  .player-fab.playing { animation: fab-glow 2s ease-in-out infinite; }
  @keyframes fab-glow {
    0%, 100% { box-shadow: 0 0 0 1px rgba(15,23,42,0.08), 0 6px 18px rgba(15,23,42,0.35), 0 0 0 6px rgba(5,150,105,0.35); }
    50%      { box-shadow: 0 0 0 1px rgba(15,23,42,0.08), 0 6px 18px rgba(15,23,42,0.35), 0 0 0 11px rgba(5,150,105,0.12); }
  }

.fab-placeholder 内 Icon :size 20 → 22
```

## Assumptions & Decisions
- 不改 player.js、不改挂载页面，纯组件内视觉优化
- 白色描边在亮/暗主题下均高对比，不引入主题分支判断（保持简单）
- 呼吸光环用固定 rgba 字面量而非 CSS 变量（避免变量拼接 rgba 的兼容问题）
- 悬浮球点击行为不变（点击展开播放条）

## Verification
1. H5 端运行（`npm run dev:h5` 或现有启动方式），音乐页播放任一歌曲
2. 收起播放条 → 悬浮球应满足：
   - 浅色封面下仍有清晰白色轮廓 + 明显投影
   - 播放中：封面旋转 + 主色光晕呼吸；暂停：静止无光晕但轮廓清晰
   - 亮色/暗色主题各验证一次（切换主题）
3. 点击悬浮球能正常展开播放条；尺寸 48px 不遮挡文章列表右侧操作
4. VS Code GetDiagnostics 无新增错误
