# 将歌词移入底部播放器 PlayerBar（弹出面板）

## 摘要
在 PlayerBar.vue 中新增歌词弹出面板：点击播放器左侧的歌曲信息（封面/标题），在播放器上方弹出歌词面板，支持自动滚动高亮、点击歌词行跳转。同时移除 Music.vue 页面中的歌词面板。

## 现状分析
- 歌词 LRC 数据随歌曲对象返回（`currentSong.lyric`），无单独 API。
- 歌词解析/高亮/滚动逻辑目前全部写在 `blog-frontend/src/views/Music.vue`（第 233-284 行）：
  - `parseLrc()` 解析 LRC → `{time, text}[]`
  - `activeLyricIndex` computed 基于 `player.currentTime + 0.3`
  - watch activeLyricIndex 平滑滚动居中当前行
  - 点击行 `player.seek(line.time)`
- PlayerBar.vue 为固定底栏（72px，z-index 900），左侧 `.song-info`（封面+标题+歌手）目前无点击行为。
- 播放器已有 el-popover 播放列表（placement top-end）先例，弹出面板风格类似。

## 修改方案

### 1. PlayerBar.vue（`blog-frontend/src/components/PlayerBar.vue`）
- **迁移歌词逻辑**（从 Music.vue 复制）：`parseLrc`、`lyricLines` computed、`activeLyricIndex` computed、`setLineRef`/`lineEls`、两个 watch（滚动居中、切歌重置）。
- **弹出面板实现**：不用 el-popover（内容高、需自动滚动，自绘更可控），改为自绘浮层：
  - 新增 `showLyric = ref(false)`；点击 `.song-info` 区域（`@click="showLyric = !showLyric"`，`cursor: pointer`）切换。
  - 面板 `div.lyric-panel` 定位：`position: absolute; bottom: calc(100% + 8px); left: 0;`（player-inner 内相对定位），宽约 420px，`max-height: 420px`，背景 `var(--card-bg)`、圆角、阴影、边框，随 PlayerBar 主题。
  - 面板头部：标题"歌词" + 关闭按钮（el-icon Close）。
  - 内容：`v-if="lyricLines.length"` 滚动歌词（沿用 Music.vue 的 `.lyric-scroll/.lyric-line` 样式，含 active 高亮、scale(1.05)），点击行 `player.seek(line.time)`；`v-else` 显示"暂无歌词"。
  - 点击面板外部关闭：在 document 上监听 `click`（onMounted 添加 / onBeforeUnmount 移除），若点击目标不在面板与 song-info 内则 `showLyric = false`。
  - 过渡动画：Vue `<transition name="lyric-fade">`（opacity + translateY）。
- **键盘**（可选简单实现）：Esc 关闭面板，同 document 监听。

### 2. Music.vue（`blog-frontend/src/views/Music.vue`）
- 删除模板中"歌词面板"区块（第 46-61 行）。
- 删除 script 中歌词相关代码：`lyricScrollRef`、`lineEls`、`parseLrc`、`lyricLines`、`activeLyricIndex`、`setLineRef` 及两个 watch（第 233-284 行）。
- 删除 style 中 `.lyric-card/.lyric-scroll/.lyric-line` 相关样式（第 415-444 行）。

## 假设与决策
- 不新建独立组件（如 LyricPanel.vue），逻辑直接放 PlayerBar，符合"改动最小"；若后续复用再抽组件。
- 面板挂在 player-inner 左侧（`position: relative` 加到 .player-inner），与歌曲信息对齐，移动端宽度自适应（max-width: calc(100vw - 32px)）。
- 暂无歌词时面板显示占位文案，仍可打开。

## 验证步骤
1. `cd blog-frontend && npm run dev` 启动前端。
2. 进入音乐页播放含歌词的歌曲：点击底栏封面/标题弹出歌词面板，当前行高亮并自动滚动居中。
3. 点击任意歌词行，播放进度跳转到对应时间。
4. 切歌后面板歌词刷新、回到顶部；无歌词歌曲显示"暂无歌词"。
5. 点击面板外部或关闭按钮/Esc 关闭面板；播放/暂停不受影响。
6. 音乐页不再显示歌词卡片。
