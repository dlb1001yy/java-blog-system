<template>
  <div class="home">
    <!-- Hero 双栏 -->
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-left">
          <span class="hero-badge">个人综合学习平台</span>
          <h1 class="hero-title">知识驱动成长<br />技术连接未来</h1>
          <p class="hero-subtitle">
            集博客、简历、考试、面试刷题、音乐于一体的一站式个人成长平台。记录学习轨迹，检验知识掌握，助力职业发展。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" round @click="$router.push('/exam')">
              开始学习
            </el-button>
            <el-button size="large" round plain @click="$router.push('/articles')">
              浏览博客
            </el-button>
          </div>
        </div>
        <div class="hero-right" aria-hidden="true">
          <div class="hero-shape shape-1"></div>
          <div class="hero-shape shape-2"></div>
          <div class="hero-shape shape-3"></div>
          <div class="hero-ring"></div>
        </div>
      </div>
    </section>

    <div class="container">
      <!-- 核心功能入口 -->
      <section class="section">
        <h2 class="section-title">核心功能</h2>
        <p class="section-subtitle">五大模块，覆盖学习全场景</p>
        <div class="module-grid">
          <div
            v-for="m in modules"
            :key="m.path"
            class="card module-card"
            @click="$router.push(m.path)"
          >
            <div class="module-icon" :style="{ background: m.bg, color: m.color }">
              <el-icon :size="24"><component :is="m.icon" /></el-icon>
            </div>
            <div class="module-name">{{ m.name }}</div>
            <div class="module-desc">{{ m.desc }}</div>
          </div>
        </div>
      </section>

      <!-- 学习概览统计 -->
      <section class="section">
        <h2 class="section-title">学习概览</h2>
        <p class="section-subtitle">站点学习数据统计</p>
        <div class="stat-grid">
          <div class="card stat-card">
            <el-icon :size="22" class="stat-icon"><Notebook /></el-icon>
            <div class="stat-value">{{ stats.articleCount || 0 }}</div>
            <div class="stat-label">文章总数</div>
          </div>
          <div class="card stat-card">
            <el-icon :size="22" class="stat-icon"><FolderOpened /></el-icon>
            <div class="stat-value">{{ stats.categoryCount || 0 }}</div>
            <div class="stat-label">分类数</div>
          </div>
          <div class="card stat-card">
            <el-icon :size="22" class="stat-icon"><CollectionTag /></el-icon>
            <div class="stat-value">{{ stats.tagCount || 0 }}</div>
            <div class="stat-label">标签数</div>
          </div>
          <div class="card stat-card">
            <el-icon :size="22" class="stat-icon"><ChatLineSquare /></el-icon>
            <div class="stat-value">{{ questionCount }}</div>
            <div class="stat-label">面试题量</div>
          </div>
        </div>
      </section>

      <!-- 内容区域 -->
      <section class="section">
        <div class="section-header">
          <div>
            <h2 class="section-title">最新文章</h2>
            <p class="section-subtitle">持续记录，分享成长</p>
          </div>
          <el-button text type="primary" @click="$router.push('/articles')">
            查看全部 →
          </el-button>
        </div>
        <div class="latest-grid">
          <template v-if="loading">
            <SkeletonCard v-for="n in 3" :key="n" />
          </template>
          <template v-else>
            <LatestBlogCard
              v-for="article in latestArticles"
              :key="article.id"
              :article="article"
            />
          </template>
        </div>
        <el-empty v-if="!loading && latestArticles.length === 0" description="暂无文章" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import articleApi from '@/api/article'
import interviewApi from '@/api/interview'
import ArticleCard from '@/components/ArticleCard.vue'
import LatestBlogCard from '@/components/LatestBlogCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import {
  Notebook, FolderOpened, CollectionTag, ChatLineSquare,
  EditPen, Tickets, ChatDotRound, Headset, User
} from '@element-plus/icons-vue'

const modules = [
  { name: '个人博客', desc: '记录学习笔记与技术心得', path: '/articles', icon: EditPen, color: '#409eff', bg: 'rgba(64,158,255,0.1)' },
  { name: '在线考试', desc: '模拟真实考试环境', path: '/exam', icon: Tickets, color: '#67c23a', bg: 'rgba(103,194,58,0.1)' },
  { name: '面试刷题', desc: '精选面试题库，助力求职', path: '/interview', icon: ChatDotRound, color: '#e6a23c', bg: 'rgba(230,162,60,0.1)' },
  { name: '音乐放松', desc: '学习之余聆听音乐放松', path: '/music', icon: Headset, color: '#f56c6c', bg: 'rgba(245,108,108,0.1)' }
  // 简历功能暂时屏蔽
  // { name: '我的简历', desc: '展示项目经验与技能', path: '/resume', icon: User, color: '#9b59b6', bg: 'rgba(155,89,182,0.1)' }
]

const latestArticles = ref([])
const loading = ref(false)
const questionCount = ref(0)
const stats = reactive({
  articleCount: 0,
  viewCount: 0,
  categoryCount: 0,
  tagCount: 0
})

const fetchLatest = async () => {
  loading.value = true
  try {
    const res = await articleApi.getLatestArticles(3)
    latestArticles.value = (res.data || []).slice(0, 3)
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  const res = await articleApi.getStats()
  Object.assign(stats, res.data)
}

const fetchQuestionCount = async () => {
  try {
    const res = await interviewApi.getQuestions({ page: 1, size: 1 })
    questionCount.value = res.data?.total || 0
  } catch {
    questionCount.value = 0
  }
}

onMounted(() => {
  fetchLatest()
  fetchStats()
  fetchQuestionCount()
})
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  overflow: hidden;
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  padding-top: 64px;
  padding-bottom: 64px;
}

.hero-left {
  flex: 1;
  min-width: 0;
}

.hero-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 13px;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  line-height: 1.25;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 15px;
  opacity: 0.9;
  line-height: 1.8;
  max-width: 520px;
  margin-bottom: 28px;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

/* 右侧几何装饰 */
.hero-right {
  position: relative;
  flex: 0 0 380px;
  height: 260px;
}

.hero-shape {
  position: absolute;
  border-radius: 16px;
}

.shape-1 {
  width: 200px;
  height: 200px;
  right: 40px;
  top: 10px;
  background: rgba(255, 255, 255, 0.16);
  transform: rotate(15deg);
}

.shape-2 {
  width: 120px;
  height: 120px;
  right: 200px;
  top: 110px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
}

.shape-3 {
  width: 80px;
  height: 80px;
  right: 20px;
  top: 160px;
  background: rgba(255, 255, 255, 0.22);
  transform: rotate(-20deg);
}

.hero-ring {
  position: absolute;
  width: 240px;
  height: 240px;
  right: 20px;
  top: 0;
  border: 2px dashed rgba(255, 255, 255, 0.35);
  border-radius: 50%;
  animation: spin 30s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.section {
  margin-top: 48px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

/* 最新博客网格 */
.latest-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 8px;
}

@media (max-width: 992px) {
  .latest-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .latest-grid {
    grid-template-columns: 1fr;
  }
}

/* 功能卡片 */
.module-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.module-card {
  padding: 24px 20px;
  cursor: pointer;
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.12));
}

.module-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
}

.module-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.module-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.stat-card {
  padding: 24px;
  text-align: center;
}

.stat-icon {
  color: var(--primary-color);
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.tab-card {
  padding: 12px 20px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 992px) {
  .module-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-right {
    display: none;
  }

  .hero-title {
    font-size: 28px;
  }

  .module-grid {
    grid-template-columns: 1fr;
  }

  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }
}
</style>
