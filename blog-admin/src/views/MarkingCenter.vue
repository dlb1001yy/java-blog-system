<template>
  <PageContainer title="阅卷中心" description="主观题批阅">
    <div class="marking-layout">
      <!-- 左侧：待批列表 -->
      <div class="pending-panel">
        <div class="search-card">
          <el-input v-model="keyword" placeholder="搜索考生 / 试卷" clearable @keyup.enter="handleSearch" @clear="handleSearch">
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="pending-list" v-loading="listLoading">
          <div
            v-for="row in pendingList"
            :key="row.id"
            class="pending-card"
            :class="{ active: selectedId === row.id }"
            @click="handleSelect(row)"
          >
            <div class="pending-card__header">
              <span class="pending-card__user">{{ row.userName || '未知考生' }}</span>
              <el-tag size="small" type="warning">待批 {{ row.subjectiveCount ?? 0 }} 题</el-tag>
            </div>
            <div class="pending-card__title">{{ row.paperTitle }}</div>
            <div class="pending-card__meta">
              <span>客观题 {{ row.objectiveScore ?? 0 }} 分</span>
              <span>
                <el-tag v-if="row.cheatFlag === 1" type="danger" size="small" effect="dark" class="cheat-tag">作弊嫌疑</el-tag>
                {{ row.submitTime }}
              </span>
            </div>
          </div>
          <el-empty v-if="!listLoading && !pendingList.length" description="暂无待批答卷" :image-size="80" />
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            small
            @current-change="fetchPending"
          />
        </div>
      </div>

      <!-- 右侧：批改面板 -->
      <div class="marking-panel">
        <template v-if="detail">
          <div class="info-bar">
            <span>考生：<b>{{ selectedRecord?.userName }}</b></span>
            <span>试卷：<b>{{ detail.paperTitle }}</b></span>
            <span>客观题得分：<b class="score-num">{{ detail.objectiveScore ?? 0 }}</b></span>
            <span v-if="detail.switchCount != null">切屏次数：<b>{{ detail.switchCount }}</b></span>
            <el-tag v-if="detail.cheatFlag === 1" type="danger" size="small" effect="dark">作弊嫌疑 · 主观题已默认零分，可直接提交</el-tag>
          </div>

          <div class="marking-body" v-loading="detailLoading">
            <div v-for="(item, index) in subjectiveItems" :key="item.questionId" class="question-card">
              <div class="question-card__head">
                <el-tag :type="item.type === 6 ? 'danger' : 'primary'" size="small">
                  {{ item.type === 6 ? '编程题' : '简答题' }}
                </el-tag>
                <span v-if="item.category" class="question-category">{{ item.category }}</span>
                <span class="question-full">共 {{ item.score }} 分</span>
              </div>
              <pre class="question-stem">{{ item.stem }}</pre>

              <div class="answer-block">
                <div class="answer-block__label">考生答案</div>
                <pre class="answer-block__content">{{ formatAnswer(item.myAnswer) }}</pre>
              </div>

              <div class="answer-block answer-block--ref">
                <div class="answer-block__label">参考答案</div>
                <pre class="answer-block__content">{{ item.referenceAnswer || '（无）' }}</pre>
              </div>

              <div class="score-row">
                <span class="score-row__label">评分</span>
                <el-input-number
                  v-model="markForms[item.questionId].score"
                  :min="0"
                  :max="Number(item.score)"
                  :step="0.5"
                  :precision="1"
                />
                <span class="score-row__label">评语</span>
                <el-input
                  v-model="markForms[item.questionId].comment"
                  type="textarea"
                  :rows="2"
                  placeholder="填写评语（可选）"
                  resize="vertical"
                  class="score-row__comment"
                />
              </div>
            </div>
            <el-empty v-if="!detailLoading && !subjectiveItems.length" description="该试卷无主观题" />
          </div>

          <div class="action-bar">
            <el-button @click="handleSave(false)" :loading="saving">保存草稿</el-button>
            <el-button type="primary" @click="handleSave(true)" :loading="saving">提交评分</el-button>
          </div>
        </template>

        <el-empty v-else description="请从左侧选择待批答卷" class="empty-panel" />
      </div>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import markingApi from '@/api/marking'
import PageContainer from '@/components/PageContainer.vue'

// ---- 左侧待批列表 ----
const keyword = ref('')
const listLoading = ref(false)
const pendingList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// ---- 右侧批改面板 ----
const selectedId = ref(null)
const selectedRecord = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const saving = ref(false)
const markForms = reactive({})

const subjectiveItems = computed(() =>
  (detail.value?.items || []).filter(item => item.type === 5 || item.type === 6)
)

