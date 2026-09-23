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
      <!-- 图标走跨端 Icon 组件（CSS mask），颜色 currentColor 跟随激活态 -->
      <view class="icon">
        <Icon :name="item.icon" :size="22" />
      </view>
      <text class="label">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue'

defineProps({
  current: { type: String, default: '' }
})

const list = [
  { path: '/pages/index/index',                  text: '首页', icon: 'home' },
  { path: '/subpkg-study/pages/interview/index', text: '刷题', icon: 'book' },
  { path: '/subpkg-music/pages/index',           text: '音乐', icon: 'music' },
  // 我的：人形图标复用字典 user
  { path: '/subpkg/pages/mine/index',            text: '我的', icon: 'user' }
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
  border-top: 1px solid var(--app-border, #E7E5E4);
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
  color: var(--app-text-tertiary, #A8A29E);
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
