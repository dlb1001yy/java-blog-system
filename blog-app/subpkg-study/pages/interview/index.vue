<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['interview-page', isDark ? 'theme-dark' : '']">
    <!-- 整页滚动容器：触底分页 -->
    <scroll-view class="container" scroll-y lower-threshold="100" @scrolltolower="onLoadMore">

      <!-- ===== 筛选面板（可折叠） ===== -->
      <view class="filter-card">
        <view class="filter-header" @click="filterExpanded = !filterExpanded">
          <view class="filter-header-left">
            <Icon name="filter" :size="16" />
            <text class="filter-title">筛选</text>
            <view v-if="activeFilterCount > 0" class="filter-badge">{{ activeFilterCount }}</view>
          </view>
          <view :class="['filter-arrow', filterExpanded ? 'expanded' : '']">
            <Icon name="chevron-down" :size="18" />
          </view>
        </view>

        <view v-show="filterExpanded" class="filter-body">
          <!-- 技术方向：多选 -->
          <view class="filter-section">
            <text class="section-label">技术方向（多选）</text>
            <view class="chip-row">
              <view
                v-for="c in categories"
                :key="c.id"
                :class="['chip', selectedCategoryIds.indexOf(c.id) > -1 ? 'active' : '']"
                @click="toggleCategory(c.id)"
              >{{ c.name }}</view>
            </view>
          </view>

          <!-- 难度：单选（中文标识，与后端 difficulty 字段一致） -->
          <view class="filter-section">
            <text class="section-label">难度</text>
            <view class="chip-row">
              <view
                v-for="opt in difficultyOptions"
                :key="opt.value"
                :class="['chip', difficulty === opt.value ? 'active' : '']"
                @click="onDifficultyChange(opt.value)"
              >{{ opt.label }}</view>
            </view>
          </view>

          <!-- 刷题状态：单选 -->
          <view class="filter-section">
            <text class="section-label">刷题状态</text>
            <view class="chip-row">
              <view
                v-for="opt in viewOptions"
                :key="opt.value"
                :class="['chip', activeView === opt.value ? 'active' : '']"
                @click="onViewChange(opt.value)"
              >{{ opt.label }}</view>
            </view>
          </view>

          <!-- 关键词搜索：confirm 触发 -->
          <view class="filter-section">
            <SearchBar
              placeholder="搜索题目关键词..."
              :value="keyword"
              @input="onKeywordInput"
              @search="onKeywordSearch"
            />
          </view>
        </view>
      </view>

      <!-- ===== 题目列表 ===== -->
      <view class="list">
        <!-- 首次加载 / 切换筛选：题目卡骨架 -->
        <view v-if="showSkeleton" class="q-skeleton-list">
          <view v-for="i in 5" :key="i" class="q-skeleton-card">
            <view class="q-sk-row">
              <view class="sk-block sk-tag"></view>
              <view class="sk-block sk-tag"></view>
            </view>
            <view class="sk-block sk-title"></view>
            <view class="sk-block sk-sub"></view>
          </view>
        </view>

        <template v-else>
          <view v-for="q in list" :key="q.id" class="question-card">
            <!-- 卡片头：点击展开/收起 -->
            <view class="question-header" @click="toggleExpand(q)">
              <view class="question-meta">
                <text v-if="q.categoryName || q.category" class="cat-tag">{{ q.categoryName || q.category }}</text>
                <text v-if="q.difficulty" :class="['diff-tag', difficultyClass(q.difficulty)]">{{ q.difficulty }}</text>
              </view>
              <text class="question-title">{{ q.title }}</text>
              <view class="question-footer">
                <!-- 标签 chips：兼容数组与逗号分隔字符串 -->
                <view class="question-tags">
                  <text
                    v-for="tag in parseTags(q.tagNames || q.tags)"
                    :key="tag"
                    class="tag-chip"
                  >{{ tag }}</text>
                </view>
                <!-- 操作：收藏 / 错题本 / 展开箭头（阻止冒泡避免触发卡片展开） -->
                <view class="question-actions" @click.stop>
                  <view :class="['act-btn', favMap[q.id] ? 'fav-active' : '']" @click="handleToggle(q, 'favorite')">
                    <Icon name="star" :size="18" />
                  </view>
                  <view :class="['act-btn', wrongMap[q.id] ? 'wrong-active' : '']" @click="handleToggle(q, 'wrong')">
                    <Icon name="book" :size="18" />
                  </view>
                  <view :class="['expand-arrow', q.expanded ? 'expanded' : '']">
                    <Icon name="chevron-down" :size="16" />
                  </view>
                </view>
              </view>
            </view>

            <!-- 展开区：懒加载答案 -->
            <view v-if="q.expanded" class="answer-panel">
              <view v-if="q.answerLoading" class="answer-loading">
                <LoadingDots :size="6" />
              </view>
              <template v-else>
                <view v-if="q.answerHtml" class="markdown-body">
                  <rich-text :nodes="q.answerHtml"></rich-text>
                </view>
                <text v-else class="answer-empty">暂无参考答案</text>
                <view v-if="q.tipsHtml" class="tips-panel">
                  <text class="tips-title">解题思路</text>
                  <view class="markdown-body tips-body">
                    <rich-text :nodes="q.tipsHtml"></rich-text>
                  </view>
                </view>
              </template>
            </view>
          </view>

          <!-- 空状态：按视图区分图标与文案 -->
          <view v-if="list.length === 0" class="empty">
            <view class="empty-icon">
              <Icon :name="emptyIconName" :size="56" :stroke="1.5" />
            </view>
            <text class="empty-title">{{ emptyText.title }}</text>
            <text class="empty-sub">{{ emptyText.sub }}</text>
          </view>

          <!-- 加载更多 / 没有更多 -->
          <view v-if="list.length > 0 && loading" class="status">
            <LoadingDots :size="6" />
          </view>
          <view v-if="list.length > 0 && !loading && !hasMore" class="status">没有更多了</view>
        </template>
      </view>
    </scroll-view>

    <!-- 底部 TabBar：tab 主页面常驻导航 -->
    <TabBar current="/subpkg-study/pages/interview/index" />
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import { requireLogin, buildLoginRedirect, isLoggedIn } from '@/common/auth.js'
import { parseMarkdown } from '@/utils/markdown.js'
import Icon from '@/components/Icon.vue'
import LoadingDots from '@/components/LoadingDots.vue'
import SearchBar from '@/components/SearchBar.vue'
import TabBar from '@/components/TabBar.vue'

