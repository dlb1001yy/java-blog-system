<template>
  <div class="exam-taking">
    <!-- 顶部固定栏 -->
    <header class="exam-topbar">
      <div class="topbar-left">
        <span class="exam-name">{{ paperTitle || '在线考试' }}</span>
      </div>
      <div class="topbar-center">
        <span class="countdown" :class="{ danger: countdownDanger }">
          <el-icon><Timer /></el-icon>
          {{ countdownText }}
        </span>
      </div>
      <div class="topbar-right">
        <span class="switch-count" :class="{ danger: switchCount > 0 }">切屏 {{ switchCount }}/3 次</span>
        <el-button size="small" @click="handleExit">退出</el-button>
      </div>
    </header>

    <!-- 防作弊警示条 -->
    <div class="anti-cheat-bar">
      ⚠️ 考试已开启防作弊监控：离开页面超过 10 秒或累计切屏达 3 次将强制交卷，禁止复制内容
    </div>

    <div v-loading="loading" class="exam-main">
      <!-- 左侧答题卡 -->
      <aside class="answer-card">
        <div class="card-header">答题卡</div>
        <div class="card-stats">
          已答 <b class="answered">{{ answeredCount }}</b> / {{ questions.length }}
        </div>
        <div class="card-grid">
          <button
            v-for="(q, i) in questions"
            :key="q.questionId"
            class="card-item"
            :class="{
              answered: isAnswered(q),
              marked: markedSet.has(q.questionId),
              current: i === currentIndex
            }"
            @click="currentIndex = i"
          >
            {{ i + 1 }}
          </button>
        </div>
        <div class="card-legend">
          <span><i class="dot answered" />已答</span>
          <span><i class="dot unanswered" />未答</span>
          <span><i class="dot marked" />标记</span>
        </div>
      </aside>

      <!-- 中央答题区 -->
      <section v-if="currentQuestion" class="question-area">
        <div class="question-head">
          <el-tag size="small" :type="typeTagType(currentQuestion.type)">{{ typeLabel(currentQuestion.type) }}</el-tag>
          <span class="question-score">{{ currentQuestion.score }} 分</span>
          <span class="question-index">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
        </div>

        <!-- 题干 Markdown -->
        <div class="question-stem markdown-body" v-html="renderStem(currentQuestion.stem)" />

        <!-- 单选 -->
        <div v-if="currentQuestion.type === 1" class="options">
          <label
            v-for="(opt, i) in currentQuestion.options"
            :key="i"
            class="option-item"
            :class="{ active: singleAnswer === optionKey(i) }"
          >
            <input
              type="radio"
              :name="`q-${currentQuestion.questionId}`"
              :checked="singleAnswer === optionKey(i)"
              @change="setSingle(optionKey(i))"
            />
            <span class="option-key">{{ optionKey(i) }}.</span>
            <span class="option-text" v-html="renderStem(opt)" />
          </label>
        </div>

        <!-- 多选 -->
        <div v-else-if="currentQuestion.type === 2" class="options">
          <label
            v-for="(opt, i) in currentQuestion.options"
            :key="i"
            class="option-item"
            :class="{ active: multiAnswer.includes(optionKey(i)) }"
          >
            <input
              type="checkbox"
              :checked="multiAnswer.includes(optionKey(i))"
              @change="toggleMulti(optionKey(i))"
            />
            <span class="option-key">{{ optionKey(i) }}.</span>
            <span class="option-text" v-html="renderStem(opt)" />
          </label>
        </div>

        <!-- 判断 -->
        <div v-else-if="currentQuestion.type === 3" class="options">
          <label class="option-item" :class="{ active: singleAnswer === '对' }">
            <input type="radio" :name="`q-${currentQuestion.questionId}`" :checked="singleAnswer === '对'" @change="setSingle('对')" />
            <span class="option-key">✓</span><span class="option-text">对</span>
          </label>
          <label class="option-item" :class="{ active: singleAnswer === '错' }">
            <input type="radio" :name="`q-${currentQuestion.questionId}`" :checked="singleAnswer === '错'" @change="setSingle('错')" />
            <span class="option-key">✗</span><span class="option-text">错</span>
          </label>
        </div>

        <!-- 填空 -->
        <div v-else-if="currentQuestion.type === 4" class="blank-area">
          <el-input
            :model-value="blankAnswer"
            placeholder="请输入答案"
            @update:model-value="v => setBlank(v)"
          />
        </div>

        <!-- 简答 -->
        <div v-else-if="currentQuestion.type === 5" class="blank-area">
          <el-input
            :model-value="textAnswer"
            type="textarea"
            :rows="6"
            placeholder="请输入答案"
            @update:model-value="v => setText(v)"
          />
        </div>

        <!-- 编程 -->
        <div v-else-if="currentQuestion.type === 6" class="blank-area">
          <!-- 语言切换 -->
          <div class="lang-bar">
            <el-radio-group v-model="codeLang" size="small">
              <el-radio-button label="javascript">JavaScript</el-radio-button>
              <el-radio-button label="java">Java</el-radio-button>
              <el-radio-button label="python">Python</el-radio-button>
            </el-radio-group>
          </div>
          <!-- Monaco 编辑器：加载失败或超时回退 textarea -->
          <vue-monaco-editor
            v-if="monacoReady"
            :value="textAnswer"
            :language="codeLang"
            theme="vs-dark"
            :options="monacoOptions"
            class="code-editor"
            @change="v => setText(v)"
            @mount="onMonacoMount"
          />
          <el-input
            v-else
            :model-value="textAnswer"
            type="textarea"
            :rows="10"
            placeholder="请输入代码"
            class="code-input"
            @update:model-value="v => setText(v)"
          />
        </div>
      </section>

      <el-empty v-else-if="!loading" description="试卷无题目" />
    </div>

    <!-- 底部操作栏 -->
    <footer class="exam-bottombar">
      <el-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</el-button>
      <el-button
        :type="markedSet.has(currentQuestion?.questionId) ? 'warning' : 'default'"
        @click="toggleMark"
      >
        {{ markedSet.has(currentQuestion?.questionId) ? '取消标记' : '标记本题' }}
      </el-button>
      <el-button :disabled="currentIndex >= questions.length - 1" @click="currentIndex++">下一题</el-button>
      <el-button type="primary" @click="confirmSubmit">交卷</el-button>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
