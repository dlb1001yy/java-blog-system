<template>
  <view class="tab-bar">
    <view
      v-for="item in list"
      :key="item.path"
      :class="['tab-item', current === item.path ? 'active' : '']"
      @click="onTap(item)"
    >
      <!-- 激活态顶部圆点指示器 -->
      <view v-if="current === item.path" class="dot"></view>
      <!-- 首页：房子轮廓（保留原 path 数据，描边 1.8） -->
      <view class="icon" v-if="item.icon === 'home'">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 11l9-7 9 7" />
          <path d="M5 10v10h14V10" />
          <path d="M9 20v-6h6v6" />
        </svg>
      </view>
      <!-- 刷题：书本轮廓（描边 1.8） -->
      <view class="icon" v-else-if="item.icon === 'book'">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
        </svg>
      </view>
      <!-- 音乐：音符轮廓（描边 1.8） -->
      <view class="icon" v-else-if="item.icon === 'music'">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 18V5l12-2v13" />
          <circle cx="6" cy="18" r="3" />
          <circle cx="18" cy="16" r="3" />
        </svg>
      </view>
      <!-- 我的：人形轮廓（保留原 path 数据，描边 1.8） -->
      <view class="icon" v-else-if="item.icon === 'mine'">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="8" r="4" />
          <path d="M4 21c0-4 4-7 8-7s8 3 8 7" />
        </svg>
      </view>
      <text class="label">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  current: { type: String, default: '' }
})

const list = [
  { path: '/pages/index/index',                  text: '首页', icon: 'home' },
  { path: '/subpkg-study/pages/interview/index', text: '刷题', icon: 'book' },
  { path: '/subpkg-music/pages/index',           text: '音乐', icon: 'music' },
  { path: '/subpkg/pages/mine/index',            text: '我的', icon: 'mine' }
]

const onTap = (item) => {
  // 使用 reLaunch 模拟 tab 切换，关闭所有页面栈、打开目标页
  uni.reLaunch({ url: item.path })
}
</script>

<style lang="scss" scoped>
/* 底部固定 tab 栏：高度 56px + 安全区底部留白 + 顶部 1px 分割线 + 顶部阴影 */
.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  height: 56px;
  padding-bottom: env(safe-area-inset-bottom);
  background: var(--app-bg-card, #FFFFFF);
  border-top: 1px solid var(--app-border, #E2E8F0);
  box-shadow: 0 -1px 8px rgba(15, 23, 42, 0.04);
}

.tab-item {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  /* 非激活态颜色 */
  color: var(--app-text-tertiary, #94A3B8);
  transition: color 0.2s;

  .icon {
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
  }

  .label {
    font-size: 11px;
    line-height: 1;
  }

  /* 激活态：主色（跟随页面根节点级联的主题变量） */
  &.active {
    color: var(--app-primary, $color-primary);
  }
}

/* 激活态顶部圆点指示器 */
.dot {
  position: absolute;
  top: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--app-primary, $color-primary);
}
</style>
