<template>
  <view :class="['taking-page', isDark ? 'theme-dark' : '']">
    <!-- 顶部自定义导航栏：试卷名 + 倒计时 + 切屏计数 + 退出 -->
    <NavBar :show-back="false">
      <template #title>
        <view class="nav-info">
          <text class="exam-name">{{ paperTitle || '在线考试' }}</text>
          <view class="countdown-wrap">
            <Icon name="clock" :size="14" :color="countdownDanger ? '#EF4444' : 'currentColor'" />
            <text :class="['countdown', countdownDanger ? 'danger' : '']">{{ countdownText }}</text>
          </view>
        </view>
      </template>
      <template #right>
        <view class="nav-right-group">
          <view :class="['switch-count', switchCount > 0 ? 'danger' : '']">
            <text class="switch-text">切屏 {{ switchCount }}/3</text>
          </view>
          <text class="exit-btn" @click="handleExit">退出</text>
        </view>
      </template>
    </NavBar>

    <!-- 答题卡切换行 -->
    <view class="status-bar">
      <text class="status-text">已答 {{ answeredCount }} / {{ questions.length }}</text>
      <view class="card-toggle" @click="showCard = !showCard">
        <text class="toggle-text">{{ showCard ? '收起答题卡' : '答题卡' }}</text>
        <Icon :name="showCard ? 'chevron-down' : 'chevron-right'" :size="14" />
      </view>
    </view>

    <!-- 防作弊警示条：浅黄底 -->
    <view class="anti-cheat-bar">
      <text class="anti-cheat-text">离开考试页面超过 10 秒或累计切屏 3 次将强制交卷</text>
    </view>

    <!-- 可折叠答题卡面板 -->
    <view v-if="showCard" class="answer-card">
      <view class="card-stats">
        <text class="stats-text">已答 </text>
        <text class="stats-num">{{ answeredCount }}</text>
        <text class="stats-text"> / {{ questions.length }}</text>
      </view>
      <view class="card-grid">
        <view
          v-for="(q, i) in questions"
          :key="q.questionId"
          :class="['card-item', { answered: isAnswered(q), marked: markedSet.has(q.questionId), current: i === currentIndex }]"
          @click="jumpTo(i)"
        >
          <text class="card-item-text">{{ i + 1 }}</text>
        </view>
      </view>
      <view class="card-legend">
        <view class="legend-item">
          <view class="legend-dot dot-answered"></view>
          <text class="legend-text">已答</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot dot-unanswered"></view>
          <text class="legend-text">未答</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot dot-marked"></view>
          <text class="legend-text">标记</text>
        </view>
      </view>
    </view>

    <!-- 主体 -->
    <view class="exam-main">
      <!-- 加载骨架 -->
      <Skeleton v-if="loading && !questions.length" type="detail" :count="1" />

      <!-- 空态 -->
      <view v-else-if="!questions.length" class="empty">
        <Icon name="document" :size="52" color="#CBD5E1" />
        <text class="empty-text">试卷无题目</text>
      </view>

      <!-- 当前题卡 -->
      <view v-else-if="currentQuestion" class="question-card">
        <!-- 题头：题型 tag + 分值 + 序号 -->
        <view class="question-head">
          <text :class="['type-tag', `type-${currentQuestion.type}`]">{{ typeLabel(currentQuestion.type) }}</text>
          <text class="question-score">{{ currentQuestion.score }} 分</text>
          <text class="question-index">{{ currentIndex + 1 }} / {{ questions.length }}</text>
        </view>

        <!-- 题干（Markdown 渲染） -->
        <view class="question-stem markdown-body">
          <rich-text :nodes="renderStem(currentQuestion.stem)"></rich-text>
        </view>

        <!-- 单选 -->
        <view v-if="currentQuestion.type === 1" class="options">
          <view
            v-for="(opt, i) in currentQuestion.options"
            :key="i"
            :class="['option-item', { active: singleAnswer === optionKey(i) }]"
            @click="setSingle(optionKey(i))"
          >
            <view :class="['radio', { checked: singleAnswer === optionKey(i) }]"></view>
            <text class="option-key">{{ optionKey(i) }}.</text>
            <view class="option-text markdown-body">
              <rich-text :nodes="renderStem(opt)"></rich-text>
            </view>
          </view>
        </view>

        <!-- 多选 -->
        <view v-else-if="currentQuestion.type === 2" class="options">
          <view
            v-for="(opt, i) in currentQuestion.options"
            :key="i"
            :class="['option-item', { active: multiAnswer.includes(optionKey(i)) }]"
            @click="toggleMulti(optionKey(i))"
          >
            <view :class="['checkbox', { checked: multiAnswer.includes(optionKey(i)) }]"></view>
            <text class="option-key">{{ optionKey(i) }}.</text>
            <view class="option-text markdown-body">
              <rich-text :nodes="renderStem(opt)"></rich-text>
            </view>
          </view>
        </view>

        <!-- 判断：对/错两个大按钮 -->
        <view v-else-if="currentQuestion.type === 3" class="judge-options">
          <view
            :class="['judge-btn', { active: singleAnswer === '对' }]"
            @click="setSingle('对')"
          >
            <text class="judge-symbol">✓</text>
            <text class="judge-label">对</text>
          </view>
          <view
            :class="['judge-btn', { active: singleAnswer === '错' }]"
            @click="setSingle('错')"
          >
            <text class="judge-symbol">✗</text>
            <text class="judge-label">错</text>
          </view>
        </view>

        <!-- 填空：每行一个空 -->
        <view v-else-if="currentQuestion.type === 4" class="blank-area">
          <view v-for="(_, bi) in blankCount" :key="bi" class="blank-row">
            <text class="blank-label">第 {{ bi + 1 }} 空</text>
            <input
              class="blank-input"
              type="text"
              :value="blankAnswers[bi] || ''"
              placeholder="请输入答案"
              @input="onBlankInput($event, bi)"
            />
          </view>
        </view>

        <!-- 简答 -->
        <view v-else-if="currentQuestion.type === 5" class="blank-area">
          <textarea
            class="answer-textarea"
            :value="textAnswer"
            placeholder="请输入答案"
            :maxlength="-1"
            @input="onTextInput"
          />
        </view>

        <!-- 编程：语言 chips + 多行等宽 textarea -->
        <view v-else-if="currentQuestion.type === 6" class="blank-area">
          <view class="lang-bar">
            <view
              v-for="lang in LANGS"
              :key="lang.value"
              :class="['lang-chip', { active: codeLang === lang.value }]"
              @click="codeLang = lang.value"
            >
              <text class="lang-text">{{ lang.label }}</text>
            </view>
          </view>
          <textarea
            class="code-textarea"
            :value="textAnswer"
            placeholder="请输入代码"
            :maxlength="-1"
            @input="onTextInput"
          />
        </view>
      </view>
    </view>

    <!-- 底部操作栏（fixed） -->
    <view class="bottom-bar">
      <view :class="['bar-btn', { disabled: currentIndex === 0 }]" @click="goPrev">
        <text class="bar-btn-text">上一题</text>
      </view>
      <view :class="['bar-btn', 'mark-btn', { marked: isMarked }]" @click="toggleMark">
        <text class="bar-btn-text">{{ isMarked ? '取消标记' : '标记本题' }}</text>
      </view>
      <view :class="['bar-btn', { disabled: currentIndex >= questions.length - 1 }]" @click="goNext">
        <text class="bar-btn-text">下一题</text>
      </view>
      <view class="bar-btn submit-btn" @click="confirmSubmit">
        <text class="bar-btn-text submit-text">交卷</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onLoad, onUnload, onShow, onHide } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import { requireLogin, buildLoginRedirect } from '@/common/auth.js'
