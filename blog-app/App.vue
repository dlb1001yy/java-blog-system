<script>
import { initTheme } from '@/common/theme.js'

export default {
  onLaunch: function() {
    console.log('App Launch')
    // 初始化主题（读取本地主题模式 + 跟随系统主题）
    initTheme()
    // 检查更新逻辑可在此处添加
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
/* 全局变量 */
@import '@/uni.scss';

/* 亮色主题 CSS 变量令牌（与 common/theme.js 亮色值保持一致） */
page {
  --app-primary: #059669;
  --app-primary-light: #10B981;
  --app-secondary: #0D9488;
  --app-bg: #FAFAF9;
  --app-bg-card: #FFFFFF;
  --app-text: #1C1917;
  --app-text-secondary: #57534E;
  --app-text-tertiary: #A8A29E;
  --app-border: #E7E5E4;
  --app-divider: #F5F5F4;
  --app-mask: rgba(0, 0, 0, 0.4);
  /* 底部安全区域（iPhone 刘海屏/Home 条） */
  --app-safe-bottom: env(safe-area-inset-bottom);

  background-color: var(--app-bg, #FAFAF9);
  font-size: 14px;
  color: var(--app-text, #303133);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 暗色变量集合（mixin）：页面根 view 的 .theme-dark 类与 H5 端 body 主题类复用 */
@mixin theme-dark-vars {
  --app-primary: #34D399;
  --app-primary-light: #6EE7B7;
  --app-secondary: #14B8A6;
  --app-bg: #0B0F0E;
  --app-bg-card: #131A18;
  --app-text: #F5F7F6;
  --app-text-secondary: #C3CDC9;
  --app-text-tertiary: #7E8C87;
  --app-border: #24302C;
  --app-divider: #1A2321;
  --app-mask: rgba(0, 0, 0, 0.6);
}

/* 暗黑主题：各页面根节点 :class="isDark ? 'theme-dark' : ''" 响应式绑定 */
.theme-dark {
  @include theme-dark-vars;
}

/* #ifdef H5 */
/* H5 端：theme.js 将主题类同步到 body；此规则命中 page 元素自身
   （特异性高于上方 page 声明），使 page 自身背景（overscroll 露出的底色）也随主题切换 */
body.theme-dark page {
  @include theme-dark-vars;
}
/* #endif */

/* 全局样式重置 */
view, text, image, button, input, textarea {
  box-sizing: border-box;
}

button {
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  &::after {
    border: none;
  }
}
</style>
