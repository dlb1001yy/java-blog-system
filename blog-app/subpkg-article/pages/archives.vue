<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['page-root', isDark ? 'theme-dark' : '']">
    <!-- 整页 scroll-view 滚动 -->
    <scroll-view class="container" scroll-y>
      <!-- 首次加载骨架：月份头横条 + 若干行横条 × 3 组 -->
      <view v-if="loading" class="timeline">
        <view v-for="g in 3" :key="'sg' + g" class="month-block">
          <view class="skeleton-block month-sk"></view>
          <view class="rows">
            <view v-for="i in 4" :key="'sr' + i" class="row-sk">
              <view :class="['skeleton-block', 'line', `w-${i % 3}`]"></view>
            </view>
          </view>
        </view>
      </view>

      <template v-else>
        <!-- 按月分组时间线 -->
        <view class="timeline">
          <view v-for="group in archives" :key="group.month" class="month-block">
            <!-- 月份头卡：月份 + 当月篇数 -->
            <view class="month-card">
              <text class="month-text">{{ group.month }}</text>
              <text class="month-count">{{ group.count }} 篇</text>
            </view>

            <!-- 垂直时间线：左圆点+竖线，右条目 -->
            <view class="rows">
              <view
                v-for="item in group.articles"
                :key="item.id"
                class="row"
                @click="goDetail(item.id)"
              >
                <view class="rail">
                  <view class="dot"></view>
                  <view class="line-rail"></view>
                </view>
                <view class="row-body">
                  <text class="row-date">{{ dayStr(item.date) }}</text>
                  <text class="row-title">{{ item.title }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="archives.length === 0" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
          <text class="empty-text">暂无归档</text>
        </view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'

// 归档数据：[{ month:'YYYY-MM', count, articles:[{ id, date, title }] }]
const archives = ref([])
const loading = ref(true)

// 日期截取 MM-DD（后端返回 yyyy-MM-dd HH:mm:ss 或 ISO，均取第 6-10 位）
const dayStr = (date) => {
  const s = date ? String(date) : ''
  return s.length >= 10 ? s.slice(5, 10) : s
}

// 拉取归档数据（loading:false，首屏由骨架呈现加载态）
const loadArchives = async () => {
  loading.value = true
  try {
    const res = await api.getArchives({ loading: false })
    archives.value = res.data || []
  } catch (e) {
    archives.value = []
  } finally {
    loading.value = false
  }
}

// 跳转文章详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}

onLoad(() => {
  loadArchives()
})

// 页面显示时同步原生导航栏配色；主题切换时实时刷新
onShow(() => applyNavBarTheme())
watch(isDark, () => applyNavBarTheme())
</script>

<style lang="scss" scoped>
/* 页面根节点：占满整屏，主题类挂在此处向 scroll-view 及内容级联 CSS 变量 */
.page-root {
  height: 100vh;
  background: var(--app-bg, #F1F5F9);
}

/* 滚动容器：占满根节点高度形成滚动区，底部预留 24px（无 TabBar） */
.container {
  height: 100%;
  box-sizing: border-box;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
}

/* 时间线容器 */
.timeline {
  padding: $spacing-md $spacing-lg 0;
}

/* 月份块：头卡 + 当月条目 */
.month-block {
  margin-bottom: $spacing-lg;
}

/* 月份头卡：白底胶囊卡，左月份右篇数 */
.month-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.month-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
}

.month-count {
  font-size: 12px;
  color: var(--app-primary, #4F46E5);
}

/* 条目区：与头卡左对齐并内缩，形成层级 */
.rows {
  padding: $spacing-sm 0 0 0;
}

/* 单条：左侧轨道 + 右侧内容 */
.row {
  display: flex;
  align-items: flex-start;
  padding: $spacing-sm 0;
}

/* 左侧轨道：圆点 + 竖线 */
.rail {
  position: relative;
  width: 20px;
  flex-shrink: 0;
  align-self: stretch;
}

/* 主色圆点：与首行文字对齐 */
.dot {
  position: absolute;
  left: 4px;
  top: 5px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--app-primary, #4F46E5);
}

/* 竖线：从圆点下方延伸到条目底部（最后一条由 overflow 裁掉） */
.line-rail {
  position: absolute;
  left: 7px;
  top: 17px;
  bottom: -8px;
  width: 2px;
  background: var(--app-border, #E2E8F0);
}

.row:last-child .line-rail {
  bottom: auto;
  height: 0;
}

/* 右侧内容：日期 + 标题同一行 */
.row-body {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: baseline;
  gap: $spacing-sm;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  padding: $spacing-md $spacing-lg;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 触控反馈：按下轻微缩放 + 半透明 */
.row-body:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 日期（MM-DD） */
.row-date {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
  flex-shrink: 0;
}

/* 标题：单行省略 */
.row-title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--app-text, #0F172A);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

/* 月份头骨架 */
.month-sk {
  height: 44px;
  width: 100%;
}

/* 条目骨架行 */
.row-sk {
  display: flex;
  padding: $spacing-sm 0;
}

.row-sk .line {
  height: 44px;
  width: 100%;
}

/* 让骨架行宽度有变化 */
.w-0 { width: 100%; }
.w-1 { width: 92%; }
.w-2 { width: 84%; }

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
</style>
