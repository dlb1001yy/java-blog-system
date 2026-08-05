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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star, Clock, Folder, PriceTag } from '@element-plus/icons-vue'
import articleApi from '@/api/article'

const router = useRouter()
const hotArticles = ref([])
const latestArticles = ref([])
const categories = ref([])
const tags = ref([])

const goDetail = (id) => router.push(`/article/${id}`)
const goCategory = (id) => router.push(`/category/${id}`)
const goTag = (id) => router.push(`/tags?tagId=${id}`)

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
</style>