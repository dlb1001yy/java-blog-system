<template>
  <div class="scores-page">
    <div class="container">
      <h2 class="page-title">成绩查询</h2>

      <!-- ========== 历史成绩列表 ========== -->
      <template v-if="!detail">
        <div class="card" v-loading="listLoading">
          <el-table
            :data="records"
            style="width: 100%"
            @row-click="openDetail"
            row-class-name="record-row"
          >
            <el-table-column prop="paperTitle" label="试卷" min-width="200" show-overflow-tooltip />
            <el-table-column label="得分" width="140">
              <template #default="{ row }">
                <span class="score-text">{{ row.finalScore ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 1 ? 'success' : 'warning'">
                  {{ row.status === 1 ? '已发布' : '待批改' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="用时" width="110">
              <template #default="{ row }">{{ formatDuration(row.durationSeconds) }}</template>
            </el-table-column>
            <el-table-column label="切屏" width="80">
              <template #default="{ row }">{{ row.switchCount ?? 0 }} 次</template>
            </el-table-column>
            <el-table-column label="提交时间" width="180">
              <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column width="90" align="center">
              <template #default>
                <el-link type="primary" :underline="false">详情</el-link>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无考试记录" />
            </template>
          </el-table>

          <div class="pagination" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="fetchRecords"
            />
          </div>
        </div>
      </template>

      <!-- ========== 成绩详情 ========== -->
      <template v-else-if="judging">
        <div class="detail-head">
          <el-button :icon="ArrowLeft" @click="backToList">返回列表</el-button>
          <span class="detail-paper">成绩详情</span>
        </div>
        <div class="card judging-card">
          <el-icon class="is-loading" :size="28"><Loading /></el-icon>
          <span class="judging-text">判分中，请稍候...</span>
        </div>
      </template>

      <template v-else>
        <div class="detail-head">
          <el-button :icon="ArrowLeft" @click="backToList">返回列表</el-button>
          <span class="detail-paper">{{ detail.paperTitle }}</span>
          <el-tag :type="detail.status === 1 ? 'success' : 'warning'">
            {{ detail.status === 1 ? '已发布' : '待批改' }}
          </el-tag>
        </div>

        <!-- Hero 卡 -->
        <div class="card hero-card" v-loading="detailLoading">
          <div class="hero-main">
            <div class="hero-score">
              <span class="hero-num">{{ detail.finalScore ?? '-' }}</span>
              <span class="hero-total">/ {{ totalScore }} 分</span>
            </div>
            <el-tag
              v-if="detail.passScore !== null && detail.passScore !== undefined && detail.finalScore !== null && detail.finalScore !== undefined"
              size="large"
              :type="detail.finalScore >= detail.passScore ? 'success' : 'danger'"
            >
              {{ detail.finalScore >= detail.passScore ? '及格' : '不及格' }}
            </el-tag>
            <el-tag v-if="detail.cheatFlag === 1" size="large" type="danger" effect="dark">作弊嫌疑</el-tag>
            <div class="hero-sub">
              <div class="hero-item">
                <span class="hero-label">用时</span>
                <span>{{ formatDuration(detail.durationSeconds) }}</span>
              </div>
              <div class="hero-item">
                <span class="hero-label">切屏次数</span>
                <span>{{ detail.switchCount ?? 0 }}</span>
              </div>
              <div class="hero-item">
                <span class="hero-label">客观题得分</span>
                <span>{{ detail.objectiveScore ?? 0 }}</span>
              </div>
              <div class="hero-item">
                <span class="hero-label">主观题得分</span>
                <span>{{ detail.subjectiveScore ?? 0 }}</span>
              </div>
              <div class="hero-item">
                <span class="hero-label">提交时间</span>
                <span>{{ formatTime(detail.submitTime) }}</span>
              </div>
            </div>
          </div>
          <el-alert
            v-if="detail.status !== 1"
            class="hero-alert"
            type="warning"
            :closable="false"
            title="主观题批改中，最终成绩以发布后为准"
          />
        </div>

        <!-- 图表区（纯 CSS 条形图） -->
        <div class="chart-grid" v-if="typeStats.length">
          <div class="card chart-card">
            <h3 class="chart-title">题型得分分布</h3>
            <div class="bar-row" v-for="t in typeStats" :key="'s' + t.type">
              <span class="bar-label">{{ t.label }}</span>
              <div class="bar-track">
                <div class="bar bar-full" :style="{ width: t.fullWidth + '%' }"></div>
                <div class="bar bar-got" :style="{ width: t.gotWidth + '%' }"></div>
              </div>
              <span class="bar-value">{{ t.got }} / {{ t.full }} 分</span>
            </div>
            <div class="chart-legend">
              <span class="legend-item"><i class="dot dot-full"></i>应得分</span>
              <span class="legend-item"><i class="dot dot-got"></i>实得分</span>
            </div>
          </div>

          <div class="card chart-card">
            <h3 class="chart-title">题型掌握度</h3>
            <div class="bar-row" v-for="t in typeStats" :key="'r' + t.type">
              <span class="bar-label">{{ t.label }}</span>
              <div class="bar-track">
                <div class="bar bar-rate" :class="rateClass(t.rate)" :style="{ width: t.rate + '%' }"></div>
              </div>
              <span class="bar-value">{{ t.rate }}%</span>
            </div>
          </div>
        </div>

        <!-- 答题回顾 -->
        <h3 class="section-title">答题回顾</h3>
        <div
          class="card item-card"
          v-for="(item, idx) in detail.items || []"
          :key="item.questionId"
        >
          <div class="item-head">
            <span class="item-index">第 {{ idx + 1 }} 题</span>
            <el-tag size="small" type="info">{{ typeLabel(item.type) }}</el-tag>
            <span class="item-score">{{ item.gotScore ?? 0 }} / {{ item.score }} 分</span>
            <el-tag v-if="item.type <= 4" size="small" :type="item.correct ? 'success' : 'danger'">
              {{ item.correct ? '正确' : '错误' }}
            </el-tag>
            <el-tag v-else size="small" type="warning">主观题</el-tag>
          </div>

          <div class="item-stem markdown-body" v-html="renderMd(item.stem)"></div>

          <div class="item-answers">
            <div class="answer-line">
              <span class="answer-label">我的答案</span>
              <span class="answer-value">{{ formatAnswer(item.myAnswer) || '未作答' }}</span>
            </div>
            <div class="answer-line" v-if="item.type <= 4 || detail.status === 1">
              <span class="answer-label">正确答案</span>
              <div class="answer-value markdown-body correct" v-html="renderMd(formatAnswer(item.type <= 4 ? item.correctAnswer : item.referenceAnswer))"></div>
            </div>
            <div class="answer-line" v-if="item.type <= 4 || detail.status === 1">
              <span class="answer-label">解析</span>
              <div class="answer-value markdown-body" v-html="renderMd(item.referenceAnswer)"></div>
            </div>
            <div class="answer-line" v-if="item.comment">
              <span class="answer-label">评语</span>
              <span class="answer-value">{{ item.comment }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import examApi from '@/api/exam'
import { useUserStore } from '@/stores/user'
import md from '@/utils/markdown'

const userStore = useUserStore()

const TYPE_MAP = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题', 6: '编程题' }
const typeLabel = (t) => TYPE_MAP[t] || '未知'

// 列表
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const listLoading = ref(false)

// 详情
const detail = ref(null)
const detailLoading = ref(false)
const judging = ref(false)

const fetchRecords = async () => {
  listLoading.value = true
  try {
    const res = await examApi.getMyRecords({ page: currentPage.value, size: pageSize })
    const data = res.data || {}
    total.value = data.total || 0
    records.value = data.records || []
  } finally {
    listLoading.value = false
  }
}

// 异步判分：items 为空或客观题未判出说明尚未就绪
const isDetailReady = (d) =>
  d && Array.isArray(d.items) && d.items.length > 0 && d.objectiveScore !== null && d.objectiveScore !== undefined

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

let pollCancelled = false

const openDetail = async (row) => {
  pollCancelled = false
  detailLoading.value = true
  judging.value = false
  detail.value = { items: [] }
  try {
    let res = await examApi.getRecordDetail(row.id)
    let data = res.data
    if (!isDetailReady(data)) {
      // 判分未就绪，轮询等待（最多 10 次，间隔 2 秒）
      judging.value = true
      detailLoading.value = false
      for (let i = 0; i < 10; i++) {
        await sleep(2000)
        if (pollCancelled) return
        res = await examApi.getRecordDetail(row.id)
        data = res.data
        if (isDetailReady(data)) break
      }
      judging.value = false
    }
    detail.value = data
  } catch (e) {
    judging.value = false
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

const backToList = () => {
  pollCancelled = true
  judging.value = false
  detail.value = null
  fetchRecords()
}

// 总分 = 各题分值合计
const totalScore = computed(() =>
  (detail.value?.items || []).reduce((s, it) => s + Number(it.score || 0), 0)
)

// 题型聚合（应得分 / 实得分 / 得分率）
const typeStats = computed(() => {
  const map = {}
  for (const it of detail.value?.items || []) {
    const label = typeLabel(it.type)
    if (!map[label]) map[label] = { label, full: 0, got: 0 }
    map[label].full += Number(it.score || 0)
    map[label].got += Number(it.gotScore || 0)
  }
  const max = Math.max(...Object.values(map).map(t => t.full), 1)
  return Object.values(map).map(t => ({
    ...t,
    fullWidth: (t.full / max) * 100,
    gotWidth: (t.got / max) * 100,
    rate: t.full ? Math.round((t.got / t.full) * 100) : 0
  }))
})

const rateClass = (rate) =>
  rate >= 80 ? 'rate-good' : rate >= 50 ? 'rate-mid' : 'rate-bad'

const formatDuration = (sec) => {
  if (!sec && sec !== 0) return '-'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return m > 0 ? `${m} 分 ${s} 秒` : `${s} 秒`
}

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

// 答案格式化：后端为 JSON 字符串（字符串或数组）
const formatAnswer = (ans) => {
  if (ans === null || ans === undefined || ans === '') return ''
  if (typeof ans !== 'string') return String(ans)
  try {
    const v = JSON.parse(ans)
    if (Array.isArray(v)) return v.join('、')
    if (typeof v === 'object') return JSON.stringify(v)
    return String(v)
  } catch (_) {
    return ans
  }
}

const renderMd = (text) => (text ? md.render(String(text)) : '<p style="color:var(--text-secondary)">无</p>')

onMounted(() => {
  if (!userStore.token) {
    records.value = []
    return
  }
  fetchRecords()
})

onBeforeUnmount(() => {
  pollCancelled = true
})
</script>

<style scoped>
.scores-page { padding-top: 24px; padding-bottom: 40px; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; }

:deep(.record-row) { cursor: pointer; }
.score-text { font-weight: 600; color: var(--primary-color); }
.pagination { display: flex; justify-content: center; margin-top: 20px; }

/* 详情头部 */
.detail-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.detail-paper { font-size: 17px; font-weight: 600; color: var(--text-primary); }

/* Hero 卡 */
.hero-card { margin-bottom: 20px; }
.hero-main { display: flex; flex-wrap: wrap; align-items: center; gap: 40px; }
.hero-score { display: flex; align-items: baseline; gap: 6px; }
.hero-num { font-size: 56px; font-weight: 700; color: var(--primary-color); line-height: 1; }
.hero-total { font-size: 16px; color: var(--text-secondary); }
.hero-sub { display: flex; flex-wrap: wrap; gap: 24px; }
.hero-item { display: flex; flex-direction: column; gap: 4px; font-size: 14px; color: var(--text-regular); }
.hero-label { font-size: 12px; color: var(--text-secondary); }
.hero-alert { margin-top: 16px; }

/* 判分中占位 */
.judging-card { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 60px 20px; }
.judging-text { font-size: 15px; color: var(--text-secondary); }

/* 图表区 */
.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
@media (max-width: 900px) { .chart-grid { grid-template-columns: 1fr; } }
.chart-title { margin-bottom: 16px; font-size: 15px; font-weight: 600; color: var(--text-primary); }
.bar-row { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.bar-label { width: 64px; flex-shrink: 0; font-size: 13px; color: var(--text-regular); text-align: right; }
.bar-track { flex: 1; position: relative; height: 26px; background: var(--bg-color, #f5f7fa); border-radius: 6px; overflow: hidden; }
.bar { position: absolute; top: 0; left: 0; height: 100%; border-radius: 6px; }
.bar-full { background: rgba(64, 158, 255, 0.18); }
.bar-got { background: linear-gradient(90deg, var(--primary-color), #79bbff); }
.bar-rate { background: var(--primary-color); }
.rate-good { background: #67c23a; }
.rate-mid { background: #e6a23c; }
.rate-bad { background: #f56c6c; }
.bar-value { width: 110px; flex-shrink: 0; font-size: 12px; color: var(--text-secondary); }
.chart-legend { display: flex; gap: 16px; margin-top: 8px; font-size: 12px; color: var(--text-secondary); }
.legend-item { display: flex; align-items: center; gap: 4px; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; }
.dot-full { background: rgba(64, 158, 255, 0.3); }
.dot-got { background: var(--primary-color); }

/* 答题回顾 */
.section-title { margin-bottom: 14px; font-size: 16px; font-weight: 600; color: var(--text-primary); }
.item-card { margin-bottom: 16px; }
.item-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.item-index { font-weight: 600; color: var(--text-primary); }
.item-score { font-size: 13px; color: var(--text-secondary); }
.item-stem { font-size: 14px; line-height: 1.7; color: var(--text-regular); margin-bottom: 12px; }
.item-answers { padding-top: 12px; border-top: 1px dashed var(--border-color); display: flex; flex-direction: column; gap: 8px; }
.answer-line { display: flex; gap: 12px; font-size: 14px; }
.answer-label { flex-shrink: 0; width: 64px; color: var(--text-secondary); text-align: right; }
.answer-value { color: var(--text-regular); flex: 1; white-space: pre-wrap; word-break: break-word; }
.answer-value.correct { color: var(--primary-color); }
</style>