const PAGE_SIZE = 10

// 技术方向：接口失败时的回退默认
const DEFAULT_CATEGORIES = ['Java基础', 'Java并发', 'JVM', 'MySQL', 'Redis', 'Spring']
const categories = ref(DEFAULT_CATEGORIES.map((name) => ({ id: name, name })))
// 多选选中的 categoryId 集合，请求时逗号拼接
const selectedCategoryIds = ref([])

// 难度选项：值与后端 difficulty 字段（简单/中等/困难）一致
const difficultyOptions = [
  { label: '全部', value: '' },
  { label: '简单', value: '简单' },
  { label: '中等', value: '中等' },
  { label: '困难', value: '困难' }
]
const difficulty = ref('')

// 刷题状态：all 走题目列表，favorites/wrong 切换列表接口
const viewOptions = [
  { label: '全部', value: 'all' },
  { label: '我的收藏', value: 'favorites' },
  { label: '错题本', value: 'wrong' }
]
const activeView = ref('all')

// 搜索关键词
const keyword = ref('')

// 筛选面板展开态
const filterExpanded = ref(true)

// 列表与分页状态
const list = ref([])
const page = ref(1)
const loading = ref(false)
const hasMore = ref(true)

// 收藏 / 错题按钮态（id -> boolean）
const favMap = reactive({})
const wrongMap = reactive({})

// 请求序列号：快速切换筛选时丢弃过期响应，避免数据错位
let fetchSeq = 0

// 首次加载或切换筛选时显示骨架
const showSkeleton = computed(() => loading.value && list.value.length === 0)

// 筛选面板标题上的激活条件计数角标
const activeFilterCount = computed(() => {
  let n = selectedCategoryIds.value.length
  if (difficulty.value) n++
  if (activeView.value !== 'all') n++
  if (keyword.value.trim()) n++
  return n
})

// 空态文案：按视图区分
const emptyText = computed(() => {
  if (activeView.value === 'favorites') {
    return { title: '还没有收藏的题目', sub: '点击题目右侧的星标即可收藏' }
  }
  if (activeView.value === 'wrong') {
    return { title: '错题本还是空的', sub: '点击题目右侧的书本图标加入错题本' }
  }
  return { title: '暂无题目', sub: '换个筛选条件试试吧' }
})

// 空态图标：收藏视图星标 / 错题视图书本 / 全部视图文档
const emptyIconName = computed(() => {
  if (activeView.value === 'favorites') return 'star'
  if (activeView.value === 'wrong') return 'book'
  return 'document'
})

