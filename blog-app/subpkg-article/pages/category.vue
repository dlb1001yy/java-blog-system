<template>
  <view :class="['category-page', isDark ? 'theme-dark' : '']">
    <!-- 分类网格：2 列卡片 -->
    <view class="grid">
      <!-- 首次加载骨架：白卡 + 左侧方块 + 右侧两横条 -->
      <template v-if="loading">
        <view v-for="i in 4" :key="'sk' + i" class="card">
          <view class="skeleton-block icon-block"></view>
          <view class="card-lines">
            <view class="skeleton-block line line-name"></view>
            <view class="skeleton-block line line-count"></view>
          </view>
        </view>
      </template>

      <template v-else>
        <view
          v-for="item in categories"
          :key="item.id"
          class="card"
          @click="goList(item)"
        >
          <view class="icon-wrap">
            <Icon name="folder" :size="22" />
          </view>
          <view class="card-info">
            <text class="name">{{ item.name }}</text>
            <!-- 后端未返回文章数时不展示该行 -->
            <text v-if="item.articleCount != null" class="count">{{ item.articleCount }} 篇文章</text>
          </view>
          <view class="arrow">
            <Icon name="chevron-right" :size="16" />
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="categories.length === 0" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
          </svg>
          <text class="empty-text">暂无分类</text>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import Icon from '@/components/Icon.vue'

// 分类列表
const categories = ref([])
const loading = ref(true)

// 拉取分类列表（loading:false，首屏由骨架呈现加载态）
const loadCategories = async () => {
  loading.value = true
  try {
    const res = await api.getCategories({ loading: false })
    categories.value = res.data || []
  } catch (e) {
    categories.value = []
  } finally {
    loading.value = false
  }
}

// 点击分类卡片 → 跳转分类文章列表
const goList = (item) => {
  uni.navigateTo({
    url: `/subpkg-article/pages/list?categoryId=${item.id}&categoryName=${encodeURIComponent(item.name)}`
  })
}

onLoad(() => {
  loadCategories()
})

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())
</script>

<style lang="scss" scoped>
/* 页面根节点：占满整屏，底部预留 24px（无 TabBar） */
.category-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  padding: 12px 16px calc(24px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 2 列网格 */
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

/* 分类卡片：白底 + 圆角 + 阴影，占一行之半（减去半个 gap） */
.card {
  width: calc(50% - #{$spacing-md} / 2);
  box-sizing: border-box;
  padding: $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 触控反馈：按下轻微缩放 + 半透明 */
.card:active {
  transform: scale(0.97);
  opacity: 0.9;
}

/* 图标底：主色 12% 透明度的圆角方块，图标取主色 */
.icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  background: rgba($color-primary, 0.12);
  color: var(--app-primary, #4F46E5);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

/* 分类名：最多 1 行省略 */
.name {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 文章数（可选字段） */
.count {
  margin-top: 4px;
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 右侧箭头 */
.arrow {
  color: var(--app-text-tertiary, #94A3B8);
  flex-shrink: 0;
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
  border-radius: $radius-md;
}

.icon-block {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: $radius-md;
}

.card-lines {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.line {
  height: 14px;
}

.line-name { width: 70%; }
.line-count { width: 45%; height: 12px; }

/* 空状态：横跨整行 */
.empty {
  width: 100%;
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
