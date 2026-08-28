# Checklist

- [x] 首页顶部展示平台概览区块：定位介绍覆盖博客/面试题库/在线考试/音乐/简历五大模块，含问候语、日期与模块快捷入口
- [x] 统计卡片共 8 张，数值来自 overview 聚合接口，加载失败不抛错（fetchOverview catch 静默，卡片显示 0）
- [x] 「模块内容分布」环形图正确渲染 9 类模块数据（文章/面试题/试题/试卷/简历/音乐/友链/留言/评论），悬停显示数量与占比
- [x] 近 7 天文章趋势图、分类文章统计图保留且正常渲染
- [x] 待处理事项（待审核评论/留言、草稿箱、待阅卷、待审核简历、今日新增）展示正常，点击可跳转
- [x] 服务状态卡片展示 Redis/数据库/磁盘/JVM 状态及运行时长、JDK 版本、操作系统、后端版本/构建时间
- [x] 明暗主题切换后页面文字、卡片、图表颜色正确适配（getThemeColors + watch 重建 option 逻辑保留）
- [x] 后端新增接口 /admin/dashboard/overview、/admin/dashboard/module-stats 可用，system-status 含新增字段（uptime/jdkVersion/osName/osVersion/appVersion/buildTime），原接口未删除
- [x] 前后端接口字段命名一致（overview/module-stats/system-status/todo 响应字段与前端取值逐一核对通过；IDE 诊断为空）
- [x] 响应式布局在 xs/sm/lg 断点下不塌陷（:xs=12 :sm=6 卡片、:xs=24 :lg=16/8 与 :lg=12/14/10 图表行）
- [ ] （用户手动执行）mvn compile 通过
- [ ] （用户手动执行）npm run build 通过