// 难度 tag 样式类
const difficultyClass = (d) =>
  ({ 简单: 'diff-easy', 中等: 'diff-medium', 困难: 'diff-hard' }[d] || 'diff-medium')

// 标签解析：tagNames 数组或逗号分隔字符串
const parseTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.filter(Boolean)
  return String(tags).split(',').map((t) => t.trim()).filter(Boolean)
}

// 拉取题目列表：按视图切换接口，收藏/错题视图只传分页参数
const fetchQuestions = async () => {
  const seq = ++fetchSeq
  loading.value = true
  try {
    let res
    if (activeView.value === 'favorites') {
      res = await api.getInterviewFavorites({ page: page.value, size: PAGE_SIZE })
    } else if (activeView.value === 'wrong') {
      res = await api.getInterviewWrong({ page: page.value, size: PAGE_SIZE })
    } else {
      const params = { page: page.value, size: PAGE_SIZE }
      if (selectedCategoryIds.value.length) params.categoryId = selectedCategoryIds.value.join(',')
      if (difficulty.value) params.difficulty = difficulty.value
      const kw = keyword.value.trim()
      if (kw) params.keyword = kw
      res = await api.getInterviewQuestions(params)
    }
    // 被后续请求取代时丢弃结果
    if (seq !== fetchSeq) return
    const data = res.data || {}
    // 收藏/错题视图记录为 { question } 嵌套结构，取内层题目对象
    const records = (data.records || []).map((r) => {
      const q = r.question || r
      return {
        ...q,
        expanded: false,
        answerLoading: false,
        answerLoaded: false,
        answerHtml: '',
        tipsHtml: '',
        toggling: false
      }
    })
    if (page.value === 1) {
      list.value = records
    } else {
      list.value = list.value.concat(records)
    }
    // 初始化按钮态：收藏视图内题目默认已收藏，错题视图默认已在错题本
    records.forEach((q) => {
      if (activeView.value === 'favorites') favMap[q.id] = true
      if (activeView.value === 'wrong') wrongMap[q.id] = true
    })
    hasMore.value = records.length === PAGE_SIZE
  } catch (e) {
    // 请求层已统一 toast 提示，这里静默保留现有列表
  } finally {
    if (seq === fetchSeq) {
      loading.value = false
    }
  }
}

// 任意筛选变化：重置分页后重新拉取第一页
const resetAndFetch = () => {
  page.value = 1
  hasMore.value = true
  list.value = []
  fetchQuestions()
}

// 拉取题库方向列表，失败保留默认方向
const loadCategories = async () => {
  try {
    const res = await api.getInterviewCategories()
    const data = res.data
    if (Array.isArray(data) && data.length) {
      // 接口返回 [{id, name}]，兼容旧字符串数组
      categories.value = data.map((c) => (typeof c === 'string' ? { id: c, name: c } : c))
    }
  } catch (e) {
    // 接口异常时保留默认方向
  }
}

// 技术方向多选切换
const toggleCategory = (id) => {
  const idx = selectedCategoryIds.value.indexOf(id)
  if (idx > -1) {
    selectedCategoryIds.value.splice(idx, 1)
  } else {
    selectedCategoryIds.value.push(id)
  }
  resetAndFetch()
}

// 难度单选
const onDifficultyChange = (val) => {
  if (difficulty.value === val) return
  difficulty.value = val
  resetAndFetch()
}

// 刷题状态单选
const onViewChange = (val) => {
  if (activeView.value === val) return
  activeView.value = val
  resetAndFetch()
}

// 搜索输入：清空时退出搜索并刷新（与 frontend @clear 行为一致）
const onKeywordInput = (val) => {
  if (!val && keyword.value) {
    keyword.value = ''
    resetAndFetch()
    return
  }
  keyword.value = val
}

// 搜索 confirm
const onKeywordSearch = (val) => {
  keyword.value = (val || '').trim()
  resetAndFetch()
}

// 展开/收起：首次展开懒加载答案并缓存到题目对象，避免重复请求
const toggleExpand = async (q) => {
  q.expanded = !q.expanded
  if (q.expanded && !q.answerLoaded) {
    q.answerLoading = true
    try {
      const res = await api.getInterviewAnswer(q.id)
      const data = res.data || {}
      q.answerHtml = data.answer ? parseMarkdown(data.answer) : ''
      q.tipsHtml = data.tips ? parseMarkdown(data.tips) : ''
      q.answerLoaded = true
    } catch (e) {
      // 加载失败收起卡片，保留未加载标记供下次重试（请求层已 toast）
      q.expanded = false
    } finally {
      q.answerLoading = false
    }
  }
}

