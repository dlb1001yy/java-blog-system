<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['page-root', isDark ? 'theme-dark' : '']">
    <!-- scroll-view 自定义下拉刷新 + 触底加载 -->
    <scroll-view
      class="container"
      scroll-y
      :refresher-enabled="true"
      :refresher-default-style="refresherStyle"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
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

    <!-- 顶部 Hero 区：渐变按主题切换 -->
    <view class="hero" :style="{ background: isDark ? darkColors.gradientHero : colors.gradientHero }">
      <view class="hero-content">
        <text class="site-title">Java码农笔记</text>
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

    <!-- 功能模块入口：4 列 2 行 -->
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

    <!-- 最新文章区：失败静默隐藏 -->
    <view v-if="showLatestSection" class="latest-section">
      <view class="section-head">
        <text class="section-title">最新文章</text>
        <view class="section-more" @click="goArticleList">
          <text class="more-text">查看全部</text>
          <Icon name="chevron-right" :size="14" />
        </view>
      </view>
      <!-- 加载中骨架 -->
      <Skeleton v-if="latestLoading" type="article" :count="3" />
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

    <!-- 搜索栏 -->
    <view class="search-wrap">
      <SearchBar
        placeholder="搜索文章..."
        :value="searchKeyword"
        @input="onSearchInput"
        @search="onSearch"
      />
    </view>

    <!-- 搜索结果数（仅搜索态） -->
    <view v-if="isSearching" class="search-info">
      <text class="search-info-text">找到 {{ searchTotal }} 篇相关文章</text>
    </view>

    <!-- 分类筛选 chips（搜索态隐藏） -->
    <view v-else class="chips-wrap">
      <CategoryChips :list="categoryChips" :active="activeCategoryId" @change="onCategoryChange" />
      <CategoryChips :list="types" :active="activeType" @change="onTypeChange" />
    </view>

    <!-- 文章列表 -->
    <view class="list">
      <!-- 首次加载 / 切换筛选：骨架 -->
      <Skeleton v-if="showSkeleton" type="article" :count="3" />
      <template v-else>
        <ArticleItem
          v-for="item in list"
          :key="item.id"
          :article="item"
          @click="goDetail"
        />
        <!-- 空状态 -->
        <view v-if="list.length === 0" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <path d="M14 2v6h6" />
            <path d="M16 13H8" />
            <path d="M16 17H8" />
            <path d="M10 9H8" />
          </svg>
          <text class="empty-text">暂无文章</text>
        </view>
        <!-- 加载更多（三点跳动动画）/ 没有更多 -->
        <view v-if="list.length > 0 && loading" class="status">
          <LoadingDots :size="6" />
        </view>
        <view v-if="list.length > 0 && !loading && !hasMore" class="status">没有更多了</view>
      </template>
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
import { initNetworkWatch, offlineMode, cacheArticleList, getCachedArticleList } from '@/common/offline.js'
import { resolveFileUrl } from '@/common/config.js'
import { optimizeImageUrl } from '@/common/imageUrl.js'
import ArticleItem from '@/components/ArticleItem.vue'
import SearchBar from '@/components/SearchBar.vue'
import CategoryChips from '@/components/CategoryChips.vue'
import Skeleton from '@/components/Skeleton.vue'
import LoadingDots from '@/components/LoadingDots.vue'
import Icon from '@/components/Icon.vue'
import PlayerBar from '@/components/PlayerBar.vue'
import TabBar from '@/components/TabBar.vue'

// 类型 chips：固定列表（全部/原创/转载/翻译）
const types = [
  { label: '全部', value: null },
  { label: '原创', value: 0 },
  { label: '转载', value: 1 },
  { label: '翻译', value: 2 }
]

// 功能模块入口：8 项 4 列 2 行
const modules = [
  { label: '文章', icon: 'document', url: '/subpkg-article/pages/list' },
  { label: '刷题', icon: 'book',     url: '/subpkg-study/pages/interview/index' },
  { label: '考试', icon: 'edit',     url: '/subpkg-study/pages/exam/index' },
  { label: '成绩', icon: 'trophy',   url: '/subpkg-study/pages/scores/index' },
  { label: '音乐', icon: 'music',    url: '/subpkg-music/pages/index' },
  { label: '留言', icon: 'mail',     url: '/subpkg/pages/message/index' },
  { label: '简历', icon: 'user',     url: '/subpkg/pages/resume/index' },
  { label: '关于', icon: 'location', url: '/subpkg/pages/about/index' }
]

// 列表与分页状态
const list = ref([])
const page = ref(1)
const loading = ref(false)
const hasMore = ref(true)
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

// 站点统计与分类
const stats = ref(null)
const categories = ref([])
// 题库题量（来自面试题分页接口 total）
const questionCount = ref(0)

