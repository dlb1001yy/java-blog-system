# Checklist

- [x] player.js 新增 collapsed 状态与 toggleCollapsed()，并随 app_player_prefs 持久化
- [x] PlayerBar 展开态右侧有收起按钮，点击后变为右侧悬浮球（44px，垂直居中）
- [x] 悬浮球：有封面显示旋转封面（暂停时静止），无封面显示主色渐变 + music 图标
- [x] hasTabBar prop：有 TabBar 页位于 TabBar 上方，无 TabBar 页位于底部上方 12px
- [x] taking.vue 播放条通过 bottom prop 避开固定交卷栏，不遮挡交卷按钮
- [x] 15 个页面均挂载 PlayerBar；音乐页不挂载
- [x] 收起状态切页、重启后保持（从 storage 恢复）
- [x] 未播放（无当前曲目）时任何页面不渲染播放条/悬浮球
- [x] 展开态各页面滚动内容底部留白，最后一条内容可完整展示
- [x] 亮/暗主题下播放条与悬浮球样式正常（CSS 变量级联）
- [x] 音乐页播放 → 刷题/文章详情/答题页，音乐持续且可暂停/切歌