// 收藏 / 错题本 toggle：返回布尔（是否已加入）更新按钮态并 toast
const handleToggle = async (q, type) => {
  if (q.toggling) return
  if (!isLoggedIn()) {
    uni.showToast({ title: '请先登录后再操作', icon: 'none' })
    return
  }
  q.toggling = true
  try {
    const apiFn = type === 'favorite' ? api.toggleInterviewFavorite : api.toggleInterviewWrong
    const res = await apiFn(q.id)
    const added = !!res.data
    if (type === 'favorite') {
      favMap[q.id] = added
    } else {
      wrongMap[q.id] = added
    }
    uni.showToast({
      title: added
        ? (type === 'favorite' ? '已加入收藏' : '已加入错题本')
        : (type === 'favorite' ? '已取消收藏' : '已移出错题本'),
      icon: 'none'
    })
    // 在收藏/错题视图内取消后，从列表移除该项
    if (!added) {
      const inMatchingView =
        (type === 'favorite' && activeView.value === 'favorites') ||
        (type === 'wrong' && activeView.value === 'wrong')
      if (inMatchingView) {
        const idx = list.value.findIndex((it) => it.id === q.id)
        if (idx > -1) list.value.splice(idx, 1)
      }
    }
  } catch (e) {
    // 请求层已统一 toast 提示
  } finally {
    q.toggling = false
  }
}

// scroll-view 触底加载更多
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  fetchQuestions()
}

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())

// 页面加载：登录校验（未登录跳登录页并回跳），通过后拉取方向与题目
onLoad(() => {
  if (!requireLogin(buildLoginRedirect())) return
  loadCategories()
  fetchQuestions()
})
</script>

<style lang="scss" scoped>
/* 页面根节点：占满整屏，主题类挂此处向滚动容器级联 CSS 变量 */
.interview-page {
  height: 100vh;
  background: var(--app-bg, #F1F5F9);
}

/* 滚动容器：占满根节点高度形成滚动区；底部留白避开固定 TabBar（56px + 安全区） */
.container {
  height: 100%;
  box-sizing: border-box;
  padding: $spacing-md $spacing-lg calc(80px + env(safe-area-inset-bottom));
}

/* ===== 筛选面板 ===== */
.filter-card {
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  margin-bottom: $spacing-md;
}

.filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  color: var(--app-text-secondary, #64748B);
  transition: opacity 0.15s ease;
}

.filter-header:active {
  opacity: 0.7;
}

.filter-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.filter-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

/* 激活筛选计数角标 */
.filter-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: $radius-full;
  background: $color-primary;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}

