<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['page-root', isDark ? 'theme-dark' : '']">
    <scroll-view
      class="container"
      scroll-y
      :refresher-enabled="true"
      :refresher-default-style="refresherStyle"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 品牌脉冲刷新动画：App 编译器不支持 refresher 具名插槽，仅非 App 平台编译 -->
      <!-- #ifndef APP-PLUS -->
      <template #refresher>
        <view class="refresher">
          <view class="refresher-dot"></view>
          <view class="refresher-dot"></view>
          <view class="refresher-dot"></view>
        </view>
      </template>
      <!-- #endif -->

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

      <!-- 分类筛选 chips（搜索态 / 标签模式隐藏） -->
      <view v-else-if="mode === 'all' || mode === 'category'" class="chips-wrap">
        <CategoryChips :list="categoryChips" :active="activeCategoryId" @change="onCategoryChange" />
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
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import ArticleItem from '@/components/ArticleItem.vue'
import SearchBar from '@/components/SearchBar.vue'
import CategoryChips from '@/components/CategoryChips.vue'
import Skeleton from '@/components/Skeleton.vue'
import LoadingDots from '@/components/LoadingDots.vue'

// 页面模式：'all' 全部 | 'category' 分类 | 'tag' 标签 | 'search' 搜索
const mode = ref('all')

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

// 分类数据与当前激活分类
const categories = ref([])
const activeCategoryId = ref(null)
const activeCategoryName = ref('')

// 标签模式参数
const tagId = ref(null)
const tagName = ref('')

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

// 同步原生导航栏标题
const syncTitle = (title) => {
  uni.setNavigationBarTitle({ title })
}

// 重置分页并重新拉取第一页
const resetAndFetch = () => {
  page.value = 1
  hasMore.value = true
  list.value = []
  fetchData()
}

// 拉取文章列表 / 搜索结果（loading:false，首屏由 Skeleton 呈现加载态）
const fetchData = async () => {
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
      }, { loading: false })
    } else {
      const params = { current: page.value, size: 10 }
      // null 参数不发送，避免后端收到字符串 "null"
      if (activeCategoryId.value != null) params.categoryId = activeCategoryId.value
      if (tagId.value != null) params.tagId = tagId.value
      res = await api.getArticles(params, { loading: false })
    }
    // 被后续请求取代时丢弃结果
    if (seq !== fetchSeq) return
    const records = (res.data && res.data.records) || []
    if (page.value === 1) {
      list.value = records
    } else {
      list.value = list.value.concat(records)
    }
    hasMore.value = records.length === 10
    if (isSearching.value && page.value === 1) {
      searchTotal.value = (res.data && res.data.total) || records.length
    }
  } catch (e) {
    if (seq === fetchSeq) {
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  } finally {
    if (seq === fetchSeq) {
      loading.value = false
    }
  }
}

// 拉取分类列表（非搜索、非标签模式下展示 chips）
const loadCategories = async () => {
  try {
    const res = await api.getCategories({ loading: false })
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

// 搜索栏 confirm：进入搜索模式
const onSearch = (val) => {
  const kw = (val || '').trim()
  if (!kw) {
    if (isSearching.value) exitSearch()
    return
  }
  isSearching.value = true
  searchKeyword.value = kw
  tagId.value = null
  mode.value = 'search'
  syncTitle('搜索结果')
  resetAndFetch()
}

// 退出搜索：恢复进入页面时的模式（分类或全部）
const exitSearch = () => {
  isSearching.value = false
  searchKeyword.value = ''
  if (tagId.value != null) {
    mode.value = 'tag'
    syncTitle(tagName.value || '标签文章')
  } else {
    mode.value = activeCategoryId.value != null ? 'category' : 'all'
    syncTitle(activeCategoryName.value || '文章列表')
  }
  resetAndFetch()
}

// 切换分类 chip：重新拉列表并更新标题
const onCategoryChange = (val) => {
  if (activeCategoryId.value === val) return
  activeCategoryId.value = val
  mode.value = val != null ? 'category' : 'all'
  const hit = categories.value.find(c => c.id === val)
  activeCategoryName.value = hit ? hit.name : ''
  syncTitle(activeCategoryName.value || '文章列表')
  resetAndFetch()
}

// 跳转文章详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())

// 页面加载：按 query 解析三种模式
onLoad((options) => {
  const opt = options || {}
  if (opt.keyword) {
    // 搜索模式
    mode.value = 'search'
    isSearching.value = true
    searchKeyword.value = opt.keyword
    syncTitle('搜索结果')
  } else if (opt.categoryId) {
    // 分类模式
    mode.value = 'category'
    activeCategoryId.value = Number(opt.categoryId)
    activeCategoryName.value = opt.categoryName || ''
    syncTitle(activeCategoryName.value || '文章列表')
    loadCategories()
  } else if (opt.tagId) {
    // 标签模式
    mode.value = 'tag'
    tagId.value = Number(opt.tagId)
    tagName.value = opt.tagName || ''
    syncTitle(tagName.value || '标签文章')
  } else {
    // 全部文章
    mode.value = 'all'
    loadCategories()
  }
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

/* 滚动容器：占满根节点高度形成滚动区，底部留白（无 TabBar，预留 24px 即可） */
.container {
  height: 100%;
  box-sizing: border-box;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
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

/* ===== 搜索栏 ===== */
.search-wrap {
  margin: 12px 16px 0;
}

/* 让 SearchBar 呈白底浮动卡片效果，避免与页面灰底融为一体 */
.search-wrap :deep(.search-bar) {
  background: var(--app-bg-card, #FFFFFF);
  box-shadow: $shadow-card;
}

/* ===== 搜索结果数 ===== */
.search-info {
  padding: 12px 16px 0;
}

.search-info-text {
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

/* ===== 分类 chips ===== */
.chips-wrap {
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