import md from '@/utils/markdown'
import examApi from '@/api/exam'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const paperTitle = ref('')
const paperDuration = ref(0)
const questions = ref([])
const currentIndex = ref(0)
const answers = ref({}) // questionId -> string[]
const markedSet = ref(new Set())

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
let hiddenAt = 0
let timer = null
let submitted = false
const startTime = Date.now()

const currentQuestion = computed(() => questions.value[currentIndex.value])
const answeredCount = computed(() => questions.value.filter(q => isAnswered(q)).length)

const TYPE_LABELS = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题', 6: '编程题' }
const typeLabel = t => TYPE_LABELS[t] || '未知'
const typeTagType = t => ({ 1: 'primary', 2: 'success', 3: 'info', 4: 'warning', 5: 'danger', 6: 'danger' }[t] || 'info')
const optionKey = i => String.fromCharCode(65 + i)

const renderStem = (text) => md.render(text || '')

const isAnswered = (q) => {
  const a = answers.value[q.questionId]
  return Array.isArray(a) && a.some(x => x !== null && x !== undefined && String(x).trim() !== '')
}

// 答案读写
const singleAnswer = computed(() => answers.value[currentQuestion.value?.questionId]?.[0] || '')
const multiAnswer = computed(() => answers.value[currentQuestion.value?.questionId] || [])
const blankAnswer = computed(() => answers.value[currentQuestion.value?.questionId]?.[0] || '')
const textAnswer = computed(() => (answers.value[currentQuestion.value?.questionId] || []).join('\n'))

const setSingle = (val) => { answers.value[currentQuestion.value.questionId] = [val] }
const toggleMulti = (val) => {
  const qid = currentQuestion.value.questionId
  const arr = answers.value[qid] || []
  const idx = arr.indexOf(val)
  if (idx >= 0) arr.splice(idx, 1)
  else { arr.push(val); arr.sort() }
  answers.value[qid] = [...arr]
}
const setBlank = (v) => { answers.value[currentQuestion.value.questionId] = [v] }
const setText = (v) => { answers.value[currentQuestion.value.questionId] = v.split('\n') }