// 最新文章区状态
const latestLoading = ref(true)
const latestFailed = ref(false)
const latestArticles = ref([])

// 筛选项
const activeCategoryId = ref(null)
const activeType = ref(null)

// 搜索状态
const searchKeyword = ref('')
const isSearching = ref(false)
const searchTotal = ref(0)

// 请求序列号：快速切换分类/搜索时丢弃过期响应，避免数据错位
let fetchSeq = 0

// 首次加载或切换筛选时显示骨架
const showSkeleton = computed(() => loading.value && list.value.length === 0)

// 分类 chips：全部 + 接口分类
const categoryChips = computed(() => {
  return [{ label: '全部', value: null }].concat(
    categories.value.map(c => ({ label: c.name, value: c.id }))
  )
})

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

// 最新文章区显隐：加载中或加载成功且有数据时显示，失败静默隐藏
const showLatestSection = computed(() => {
  return latestLoading.value || (!latestFailed.value && latestArticles.value.length > 0)
})

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

// 拉取文章列表 / 搜索结果
const fetchData = async () => {
  // 离线模式：不发请求，第一页读缓存降级（无缓存则展示空态）
  if (offlineMode.value) {
    if (page.value === 1) {
      const cached = getCachedArticleList()
      list.value = cached || []
      hasMore.value = false
      uni.showToast({ title: '已进入离线阅读模式', icon: 'none' })
    }
    return
  }
  if (!hasMore.value) return
  const seq = ++fetchSeq
  loading.value = true
  try {
    let res
    if (isSearching.value) {
      res = await api.searchArticles({
        keyword: searchKeyword.value,
        current: page.value,
        size: 10
      })
    } else {
      const params = { current: page.value, size: 10 }
      // null 参数不发送，避免后端收到字符串 "null"
      if (activeCategoryId.value != null) params.categoryId = activeCategoryId.value
      if (activeType.value != null) params.type = activeType.value
      res = await api.getArticles(params)
    }
    // 被后续请求取代时丢弃结果
    if (seq !== fetchSeq) return
    const records = (res.data && res.data.records) || []
    if (page.value === 1) {
      list.value = records
      // 成功拿到第一页数据后写入离线缓存
      cacheArticleList(records)
    } else {
      list.value = list.value.concat(records)
    }
    hasMore.value = records.length === 10
    if (isSearching.value && page.value === 1) {
      searchTotal.value = (res.data && res.data.total) || records.length
    }
  } catch (e) {
    if (seq === fetchSeq) {
      // 网络类失败：尝试离线缓存降级
      if (page.value === 1) {
        const cached = getCachedArticleList()
        if (cached && cached.length) {
          list.value = cached
          hasMore.value = false
          uni.showToast({ title: '已进入离线阅读模式', icon: 'none' })
          return
        }
      }
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  } finally {
    if (seq === fetchSeq) {
      loading.value = false
    }
  }
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

// 拉取最新文章：取前 3 篇，失败静默隐藏该区
const loadLatest = async () => {
  if (offlineMode.value) {
    latestLoading.value = false
    latestFailed.value = true
    return
  }
  latestLoading.value = true
  try {
    const res = await api.getLatestArticles()
    const records = Array.isArray(res.data) ? res.data : []
    latestArticles.value = records.slice(0, 3)
    if (latestArticles.value.length === 0) latestFailed.value = true
  } catch (e) {
    latestFailed.value = true
  } finally {
    latestLoading.value = false
  }
}

// 最新文章封面：相对路径拼接 origin + 命中 CDN 时追加压缩参数
const latestCover = (item) => optimizeImageUrl(resolveFileUrl(item && item.coverImage), 120)

// 拉取分类列表
const loadCategories = async () => {
  try {
    const res = await api.getCategories()
    categories.value = res.data || []
  } catch (e) {
    categories.value = []
  }
}

// 搜索栏输入：清空时退出搜索
const onSearchInput = (val) => {
  searchKeyword.value = val
  if (!val && isSearching.value) {
    exitSearch()
  }
}

// 搜索栏 confirm：进入搜索
const onSearch = (val) => {
  const kw = (val || '').trim()
  if (!kw) {
    if (isSearching.value) exitSearch()
    return
  }
  isSearching.value = true
  searchKeyword.value = kw
  page.value = 1
  hasMore.value = true
  list.value = []
  fetchData()
}

// 退出搜索，恢复普通列表
const exitSearch = () => {
  isSearching.value = false
  searchKeyword.value = ''
  page.value = 1
  hasMore.value = true
  list.value = []
  fetchData()
}

// 切换分类
const onCategoryChange = (val) => {
  if (activeCategoryId.value === val) return
  activeCategoryId.value = val
  page.value = 1
  hasMore.value = true
  list.value = []
  fetchData()
}

// 切换类型
const onTypeChange = (val) => {
  if (activeType.value === val) return
  activeType.value = val
  page.value = 1
  hasMore.value = true
  list.value = []
  fetchData()
}

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

// 页面加载：初始化网络监听，并行拉取统计、题量、分类、最新文章、文章
onLoad(() => {
  initNetworkWatch()
  loadStats()
  loadQuestionCount()
  loadCategories()
  loadLatest()
  fetchData()
})

// scroll-view 触底加载更多
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  fetchData()
}

// 自定义下拉刷新：重置分页后重新拉取，同时刷新统计与最新文章
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  list.value = []
  loadStats()
  loadQuestionCount()
  loadLatest()
  await fetchData()
  refreshing.value = false
}
</script>

