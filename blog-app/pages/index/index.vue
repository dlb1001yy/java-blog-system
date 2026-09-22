<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['page-root', isDark ? 'theme-dark' : '']">
    <!-- scroll-view 自定义下拉刷新 -->
    <scroll-view
      class="container"
      scroll-y
      :refresher-enabled="true"
      :refresher-default-style="refresherStyle"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 品牌脉冲刷新动画：App 编译器不支持 refresher 具名插槽，仅非 App 平台编译；App 回退系统原生样式 -->
      <!-- #ifndef APP-PLUS -->
      <template #refresher>
        <view class="refresher">
          <view class="refresher-dot"></view>
          <view class="refresher-dot"></view>
          <view class="refresher-dot"></view>
        </view>
      </template>
      <!-- #endif -->

      <!-- 内容包裹层：padding 置于内层，App 端 scroll-view 宿主 padding 不计入滚动高度，会导致内容被切/底部死区空白 -->
      <view class="scroll-content">

    <!-- 顶部 Hero 区：渐变按主题切换 -->
    <view class="hero" :style="{ background: isDark ? darkColors.gradientHero : colors.gradientHero }">
      <view class="hero-content">
        <view class="hero-brand">
          <image class="hero-logo" src="/static/logo.png" mode="aspectFit" />
          <text class="site-title">Java码农笔记</text>
        </view>
        <text class="site-subtitle">分享技术，记录成长</text>
      </view>
      <view v-if="statsText" class="hero-stats">
        <text class="stats-text">{{ statsText }}</text>
      </view>
    </view>

    <!-- 站点统计卡：1x4 小网格，负 margin 上浮于 Hero 底部 -->
    <view class="stats-card">
      <view v-for="s in statItems" :key="s.label" class="stat-item">
        <text class="stat-num">{{ s.value }}</text>
        <text class="stat-label">{{ s.label }}</text>
      </view>
    </view>

    <!-- 功能模块入口：3 列 2 行 -->
    <view class="modules">
      <view class="modules-grid">
        <view
          v-for="(m, i) in modules"
          :key="m.url"
          class="module-item"
          @click="goModule(m.url)"
        >
          <view :class="['icon-box', `m-${i}`]">
            <Icon :name="m.icon" :size="22" />
          </view>
          <text class="module-name">{{ m.label }}</text>
        </view>
      </view>
    </view>

    <!-- 最新文章区：加载中骨架 / 失败重试空态 / 空数据文案 / 正常列表 -->
    <view class="latest-section">
      <view class="section-head">
        <text class="section-title">最新文章</text>
        <view class="section-more" @click="goArticleList">
          <text class="more-text">查看全部</text>
          <Icon name="chevron-right" :size="14" />
        </view>
      </view>
      <!-- 加载中骨架 -->
      <Skeleton v-if="latestLoading" type="article" :count="3" />
      <!-- 加载失败空态：图标 + 文案 + 重试 -->
      <view v-else-if="latestFailed" class="latest-empty">
        <svg class="latest-empty-icon" viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
        <text class="latest-empty-text">最新文章加载失败</text>
        <view class="latest-retry" @click="loadLatest">重试</view>
      </view>
      <!-- 空数据态：非失败的空列表 -->
      <view v-else-if="latestArticles.length === 0" class="latest-empty">
        <text class="latest-empty-text">暂无文章</text>
      </view>
      <template v-else>
        <view
          v-for="item in latestArticles"
          :key="item.id"
          class="latest-item"
          @click="goDetail(item.id)"
        >
          <!-- 封面缩略 60x60，无封面用浅色图标占位 -->
          <image
            v-if="item.coverImage"
            class="latest-cover"
            :src="latestCover(item)"
            mode="aspectFill"
            lazy-load
          />
          <view v-else class="latest-cover latest-cover-placeholder">
            <Icon name="document" :size="20" />
          </view>
          <view class="latest-info">
            <text class="latest-title">{{ item.title }}</text>
            <text class="latest-date">{{ (item.createTime || '').slice(0, 10) }}</text>
          </view>
        </view>
      </template>
    </view>
    </view>

    </scroll-view>

    <!-- 全局迷你播放条：fixed 定位，置于 TabBar 之上 -->
    <PlayerBar />

    <!-- 底部 TabBar：fixed 定位，置于滚动容器外 -->
    <TabBar current="/pages/index/index" />
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { colors, darkColors, isDark, applyNavBarTheme } from '@/common/theme.js'
import { initNetworkWatch, offlineMode } from '@/common/offline.js'
import { resolveFileUrl } from '@/common/config.js'
import { optimizeImageUrl } from '@/common/imageUrl.js'
import Skeleton from '@/components/Skeleton.vue'
import Icon from '@/components/Icon.vue'
import PlayerBar from '@/components/PlayerBar.vue'
import TabBar from '@/components/TabBar.vue'

