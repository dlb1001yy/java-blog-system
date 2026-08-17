<template>
  <view :class="['detail-page', isDark ? 'theme-dark' : '']">
    <!-- 加载中：详情页骨架 -->
    <Skeleton v-if="!article" type="detail" :count="1" />

    <template v-else>
      <!-- 标题区 -->
      <view class="card header-card">
        <text class="title">{{ article.title }}</text>
        <view class="meta">
          <view class="meta-item">
            <Icon name="clock" :size="14" color="#94A3B8" />
            <text class="meta-text">{{ formatRelativeTime(article.createTime) }}</text>
          </view>
          <view class="meta-item">
            <Icon name="eye" :size="14" color="#94A3B8" />
            <text class="meta-text">{{ article.viewCount }}</text>
          </view>
          <text :class="['type-badge', `type-${article.type}`]">{{ typeMap[article.type] }}</text>
        </view>

        <!-- 操作条：稍后阅读 + 分享海报 -->
        <view class="action-bar">
          <view :class="['action-btn', isSaved ? 'active' : '']" @click="onToggleReadLater">
            <!-- 内联 bookmark SVG 图标 16x16 -->
            <svg class="action-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
            </svg>
            <text class="action-text">{{ isSaved ? '已收藏' : '稍后阅读' }}</text>
          </view>
          <!-- 第二个操作按钮：分享海报 -->
          <view class="action-btn" @click="showShare = true">
            <!-- 内联 share SVG 图标 16x16 -->
            <svg class="action-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="18" cy="5" r="3" />
              <circle cx="6" cy="12" r="3" />
              <circle cx="18" cy="19" r="3" />
              <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" />
              <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
            </svg>
            <text class="action-text">分享海报</text>
          </view>
        </view>
      </view>

      <!-- 内容区：文本段走 rich-text，图片段抽出用 image 渲染（支持点击预览与加载渐显） -->
      <view class="card content-card">
        <template v-for="(seg, i) in contentSegments" :key="i">
          <view v-if="seg.type === 'html'" class="markdown-body">
            <rich-text :nodes="seg.html"></rich-text>
          </view>
          <image
            v-else
            class="md-img"
            :class="{ loaded: imgLoaded[i] }"
            :src="optimizeImageUrl(resolveFileUrl(seg.src), 750)"
            mode="widthFix"
            lazy-load
            @load="imgLoaded[i] = true"
            @click="previewImage(i)"
          />
        </template>
      </view>

      <!-- 评论区 -->
      <view class="card comments-card">
        <text class="section-title">评论 ({{ comments.length }})</text>

        <!-- 评论输入 -->
        <view class="comment-form">
          <textarea
            v-model="form.content"
            placeholder="说点什么..."
            class="form-textarea"
            :maxlength="500"
          />
          <input
            v-model="form.nickname"
            placeholder="昵称"
            class="form-input"
            :maxlength="20"
          />
          <button class="form-submit" @click="submitComment">发表</button>
        </view>

        <!-- 评论列表 -->
        <view class="comment-list" v-if="comments.length">
          <view
            v-for="(c, idx) in comments"
            :key="c.id"
            :class="['comment-item', idx === comments.length - 1 ? 'last' : '']"
          >
            <view class="comment-head">
              <view class="avatar">{{ getAvatarText(c.nickname) }}</view>
              <text class="comment-name">{{ c.nickname }}</text>
              <text class="comment-time">{{ formatRelativeTime(c.createTime) }}</text>
            </view>
            <text class="comment-content">{{ c.content }}</text>
          </view>
        </view>
        <view v-else class="empty-comments">
          <text class="empty-text">还没有评论，快来抢沙发吧～</text>
        </view>
      </view>

      <!-- 相关文章区 -->
      <view class="card related-card" v-if="relatedArticles.length">
        <text class="section-title">相关文章</text>
        <view class="related-list">
          <view
            v-for="item in relatedArticles"
            :key="item.id"
            class="related-item"
            @click="goRelated(item.id)"
          >
            <text class="related-title">{{ item.title }}</text>
            <image
              v-if="resolveFileUrl(item.coverImage)"
              :src="resolveFileUrl(item.coverImage)"
              class="related-cover"
              mode="aspectFill"
            />
          </view>
        </view>
      </view>

      <!-- 浮动点赞按钮 -->
      <view
        :class="['like-fab', likeAnimating ? 'animating' : '']"
        @click="handleLike"
      >
        <Icon name="heart" :size="24" color="#fff" />
        <text class="like-count">{{ article.likeCount }}</text>
      </view>
    </template>

    <!-- 分享海报弹层（文章存在时才挂载） -->
    <SharePoster v-if="article" :article="article" v-model:show="showShare" />
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { resolveFileUrl } from '@/common/config.js'
import { optimizeImageUrl } from '@/common/imageUrl.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import { parseMarkdown, splitHtmlImages } from '@/utils/markdown.js'
import {
  initNetworkWatch,
  offlineMode,
  cacheArticleDetail,
  getCachedArticleDetail,
  toggleReadLater,
  readLaterIds,
  isReadLater
} from '@/common/offline.js'
import Icon from '@/components/Icon.vue'
import Skeleton from '@/components/Skeleton.vue'
import SharePoster from '@/components/SharePoster.vue'