const fetchPending = async () => {
  listLoading.value = true
  try {
    const res = await markingApi.pending({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    pendingList.value = res.data.records
    total.value = res.data.total
  } finally {
    listLoading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; fetchPending() }

const handleSelect = async (row) => {
  if (selectedId.value === row.id) return
  selectedId.value = row.id
  selectedRecord.value = row
  detail.value = null
  detailLoading.value = true
  try {
    const res = await markingApi.detail(row.id)
    detail.value = res.data
    // 重建评分表单，已批过的（gotScore 非空）回填；作弊答卷未批的主观题默认记零分
    const isCheat = detail.value.cheatFlag === 1
    Object.keys(markForms).forEach(k => delete markForms[k])
    subjectiveItems.value.forEach(item => {
      if (item.gotScore != null) {
        markForms[item.questionId] = {
          score: Number(item.gotScore),
          comment: item.comment || ''
        }
      } else if (isCheat) {
        markForms[item.questionId] = {
          score: 0,
          comment: '作弊嫌疑，主观题记零分'
        }
      } else {
        markForms[item.questionId] = { score: null, comment: item.comment || '' }
      }
    })
  } finally {
    detailLoading.value = false
  }
}

// myAnswer 为序列化字符串，尝试还原为可读文本
const formatAnswer = (val) => {
  if (val == null || val === '') return '（未作答）'
  const str = String(val)
  if (/^[[{]/.test(str)) {
    try {
      const parsed = JSON.parse(str)
      return Array.isArray(parsed) ? parsed.map(v => (typeof v === 'object' ? JSON.stringify(v) : String(v))).join('\n') : JSON.stringify(parsed, null, 2)
    } catch {
      // 非 JSON，原样返回
    }
  }
  return str.replace(/^"|"$/g, '')
}

const handleSave = (submit) => {
  if (submit) {
    const unscored = subjectiveItems.value.filter(item => markForms[item.questionId]?.score == null)
    if (unscored.length) {
      ElMessage.warning(`还有 ${unscored.length} 道主观题未评分，请完成全部评分后提交`)
      return
    }
    ElMessageBox.confirm('提交后将汇总发布成绩且不可再修改，确定提交评分吗？', '确认提交', { type: 'warning' })
      .then(() => doSave(true)).catch(() => {})
  } else {
    doSave(false)
  }
}

const doSave = async (submit) => {
  saving.value = true
  try {
    const items = subjectiveItems.value.map(item => ({
      questionId: item.questionId,
      score: markForms[item.questionId]?.score,
      comment: markForms[item.questionId]?.comment || ''
    }))
    await markingApi.save(selectedId.value, items, submit)
    if (submit) {
      ElMessage.success('批改完成，成绩已发布')
      clearPanel()
      fetchPending()
    } else {
      ElMessage.success('草稿已保存')
    }
  } finally {
    saving.value = false
  }
}

const clearPanel = () => {
  selectedId.value = null
  selectedRecord.value = null
  detail.value = null
  Object.keys(markForms).forEach(k => delete markForms[k])
}

onMounted(() => fetchPending())
</script>

<style scoped>
.marking-layout {
  display: flex;
  gap: var(--space-5);
  align-items: stretch;
}

/* ---- 左侧待批列表 ---- */
.pending-panel {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.search-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.pending-list {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  min-height: 200px;
  max-height: calc(100vh - 320px);
  overflow-y: auto;
}

.pending-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  transition: background var(--transition-base), border-color var(--transition-base);
}

.pending-card:hover { background: var(--bg-subtle); }
.pending-card.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.pending-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}
.pending-card__user { font-weight: 600; }
.pending-card__title {
  color: var(--text-regular);
  font-size: 13px;
  margin-bottom: var(--space-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pending-card__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary, #909399);
  font-size: 12px;
}

.cheat-tag { margin-right: 4px; }

.pagination-wrap { display: flex; justify-content: center; }

/* ---- 右侧批改面板 ---- */
.marking-panel {
  flex: 1;
  min-width: 0;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 220px);
}

.info-bar {
  display: flex;
  gap: var(--space-6);
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-color);
  color: var(--text-regular);
  flex-wrap: wrap;
}
.score-num { color: var(--el-color-primary); font-size: 16px; }

.marking-body {
  flex: 1;
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  overflow-y: auto;
}

.question-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-4);
}

.question-card__head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.question-category { color: var(--text-secondary, #909399); font-size: 12px; }
.question-full { margin-left: auto; color: var(--text-regular); font-size: 13px; }

.question-stem {
  margin: 0 0 var(--space-3);
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  line-height: 1.6;
}

.answer-block { margin-bottom: var(--space-3); }
.answer-block__label {
  font-size: 12px;
  color: var(--text-secondary, #909399);
  margin-bottom: var(--space-1);
}
.answer-block__content {
  margin: 0;
  background: var(--bg-subtle);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  font-family: 'JetBrains Mono', Consolas, Menlo, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 320px;
  overflow-y: auto;
}
.answer-block--ref .answer-block__content {
  background: var(--el-color-success-light-9, #f0f9eb);
  border-color: var(--el-color-success-light-7, #e1f3d8);
}

.score-row {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  margin-top: var(--space-3);
}
.score-row__label { line-height: 32px; color: var(--text-regular); flex-shrink: 0; }
.score-row__comment { flex: 1; }

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-color);
}

.empty-panel { margin: auto; }
</style>