// 功能模块入口：6 项 3 列 2 行
const modules = [
  { label: '文章', icon: 'document', url: '/subpkg-article/pages/list' },
  { label: '刷题', icon: 'book',     url: '/subpkg-study/pages/interview/index' },
  { label: '考试', icon: 'edit',     url: '/subpkg-study/pages/exam/index' },
  { label: '成绩', icon: 'trophy',   url: '/subpkg-study/pages/scores/index' },
  { label: '音乐', icon: 'music',    url: '/subpkg-music/pages/index' },
  { label: '简历', icon: 'user',     url: '/subpkg/pages/resume/index' }
]

// 自定义下拉刷新进行中（绑定 refresher-triggered）
const refreshing = ref(false)

// 刷新样式：非 App 平台配合自定义插槽动画（none 隐藏系统样式）；
// App 平台编译器不支持 refresher 具名插槽，回退系统原生刷新样式
// #ifdef APP-PLUS
const refresherStyle = 'black'
// #endif
// #ifndef APP-PLUS
const refresherStyle = 'none'
// #endif

// 站点统计
const stats = ref(null)
// 题库题量（来自面试题分页接口 total）
const questionCount = ref(0)

// 最新文章区状态
const latestLoading = ref(true)
const latestFailed = ref(false)
const latestArticles = ref([])

// Hero 主行统计文本："12 篇文章 · 1.2k 浏览"
const statsText = computed(() => {
  if (!stats.value) return ''
  const articleCount = stats.value.articleCount || 0
  return `${formatCount(articleCount)} 篇文章 · ${formatCount(stats.value.viewCount)} 浏览`
})

// 统计卡 4 项：文章 / 分类 / 标签 / 题量
const statItems = computed(() => [
  { label: '文章', value: formatCount(stats.value && stats.value.articleCount) },
  { label: '分类', value: formatCount(stats.value && stats.value.categoryCount) },
  { label: '标签', value: formatCount(stats.value && stats.value.tagCount) },
  { label: '题量', value: formatCount(questionCount.value) }
])

// 数字格式化：>=10000 显示 1.2w，>=1000 显示 1.2k
const formatCount = (n) => {
  if (n == null) return '0'
  if (n >= 10000) {
    return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  }
  if (n >= 1000) {
    return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  }
  return String(n)
}

// 拉取站点统计
const loadStats = async () => {
  try {
    const res = await api.getStats()
    stats.value = res.data
  } catch (e) {
    // 静默失败，统计卡显示 0
  }
}

// 拉取题库题量：分页接口 size=1 仅取 total，失败静默置 0
const loadQuestionCount = async () => {
  if (offlineMode.value) return
  try {
    const res = await api.getInterviewQuestions({ page: 1, size: 1 })
    questionCount.value = (res.data && res.data.total) || 0
  } catch (e) {
    questionCount.value = 0
  }
}

// 拉取最新文章：取前 3 篇，失败置失败态展示重试空态
const loadLatest = async () => {
  if (offlineMode.value) {
    latestLoading.value = false
    latestFailed.value = true
    return
  }
  latestLoading.value = true
  latestFailed.value = false
  try {
    const res = await api.getLatestArticles()
    const records = Array.isArray(res.data) ? res.data : []
    latestArticles.value = records.slice(0, 3)
  } catch (e) {
    latestFailed.value = true
  } finally {
    latestLoading.value = false
  }
}

// 最新文章封面：相对路径拼接 origin + 命中 CDN 时追加压缩参数
const latestCover = (item) => optimizeImageUrl(resolveFileUrl(item && item.coverImage), 120)

// 跳转文章详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}

// 跳转文章列表页（最新文章"查看全部"）
const goArticleList = () => {
  uni.navigateTo({ url: '/subpkg-article/pages/list' })
}

// 跳转功能模块
const goModule = (url) => {
  uni.navigateTo({ url })
}

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())

// 页面加载：初始化网络监听，并行拉取统计、题量、最新文章
onLoad(() => {
  initNetworkWatch()
  loadStats()
  loadQuestionCount()
  loadLatest()
})

// 自定义下拉刷新：重新拉取统计、题量与最新文章
const onRefresh = async () => {
  refreshing.value = true
  loadStats()
  loadQuestionCount()
  await loadLatest()
  refreshing.value = false
}
</script>

