<template>
  <view :class="['readlater-page', isDark ? 'theme-dark' : '']">
    <!-- 稍后阅读列表：数据来自 storage，离线可读 -->
    <template v-if="list.length">
      <SwipeCell
        v-for="(item, i) in list"
        :key="item.id"
        class="cell"
        :ref="(el) => (cellEls[i] = el)"
        @open="onCellOpen(i)"
        @click="goDetail(item)"
      >
        <!-- 默认插槽：条目卡片 -->
        <view class="item-card">
          <text class="item-title">{{ item.title }}</text>
          <text class="item-summary">{{ item.summary }}</text>
          <view class="item-footer">
            <text class="item-date">{{ dateStr(item) }}</text>
            <text :class="['item-badge', `type-${item.type}`]">{{ typeMap[item.type] }}</text>
          </view>
        </view>
        <!-- 右侧操作区：删除按钮（宽 = actionWidth 72px） -->
        <template #actions>
          <view class="del-btn" @click="onDelete(item)">
            <!-- 内联 SVG trash 图标 20x20 stroke 1.8 currentColor -->
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              <line x1="10" y1="11" x2="10" y2="17" />
              <line x1="14" y1="11" x2="14" y2="17" />
            </svg>
            <text class="del-text">删除</text>
          </view>
        </template>
      </SwipeCell>
    </template>

    <!-- 空状态 -->
    <view v-else class="empty">
      <!-- 内联 SVG 书签图标 56x56 -->
      <svg class="empty-icon" viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
      </svg>
      <text class="empty-title">还没有稍后阅读的文章</text>
      <text class="empty-sub">在文章详情页点击「稍后阅读」即可离线收藏</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import { getReadLaterList, removeReadLater } from '@/common/offline.js'
import SwipeCell from '@/components/SwipeCell.vue'

// 稍后阅读列表（元素为完整文章对象，来自 storage）
const list = ref(getReadLaterList() || [])

// 每个 SwipeCell 的组件实例（函数 ref 收集，供互斥收起）
const cellEls = ref([])

// 文章类型映射
const typeMap = { 0: '原创', 1: '转载', 2: '翻译' }

// 日期字符串（截取前 10 位 yyyy-MM-dd），兼容 createTime 缺失
const dateStr = (item) => {
  const t = item && item.createTime
  return t ? String(t).slice(0, 10) : ''
}

// 某条目展开时收起其他所有条目
const onCellOpen = (i) => {
  cellEls.value.forEach((cell, j) => {
    if (j !== i && cell && cell.close) cell.close()
  })
}

// 未滑动时点击条目 → 跳转文章详情
const goDetail = (item) => {
  uni.navigateTo({ url: '/pages/article/detail?id=' + item.id })
}

// 删除：先移除 storage 记录（内部同步刷新 readLaterIds），再更新本地列表
const onDelete = (item) => {
  removeReadLater(item.id)
  const idx = list.value.findIndex((it) => it.id === item.id)
  if (idx > -1) list.value.splice(idx, 1)
  uni.showToast({ title: '已移除', icon: 'none' })
}

onLoad(() => {
  list.value = getReadLaterList() || []
})

// 每次页面显示：同步原生导航栏配色 + 重新读取列表（与详情页 toggle 保持同步）
onShow(() => {
  applyNavBarTheme()
  list.value = getReadLaterList() || []
})

// 主题切换时实时刷新原生导航栏配色
watch(isDark, () => applyNavBarTheme())
</script>

<style lang="scss" scoped>
/* 页面容器 */
.readlater-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  padding: 12px 16px calc(24px + env(safe-area-inset-bottom));
}

/* 每个 SwipeCell 条目间距 */
.cell {
  display: block;
  margin-bottom: $spacing-md;
}

/* 条目卡片：白卡 + 圆角 12px + 阴影 + 内边距 16px */
.item-card {
  padding: $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  display: flex;
  flex-direction: column;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

/* 触控反馈：按下轻微缩放 + 半透明 */
.item-card:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 标题：15px 600 最多 2 行省略 */
.item-title {
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

/* 摘要：13px 次要色 最多 2 行省略 */
.item-summary {
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

/* 底部行：日期 + 类型徽章 */
.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $spacing-sm;
}

/* 日期 */
.item-date {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 类型徽章：胶囊形白字 */
.item-badge {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: 11px;
  color: #fff;
  line-height: 1.4;
}
/* 原创 -> 主色（暗色下由 --app-primary 自适应） */
.type-0 { background: var(--app-primary, #4F46E5); }
/* 转载 -> 警告色 */
.type-1 { background: #F59E0B; }
/* 翻译 -> 成功色 */
.type-2 { background: #10B981; }

/* 删除按钮：撑满右侧操作区（宽 72px），红底白字竖排 */
.del-btn {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: var(--app-danger, #EF4444);
  color: #fff;
}

.del-text {
  font-size: 12px;
  color: #fff;
  line-height: 1.2;
}

/* 空状态：居中 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 25vh 32px 0;
}

.empty-icon {
  color: var(--app-text-tertiary, #94A3B8);
}

.empty-title {
  margin-top: $spacing-lg;
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

.empty-sub {
  margin-top: $spacing-sm;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
  line-height: 1.6;
  text-align: center;
}
</style>
