<template>
  <view class="skeleton">
    <!-- 文章卡片骨架：count 个 -->
    <template v-if="type === 'article'">
      <view v-for="i in count" :key="i" class="article-card">
        <!-- 左侧灰色方块 -->
        <view class="skeleton-block cover"></view>
        <!-- 右侧三行灰色横条 -->
        <view class="lines">
          <view class="skeleton-block line line-title"></view>
          <view class="skeleton-block line line-summary"></view>
          <view class="skeleton-block line line-meta"></view>
        </view>
      </view>
    </template>

    <!-- 详情页骨架：大标题横条 + 多行内容横条 -->
    <template v-else-if="type === 'detail'">
      <view class="detail">
        <view class="skeleton-block detail-title"></view>
        <view class="skeleton-block detail-meta"></view>
        <view class="skeleton-block detail-cover"></view>
        <view v-for="i in 6" :key="i" :class="['skeleton-block', 'detail-line', `detail-line-${i % 3}`]"></view>
      </view>
    </template>
  </view>
</template>

<script setup>
defineProps({
  // 骨架类型：article 文章卡片 / detail 详情页
  type: { type: String, default: 'article' },
  // article 类型下渲染的卡片数量
  count: { type: Number, default: 3 }
})
</script>

<style lang="scss" scoped>
/* shimmer 动画：linear-gradient 从左到右滑动 */
@keyframes shimmer {
  0% { background-position: -468px 0; }
  100% { background-position: 468px 0; }
}

/* 骨架块基础样式：灰底 + 渐变扫光（颜色跟随主题变量） */
.skeleton-block {
  background: linear-gradient(90deg, var(--app-border, #E2E8F0) 25%, var(--app-bg, #F1F5F9) 50%, var(--app-border, #E2E8F0) 75%);
  background-size: 936px 100%;
  animation: shimmer 1.5s infinite linear;
  border-radius: $radius-md;
}

/* ===== article 类型 ===== */
.article-card {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.cover {
  width: 100px;
  height: 80px;
  flex-shrink: 0;
}

.lines {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  justify-content: center;
}

.line {
  height: 14px;
}

.line-title { width: 60%; }
.line-summary { width: 90%; }
.line-meta { width: 40%; height: 12px; }

/* ===== detail 类型 ===== */
.detail {
  padding: $spacing-lg;
}

.detail-title {
  height: 24px;
  width: 70%;
  margin-bottom: $spacing-md;
}

.detail-meta {
  height: 12px;
  width: 30%;
  margin-bottom: $spacing-lg;
}

.detail-cover {
  width: 100%;
  height: 160px;
  margin-bottom: $spacing-lg;
}

.detail-line {
  height: 14px;
  margin-bottom: $spacing-sm;
}

/* 让每行宽度有变化，更贴近真实段落 */
.detail-line-0 { width: 100%; }
.detail-line-1 { width: 96%; }
.detail-line-2 { width: 70%; }
</style>
