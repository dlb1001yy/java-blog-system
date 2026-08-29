<template>
  <!-- 外层 view 作为页面根节点，主题类挂此处向内级联 CSS 变量 -->
  <view :class="['scores-page', isDark ? 'theme-dark' : '']">
    <!-- ========== 记录列表态：scroll-view 分页，触底加载下一页 ========== -->
    <scroll-view
      v-if="!detail"
      class="scroll-body"
      scroll-y
      lower-threshold="120"
      @scrolltolower="onLoadMore"
    >
      <view class="list-body">
        <!-- 首屏骨架 -->
        <Skeleton v-if="loading && !records.length" type="article" :count="3" />

        <template v-else>
          <!-- 记录卡列表 -->
          <view
            v-for="rec in records"
            :key="rec.id"
            class="record-card"
            @click="openDetail(rec)"
          >
            <view class="record-main">
              <text class="record-title">{{ rec.paperTitle || '考试记录' }}</text>
              <view class="record-meta">
                <text class="record-score">{{ rec.finalScore != null ? rec.finalScore : '-' }} 分</text>
                <!-- 状态 tag：已发布=绿 / 待批改=橙 -->
                <text :class="['status-tag', rec.status === 1 ? 'tag-published' : 'tag-pending']">
                  {{ rec.status === 1 ? '已发布' : '待批改' }}
                </text>
              </view>
            </view>
            <view class="record-sub">
              <text class="sub-item">用时 {{ formatDuration(rec.durationSeconds) }}</text>
              <text class="sub-item">切屏 {{ rec.switchCount != null ? rec.switchCount : 0 }} 次</text>
              <text class="sub-item">{{ formatTime(rec.submitTime) }}</text>
            </view>
            <view class="record-arrow">
              <Icon name="chevron-right" :size="16" color="#CBD5E1" />
            </view>
          </view>

          <!-- 空态 -->
          <view v-if="!records.length" class="empty">
            <Icon name="trophy" :size="52" color="#CBD5E1" />
            <text class="empty-text">暂无考试记录</text>
          </view>

          <!-- 分页加载更多 -->
          <view v-if="records.length && loading" class="status-row">
            <LoadingDots :size="6" />
          </view>
          <view v-if="records.length && !loading && !hasMore" class="status-row">
            <text class="status-text">没有更多了</text>
          </view>
        </template>
      </view>
    </scroll-view>

    <!-- ========== 详情态：本地 state 切换，页面级滚动 ========== -->
    <view v-else class="detail-body">
      <!-- 详情头部：返回 + 试卷名 + 状态 -->
      <view class="detail-head">
        <view class="back-btn" @click="backToList">
          <Icon name="chevron-left" :size="18" />
          <text class="back-text">返回列表</text>
        </view>
        <text class="detail-paper">{{ detail.paperTitle || '成绩详情' }}</text>
        <text :class="['status-tag', detail.status === 1 ? 'tag-published' : 'tag-pending']">
          {{ detail.status === 1 ? '已发布' : '待批改' }}
        </text>
      </view>

      <view v-if="judging" class="judging-card">
        <LoadingDots :size="8" />
        <text class="judging-text">判分中，请稍候...</text>
      </view>

      <view v-else>
        <!-- Hero 得分卡 -->
        <view class="hero-card">
          <view class="hero-main">
            <view class="hero-score">
              <text class="hero-num">{{ detail.finalScore != null ? detail.finalScore : '-' }}</text>
              <text class="hero-total">/ {{ totalScore }} 分</text>
            </view>
            <view class="hero-tags">
              <!-- 及格 tag：及格线按总分 60% -->
              <text
                v-if="detail.finalScore != null"
                :class="['hero-tag', isPassed ? 'tag-pass' : 'tag-fail']"
              >{{ isPassed ? '及格' : '不及格' }}</text>
              <!-- 作弊嫌疑 tag：switchCount>=3 红 -->
              <text v-if="detail.switchCount >= 3" class="hero-tag tag-cheat">作弊嫌疑</text>
            </view>
          </view>
          <view class="hero-sub">
            <view class="hero-item">
              <text class="hero-label">用时</text>
              <text class="hero-value">{{ formatDuration(detail.durationSeconds) }}</text>
            </view>
            <view class="hero-item">
              <text class="hero-label">切屏次数</text>
              <text class="hero-value">{{ detail.switchCount != null ? detail.switchCount : 0 }}</text>
            </view>
            <view class="hero-item">
              <text class="hero-label">客观题得分</text>
              <text class="hero-value">{{ detail.objectiveScore != null ? detail.objectiveScore : 0 }}</text>
            </view>
            <view class="hero-item">
              <text class="hero-label">主观题得分</text>
              <text class="hero-value">{{ detail.subjectiveScore != null ? detail.subjectiveScore : 0 }}</text>
            </view>
            <view class="hero-item">
              <text class="hero-label">提交时间</text>
              <text class="hero-value">{{ formatTime(detail.submitTime) }}</text>
            </view>
          </view>
          <!-- 待批改警示条 -->
          <view v-if="detail.status !== 1" class="pending-alert">
            <text class="pending-text">主观题批改中，最终成绩以发布后为准</text>
          </view>
        </view>

        <!-- 纯 CSS 图表：题型得分分布 + 题型掌握度 -->
        <view v-if="typeStats.length" class="chart-wrap">
          <view class="chart-card">
            <text class="chart-title">题型得分分布</text>
            <view v-for="t in typeStats" :key="'s' + t.type" class="bar-row">
              <text class="bar-label">{{ t.label }}</text>
              <view class="bar-track">
                <view class="bar bar-full" :style="{ width: t.fullWidth + '%' }"></view>
                <view class="bar bar-got" :style="{ width: t.gotWidth + '%' }"></view>
              </view>
              <text class="bar-value">{{ t.got }} / {{ t.full }} 分</text>
            </view>
            <view class="chart-legend">
              <view class="legend-item">
                <view class="legend-dot dot-full"></view>
                <text class="legend-text">应得分</text>
              </view>
              <view class="legend-item">
                <view class="legend-dot dot-got"></view>
                <text class="legend-text">实得分</text>
              </view>
            </view>
          </view>

          <view class="chart-card">
            <text class="chart-title">题型掌握度</text>
            <view v-for="t in typeStats" :key="'r' + t.type" class="bar-row">
              <text class="bar-label">{{ t.label }}</text>
              <view class="bar-track">
                <view :class="['bar', 'bar-rate', rateClass(t.rate)]" :style="{ width: t.rate + '%' }"></view>
              </view>
              <text class="bar-value">{{ t.rate }}%</text>
            </view>
          </view>
        </view>

        <!-- 逐题答题回顾 -->
        <text class="section-title">答题回顾</text>
        <view
          v-for="(item, idx) in detail.items || []"
          :key="item.questionId"
          class="item-card"
        >
          <view class="item-head">
            <text class="item-index">第 {{ idx + 1 }} 题</text>
            <text class="type-tag">{{ typeLabel(item.type) }}</text>
            <text class="item-score">{{ item.gotScore != null ? item.gotScore : 0 }} / {{ item.score }} 分</text>
            <!-- 客观题对错 tag / 主观题待批改 -->
            <text v-if="item.type <= 4" :class="['status-tag', item.correct ? 'tag-published' : 'tag-fail']">
              {{ item.correct ? '正确' : '错误' }}
            </text>
            <text v-else class="status-tag tag-pending">待批改</text>
          </view>

          <!-- 题干 -->
          <view class="item-stem markdown-body">
            <rich-text :nodes="renderMd(item.stem)"></rich-text>
          </view>

          <view class="item-answers">
            <!-- 我的答案 -->
            <view class="answer-line">
              <text class="answer-label">我的答案</text>
              <text class="answer-value">{{ formatAnswer(item.myAnswer, item.type) || '未作答' }}</text>
            </view>
            <!-- 正确答案：仅客观题或已发布展示 -->
            <view v-if="item.type <= 4 || detail.status === 1" class="answer-line">
              <text class="answer-label">正确答案</text>
              <view class="answer-value markdown-body correct">
                <rich-text :nodes="renderMd(formatAnswer(item.type <= 4 ? item.correctAnswer : item.referenceAnswer, item.type))"></rich-text>
              </view>
            </view>
            <!-- 解析：仅客观题或已发布展示 -->
            <view v-if="item.type <= 4 || detail.status === 1" class="answer-line">
              <text class="answer-label">解析</text>
              <view class="answer-value markdown-body correct">
                <rich-text :nodes="renderMd(item.referenceAnswer)"></rich-text>
              </view>
            </view>
            <!-- 主观题评语 -->
            <view v-if="item.comment" class="answer-line">
              <text class="answer-label">评语</text>
              <text class="answer-value">{{ item.comment }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import { requireLogin, buildLoginRedirect } from '@/common/auth.js'
import { parseMarkdown } from '@/utils/markdown.js'
import Icon from '@/components/Icon.vue'
import Skeleton from '@/components/Skeleton.vue'
import LoadingDots from '@/components/LoadingDots.vue'

// 题型映射（与 frontend Scores TYPE_MAP 一致）
const TYPE_MAP = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题', 6: '编程题' }
const typeLabel = (t) => TYPE_MAP[t] || '未知'

// ===== 列表态 =====
const records = ref([])
const page = ref(1)
const loading = ref(false)
const hasMore = ref(true)

// ===== 详情态（本地 state 切换） =====
const detail = ref(null)
const judging = ref(false)
let pollCancelled = false

// 拉取记录分页
const fetchRecords = async () => {
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const res = await api.getExamRecords({ page: page.value, size: 10 })
    const data = res.data || {}
    const list = data.records || []
    records.value = page.value === 1 ? list : records.value.concat(list)
    hasMore.value = list.length === 10
  } catch (e) {
    if (page.value === 1) records.value = []
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 触底加载下一页
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  fetchRecords()
}

// 异步判分是否就绪（与 frontend isDetailReady 一致）
const isDetailReady = (d) =>
  d && Array.isArray(d.items) && d.items.length > 0 && d.objectiveScore !== null && d.objectiveScore !== undefined

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

// 打开详情：未就绪时轮询（最多 10 次，间隔 2s）
const openDetail = async (row) => {
  pollCancelled = false
  judging.value = false
  detail.value = { items: [] }
  try {
    let res = await api.getExamRecord(row.id)
    let data = res.data
    if (!isDetailReady(data)) {
      judging.value = true
      for (let i = 0; i < 10; i++) {
        await sleep(2000)
        if (pollCancelled) return
        res = await api.getExamRecord(row.id)
        data = res.data
        if (isDetailReady(data)) break
      }
      judging.value = false
    }
    detail.value = data
  } catch (e) {
    judging.value = false
    detail.value = null
  }
}

// 返回列表态并刷新
const backToList = () => {
  pollCancelled = true
  judging.value = false
  detail.value = null
  page.value = 1
  hasMore.value = true
  records.value = []
  fetchRecords()
}

// 总分 = 各题分值合计（与 frontend 一致）
const totalScore = computed(() =>
  (detail.value?.items || []).reduce((s, it) => s + Number(it.score || 0), 0)
)

// 及格：得分 >= 满分 60%
const isPassed = computed(() => {
  if (!detail.value) return false
  const score = Number(detail.value.finalScore)
  if (isNaN(score)) return false
  return totalScore.value ? score >= totalScore.value * 0.6 : score >= 60
})

// 题型聚合（应得分 / 实得分 / 得分率，与 frontend typeStats 逻辑一致）
const typeStats = computed(() => {
  const map = {}
  for (const it of detail.value?.items || []) {
    const key = it.type
    if (!map[key]) map[key] = { type: key, label: typeLabel(it.type), full: 0, got: 0 }
    map[key].full += Number(it.score || 0)
    map[key].got += Number(it.gotScore || 0)
  }
  const list = Object.values(map)
  const max = Math.max(...list.map((t) => t.full), 1)
  return list.map((t) => ({
    ...t,
    got: Math.round(t.got * 10) / 10,
    fullWidth: (t.full / max) * 100,
    gotWidth: (t.got / max) * 100,
    rate: t.full ? Math.round((t.got / t.full) * 100) : 0
  }))
})

// 掌握度配色：>=80% 绿 / >=50% 橙 / 其余红
const rateClass = (rate) => (rate >= 80 ? 'rate-good' : rate >= 50 ? 'rate-mid' : 'rate-bad')

// 用时格式化
const formatDuration = (sec) => {
  if (sec == null) return '-'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return m > 0 ? `${m} 分 ${s} 秒` : `${s} 秒`
}

// 时间格式化：ISO 转 yyyy-MM-dd HH:mm:ss
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

// Markdown 渲染（空内容显示"无"）
const renderMd = (text) => (text ? parseMarkdown(String(text)) : '<p>无</p>')

// 单值展示：单选/多选数字索引转字母、判断转对/错（与 frontend toDisplayValue 一致）
const toDisplayValue = (v, type) => {
  if (v === null || v === undefined) return ''
  if ((type === 1 || type === 2) && typeof v === 'number' && Number.isInteger(v) && v >= 0 && v < 26) {
    return String.fromCharCode(65 + v)
  }
  if (type === 3) {
    if (typeof v === 'boolean') return v ? '对' : '错'
    if (typeof v === 'number') return v !== 0 ? '对' : '错'
    if (typeof v === 'string') {
      const t = v.trim().toLowerCase()
      if (['对', '正确', '√', '是', 'true', 't', 'yes', 'y', '1'].includes(t)) return '对'
      if (['错', '错误', '×', 'x', '否', 'false', 'f', 'no', 'n', '0'].includes(t)) return '错'
    }
  }
  if (typeof v === 'object') return JSON.stringify(v)
  return String(v)
}

// 答案格式化：JSON 字符串解析 + 数组拼接（与 frontend formatAnswer 一致）
const formatAnswer = (ans, type) => {
  if (ans === null || ans === undefined || ans === '') return ''
  if (typeof ans !== 'string') return toDisplayValue(ans, type)
  try {
    const v = JSON.parse(ans)
    if (Array.isArray(v)) return v.map((x) => toDisplayValue(x, type)).filter(Boolean).join('、')
    return toDisplayValue(v, type)
  } catch (e) {
    return toDisplayValue(ans, type)
  }
}

onLoad(() => {
  if (!requireLogin(buildLoginRedirect())) return
  fetchRecords()
})

onShow(() => applyNavBarTheme())

onUnload(() => {
  pollCancelled = true
})
</script>

<style lang="scss" scoped>
.scores-page {
  height: 100vh;
  background: var(--app-bg, #F1F5F9);
  box-sizing: border-box;
}

/* 列表态滚动容器：占满根高度形成滚动区 */
.scroll-body {
  height: 100%;
  box-sizing: border-box;
}

.list-body {
  padding: $spacing-lg;
  padding-bottom: calc($spacing-xl + env(safe-area-inset-bottom));
}

/* 详情态：页面级滚动，带同样留白 */
.detail-body {
  min-height: 100%;
  padding: $spacing-lg;
  padding-bottom: calc($spacing-xl + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* ===== 记录卡 ===== */
.record-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-lg;
  padding-right: 36px;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.record-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
}

.record-title {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-shrink: 0;
}

.record-score {
  font-size: 15px;
  font-weight: 700;
  color: var(--app-primary, #4F46E5);
}

.record-sub {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.sub-item {
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}

/* 状态 tag：已发布绿 / 待批改橙 */
.status-tag {
  padding: 2px 8px;
  border-radius: $radius-sm;
  font-size: 11px;
  flex-shrink: 0;
}

.tag-published {
  color: $color-success;
  background: rgba(16, 185, 129, 0.12);
}

.tag-pending {
  color: $color-warning;
  background: rgba(245, 158, 11, 0.12);
}

.tag-fail {
  color: $color-danger;
  background: rgba(239, 68, 68, 0.1);
}

/* 右侧箭头：绝对定位垂直居中 */
.record-arrow {
  position: absolute;
  right: $spacing-lg;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
}

/* ===== 空态 / 加载更多 ===== */
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

.status-row {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg 0;
  color: var(--app-text-tertiary, #94A3B8);
}

.status-text {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* ===== 详情头部 ===== */
.detail-head {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  color: var(--app-primary, #4F46E5);
}

.back-text {
  font-size: 13px;
  color: var(--app-primary, #4F46E5);
}

.detail-paper {
  flex: 1;
  min-width: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 判分中 ===== */
.judging-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  padding: 60px 20px;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  color: var(--app-text-secondary, #64748B);
}

.judging-text {
  font-size: 14px;
  color: var(--app-text-secondary, #64748B);
}

/* ===== Hero 得分卡 ===== */
.hero-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.hero-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.hero-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.hero-num {
  font-size: 44px;
  font-weight: 700;
  line-height: 1;
  color: var(--app-primary, #4F46E5);
}

.hero-total {
  font-size: 14px;
  color: var(--app-text-secondary, #64748B);
}

.hero-tags {
  display: flex;
  gap: $spacing-sm;
}

.hero-tag {
  padding: 4px 12px;
  border-radius: $radius-full;
  font-size: 12px;
  font-weight: 600;
}

.tag-pass {
  color: #FFFFFF;
  background: $color-success;
}

.tag-fail {
  color: #FFFFFF;
  background: $color-danger;
}

.tag-cheat {
  color: #FFFFFF;
  background: $color-danger;
}

.hero-sub {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xl $spacing-xxl;
}

.hero-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hero-label {
  font-size: 11px;
  color: var(--app-text-secondary, #64748B);
}

.hero-value {
  font-size: 13px;
  color: var(--app-text, #0F172A);
}

/* 待批改警示条 */
.pending-alert {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background: rgba(245, 158, 11, 0.12);
  border-radius: $radius-md;
}

.pending-text {
  font-size: 12px;
  color: $color-warning;
  line-height: 1.6;
}

/* ===== 纯 CSS 图表 ===== */
.chart-wrap {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.chart-card {
  padding: $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.chart-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-lg;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.bar-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
  text-align: right;
  white-space: nowrap;
}

.bar-track {
  position: relative;
  flex: 1;
  height: 22px;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-md;
  overflow: hidden;
}

.bar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  border-radius: $radius-md;
}

/* 应得分：主色浅底 */
.bar-full {
  background: rgba(79, 70, 229, 0.18);
}

/* 实得分：主色 */
.bar-got {
  background: var(--app-primary, #4F46E5);
}

/* 掌握度：>=80 绿 / >=50 橙 / 其余红 */
.bar-rate.rate-good {
  background: $color-success;
}

.bar-rate.rate-mid {
  background: $color-warning;
}

.bar-rate.rate-bad {
  background: $color-danger;
}

.bar-value {
  width: 88px;
  flex-shrink: 0;
  font-size: 11px;
  color: var(--app-text-secondary, #64748B);
}

.chart-legend {
  display: flex;
  gap: $spacing-lg;
  margin-top: $spacing-sm;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.dot-full {
  background: rgba(79, 70, 229, 0.3);
}

.dot-got {
  background: var(--app-primary, #4F46E5);
}

.legend-text {
  font-size: 11px;
  color: var(--app-text-secondary, #64748B);
}

/* ===== 逐题回顾 ===== */
.section-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-md;
}

.item-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.item-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.item-index {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

/* 题型 tag */
.type-tag {
  padding: 2px 8px;
  border-radius: $radius-sm;
  font-size: 11px;
  color: var(--app-text-secondary, #64748B);
  background: var(--app-bg, #F1F5F9);
  flex-shrink: 0;
}

.item-score {
  margin-left: auto;
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}

.item-stem {
  font-size: 14px;
  line-height: 1.7;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-md;
}

.item-stem :deep(p) {
  margin: 0 0 8px 0;
}

.item-stem :deep(pre) {
  background: var(--app-bg, #F1F5F9);
  padding: 12px;
  border-radius: $radius-md;
  font-family: 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  overflow-x: auto;
  margin: 0 0 8px 0;
}

.item-stem :deep(code) {
  font-family: 'Menlo', 'Consolas', monospace;
}

.item-answers {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding-top: $spacing-md;
  border-top: 1px dashed var(--app-border, #E2E8F0);
}

.answer-line {
  display: flex;
  gap: $spacing-md;
}

.answer-label {
  flex-shrink: 0;
  width: 56px;
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
  text-align: right;
}

.answer-value {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--app-text, #0F172A);
  word-break: break-word;
  white-space: pre-wrap;
}

/* 正确答案 / 解析：主色文字 */
.answer-value.correct {
  color: var(--app-primary, #4F46E5);
}

.answer-value :deep(p) {
  margin: 0;
}

.answer-value :deep(pre) {
  background: var(--app-bg, #F1F5F9);
  padding: 10px;
  border-radius: $radius-md;
  font-family: 'Menlo', 'Consolas', monospace;
  font-size: 12px;
  overflow-x: auto;
}
</style>
