# Checklist

- [x] admin favicon.svg 为翡翠绿渐变咖啡杯图形，无紫色闪电残留；icons.svg 无紫色（5 处 #aa3bff→#10B981，Read 磁盘验证）
- [x] frontend favicon.svg 为翡翠绿渐变咖啡杯图形，无蓝色残留
- [x] admin 侧栏、frontend 页头、app 首页 Hero 站名均为"Java码农笔记"（含 Login.vue、index.html title 一并统一）
- [x] app 首页 Hero 含品牌图形（.hero-brand + 48px logo.png）露出
- [x] frontend 首页 Hero 为品牌绿渐变（#047857→#0D9488），无紫色
- [x] LatestBlogCard 无 #9b59b6 紫色，hover 阴影用令牌
- [x] frontend Home 模块图标配色与 app 宫格色板一致（#059669/#0D9488/#14B8A6/#2DD4BF）
- [x] app 圆角令牌为 8/12/16/20，页面无 10px/14px 硬编码圆角（rpx 体系除外）
- [x] app 按压反馈统一 scale(0.98)+opacity .9（module-item/latest-item/ArticleItem/retry 一致）
- [x] frontend 阴影令牌为 slate 基（rgba(15,23,42)），--container-padding 已定义
- [x] SkeletonCard 底色使用令牌（var(--border-color)）
- [x] admin 表格行 hover 全局定义一次（global.css），15 个视图内 :deep 重复清零
- [x] admin .hover-lift 死代码已删除，global.css .slide-fade 冲突定义已清理（layout scoped 横向版保留）
- [x] admin el-empty image-size 统一 80（3 处 60→80）
- [x] app 首页最新文章失败显示空态+重试，非静默隐藏（四分支：骨架/失败/空数据/列表）
- [x] blog-frontend 构建成功（vite build 退出码 0）
- [x] blog-admin 构建成功（vite build 退出码 0）
