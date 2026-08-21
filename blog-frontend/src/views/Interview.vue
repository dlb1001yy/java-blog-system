<template>
  <div class="interview-page">
    <div class="container">
      <h2 class="page-title">面试刷题</h2>
      <div class="layout">
        <!-- 左侧筛选栏 -->
        <aside class="sidebar filter-sidebar">
          <div class="card">
            <h3 class="filter-title">技术方向</h3>
            <el-checkbox-group v-model="selectedCategories" @change="handleFilterChange">
              <el-checkbox v-for="c in categories" :key="c" :label="c" :value="c">{{ c }}</el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="card">
            <h3 class="filter-title">难度</h3>
            <el-radio-group v-model="difficulty" @change="handleFilterChange">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="简单">简单</el-radio-button>
              <el-radio-button value="中等">中等</el-radio-button>
              <el-radio-button value="困难">困难</el-radio-button>
            </el-radio-group>
          </div>
          <div class="card">
            <h3 class="filter-title">刷题状态</h3>
            <el-radio-group v-model="status" @change="handleFilterChange">
              <el-radio value="all">全部</el-radio>
              <el-radio value="favorite">我的收藏</el-radio>
              <el-radio value="wrong">错题本</el-radio>
            </el-radio-group>
          </div>
          <div class="card">
            <h3 class="filter-title">搜索</h3>
            <el-input
              v-model="keyword"
              placeholder="搜索题目关键词..."
              clearable
              @keyup.enter="handleFilterChange"
              @clear="handleFilterChange"
            >
              <template #append>
                <el-button @click="handleFilterChange">搜索</el-button>
              </template>
            </el-input>
          </div>
        </aside>

        <!-- 主区：题目列表 -->
        <div class="main-content">
          <div class="card question-card" v-for="q in questionList" :key="q.id">
            <div class="question-header" @click="toggleExpand(q)">
              <div class="question-meta">
                <el-tag size="small" type="info">{{ q.category }}</el-tag>
                <el-tag size="small" :type="difficultyType(q.difficulty)">{{ q.difficulty }}</el-tag>
              </div>
              <h3 class="question-title">{{ q.title }}</h3>
              <div class="question-footer">
                <div class="question-tags">
                  <span class="tag-chip" v-for="tag in parseTags(q.tags)" :key="tag">{{ tag }}</span>
                </div>
                <div class="question-actions" @click.stop>
                  <el-button
                    :icon="Star"
                    circle
                    size="small"
                    :type="favMap[q.id] ? 'warning' : ''"
                    title="收藏"
                    @click="handleToggle(q.id, 'favorite')"
                  />
                  <el-button
                    :icon="CircleClose"
                    circle
                    size="small"
                    :type="wrongMap[q.id] ? 'danger' : ''"
                    title="错题本"
                    @click="handleToggle(q.id, 'wrong')"
                  />
                  <el-icon class="expand-arrow" :class="{ expanded: q.expanded }">
                    <ArrowDown />
                  </el-icon>
                </div>
              </div>
            </div>

            <div v-if="q.expanded" class="answer-panel">
              <div v-if="q.answerLoading" class="answer-loading">
                <el-skeleton :rows="4" animated />
              </div>
              <div v-else-if="q.answerHtml" class="markdown-body answer-body" v-html="q.answerHtml"></div>
              <div v-else class="answer-empty">暂无参考答案</div>
              <template v-if="!q.answerLoading && q.tipsHtml">
                <div class="tips-panel">
                  <h4 class="tips-title">解题思路</h4>
                  <div class="markdown-body answer-body" v-html="q.tipsHtml"></div>
                </div>
              </template>
            </div>
          </div>

          <el-empty v-if="!loading && questionList.length === 0" description="暂无题目" />

          <div class="pagination" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="fetchQuestions"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, CircleClose, ArrowDown } from '@element-plus/icons-vue'
import interviewApi from '@/api/interview'
import { useUserStore } from '@/stores/user'
import md from '@/utils/markdown'

const userStore = useUserStore()