.filter-arrow {
  display: flex;
  align-items: center;
  color: var(--app-text-tertiary, #94A3B8);
  transition: transform 0.3s ease;
}

.filter-arrow.expanded {
  transform: rotate(180deg);
}

.filter-body {
  padding: $spacing-md $spacing-lg $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  border-top: 1px solid var(--app-divider, #F1F5F9);
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-secondary, #64748B);
}

/* chips 多行 wrap */
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.chip {
  padding: 5px 14px;
  border-radius: $radius-full;
  font-size: 13px;
  line-height: 1.4;
  background: var(--app-bg, #F1F5F9);
  color: var(--app-text-secondary, #64748B);
  transition: all 0.2s ease;
}

.chip:active {
  transform: scale(0.92);
}

.chip.active {
  background: $color-primary;
  color: #fff;
}

/* ===== 题目列表 ===== */
.list {
  display: flex;
  flex-direction: column;
}

.question-card {
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
}

.question-header {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  transition: opacity 0.15s ease;
}

.question-header:active {
  opacity: 0.75;
}

/* 分类 + 难度 tag 行 */
.question-meta {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

/* 分类 tag：主色浅底 */
.cat-tag {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  line-height: 1.5;
  color: var(--app-primary, #4F46E5);
  background: rgba(129, 140, 248, 0.14);
}

/* 难度 tag：简单绿 / 中等橙 / 困难红浅底（底色由同名令牌色派生） */
.diff-tag {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  line-height: 1.5;
}

.diff-easy {
  color: $color-success;
  background: rgba(52, 211, 153, 0.16);
}

.diff-medium {
  color: $color-warning;
  background: rgba(251, 191, 36, 0.18);
}

.diff-hard {
  color: $color-danger;
  background: rgba(248, 113, 113, 0.16);
}

/* 标题：加粗 2 行省略 */
.question-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.question-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
}

.question-tags {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-chip {
  padding: 1px 8px;
  border-radius: $radius-full;
  font-size: 11px;
  line-height: 1.5;
  color: var(--app-text-secondary, #64748B);
  background: var(--app-bg, #F1F5F9);
}

.question-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

/* 收藏 / 错题圆形按钮 */
.act-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text-tertiary, #94A3B8);
  transition: transform 0.15s ease;
}

.act-btn:active {
  transform: scale(0.85);
}

/* 已收藏：星标填充琥珀色 #F59E0B */
.act-btn.fav-active {
  color: $color-warning;
}

.act-btn.fav-active :deep(.icon svg) {
  fill: $color-warning;
}

/* 已加入错题本：主色 */
.act-btn.wrong-active {
  color: var(--app-primary, #4F46E5);
}

/* 展开箭头 */
.expand-arrow {
  display: flex;
  align-items: center;
  color: var(--app-text-tertiary, #94A3B8);
  transition: transform 0.3s ease;
}

.expand-arrow.expanded {
  transform: rotate(180deg);
}

/* ===== 展开区（答案） ===== */
.answer-panel {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px dashed var(--app-border, #E2E8F0);
}

.answer-loading {
  padding: $spacing-md 0;
  color: var(--app-text-tertiary, #94A3B8);
}

.answer-empty {
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

/* markdown 富文本：rich-text 渲染的节点需用 :deep() 穿透 */
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  color: var(--app-text, #0F172A);
}

.markdown-body :deep(p) {
  margin: 0 0 12px 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  font-weight: 600;
  color: var(--app-text, #0F172A);
  margin: 12px 0 8px;
  line-height: 1.4;
}

.markdown-body :deep(h1) { font-size: 17px; }
.markdown-body :deep(h2) { font-size: 16px; }
.markdown-body :deep(h3) { font-size: 15px; }

.markdown-body :deep(pre) {
  background: var(--app-bg, #F1F5F9);
  padding: 10px;
  border-radius: $radius-md;
  font-family: 'Menlo', 'Consolas', monospace;
  font-size: 12px;
  overflow-x: auto;
  margin: 0 0 12px 0;
}

.markdown-body :deep(code) {
  font-family: 'Menlo', 'Consolas', monospace;
}

.markdown-body :deep(:not(pre) > code) {
  background: var(--app-bg, #F1F5F9);
  padding: 1px 5px;
  border-radius: $radius-sm;
  font-size: 12px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 18px;
  margin: 0 0 12px 0;
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid var(--app-primary, #4F46E5);
  padding-left: 10px;
  color: var(--app-text-secondary, #64748B);
  margin: 0 0 12px 0;
  line-height: 1.6;
}

.markdown-body :deep(a) {
  color: var(--app-primary, #4F46E5);
}

/* 解题思路提示面板：浅色左条 */
.tips-panel {
  margin-top: $spacing-md;
  padding: $spacing-md;
  border-radius: $radius-md;
  border-left: 3px solid var(--app-primary, #4F46E5);
  background: rgba(129, 140, 248, 0.08);
}

.tips-title {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-primary, #4F46E5);
  margin-bottom: $spacing-sm;
}

.tips-body {
  font-size: 13px;
}

/* ===== 骨架（题目卡） ===== */
.q-skeleton-list {
  display: flex;
  flex-direction: column;
}

.q-skeleton-card {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.q-sk-row {
  display: flex;
  gap: $spacing-sm;
}

@keyframes sk-shimmer {
  0% { background-position: -468px 0; }
  100% { background-position: 468px 0; }
}

.sk-block {
  background: linear-gradient(90deg, var(--app-border, #E2E8F0) 25%, var(--app-bg, #F1F5F9) 50%, var(--app-border, #E2E8F0) 75%);
  background-size: 936px 100%;
  animation: sk-shimmer 1.5s infinite linear;
  border-radius: $radius-sm;
}

.sk-tag {
  width: 56px;
  height: 16px;
  border-radius: $radius-full;
}

.sk-title {
  width: 78%;
  height: 16px;
  border-radius: $radius-sm;
}

.sk-sub {
  width: 45%;
  height: 12px;
}

/* ===== 空态 ===== */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 32px;
}

/* 空态图标：复用 Icon 组件按视图切换 */
.empty-icon {
  display: flex;
  color: var(--app-text-tertiary, #94A3B8);
  opacity: 0.6;
}

.empty-title {
  margin-top: $spacing-md;
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

.empty-sub {
  margin-top: $spacing-sm;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
  text-align: center;
  line-height: 1.6;
}

/* ===== 加载更多 / 没有更多 ===== */
.status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-md;
  color: var(--app-text-tertiary, #94A3B8);
  font-size: 12px;
}
</style>
