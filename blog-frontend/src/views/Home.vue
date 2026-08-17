<template>
  <div class="home">
    <div class="container">
      <!-- Banner -->
      <div class="banner">
        <div class="banner-content">
          <h1>Java码农笔记</h1>
          <p>记录Java学习之路，分享技术心得</p>
          <div class="banner-stats">
            <div class="stat-item">
              <span class="stat-number">{{ stats.articleCount || 0 }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">{{ stats.viewCount || 0 }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">{{ stats.categoryCount || 0 }}</span>
              <span class="stat-label">分类</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="layout">
        <!-- 主内容 -->
        <div class="main-content">
          <!-- Tabs -->
          <div class="card tab-card">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
              <el-tab-pane label="全部" name="all" />
              <el-tab-pane label="原创" name="original" />
              <el-tab-pane label="转载" name="reproduced" />
              <el-tab-pane label="翻译" name="translated" />
            </el-tabs>
          </div>

          <template v-if="loading">
            <SkeletonCard v-for="n in 3" :key="n" />
          </template>
          <template v-else>
            <!-- 文章列表 -->
            <ArticleCard
              v-for="article in articleList"
              :key="article.id"
              :article="article"
            />
          </template>

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>

          <!-- 空状态 -->
          <el-empty v-if="!loading && articleList.length === 0" description="暂无文章" />
        </div>

        <!-- 侧边栏 -->
        <AppSidebar />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import articleApi from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import AppSidebar from '@/components/AppSidebar.vue'

const activeTab = ref('all')
const articleList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = reactive({
  articleCount: 0,
  viewCount: 0,
  categoryCount: 0
})

const typeMap = {
  all: null,
  original: 0,
  reproduced: 1,
  translated: 2
}

const fetchArticles = async () => {
  const params = {
    current: currentPage.value,
    size: pageSize.value
  }
  const type = typeMap[activeTab.value]
  if (type !== null) {
    params.type = type
  }
  
  loading.value = true
  try {
    const res = await articleApi.getArticles(params)
    articleList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  fetchArticles()
}

const handlePageChange = () => {
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const fetchStats = async () => {
  const res = await articleApi.getStats()
  Object.assign(stats, res.data)
}

onMounted(() => {
  fetchArticles()
  fetchStats()
})
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: var(--radius-lg);
  padding: 60px 40px;
  margin-bottom: 24px;
  text-align: center;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.home .container {
  padding-top: 24px;
}

.banner::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner h1 {
  font-size: 36px;
  margin-bottom: 12px;
  font-weight: 700;
}

.banner p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 32px;
}

.banner-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: rgba(255,255,255,0.3);
}

.tab-card {
  padding: 12px 20px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .banner {
    padding: 40px 20px;
  }
  
  .banner h1 {
    font-size: 24px;
  }
}
</style>