// 编程题 Monaco 编辑器
const codeLang = ref('javascript')
const monacoReady = ref(true) // Monaco 加载失败/超时后置 false 回退 textarea
const monacoOptions = {
  minimap: { enabled: false },
  fontSize: 13,
  automaticLayout: true,
  scrollBeyondLastLine: false,
  tabSize: 4,
  renderLineHighlight: 'all'
}
let monacoMounted = false
let monacoTimeout = null
const onMonacoMount = () => {
  monacoMounted = true
  clearTimeout(monacoTimeout)
}
// 监控 Monaco 是否加载成功：15 秒内未 mount 视为加载失败（如 CDN 不可用），回退 textarea
const watchMonaco = () => {
  clearTimeout(monacoTimeout)
  monacoTimeout = setTimeout(() => {
    if (!monacoMounted) monacoReady.value = false
  }, 15000)
}
watchMonaco()
// Monaco 内部加载异常（AMD loader error 等）时回退
if (typeof window !== 'undefined') {
  window.addEventListener('error', (e) => {
    if (typeof e?.message === 'string' && e.message.includes('vs/loader')) monacoReady.value = false
  })
}

const toggleMark = () => {
  const qid = currentQuestion.value.questionId
  const s = new Set(markedSet.value)
  s.has(qid) ? s.delete(qid) : s.add(qid)
  markedSet.value = s
}

// 加载试卷
const loadPaper = async () => {
  loading.value = true
  try {
    const res = await examApi.getPaperQuestions(route.params.paperId)
    questions.value = res.data || []
    if (res.data?.title) paperTitle.value = res.data.title
  } finally {
    loading.value = false
  }
}

// 倒计时
const startTimer = () => {
  timer = setInterval(() => {
    if (remainSeconds.value > 0) remainSeconds.value--
    if (remainSeconds.value <= 0) {
      clearInterval(timer)
      if (!submitted) {
        ElMessage.warning('考试时间到，自动交卷')
        doSubmit()
      }
    }
  }, 1000)
}

// 防作弊事件
const onVisibilityChange = () => {
  if (submitted) return
  if (document.hidden) {
    hiddenAt = Date.now()
  } else if (hiddenAt) {
    const away = Date.now() - hiddenAt
    hiddenAt = 0
    switchCount.value++
    if (away > 10000 || switchCount.value >= 3) {
      ElMessage.error('切屏超限，强制交卷')
      doSubmit()
    } else {
      ElMessage.warning(`检测到切屏，已累计 ${switchCount.value}/3 次`)
    }
  }
}
const blockEvent = e => e.preventDefault()
const onBeforeUnload = e => { e.preventDefault(); e.returnValue = '' }

// 退出
const handleExit = () => {
  ElMessageBox.confirm('退出后答题记录将不会保存，确定退出考试吗？', '退出考试', {
    confirmButtonText: '确定退出',
    cancelButtonText: '继续答题',
    type: 'warning'
  }).then(() => {
    cleanup()
    router.push('/exam')
  }).catch(() => {})
}

// 交卷
const confirmSubmit = () => {
  const unanswered = questions.value.length - answeredCount.value
  ElMessageBox.confirm(
    unanswered > 0 ? `还有 ${unanswered} 道题未作答，确定交卷吗？` : '确定交卷吗？',
    '交卷确认',
    { confirmButtonText: '交卷', cancelButtonText: '再检查一下', type: 'warning' }
  ).then(doSubmit).catch(() => {})
}

const doSubmit = async () => {
  if (submitted) return
  submitted = true
  cleanup()
  if (!userStore.token) {
    ElMessage.error('请先登录后再交卷')
    router.push('/exam')
    return
  }
  try {
    const payload = {
      answers: questions.value
        .filter(q => isAnswered(q))
        .map(q => ({ questionId: q.questionId, answer: answers.value[q.questionId] })),
      switchCount: switchCount.value,
      durationSeconds: Math.floor((Date.now() - startTime) / 1000)
    }
    await examApi.submitPaper(route.params.paperId, payload)
    ElMessage.success('交卷成功')
    router.push('/scores')
  } catch {
    submitted = false
  }
}

const cleanup = () => {
  clearInterval(timer)
  clearTimeout(monacoTimeout)
  document.removeEventListener('visibilitychange', onVisibilityChange)
  document.removeEventListener('contextmenu', blockEvent)
  document.removeEventListener('copy', blockEvent)
  document.removeEventListener('cut', blockEvent)
  window.removeEventListener('beforeunload', onBeforeUnload)
}

onMounted(async () => {
  await loadPaper()
  if (!questions.value.length) return
  if (route.query.title) paperTitle.value = route.query.title
  if (route.query.duration) paperDuration.value = Number(route.query.duration)
  remainSeconds.value = (paperDuration.value || 60) * 60
  startTimer()
  document.addEventListener('visibilitychange', onVisibilityChange)
  document.addEventListener('contextmenu', blockEvent)
  document.addEventListener('copy', blockEvent)
  document.addEventListener('cut', blockEvent)
  window.addEventListener('beforeunload', onBeforeUnload)
})