// 文章类型映射
const typeMap = { 0: '原创', 1: '转载', 2: '翻译' }

const article = ref(null)
const comments = ref([])
const relatedArticles = ref([])
const form = ref({ nickname: '', content: '' })
const likeAnimating = ref(false)
// 分享海报弹层显隐
const showShare = ref(false)

// 正文分段：markdown 转 html 后按图片切分，图片段单独用 image 组件渲染
const contentSegments = computed(() => {
  return splitHtmlImages(parseMarkdown(article.value?.content || ''))
})

// 正文图片预览地址列表（绝对地址）
const contentImages = computed(() => {
  return contentSegments.value
    .filter((s) => s.type === 'img')
    .map((s) => resolveFileUrl(s.src))
})

// 图片加载完成标记（key 为分段下标），触发 .loaded 渐显；文章切换时重置
const imgLoaded = ref({})
watch(() => article.value?.id, () => { imgLoaded.value = {} })

// 预览正文图片：i 为分段下标，先换算为该图片在预览列表中的序号
const previewImage = (i) => {
  const imgIndex = contentSegments.value
    .slice(0, i)
    .filter((s) => s.type === 'img')
    .length
  uni.previewImage({
    urls: contentImages.value,
    current: contentImages.value[imgIndex]
  })
}

// 是否已加入稍后阅读（isReadLater 读取响应式 id 集合，切换后按钮自动高亮）
const isSaved = computed(() => isReadLater(article.value && article.value.id))

// 切换稍后阅读（toggleReadLater 内部同步刷新 readLaterIds，按钮状态随之更新）
const onToggleReadLater = () => {
  if (!article.value) return
  toggleReadLater(article.value)
}

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())

