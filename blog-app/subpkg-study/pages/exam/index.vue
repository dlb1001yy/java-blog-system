<template>
  <view :class="['exam-page', isDark ? 'theme-dark' : '']">
    <!-- 页头卡：标题 + 警示文案 -->
    <view class="header-card">
      <text class="page-title">在线考试</text>
      <view class="warn-bar">
        <text class="warn-text">考试过程中切屏超过 3 次将强制交卷，请诚信作答</text>
      </view>
    </view>

    <!-- 列表 -->
    <view class="paper-list">
      <!-- 首次加载骨架 -->
      <Skeleton v-if="loading && !papers.length" type="article" :count="3" />

      <template v-else>
        <view v-for="paper in papers" :key="paper.id" class="paper-card" @click="startExam(paper)">
          <view class="paper-info">
            <text class="paper-title">{{ paper.title }}</text>
            <text class="paper-desc">{{ paper.description || '暂无描述' }}</text>
            <view class="paper-meta">
              <text class="meta-tag tag-warn">总分 {{ paper.totalScore }} 分</text>
              <text class="meta-tag tag-success">时长 {{ paper.duration }} 分钟</text>
              <text class="meta-tag tag-info">{{ paper.questionCount || 0 }} 道题</text>
            </view>
          </view>
          <view class="paper-action" @click.stop="startExam(paper)">
            <text class="start-btn">开始考试</text>
          </view>
        </view>

        <!-- 空态 -->
        <view v-if="!papers.length" class="empty">
          <Icon name="document" :size="52" color="#CBD5E1" />
          <text class="empty-text">暂无已发布的试卷</text>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import { requireLogin, buildLoginRedirect } from '@/common/auth.js'
import Icon from '@/components/Icon.vue'
import Skeleton from '@/components/Skeleton.vue'

const loading = ref(false)
const papers = ref([])

// 拉取已发布试卷列表
const loadPapers = async () => {
  loading.value = true
  try {
    const res = await api.getExamPapers({ page: 1, size: 50 })
    papers.value = (res.data && res.data.records) || res.data || []
  } catch (e) {
    papers.value = []
  } finally {
    loading.value = false
  }
}

// 开始考试：携带试卷标题与时长（答题页倒计时来源）
const startExam = (paper) => {
  uni.navigateTo({
    url: `/subpkg-study/pages/exam/taking?paperId=${paper.id}&title=${encodeURIComponent(paper.title || '')}&duration=${paper.duration || 60}`
  })
}

onLoad(() => {
  if (!requireLogin(buildLoginRedirect())) return
  loadPapers()
})

onShow(() => applyNavBarTheme())
</script>

<style lang="scss" scoped>
.exam-page {
  min-height: 100vh;
  padding: $spacing-lg;
  background: var(--app-bg, #F1F5F9);
  box-sizing: border-box;
}

/* ===== 页头卡 ===== */
.header-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.page-title {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-md;
}

/* 警示条：浅黄底 */
.warn-bar {
  padding: $spacing-sm $spacing-md;
  background: rgba(245, 158, 11, 0.12);
  border-radius: $radius-md;
}

.warn-text {
  font-size: 12px;
  color: $color-warning;
  line-height: 1.6;
}

/* ===== 试卷卡片 ===== */
.paper-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.paper-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.paper-info {
  flex: 1;
  min-width: 0;
}

.paper-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-sm;
}

.paper-desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
  line-height: 1.6;
  margin-bottom: $spacing-md;
}

.paper-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.meta-tag {
  padding: 2px 8px;
  border-radius: $radius-sm;
  font-size: 11px;
  line-height: 1.6;
}

/* 三个 tag 配色（纯 CSS，替代 element tag） */
.tag-warn {
  color: $color-warning;
  background: rgba(245, 158, 11, 0.12);
}

.tag-success {
  color: $color-success;
  background: rgba(16, 185, 129, 0.12);
}

.tag-info {
  color: var(--app-text-secondary, #64748B);
  background: var(--app-bg, #F1F5F9);
}

/* 开始考试按钮：主色 */
.paper-action {
  flex-shrink: 0;
}

.start-btn {
  display: inline-block;
  padding: 8px 16px;
  border-radius: $radius-full;
  font-size: 13px;
  font-weight: 600;
  color: #FFFFFF;
  background: var(--app-primary, #4F46E5);
}

.start-btn:active {
  opacity: 0.85;
}

/* ===== 空态 ===== */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: 60px 0;
}

.empty-text {
  font-size: 13px;
  color: var(--app-text-tertiary, #94A3B8);
}
</style>
