<template>
  <view :class="['tags-page', isDark ? 'theme-dark' : '']">
    <!-- 首次加载骨架：整行胶囊块 -->
    <view v-if="loading" class="cloud">
      <view v-for="i in 8" :key="'sk' + i" :class="['skeleton-block', 'tag', `sk-${i % 4}`]"></view>
    </view>

    <!-- 标签云 -->
    <template v-else>
      <view class="cloud">
        <view
          v-for="tag in tags"
          :key="tag.id"
          class="tag"
          :style="tagStyle(tag)"
          @click="goList(tag)"
        >
          {{ tag.name }}
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="tags.length === 0" class="empty">
        <svg class="empty-icon" viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
          <line x1="7" y1="7" x2="7.01" y2="7" />
        </svg>
        <text class="empty-text">暂无标签</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'

// 标签列表
const tags = ref([])
const loading = ref(true)

// 浅色胶囊底色池：主色/辅色/强调色的浅色变体（亮色 / 暗色两套）
const lightBgPool = ['#EEF2FF', '#E0F2FE', '#F3E8FF', '#ECFDF5', '#FEF3C7']
const darkBgPool = ['#312E81', '#155E75', '#4C1D95', '#064E3B', '#78350F']

// 字号池：按 tag.id % 5 伪随机取 14-22px
const fontPool = [14, 15, 17, 19, 22]

// 标签样式：字号按 id % 5，底色按浅色池 id % 5 取（暗色下换深色变体池）
const tagStyle = (tag) => {
  const id = Number(tag.id) || 0
  const idx = id % 5
  const bgPool = isDark.value ? darkBgPool : lightBgPool
  return {
    fontSize: fontPool[idx] + 'px',
    background: bgPool[idx]
  }
}

// 拉取标签列表（loading:false，首屏由骨架呈现加载态）
const loadTags = async () => {
  loading.value = true
  try {
    const res = await api.getTags({ loading: false })
    tags.value = res.data || []
  } catch (e) {
    tags.value = []
  } finally {
    loading.value = false
  }
}

// 点击标签 → 跳转标签文章列表
const goList = (tag) => {
  uni.navigateTo({
    url: `/subpkg-article/pages/list?tagId=${tag.id}&tagName=${encodeURIComponent(tag.name)}`
  })
}

onLoad(() => {
  loadTags()
})

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())
</script>

<style lang="scss" scoped>
/* 页面根节点：占满整屏，底部预留 24px（无 TabBar） */
.tags-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  padding: $spacing-xl $spacing-lg calc(24px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 标签云：flex wrap 居中排布 */
.cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: $spacing-md;
}

/* 单个标签：胶囊形，底色由内联 style 注入（浅色随机变体） */
.tag {
  padding: 8px 18px;
  border-radius: $radius-full;
  color: var(--app-text, #0F172A);
  line-height: 1.4;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 触控反馈：按下轻微缩放 + 半透明 */
.tag:active {
  transform: scale(0.94);
  opacity: 0.85;
}

/* ===== 骨架块（与 Skeleton 组件同款扫光） ===== */
@keyframes shimmer {
  0% { background-position: -468px 0; }
  100% { background-position: 468px 0; }
}

.skeleton-block {
  background: linear-gradient(90deg, var(--app-border, #E2E8F0) 25%, var(--app-bg, #F1F5F9) 50%, var(--app-border, #E2E8F0) 75%);
  background-size: 936px 100%;
  animation: shimmer 1.5s infinite linear;
  height: 34px;
}

/* 骨架胶囊宽度错落 */
.sk-0 { width: 76px; }
.sk-1 { width: 96px; }
.sk-2 { width: 64px; }
.sk-3 { width: 110px; }

/* 空状态 */
.empty {
  padding: 56px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--app-text-tertiary, #94A3B8);
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-text {
  font-size: 14px;
  color: var(--app-text-tertiary, #94A3B8);
}
</style>
