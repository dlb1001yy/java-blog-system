<template>
  <view class="article-item" @click="goDetail">
    <!-- 封面图（若有）：lazy-load 懒加载，加载完成后加 loaded 类渐显 -->
    <image
      v-if="coverUrl && !coverError"
      :src="coverSrc"
      class="cover"
      :class="{ loaded: coverLoaded }"
      mode="aspectFill"
      lazy-load
      @error="coverError = true"
      @load="onCoverLoad"
    />
    <!-- 内容区 -->
    <view :class="['content', (coverUrl && !coverError) ? '' : 'full']">
      <!-- 顶部 meta 行：类型徽章 + 日期 -->
      <view class="meta">
        <text :class="['badge', `type-${article.type}`]">{{ typeMap[article.type] }}</text>
        <text class="date">{{ dateStr }}</text>
      </view>
      <!-- 标题 -->
      <text class="title">{{ article.title }}</text>
      <!-- 摘要 -->
      <text class="summary">{{ article.summary }}</text>
      <!-- 底部 footer：浏览数 + 分类 -->
      <view class="footer">
        <view class="stat">
          <!-- 内联 SVG eye 图标 16x16 stroke 1.8 currentColor -->
          <svg class="stat-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
          <text class="stat-text">{{ article.viewCount }}</text>
        </view>
        <view v-if="article.categoryName" class="stat">
          <!-- 内联 SVG folder 图标 16x16 stroke 1.8 currentColor -->
          <svg class="stat-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
          </svg>
          <text class="stat-text">{{ article.categoryName }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { resolveFileUrl } from '@/common/config.js'
import { optimizeImageUrl } from '@/common/imageUrl.js'

const props = defineProps({ article: Object })
const emit = defineEmits(['click'])

// 封面图加载失败标记：失败后回退纯文本布局
// 封面图加载完成标记：成功后加 loaded 类触发渐显
const coverError = ref(false)
const coverLoaded = ref(false)
// article 切换时重置加载状态，等待新封面重新加载
watch(() => props.article?.id, () => {
  coverError.value = false
  coverLoaded.value = false
})

// 封面图加载完成回调：置 true 触发 .loaded 渐显
const onCoverLoad = () => { coverLoaded.value = true }

// 封面 URL：相对路径拼接服务器 origin，完整 URL 原样返回
const coverUrl = computed(() => resolveFileUrl(props.article?.coverImage))

// 封面 src：命中 CDN 白名单时追加 OSS 处理参数（200px 宽 + webp）
const coverSrc = computed(() => optimizeImageUrl(coverUrl.value, 200))

// 文章类型映射
const typeMap = { 0: '原创', 1: '转载', 2: '翻译' }

// 日期字符串（截取前 10 位 yyyy-MM-dd），兼容 createTime 缺失
const dateStr = computed(() => {
  const t = props.article && props.article.createTime
  return t ? String(t).slice(0, 10) : ''
})

const goDetail = () => {
  emit('click', props.article.id)
}
</script>

<style lang="scss" scoped>
/* 卡片容器：白底 + 圆角 12px + 卡片阴影 + 内边距 16px + 下外边距 12px */
.article-item {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  /* 触控反馈：按下轻微缩放 */
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 按下态：缩放 + 半透明反馈 */
.article-item:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 左侧封面图：100x80 圆角 8px，默认透明，加载完成后渐显（软 LQIP 占位渐显） */
.cover {
  width: 100px;
  height: 80px;
  border-radius: $radius-md;
  flex-shrink: 0;
  background: var(--app-bg, #F1F5F9);
  opacity: 0;
  transition: opacity 0.3s ease;
}

/* 封面加载完成后渐显，露出图片 */
.cover.loaded {
  opacity: 1;
}

/* 内容区：纵向布局 */
.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

/* 顶部 meta 行 */
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

/* 类型徽章：胶囊形 */
.badge {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  color: #fff;
  line-height: 1.4;
}
/* 原创 -> 主色 */
.type-0 { background: $color-primary; }
/* 转载 -> 警告色 */
.type-1 { background: $color-warning; }
/* 翻译 -> 成功色 */
.type-2 { background: $color-success; }

/* 日期 */
.date {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 标题：最多 2 行省略 */
.title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

/* 摘要：最多 2 行省略 */
.summary {
  margin-top: 6px;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

/* 底部 footer 行 */
.footer {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-sm;
  font-size: 11px;
  color: var(--app-text-tertiary, #94A3B8);
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  display: inline-block;
  flex-shrink: 0;
}

.stat-text {
  font-size: 11px;
  color: var(--app-text-tertiary, #94A3B8);
}
</style>
