<template>
  <view class="search-bar">
    <view class="search-icon">
      <!-- 搜索图标，颜色取自 theme.js 的 textTertiary -->
      <Icon name="search" :size="18" :color="iconColor" />
    </view>
    <input
      class="search-input"
      :value="value"
      :placeholder="placeholder"
      placeholder-class="placeholder"
      confirm-type="search"
      @input="onInput"
      @confirm="onSearch"
    />
    <!-- 清除按钮，仅有值时显示 -->
    <view v-if="value" class="clear-btn" @click="onClear">×</view>
  </view>
</template>

<script setup>
import { colors } from '../common/theme.js'
import Icon from './Icon.vue'

const props = defineProps({
  // 占位提示文字
  placeholder: { type: String, default: '搜索文章...' },
  // 输入值（受控）
  value: { type: String, default: '' }
})

const emit = defineEmits(['input', 'search'])

// 搜索图标颜色：使用 theme.js 的三级文字色
const iconColor = colors.textTertiary

// 输入事件：向父级回传最新值
const onInput = (e) => {
  emit('input', e.detail.value)
}

// 键盘 confirm 或视为搜索触发
const onSearch = () => {
  emit('search', props.value)
}

// 清空输入
const onClear = () => {
  emit('input', '')
}
</script>

<style lang="scss" scoped>
/* 容器：胶囊形灰底（父页面可用 :deep 覆盖为白底卡片） */
.search-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-full;
  padding: 0 14px;
  height: 40px;
}

/* 搜索图标 */
.search-icon {
  margin-right: $spacing-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 输入框 */
.search-input {
  flex: 1;
  font-size: 14px;
  color: var(--app-text, #0F172A);
  height: 100%;
}

/* placeholder 颜色（input 通过 placeholder-class 指定） */
.placeholder {
  color: var(--app-text-tertiary, #94A3B8);
}

/* 清除按钮 */
.clear-btn {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  /* 边框灰：跟随主题变量，暗色下仍与白叉对比清晰 */
  background: var(--app-border, #CBD5E1);
  color: #fff;
  text-align: center;
  line-height: 20px;
  font-size: 14px;
  margin-left: $spacing-xs;
  flex-shrink: 0;
}
</style>
