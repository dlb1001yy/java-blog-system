<template>
  <div class="sidebar">
    <!-- 热门文章 -->
    <div class="card">
      <h4 class="sidebar-title">
        <el-icon><Star /></el-icon> 热门文章
      </h4>
      <ul class="article-list">
        <li v-for="article in hotArticles" :key="article.id" @click="goDetail(article.id)">
          {{ article.title }}
        </li>
      </ul>
    </div>

    <!-- 最新文章 -->
    <div class="card">
      <h4 class="sidebar-title">
        <el-icon><Clock /></el-icon> 最新发布
      </h4>
      <ul class="article-list">
        <li v-for="article in latestArticles" :key="article.id" @click="goDetail(article.id)">
          {{ article.title }}
        </li>
      </ul>
    </div>

    <!-- 分类 -->
    <div class="card">
      <h4 class="sidebar-title">
        <el-icon><Folder /></el-icon> 分类
      </h4>
      <div class="tag-cloud">
        <el-tag 
          v-for="cat in categories" 
          :key="cat.id"
          class="tag-item"
          @click="goCategory(cat.id)"
        >
          {{ cat.name }}
        </el-tag>
      </div>
    </div>

    <!-- 标签 -->
    <div class="card">
      <h4 class="sidebar-title">
        <el-icon><PriceTag /></el-icon> 标签
      </h4>
      <div class="tag-cloud">
        <el-tag 
          v-for="tag in tags" 
          :key="tag.id"
          type="info"
          class="tag-item"
          @click="goTag(tag.id)"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </div>

    <!-- 归档 -->
    <div class="card">
      <h4 class="sidebar-title">
        <el-icon><Calendar /></el-icon> 归档
      </h4>
      <div class="archive-years">
        <!-- 按年份折叠，默认展开最近一年 -->
        <div v-for="(year, index) in archiveYears" :key="year.year" class="archive-year">
          <div class="archive-year-header" @click="year.open = !year.open">
            <el-icon class="arrow" :class="{ expanded: year.open }"><ArrowRight /></el-icon>
            <span class="year-text">{{ year.year }}年 ({{ year.count }}篇)</span>
          </div>
          <ul v-show="year.open" class="archive-months">
            <li v-for="m in year.months" :key="m.month" @click="goArchive(m.year, m.month)">
              {{ m.year }}年{{ String(m.month).padStart(2, '0') }}月 ({{ m.count }}篇)
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star, Clock, Folder, PriceTag, Calendar, ArrowRight } from '@element-plus/icons-vue'
import articleApi from '@/api/article'

const router = useRouter()
const hotArticles = ref([])
const latestArticles = ref([])
const categories = ref([])
const tags = ref([])
// 归档数据：按年份分组，每年含各月篇数
const archiveYears = ref([])

const goDetail = (id) => router.push(`/article/${id}`)
const goCategory = (id) => router.push(`/category/${id}`)
const goTag = (id) => router.push(`/tags?tagId=${id}`)
// 跳转归档页
const goArchive = (year, month) => router.push({ path: '/archives', query: { year, month } })

onMounted(async () => {
  try {
    const [hotRes, latestRes, catRes, tagRes] = await Promise.all([
      articleApi.getHotArticles(),
      articleApi.getLatestArticles(),
      articleApi.getCategories(),
      articleApi.getTags()
    ])
    hotArticles.value = hotRes.data || []
    latestArticles.value = latestRes.data || []
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
  } catch (error) {
    console.error('加载侧边栏数据失败', error)
  }
  // 单独加载归档数据，失败不影响其他模块
  try {
    const res = await articleApi.getArchives()
    const groups = res.data || []
    // 归档接口返回按月分组（month: 'YYYY-MM'），前端再按年份分组
    const yearMap = new Map()
    groups.forEach(g => {
      if (!g.month) return
      const [y, m] = g.month.split('-').map(Number)
      if (!y || !m) return
      if (!yearMap.has(y)) yearMap.set(y, { year: y, count: 0, months: [], open: false })
      const yearItem = yearMap.get(y)
      yearItem.months.push({ year: y, month: m, count: g.count || (g.articles ? g.articles.length : 0) })
      yearItem.count += g.count || (g.articles ? g.articles.length : 0)
    })
    // 年份倒序、每年月份倒序，默认展开最近一年
    archiveYears.value = [...yearMap.values()].sort((a, b) => b.year - a.year)
    archiveYears.value.forEach((y, i) => {
      y.months.sort((a, b) => b.month - a.month)
      y.open = i === 0
    })
  } catch (error) {
    console.error('加载归档数据失败', error)
  }
})
</script>

<style scoped>
.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}
.article-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.article-list li {
  padding: 8px 0;
  font-size: 14px;
  color: var(--text-regular);
  cursor: pointer;
  border-bottom: 1px dashed var(--border-color);
  transition: color 0.3s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-list li:last-child {
  border-bottom: none;
}
.article-list li:hover {
  color: var(--primary-color);
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag-item {
  cursor: pointer;
}
/* 归档模块样式 */
.archive-year-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-regular);
  cursor: pointer;
}
.archive-year-header .arrow {
  transition: transform 0.3s;
  font-size: 12px;
}
.archive-year-header .arrow.expanded {
  transform: rotate(90deg);
}
.archive-year-header:hover {
  color: var(--primary-color);
}
.archive-months {
  list-style: none;
  padding: 0;
  margin: 0 0 4px 18px;
}
.archive-months li {
  padding: 6px 0;
  font-size: 13px;
  color: var(--text-regular);
  cursor: pointer;
  transition: color 0.3s;
}
.archive-months li:hover {
  color: var(--primary-color);
}
</style>