import { parseMarkdown } from '@/utils/markdown.js'
import NavBar from '@/components/NavBar.vue'
import Icon from '@/components/Icon.vue'
import Skeleton from '@/components/Skeleton.vue'

// 题型映射（与 frontend ExamTaking TYPE_LABELS 一致：1单选 2多选 3判断 4填空 5简答 6编程）
const TYPE_LABELS = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题', 6: '编程题' }
const typeLabel = (t) => TYPE_LABELS[t] || '未知'
// 选项标识：索引转 A/B/C/D
const optionKey = (i) => String.fromCharCode(65 + i)
// 编程题语言 chips
const LANGS = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'java', label: 'Java' },
  { value: 'python', label: 'Python' }
]

const loading = ref(false)
const paperId = ref('')
const paperTitle = ref('')
const paperDuration = ref(0)
const questions = ref([])
const currentIndex = ref(0)
// 答案存储：questionId -> string[]（与 frontend 一致）
const answers = reactive({})
const markedSet = reactive(new Set())
const showCard = ref(false)
const codeLang = ref('javascript')

// 倒计时
const remainSeconds = ref(0)
const countdownDanger = computed(() => remainSeconds.value > 0 && remainSeconds.value < 300)
const countdownText = computed(() => {
  const m = Math.floor(remainSeconds.value / 60)
  const s = remainSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

// 防作弊
const switchCount = ref(0)
let hideAt = 0
let timer = null
let submitted = false
let startTime = Date.now()

const currentQuestion = computed(() => questions.value[currentIndex.value])
const answeredCount = computed(() => questions.value.filter((q) => isAnswered(q)).length)
const isMarked = computed(() => !!currentQuestion.value && markedSet.has(currentQuestion.value.questionId))

// 当前题答案视图
const singleAnswer = computed(() => (answers[currentQuestion.value?.questionId] || [])[0] || '')
const multiAnswer = computed(() => answers[currentQuestion.value?.questionId] || [])
const textAnswer = computed(() => (answers[currentQuestion.value?.questionId] || []).join('\n'))
// 填空空位数量：优先按已存答案，其次按题干下划线占位符推断，至少 1
const blankCount = computed(() => {
  const q = currentQuestion.value
  if (!q) return 1
  const stored = answers[q.questionId]
  if (Array.isArray(stored) && stored.length > 1) return stored.length
  const matches = String(q.stem || '').match(/_{2,}/g)
  return Math.max(1, matches ? matches.length : 1)
})
const blankAnswers = computed(() => answers[currentQuestion.value?.questionId] || [])

// Markdown 渲染
const renderStem = (text) => parseMarkdown(text || '')

// 是否已作答（与 frontend isAnswered 一致：数组内存在非空项）
const isAnswered = (q) => {
  const a = answers[q.questionId]
  return Array.isArray(a) && a.some((x) => x !== null && x !== undefined && String(x).trim() !== '')
}

// ===== 答案读写 =====
const setSingle = (val) => {
  answers[currentQuestion.value.questionId] = [val]
}
// 多选：可反复点选，答案按选项顺序排序数组
const toggleMulti = (val) => {
  const qid = currentQuestion.value.questionId
  const arr = answers[qid] || []
  const idx = arr.indexOf(val)
  if (idx >= 0) arr.splice(idx, 1)
  else {
    arr.push(val)
    arr.sort()
  }
  answers[qid] = [...arr]
}
// 填空：按空位写入
const onBlankInput = (e, bi) => {
  const qid = currentQuestion.value.questionId
  const arr = Array.isArray(answers[qid]) ? [...answers[qid]] : []
  while (arr.length < blankCount.value) arr.push('')
  arr[bi] = e.detail.value
  answers[qid] = arr
}
// 简答/编程：文本按 \n split 存数组
const onTextInput = (e) => {
  answers[currentQuestion.value.questionId] = String(e.detail.value || '').split('\n')
}

// 标记本题（toggle）
const toggleMark = () => {
  const qid = currentQuestion.value.questionId
  if (markedSet.has(qid)) markedSet.delete(qid)
  else markedSet.add(qid)
}

// 题号跳转
const jumpTo = (i) => {
  currentIndex.value = i
  showCard.value = false
}
const goPrev = () => {
  if (currentIndex.value > 0) currentIndex.value--
}
const goNext = () => {
  if (currentIndex.value < questions.value.length - 1) currentIndex.value++
}

// options 兼容数组或 JSON 字符串（try JSON.parse）
const parseOptions = (opt) => {
  if (Array.isArray(opt)) return opt
  if (typeof opt === 'string' && opt.trim()) {
    try {
      const v = JSON.parse(opt)
      if (Array.isArray(v)) return v
    } catch (e) {
      // 非 JSON，忽略
    }
  }
  return []
}

// 加载试卷题目
const loadPaper = async () => {
  loading.value = true
  try {
    const res = await api.getExamPaper(paperId.value)
    questions.value = (res.data || []).map((q) => ({ ...q, options: parseOptions(q.options) }))
  } catch (e) {
    questions.value = []
  } finally {
    loading.value = false
  }
}

// ===== 计时 =====
const startTimer = () => {
  timer = setInterval(() => {
    if (remainSeconds.value > 0) remainSeconds.value--
    if (remainSeconds.value <= 0) {
      stopTimer()
      if (!submitted) {
        uni.showToast({ title: '考试时间到，自动交卷', icon: 'none' })
        doSubmit()
      }
    }
  }, 1000)
}
const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

// ===== 防作弊：onHide 记 hideAt，onShow 算差值 =====
onHide(() => {
  if (!submitted) hideAt = Date.now()
})

onShow(() => {
  // custom 导航页也保留根类主题同步
  applyNavBarTheme()
  if (submitted || !hideAt) return
  const away = Date.now() - hideAt
  hideAt = 0
  if (!questions.value.length) return
  switchCount.value++
  if (away > 10000 || switchCount.value >= 3) {
    uni.showToast({ title: '切屏超限，已强制交卷', icon: 'none' })
    doSubmit()
  } else {
    uni.showToast({ title: `检测到切屏，已累计 ${switchCount.value}/3 次`, icon: 'none' })
  }
})

// ===== 退出 =====
const handleExit = () => {
  uni.showModal({
    title: '退出考试',
    content: '退出后答题记录将不会保存，确定退出考试吗？',
    confirmText: '确定退出',
    cancelText: '继续答题',
    success: (res) => {
      if (res.confirm) {
        cleanup()
        uni.navigateBack()
      }
    }
  })
}

// ===== 交卷 =====
const confirmSubmit = () => {
  const unanswered = questions.value.length - answeredCount.value
  uni.showModal({
    title: '交卷确认',
    content: unanswered > 0 ? `还有 ${unanswered} 道题未作答，确定交卷吗？` : '确定交卷吗？',
    confirmText: '交卷',
    cancelText: '再检查一下',
    success: (res) => {
      if (res.confirm) doSubmit()
    }
  })
}

const doSubmit = async () => {
  if (submitted) return
  submitted = true
  stopTimer()
  // 提交体与 frontend ExamTaking 逐字段对齐
  const payload = {
    answers: questions.value
      .filter((q) => isAnswered(q))
      .map((q) => ({ questionId: q.questionId, answer: answers[q.questionId] })),
    switchCount: switchCount.value,
    durationSeconds: Math.floor((Date.now() - startTime) / 1000)
  }
  try {
    await api.submitExam(paperId.value, payload)
    uni.showToast({ title: '交卷成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/subpkg-study/pages/scores/index' })
    }, 800)
  } catch (e) {
    // 失败允许重试
    submitted = false
    startTimer()
  }
}