<style lang="scss" scoped>
/* 页面根节点：占满整屏，主题类挂在此处向 scroll-view 及内容级联 CSS 变量 */
.page-root {
  height: 100vh;
  background: var(--app-bg, #FAFAF9);
}

/* 滚动容器：占满根节点高度形成滚动区；底部留白见内层 .scroll-content（避开 PlayerBar + TabBar） */
.container {
  height: 100%;
}

/* 内容包裹层：padding 计入滚动内容高度，滚动底界正常，内容不被切 */
.scroll-content {
  box-sizing: border-box;
  padding-bottom: calc(140px + env(safe-area-inset-bottom));
}

/* ===== 自定义下拉刷新区：三个品牌色圆点脉冲 ===== */
.refresher {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  height: 100rpx; /* ≈ 50px 刷新区域 */
}

.refresher-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: var(--app-primary, #059669);
  animation: refresher-pulse 1.2s ease-in-out infinite;
}

/* 三个圆点依次波动 */
.refresher-dot:nth-child(2) { animation-delay: 0.2s; }
.refresher-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes refresher-pulse {
  0%, 100% { transform: scale(0.6); opacity: 0.4; }
  50% { transform: scale(1.2); opacity: 1; }
}

/* ===== Hero 区 ===== */
.hero {
  height: 176px;
  box-sizing: border-box;
  padding: 24px 20px 48px;
  border-radius: 0 0 16px 16px;
  /* 内联 style 注入渐变，此处仅作兜底色 */
  background: $color-primary;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* 品牌行：logo 图形与站名水平排列 */
.hero-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 品牌图形：48px 咖啡杯 + 代码符号 */
.hero-logo {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}

.site-title {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
}

.site-subtitle {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

/* Hero 主行统计：推到底部，避开下方上浮的统计卡 */
.hero-stats {
  margin-top: auto;
  align-self: flex-start;
}

.stats-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

/* ===== 统计卡：1x4 网格，上浮于 Hero 底部 ===== */
.stats-card {
  display: flex;
  margin: -32px $spacing-lg 0;
  padding: $spacing-md 0;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-floating;
  position: relative;
  z-index: 5;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

/* 相邻项之间细分隔线 */
.stat-item + .stat-item {
  border-left: 1px solid var(--app-divider, #F5F5F4);
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text, #1C1917);
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: var(--app-text-tertiary, #A8A29E);
}

/* ===== 功能模块入口：4 列 2 行 ===== */
.modules {
  margin: $spacing-md $spacing-lg 0;
  padding: $spacing-lg $spacing-xs $spacing-sm;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.modules-grid {
  display: flex;
  flex-wrap: wrap;
}

.module-item {
  width: 33.33%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-sm 0;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 按压反馈：与 ArticleItem 一致的缩放 + 半透明 */
.module-item:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 圆形浅色底图标容器：Icon 颜色继承容器 color */
.icon-box {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-xs;
}

/* 六个模块依次配色（浅色底 + 品牌色图标） */
.m-0 { background: rgba($color-primary, 0.12); color: $color-primary; }
.m-1 { background: rgba($color-secondary, 0.12); color: $color-secondary; }
.m-2 { background: rgba($color-accent, 0.12); color: $color-accent; }
.m-3 { background: rgba($color-warning, 0.14); color: $color-warning; }
.m-4 { background: rgba($color-primary-light, 0.12); color: $color-primary-light; }
.m-5 { background: rgba($color-success, 0.12); color: $color-success; }

.module-name {
  font-size: 12px;
  color: var(--app-text, #1C1917);
  line-height: 1.4;
}

/* ===== 最新文章区 ===== */
.latest-section {
  margin-top: $spacing-lg;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $spacing-lg;
  margin-bottom: $spacing-sm;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text, #1C1917);
}

.section-more {
  display: flex;
  align-items: center;
  gap: 2px;
  color: var(--app-text-tertiary, #A8A29E);
}

.more-text {
  font-size: 12px;
  color: var(--app-text-tertiary, #A8A29E);
}

/* 最新文章空态：失败重试 / 空数据文案，白底圆角卡片居中 */
.latest-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin: 0 $spacing-lg;
  padding: 28px $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-md;
  box-shadow: $shadow-card;
}

/* 失败图标：与归档页空态同款自绘 SVG，继承容器色半透明 */
.latest-empty-icon {
  margin-bottom: 4px;
  color: var(--app-text-secondary, #57534E);
  opacity: 0.5;
}

.latest-empty-text {
  font-size: 14px;
  color: var(--app-text-secondary, #57534E);
}

/* 重试按钮：品牌色描边小按钮 */
.latest-retry {
  margin-top: 4px;
  padding: 6px 24px;
  font-size: 13px;
  color: var(--app-primary, #059669);
  border: 1px solid var(--app-primary, #059669);
  border-radius: 999px;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 按压反馈 */
.latest-retry:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 紧凑最新卡：60x60 缩略 + 标题 2 行 + 日期 */
.latest-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin: 0 $spacing-lg $spacing-sm;
  padding: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 按压反馈：与 ArticleItem 一致的缩放 + 半透明 */
.latest-item:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.latest-item:last-child {
  margin-bottom: 0;
}

.latest-cover {
  width: 60px;
  height: 60px;
  border-radius: $radius-md;
  flex-shrink: 0;
  background: var(--app-bg, #FAFAF9);
}

/* 无封面：浅底 + document 图标占位 */
.latest-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text-tertiary, #A8A29E);
}

.latest-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.latest-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text, #1C1917);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.latest-date {
  font-size: 11px;
  color: var(--app-text-tertiary, #A8A29E);
}
</style>
