<template>
  <view class="loading-dots">
    <view class="dot" :style="dotStyle"></view>
    <view class="dot dot-2" :style="dotStyle"></view>
    <view class="dot dot-3" :style="dotStyle"></view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 圆点直径（px）
  size: { type: Number, default: 6 }
})

// 圆点尺寸（颜色继承 currentColor，随父容器文字色变化）
const dotStyle = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px'
}))
</script>

<style lang="scss" scoped>
/* 三点跳动加载指示器：水平排列，依次向上弹跳 */
.loading-dots {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 16px;
}

.dot {
  border-radius: 50%;
  background: currentColor;
  animation: dot-bounce 1s ease-in-out infinite;
}

.dot-2 { animation-delay: 0.15s; }
.dot-3 { animation-delay: 0.3s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}
</style>