// 页面加载：并行拉取详情、评论、相关文章；离线/网络失败时降级到详情缓存
onLoad(async (options) => {
  const id = options.id
  // 初始化网络监听（幂等，独立进入本页也能感知离线）
  initNetworkWatch()

  // 离线模式：读详情缓存降级，评论与相关文章置空
  if (offlineMode.value) {
    const cached = getCachedArticleDetail(id)
    if (cached) {
      article.value = cached
      comments.value = []
      relatedArticles.value = []
      uni.showToast({ title: '已进入离线阅读模式', icon: 'none' })
    } else {
      uni.showToast({ title: '暂无离线内容', icon: 'none' })
    }
    return
  }

  // 在线：并行拉取文章详情、评论、相关文章
  try {
    const [res, cRes, rRes] = await Promise.all([
      api.getArticleDetail(id),
      api.getComments(id),
      api.getRelatedArticles(id)
    ])
    article.value = res.data
    comments.value = cRes.data || []
    relatedArticles.value = (rRes.data || []).filter(Boolean)
    // 成功后写入详情离线缓存
    cacheArticleDetail(res.data)
  } catch (e) {
    // 网络类失败：尝试详情缓存降级
    const cached = getCachedArticleDetail(id)
    if (cached) {
      article.value = cached
      comments.value = []
      relatedArticles.value = []
      uni.showToast({ title: '已进入离线阅读模式', icon: 'none' })
    } else {
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  }
})

// 相对时间：刚刚 / X 分钟前 / X 小时前 / X 天前 / YYYY-MM-DD
const formatRelativeTime = (date) => {
  if (!date) return ''
  const target = new Date(date)
  if (isNaN(target.getTime())) return String(date).slice(0, 10)
  const diff = Date.now() - target.getTime()
  const sec = Math.floor(diff / 1000)
  const min = Math.floor(sec / 60)
  const hour = Math.floor(min / 60)
  const day = Math.floor(hour / 24)
  if (sec < 60) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  if (hour < 24) return `${hour} 小时前`
  if (day < 30) return `${day} 天前`
  // 超过 30 天，返回 YYYY-MM-DD
  const y = target.getFullYear()
  const m = String(target.getMonth() + 1).padStart(2, '0')
  const d = String(target.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 取昵称首字母大写作为头像文字
const getAvatarText = (nickname) => {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}

// 点赞：调用接口并更新计数，附带缩放动画反馈
const handleLike = async () => {
  if (likeAnimating.value) return
  try {
    await api.likeArticle(article.value.id)
    article.value.likeCount++
    likeAnimating.value = true
    setTimeout(() => { likeAnimating.value = false }, 300)
    uni.showToast({ title: '点赞成功', icon: 'none' })
  } catch (e) {
    uni.showToast({ title: '点赞失败', icon: 'none' })
  }
}

// 提交评论：校验后调用接口并刷新列表
const submitComment = async () => {
  if (!form.value.nickname || !form.value.content) {
    return uni.showToast({ title: '请填写完整', icon: 'none' })
  }
  try {
    await api.addComment({ ...form.value, articleId: article.value.id })
    uni.showToast({ title: '评论成功', icon: 'none' })
    form.value.content = ''
    // 刷新评论列表
    const cRes = await api.getComments(article.value.id)
    comments.value = cRes.data || []
  } catch (e) {
    uni.showToast({ title: '评论失败', icon: 'none' })
  }
}

// 跳转相关文章详情
const goRelated = (id) => {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
/* 页面容器：灰底，底部留白避开浮动按钮 */
.detail-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  padding: $spacing-md;
  padding-bottom: calc(140px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 通用卡片 */
.card {
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

/* ===== 标题区 ===== */
.header-card {
  padding: $spacing-xl $spacing-lg;
}
.title {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
  line-height: 1.4;
  margin-bottom: 12px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.meta-text {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}
/* 类型徽章：胶囊形 */
.type-badge {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  color: #fff;
  line-height: 1.4;
}
.type-0 { background: $color-primary; }
.type-1 { background: $color-warning; }
.type-2 { background: $color-success; }

/* ===== 操作条（meta 行下）：稍后阅读 + 预留按钮位 ===== */
.action-bar {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}
/* 胶囊按钮：透明底 + 1px 边框，按下缩放反馈 */
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 32px;
  border-radius: $radius-full;
  border: 1px solid var(--app-border, #E2E8F0);
  background: transparent;
  color: var(--app-text-secondary, #64748B);
  transition: transform 0.15s ease;
}
.action-btn:active {
  transform: scale(0.95);
}
/* 已加入稍后阅读：主色边框 + 主色文字高亮 */
.action-btn.active {
  border-color: var(--app-primary, #4F46E5);
  color: var(--app-primary, #4F46E5);
}
.action-icon {
  flex-shrink: 0;
}
.action-text {
  font-size: 13px;
  line-height: 1;
}

/* ===== 内容区 ===== */
.content-card {
  margin-top: $spacing-md;
  padding: $spacing-xl $spacing-lg;
}
/* markdown-body 内部样式：rich-text 渲染的节点需用 :deep() 穿透 */
.markdown-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--app-text, #0F172A);
}
.markdown-body :deep(p) {
  margin: 0 0 16px 0;
}
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  font-weight: 700;
  color: var(--app-text, #0F172A);
  margin: 20px 0 12px;
  line-height: 1.4;
}
.markdown-body :deep(h1) { font-size: 20px; }
.markdown-body :deep(h2) {
  font-size: 18px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--app-border, #E2E8F0);
}
.markdown-body :deep(h3) { font-size: 16px; }
.markdown-body :deep(pre) {
  background: var(--app-bg, #F1F5F9);
  padding: 12px;
  border-radius: $radius-md;
  font-family: 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  overflow-x: auto;
  margin: 0 0 16px 0;
}
.markdown-body :deep(code) {
  font-family: 'Menlo', 'Consolas', monospace;
}
.markdown-body :deep(pre code) {
  background: transparent;
  padding: 0;
}
.markdown-body :deep(:not(pre) > code) {
  background: var(--app-bg, #F1F5F9);
  padding: 2px 6px;
  border-radius: $radius-sm;
  font-size: 13px;
}
.markdown-body :deep(blockquote) {
  border-left: 4px solid $color-primary;
  padding-left: 12px;
  color: var(--app-text-secondary, #64748B);
  margin: 0 0 16px 0;
  line-height: 1.6;
}
.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: $radius-md;
  margin: 8px 0;
}
/* 正文图片段（image 组件渲染）：占位底 + 加载完成后渐显（同封面图交互） */
.md-img {
  display: block;
  width: 100%;
  border-radius: $radius-md;
  margin: 8px 0;
  background: var(--app-bg, #F1F5F9);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.md-img.loaded {
  opacity: 1;
}
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
  margin: 0 0 16px 0;
}
.markdown-body :deep(li) {
  margin-bottom: 4px;
}
.markdown-body :deep(a) {
  color: $color-primary;
  text-decoration: none;
}

/* ===== 评论区 ===== */
.comments-card {
  margin-top: $spacing-md;
  padding: $spacing-lg;
}
.section-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-lg;
}
.comment-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}
.form-textarea {
  width: 100%;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-md;
  padding: 12px;
  min-height: 80px;
  font-size: 14px;
  color: var(--app-text, #0F172A);
  box-sizing: border-box;
}
.form-input {
  width: 100%;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-md;
  padding: 0 12px;
  height: 40px;
  font-size: 14px;
  color: var(--app-text, #0F172A);
  box-sizing: border-box;
}
.form-submit {
  background: $color-primary;
  color: #fff;
  border-radius: $radius-md;
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  padding: 0;
  border: none;
}
/* 修正小程序 button 默认样式 */
.form-submit::after {
  border: none;
}

/* 评论列表 */
.comment-list {
  margin-top: $spacing-sm;
}
.comment-item {
  padding: $spacing-md 0;
  border-bottom: 1px solid var(--app-divider, #F1F5F9);
}
.comment-item.last {
  border-bottom: none;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: $color-primary;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.comment-name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}
.comment-time {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}
.comment-content {
  display: block;
  margin-top: $spacing-sm;
  font-size: 14px;
  color: var(--app-text-secondary, #334155);
  line-height: 1.6;
  word-break: break-word;
}
.empty-comments {
  padding: $spacing-xl 0;
  text-align: center;
}
.empty-text {
  font-size: 13px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* ===== 相关文章区 ===== */
.related-card {
  margin-top: $spacing-md;
  padding: $spacing-lg;
}
.related-list {
  display: flex;
  flex-direction: column;
}
.related-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-sm 0;
  /* 触控反馈：按下轻微缩放 */
  transition: transform 0.15s ease, opacity 0.15s ease;
}
.related-item:active {
  transform: scale(0.98);
  opacity: 0.9;
}
.related-title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--app-text, #0F172A);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}
.related-cover {
  width: 60px;
  height: 60px;
  border-radius: $radius-md;
  flex-shrink: 0;
  background: var(--app-bg, #F1F5F9);
}

/* ===== 浮动点赞按钮 ===== */
.like-fab {
  position: fixed;
  right: 16px;
  bottom: calc(80px + env(safe-area-inset-bottom));
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: $color-primary;
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 99;
  transition: transform 0.18s ease;
}
.like-fab.animating {
  transform: scale(0.85);
}
.like-count {
  position: absolute;
  bottom: -18px;
  font-size: 11px;
  color: #fff;
  line-height: 1;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.3);
}
</style>
