# Checklist

- [x] blog-frontend `global.css` 主色为 `#059669`，含完整 Element Plus 主色阶梯
- [x] blog-frontend 圆角阶梯为 8/12/16px，字体栈与 admin 一致
- [x] blog-frontend 无 `#409eff` / `#66b1ff` / `rgba(64,158,255` 残留（LogoIcon、Home、Upload、ExamTaking 已清理；额外清理 AppHeader/BackToTop/AboutSite/Interview/PlayerBar/Music/Scores 中的残留）
- [x] blog-app `theme.js` / `uni.scss` / `App.vue` / `theme.json` 令牌为绿色体系且相互同步
- [x] blog-app 暗色模式：primary `#34D399`、bg `#0B0F0E`、card `#131A18`
- [x] blog-app 页面/组件无 `#4F46E5` / `#6366F1` / `#4338CA` / `#818CF8` 硬编码残留（SharePoster、detail、mine、readlater、taking、category、archives 等共 31 个文件已清理，fallback 同步更新）
- [x] blog-admin 零改动（git status 确认）
- [x] blog-frontend 生产构建成功（vite build 退出码 0）
- [x] blog-admin 生产构建成功（vite build 退出码 0）
- [x] 状态色（success/warning/danger）未被改动
