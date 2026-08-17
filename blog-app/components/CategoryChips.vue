<template>
  <scroll-view scroll-x class="chips-scroll" :show-scrollbar="false">
    <view class="chips-list">
      <view
        v-for="item in list"
        :key="item.value"
        :class="['chip', active === item.value ? 'active' : '']"
        @click="onTap(item.value)"
      >
        {{ item.label }}
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
const props = defineProps({
  // 分类列表：[{ label, value }]
  list: { type: Array, default: () => [] },
  // 当前激活项的 value
  active: { type: [String, Number], default: '' }
})

const emit = defineEmits(['change'])

// 点击 chip，回传 value 给父级
const onTap = (value) => {
  emit('change', value)
}
</script>

<style lang="scss" scoped>
/* 横向滚动容器：保证内容不换行 */
.chips-scroll {
  white-space: nowrap;
  width: 100%;
}

/* chips 列表：inline-flex 实现横向排列 */
.chips-list {
  display: inline-flex;
  gap: $spacing-sm;
  padding: 0 $spacing-lg;
}

/* 单个 chip：胶囊形，非激活浅底用主题变量（暗色下可读） */
.chip {
  padding: 6px 14px;
  border-radius: $radius-full;
  font-size: 13px;
  background: var(--app-bg, #F1F5F9);
  color: var(--app-text-secondary, #64748B);
  transition: all 0.2s;
  white-space: nowrap;
}

/* 激活态：主色背景 + 白字 */
.chip.active {
  background: $color-primary;
  color: #fff;
}
</style>
