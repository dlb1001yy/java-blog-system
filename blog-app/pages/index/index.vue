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

    <!-- 搜索栏：负 margin 上浮到 hero 边缘 -->
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
import ArticleItem from '@/components/ArticleItem.vue'
import SearchBar from '@/components/SearchBar.vue'
import CategoryChips from '@/components/CategoryChips.vue'
import Skeleton from '@/components/Skeleton.vue'
import LoadingDots from '@/components/LoadingDots.vue'
import TabBar from '@/components/TabBar.vue'

// 类型 chips：固定列表（全部/原创/转载/翻译）
const types = [
  { label: '全部', value: null },
  { label: '原创', value: 0 },
  { label: '转载', value: 1 },
  { label: '翻译', value: 2 }
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

// Hero 右下角统计文本："12 篇文章 · 1.2k 浏览"
const statsText = computed(() => {
  if (!stats.value) return ''
  const articleCount = stats.value.articleCount || 0
  return `${articleCount} 篇文章 · ${formatViewCount(stats.value.viewCount)} 浏览`
})

// 浏览数格式化：>=1000 显示为 1.2k
const formatViewCount = (n) => {
  if (n == null) return '0'
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
    // 静默失败，hero 不显示统计即可
  }
}

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

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())

// 页面加载：初始化网络监听，并行拉取统计、分类、文章
onLoad(() => {
  initNetworkWatch()
  loadStats()
  loadCategories()
  fetchData()
})

// scroll-view 触底加载更多
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  fetchData()
}

// 自定义下拉刷新：重置分页后重新拉取第一页，完成后收起刷新动画
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  list.value = []
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

/* 滚动容器：占满根节点高度形成滚动区，底部留白避开 TabBar */
.container {
  height: 100%;
  box-sizing: border-box;
  padding-bottom: calc(56px + env(safe-area-inset-bottom) + 12px);
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
  height: 140px;
  box-sizing: border-box;
  padding: 24px 20px;
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

/* 右下角统计：推到底部右对齐 */
.hero-stats {
  margin-top: auto;
  align-self: flex-end;
}

.stats-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

/* ===== 搜索栏（上浮到 hero 边缘） ===== */
.search-wrap {
  margin: -20px 16px 0;
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
