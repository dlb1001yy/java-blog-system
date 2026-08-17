<template>
  <view :class="['nav-bar', transparent ? 'transparent' : '']">
    <!-- 状态栏占位（高度跟随系统状态栏） -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <!-- 导航行：高 44px，左返回 / 中标题 / 右插槽 -->
    <view class="nav-row">
      <!-- 左侧返回按钮 -->
      <view v-if="showBack" class="nav-back" @click="goBack">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M15 18 L9 12 L15 6" />
        </svg>
      </view>
      <!-- 中间标题：优先 title prop，否则用 name="title" 插槽 -->
      <view class="nav-title">
        <text v-if="title" class="title-text">{{ title }}</text>
        <slot v-else name="title"></slot>
      </view>
      <!-- 右侧插槽 -->
      <view class="nav-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup>
// 自定义导航栏：状态栏占位 + 44px 导航行，普通流式布局（非 fixed）

defineProps({
  // 标题文字（传入时优先于 title 插槽）
  title: { type: String, default: '' },
  // 是否显示左侧返回按钮
  showBack: { type: Boolean, default: true },
  // 透明模式：不画背景与底边（用于浮在渐变背景上）
  transparent: { type: Boolean, default: false }
})

// 状态栏高度：优先 getWindowInfo，回退 getSystemInfoSync，读取失败保持 0
let statusBarHeight = 0
try {
  statusBarHeight = uni.getWindowInfo ? uni.getWindowInfo().statusBarHeight : uni.getSystemInfoSync().statusBarHeight
} catch (e) {
  // 读取失败时保持 0（H5 等平台状态栏高度即为 0）
}

// 返回上一页；无上一页时回退重启到首页
const goBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.reLaunch({ url: '/pages/index/index' })
    }
  })
}
</script>

<style lang="scss" scoped>
/* 根节点：卡片底色 + 底边分割线（跟随主题 CSS 变量） */
.nav-bar {
  background: var(--app-bg-card, #FFFFFF);
  border-bottom: 1px solid var(--app-border, #E2E8F0);
}

/* 透明模式：不画背景与底边 */
.nav-bar.transparent {
  background: transparent;
  border-bottom: none;
}

/* 状态栏占位 */
.status-bar {
  width: 100%;
}

/* 导航行：高 44px */
.nav-row {
  position: relative;
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 12px;
}

/* 左侧返回按钮 */
.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text, #0F172A);
  flex-shrink: 0;
}

/* 中间标题：绝对定位水平居中，两侧留出按钮空间 */
.nav-title {
  position: absolute;
  left: 88px;
  right: 88px;
  top: 0;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 标题文字：单行省略 */
.title-text {
  max-width: 100%;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧插槽区域：靠右对齐 */
.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
</style>
