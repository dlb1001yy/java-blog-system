# 计划：简历分享页隐藏全站头部/底部/播放条等布局

## 问题分析
- 分享链接 `/resume/share/:token` 使用 [Resume.vue](d:/my-project/java-blog-system/blog-frontend/src/views/Resume.vue)，路由未设置 `hideLayout`。
- [App.vue:31](d:/my-project/java-blog-system/blog-frontend/src/App.vue#L31) 已有 `hideLayout` 机制：`route.meta.hideLayout` 为真时不渲染 AppHeader/AppFooter/PlayerBar/BackToTop，只渲染 router-view。**框架能力已存在，只是该路由没加 meta**。

## Proposed Changes

文件：`blog-frontend/src/router/index.js`（第 51-56 行）

```js
{
  path: '/resume/share/:token',
  name: 'ResumeShare',
  meta: { title: '简历', hideLayout: true },
  component: () => import('@/views/Resume.vue')
}
```

仅加一行 `hideLayout: true`。简历页本身内容（头像、各 section、打印按钮）保留。

## 验证
访问分享链接 → 页面只有简历内容卡片，无顶部导航、页脚、音乐播放条、回到顶部按钮；打印/导出 PDF 按钮仍可用。其他页面布局不受影响。

## 改动文件
- `blog-frontend/src/router/index.js`