const categories = ['后端', '前端', '数据库', 'DevOps', '算法']
const selectedCategories = ref([])
const difficulty = ref('')
const status = ref('all')
const keyword = ref('')

const questionList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

const favMap = reactive({})
const wrongMap = reactive({})

const difficultyType = (d) =>
  ({ 简单: 'success', 中等: 'warning', 困难: 'danger' }[d] || 'info')

const parseTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  return tags.split(',').map(t => t.trim()).filter(Boolean)
}

const isLoggedIn = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录后再操作')
    return false
  }
  return true
}

const fetchQuestions = async () => {
  loading.value = true
  try {
    let res
    if (status.value === 'favorite') {
      res = await interviewApi.getFavorites({ page: currentPage.value, size: pageSize })
    } else if (status.value === 'wrong') {
      res = await interviewApi.getWrongList({ page: currentPage.value, size: pageSize })
    } else {
      res = await interviewApi.getQuestions({
        page: currentPage.value,
        size: pageSize,
        category: selectedCategories.value.join(',') || undefined,
        difficulty: difficulty.value || undefined,
        keyword: keyword.value.trim() || undefined
      })
    }
    const data = res.data || {}
    total.value = data.total || 0
    questionList.value = (data.records || []).map(r => {
      const q = r.question || r
      return { ...q, expanded: false, answerLoading: false, answerHtml: '', tipsHtml: '' }
    })
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchQuestions()
}

const toggleExpand = async (q) => {
  q.expanded = !q.expanded
  if (q.expanded && !q.answerHtml) {
    q.answerLoading = true
    try {
      const res = await interviewApi.getQuestionAnswer(q.id)
      const answer = res.data?.answer || ''
      q.answerHtml = answer ? md.render(answer) : ''
      const tips = res.data?.tips || ''
      q.tipsHtml = tips ? md.render(tips) : ''
    } finally {
      q.answerLoading = false
    }
  }
}

const handleToggle = async (id, type) => {
  if (!isLoggedIn()) return
  const api = type === 'favorite' ? interviewApi.toggleFavorite : interviewApi.toggleWrong
  const map = type === 'favorite' ? favMap : wrongMap
  const res = await api(id)
  map[id] = !!res.data
  ElMessage.success(res.data
    ? (type === 'favorite' ? '已加入收藏' : '已加入错题本')
    : (type === 'favorite' ? '已取消收藏' : '已移出错题本'))
  // 在收藏/错题列表视图中取消后刷新列表
  if (status.value !== 'all' && !res.data) fetchQuestions()
}

onMounted(fetchQuestions)
</script>

<style scoped>
.interview-page { padding-top: 24px; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; }
.filter-sidebar { position: sticky; top: 84px; align-self: flex-start; }
.filter-title { margin-bottom: 12px; font-size: 15px; font-weight: 600; color: var(--text-primary); }
.question-card { cursor: pointer; }
.question-header { display: flex; flex-direction: column; gap: 8px; }
.question-meta { display: flex; gap: 8px; }
.question-title { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.question-footer { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.question-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-chip {
  padding: 2px 10px;
  font-size: 12px;
  color: var(--primary-color);
  background: rgba(64, 158, 255, 0.08);
  border-radius: 999px;
}
.question-actions { display: flex; align-items: center; gap: 4px; }
.expand-arrow { margin-left: 8px; color: var(--text-secondary); transition: transform 0.3s; }
.expand-arrow.expanded { transform: rotate(180deg); }
.answer-panel { margin-top: 16px; padding-top: 16px; border-top: 1px dashed var(--border-color); cursor: default; }
.answer-body { font-size: 14px; line-height: 1.7; color: var(--text-regular); }
.answer-empty { color: var(--text-secondary); }
.tips-panel {
  margin-top: 16px;
  padding: 12px 16px;
  border-left: 3px solid var(--primary-color);
  background: rgba(64, 158, 255, 0.05);
  border-radius: var(--radius-sm, 6px);
}
.tips-title { margin-bottom: 8px; font-size: 14px; font-weight: 600; color: var(--primary-color); }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