<style lang="scss" scoped>
/* 页面根节点：占满整屏，主题类挂在此处向 scroll-view 及内容级联 CSS 变量 */
.page-root {
  height: 100vh;
  background: var(--app-bg, #F1F5F9);
}

/* 滚动容器：占满根节点高度形成滚动区，底部留白避开 PlayerBar + TabBar */
.container {
  height: 100%;
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
  background: var(--app-primary, #4F46E5);
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
  border-radius: 14px;
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
  border-left: 1px solid var(--app-divider, #F1F5F9);
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* ===== 功能模块入口：4 列 2 行 ===== */
.modules {
  margin: $spacing-md $spacing-lg 0;
  padding: $spacing-lg $spacing-xs $spacing-sm;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: 14px;
  box-shadow: $shadow-card;
}

.modules-grid {
  display: flex;
  flex-wrap: wrap;
}

.module-item {
  width: 25%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-sm 0;
  transition: opacity 0.15s ease;
}

/* 按压反馈 */
.module-item:active {
  opacity: 0.6;
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

/* 八个模块依次配色（浅色底 + 品牌色图标） */
.m-0 { background: rgba($color-primary, 0.12); color: $color-primary; }
.m-1 { background: rgba($color-secondary, 0.12); color: $color-secondary; }
.m-2 { background: rgba($color-accent, 0.12); color: $color-accent; }
.m-3 { background: rgba($color-warning, 0.14); color: $color-warning; }
.m-4 { background: rgba($color-primary-light, 0.12); color: $color-primary-light; }
.m-5 { background: rgba($color-success, 0.12); color: $color-success; }
.m-6 { background: rgba($color-accent, 0.12); color: $color-accent; }
.m-7 {
  background: rgba($color-text-secondary, 0.15);
  color: var(--app-text-secondary, #64748B);
}

.module-name {
  font-size: 12px;
  color: var(--app-text, #0F172A);
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
  color: var(--app-text, #0F172A);
}

.section-more {
  display: flex;
  align-items: center;
  gap: 2px;
  color: var(--app-text-tertiary, #94A3B8);
}

.more-text {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 紧凑最新卡：60x60 缩略 + 标题 2 行 + 日期 */
.latest-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin: 0 $spacing-lg $spacing-sm;
  padding: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: 10px;
  box-shadow: $shadow-card;
  transition: opacity 0.15s ease;
}

.latest-item:active {
  opacity: 0.85;
}

.latest-item:last-child {
  margin-bottom: 0;
}

.latest-cover {
  width: 60px;
  height: 60px;
  border-radius: $radius-md;
  flex-shrink: 0;
  background: var(--app-bg, #F1F5F9);
}

/* 无封面：浅底 + document 图标占位 */
.latest-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text-tertiary, #94A3B8);
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
  color: var(--app-text, #0F172A);
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
  color: var(--app-text-tertiary, #94A3B8);
}

/* ===== 搜索栏 ===== */
.search-wrap {
  margin: $spacing-md $spacing-lg 0;
  position: relative;
  z-index: 5;
}
/* 让 SearchBar 呈白底浮动卡片效果，避免与页面灰底融为一体 */
.search-wrap :deep(.search-bar) {
  background: var(--app-bg-card, #FFFFFF);
  box-shadow: $shadow-floating;
}

/* ===== 搜索结果数 ===== */
.search-info {
  padding: 12px 16px 0;
}

.search-info-text {
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

/* ===== 分类 chips（两行，gap 8px） ===== */
.chips-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 12px;
}

/* ===== 文章列表 ===== */
.list {
  padding: 12px 16px;
}

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

/* 加载更多 / 没有更多 */
.status {
  text-align: center;
  padding: 12px;
  color: var(--app-text-tertiary, #94A3B8);
  font-size: 12px;
}
</style>