onBeforeUnmount(cleanup)
</script>

<style scoped>
.exam-taking {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  background: var(--bg-color, #f5f7fa);
}

/* 顶部栏 */
.exam-topbar {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--header-bg, #fff);
  border-bottom: 1px solid var(--border-color, #e4e7ed);
}

.exam-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary, #303133);
}

.countdown {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 20px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--primary-color, #409eff);
}

.countdown.danger {
  color: var(--el-color-danger, #f56c6c);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.switch-count {
  font-size: 13px;
  color: var(--text-secondary, #909399);
}

.switch-count.danger {
  color: var(--el-color-warning, #e6a23c);
  font-weight: 600;
}

/* 警示条 */
.anti-cheat-bar {
  flex-shrink: 0;
  padding: 6px 20px;
  font-size: 12px;
  color: var(--el-color-warning-dark, #b88230);
  background: rgba(230, 162, 60, 0.1);
  border-bottom: 1px solid rgba(230, 162, 60, 0.25);
  text-align: center;
}

/* 主体 */
.exam-main {
  flex: 1;
  display: flex;
  min-height: 0;
  padding: 16px;
  gap: 16px;
}

/* 答题卡 */
.answer-card {
  width: 260px;
  flex-shrink: 0;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, #e4e7ed);
  border-radius: 8px;
  padding: 16px;
  overflow-y: auto;
}

.card-header {
  font-weight: 700;
  color: var(--text-primary, #303133);
  margin-bottom: 8px;
}

.card-stats {
  font-size: 13px;
  color: var(--text-regular, #606266);
  margin-bottom: 12px;
}

.card-stats .answered {
  color: var(--primary-color, #409eff);
  font-size: 16px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}

.card-item {
  height: 34px;
  border: 1px solid var(--border-color, #dcdfe6);
  border-radius: 6px;
  background: #fff;
  color: var(--text-regular, #606266);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.card-item.answered {
  background: var(--primary-color, #409eff);
  border-color: var(--primary-color, #409eff);
  color: #fff;
}

.card-item.marked {
  border: 2px solid var(--el-color-warning, #e6a23c);
}

.card-item.current {
  border: 2px solid var(--primary-color, #409eff);
  font-weight: 700;
}

.card-item.current:not(.answered) {
  background: rgba(64, 158, 255, 0.08);
  color: var(--primary-color, #409eff);
}

.card-legend {
  display: flex;
  gap: 14px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--text-secondary, #909399);
}

.card-legend span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  display: inline-block;
  border: 1px solid var(--border-color, #dcdfe6);
  background: #fff;
}

.dot.answered { background: var(--primary-color, #409eff); border-color: var(--primary-color, #409eff); }
.dot.marked { border: 2px solid var(--el-color-warning, #e6a23c); }

/* 答题区 */
.question-area {
  flex: 1;
  min-width: 0;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, #e4e7ed);
  border-radius: 8px;
  padding: 24px 32px;
  overflow-y: auto;
}

.question-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.question-score {
  font-size: 13px;
  color: var(--el-color-warning, #e6a23c);
  font-weight: 600;
}

.question-index {
  margin-left: auto;
  font-size: 13px;
  color: var(--text-secondary, #909399);
}

.question-stem {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary, #303133);
  margin-bottom: 20px;
}

/* 选项 */
.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border: 1px solid var(--border-color, #e4e7ed);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: var(--primary-color, #409eff);
}

.option-item.active {
  border-color: var(--primary-color, #409eff);
  background: rgba(64, 158, 255, 0.06);
}

.option-item input {
  margin-top: 4px;
  accent-color: var(--primary-color, #409eff);
  cursor: pointer;
}

.option-key {
  font-weight: 700;
  color: var(--primary-color, #409eff);
}

.option-text {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}

.option-text :deep(p) { margin: 0; }

.blank-area {
  max-width: 800px;
}

.code-input :deep(textarea) {
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: 13px;
}

.lang-bar {
  margin-bottom: 10px;
}

.code-editor {
  height: 360px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--border-color, #e4e7ed);
}

/* 底部栏 */
.exam-bottombar {
  height: 64px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: var(--header-bg, #fff);
  border-top: 1px solid var(--border-color, #e4e7ed);
}
</style>