const cleanup = () => {
  stopTimer()
  hideAt = 0
}

onLoad((options) => {
  if (!requireLogin(buildLoginRedirect())) return
  paperId.value = options.paperId || ''
  paperTitle.value = options.title ? decodeURIComponent(options.title) : ''
  paperDuration.value = Number(options.duration) || 60
  startTime = Date.now()
  remainSeconds.value = paperDuration.value * 60
  loadPaper().then(() => {
    if (!questions.value.length) return
    startTimer()
  })
})

onUnload(cleanup)
</script>

<style lang="scss" scoped>
.taking-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  box-sizing: border-box;
  padding-bottom: calc(64px + env(safe-area-inset-bottom));
}

/* ===== 导航栏内嵌信息 ===== */
.nav-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  max-width: 100%;
}

.exam-name {
  max-width: 30vw;
  font-size: 14px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.countdown-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.countdown {
  font-size: 16px;
  font-weight: 700;
  font-family: 'Menlo', 'Consolas', monospace;
  color: var(--app-primary, #4F46E5);
}

.countdown.danger {
  color: $color-danger;
}

.nav-right-group {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.switch-count {
  display: flex;
}

.switch-text {
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}

.switch-count.danger .switch-text {
  color: $color-warning;
  font-weight: 600;
}

.exit-btn {
  font-size: 13px;
  color: var(--app-primary, #4F46E5);
}

/* ===== 状态行：已答统计 + 答题卡切换 ===== */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-bottom: 1px solid var(--app-border, #E2E8F0);
}

.status-text {
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

.card-toggle {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toggle-text {
  font-size: 13px;
  color: var(--app-primary, #4F46E5);
}

/* ===== 防作弊警示条：浅黄底 ===== */
.anti-cheat-bar {
  padding: $spacing-sm $spacing-lg;
  background: rgba(245, 158, 11, 0.12);
  border-bottom: 1px solid rgba(245, 158, 11, 0.25);
}

.anti-cheat-text {
  font-size: 11px;
  color: $color-warning;
  line-height: 1.6;
}

/* ===== 答题卡面板 ===== */
.answer-card {
  padding: $spacing-lg;
  margin: $spacing-md $spacing-lg 0;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.card-stats {
  display: flex;
  align-items: baseline;
  margin-bottom: $spacing-md;
}

.stats-text {
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

.stats-num {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-primary, #4F46E5);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: $spacing-sm;
}

.card-item {
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-md;
  background: var(--app-bg-card, #FFFFFF);
}

.card-item-text {
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

/* 三态：已答主色底 */
.card-item.answered {
  background: var(--app-primary, #4F46E5);
  border-color: var(--app-primary, #4F46E5);
}

.card-item.answered .card-item-text {
  color: #FFFFFF;
}

/* 标记：橙边 */
.card-item.marked {
  border: 2px solid $color-warning;
}

/* 当前：主色描边 */
.card-item.current {
  border: 2px solid var(--app-primary, #4F46E5);
}

.card-item.current:not(.answered) .card-item-text {
  color: var(--app-primary, #4F46E5);
  font-weight: 700;
}

.card-legend {
  display: flex;
  gap: $spacing-lg;
  margin-top: $spacing-md;
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
  border: 1px solid var(--app-border, #E2E8F0);
  background: var(--app-bg-card, #FFFFFF);
}

.dot-answered {
  background: var(--app-primary, #4F46E5);
  border-color: var(--app-primary, #4F46E5);
}

.dot-marked {
  border: 2px solid $color-warning;
}

.legend-text {
  font-size: 11px;
  color: var(--app-text-secondary, #64748B);
}

/* ===== 主体 ===== */
.exam-main {
  padding: $spacing-md $spacing-lg;
}

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

.question-card {
  padding: $spacing-lg;
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.question-head {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.type-tag {
  padding: 2px 8px;
  border-radius: $radius-sm;
  font-size: 11px;
  color: var(--app-text-secondary, #64748B);
  background: var(--app-bg, #F1F5F9);
}

/* 题型 tag 配色（参考 frontend typeTagType） */
.type-1 {
  color: var(--app-primary, #4F46E5);
  background: rgba(79, 70, 229, 0.1);
}

.type-2 {
  color: $color-success;
  background: rgba(16, 185, 129, 0.1);
}

.type-3 {
  color: var(--app-text-secondary, #64748B);
  background: var(--app-bg, #F1F5F9);
}

.type-4 {
  color: $color-warning;
  background: rgba(245, 158, 11, 0.12);
}

.type-5,
.type-6 {
  color: $color-danger;
  background: rgba(239, 68, 68, 0.1);
}

.question-score {
  font-size: 13px;
  font-weight: 600;
  color: $color-warning;
}

.question-index {
  margin-left: auto;
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}

/* 题干 Markdown */
.question-stem {
  font-size: 15px;
  line-height: 1.8;
  color: var(--app-text, #0F172A);
  margin-bottom: $spacing-lg;
}

.question-stem :deep(p) {
  margin: 0 0 8px 0;
}

.question-stem :deep(pre) {
  background: var(--app-bg, #F1F5F9);
  padding: 12px;
  border-radius: $radius-md;
  font-family: 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  overflow-x: auto;
  margin: 0 0 8px 0;
}

.question-stem :deep(code) {
  font-family: 'Menlo', 'Consolas', monospace;
}

.question-stem :deep(:not(pre) > code) {
  background: var(--app-bg, #F1F5F9);
  padding: 2px 6px;
  border-radius: $radius-sm;
  font-size: 13px;
}

.question-stem :deep(ul),
.question-stem :deep(ol) {
  padding-left: 20px;
  margin: 0 0 8px 0;
}

.question-stem :deep(img) {
  max-width: 100%;
}

/* ===== 选项 ===== */
.options {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-md;
  background: var(--app-bg-card, #FFFFFF);
}

/* 选中：主色边框 */
.option-item.active {
  border-color: var(--app-primary, #4F46E5);
  background: rgba(79, 70, 229, 0.06);
}

.radio,
.checkbox {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  margin-top: 1px;
  border: 2px solid var(--app-border, #E2E8F0);
  background: var(--app-bg-card, #FFFFFF);
  position: relative;
}

.radio {
  border-radius: 50%;
}

.checkbox {
  border-radius: $radius-sm;
}

.radio.checked,
.checkbox.checked {
  border-color: var(--app-primary, #4F46E5);
  background: var(--app-primary, #4F46E5);
}

.radio.checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 4px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FFFFFF;
}

.checkbox.checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 6px;
  height: 10px;
  border: solid #FFFFFF;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.option-key {
  flex-shrink: 0;
  font-weight: 700;
  color: var(--app-primary, #4F46E5);
}

.option-text {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--app-text, #0F172A);
  word-break: break-word;
}

.option-text :deep(p) {
  margin: 0;
}

/* ===== 判断题大按钮 ===== */
.judge-options {
  display: flex;
  gap: $spacing-lg;
}

.judge-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px 0;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-md;
  background: var(--app-bg-card, #FFFFFF);
}

.judge-btn.active {
  border-color: var(--app-primary, #4F46E5);
  background: rgba(79, 70, 229, 0.06);
}

.judge-symbol {
  font-size: 22px;
  font-weight: 700;
  color: var(--app-primary, #4F46E5);
}

.judge-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

/* ===== 填空 / 简答 / 编程 ===== */
.blank-area {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.blank-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.blank-label {
  flex-shrink: 0;
  width: 52px;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

.blank-input {
  flex: 1;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-md;
  background: var(--app-bg, #F1F5F9);
  font-size: 14px;
  color: var(--app-text, #0F172A);
}

.answer-textarea {
  width: 100%;
  min-height: 140px;
  padding: 12px;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-md;
  background: var(--app-bg, #F1F5F9);
  font-size: 14px;
  line-height: 1.7;
  color: var(--app-text, #0F172A);
  box-sizing: border-box;
}

/* 编程题：语言 chips */
.lang-bar {
  display: flex;
  gap: $spacing-sm;
}

.lang-chip {
  padding: 5px 14px;
  border-radius: $radius-full;
  border: 1px solid var(--app-border, #E2E8F0);
  background: var(--app-bg-card, #FFFFFF);
}

.lang-chip.active {
  border-color: var(--app-primary, #4F46E5);
  background: rgba(79, 70, 229, 0.08);
}

.lang-text {
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}

.lang-chip.active .lang-text {
  color: var(--app-primary, #4F46E5);
  font-weight: 600;
}

/* 代码输入区：等宽字体 + 6 行以上高度 */
.code-textarea {
  width: 100%;
  min-height: 180px;
  padding: 12px;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-md;
  background: var(--app-bg, #F1F5F9);
  font-family: 'JetBrains Mono', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: var(--app-text, #0F172A);
  box-sizing: border-box;
}

/* ===== 底部操作栏（fixed） ===== */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-lg;
  padding-bottom: calc($spacing-sm + env(safe-area-inset-bottom));
  background: var(--app-bg-card, #FFFFFF);
  border-top: 1px solid var(--app-border, #E2E8F0);
  box-shadow: 0 -1px 8px rgba(15, 23, 42, 0.04);
}

.bar-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-full;
  background: var(--app-bg-card, #FFFFFF);
}

.bar-btn.disabled {
  opacity: 0.4;
}

.bar-btn-text {
  font-size: 13px;
  color: var(--app-text, #0F172A);
}

/* 标记本题：橙色态 */
.mark-btn.marked {
  border-color: $color-warning;
  background: rgba(245, 158, 11, 0.1);
}

.mark-btn.marked .bar-btn-text {
  color: $color-warning;
  font-weight: 600;
}

/* 交卷：主色按钮 */
.submit-btn {
  flex: 1.3;
  border: none;
  background: var(--app-primary, #4F46E5);
}

.submit-text {
  color: #FFFFFF;
  font-weight: 600;
}
</style>
