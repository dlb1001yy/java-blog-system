<template>
  <div class="article-list-page">
    <div class="container">
      <div class="layout">
        <div class="main-content">
          <div class="card page-header" v-if="pageTitle">
            <h2>{{ pageTitle }}</h2>
          </div>
          <ArticleCard v-for="article in articleList" :key="article.id" :article="article" />
          <div class="pagination" v-if="total > 0">
            <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="handlePageChange" />
          </div>
          <el-empty v-if="articleList.length === 0" description="暂无文章" />
        </div>
        <AppSidebar />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import articleApi from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import AppSidebar from '@/components/AppSidebar.vue'

const route = useRoute()
const articleList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pageTitle = ref('')

const fetchArticles = async () => {
  const params = { current: currentPage.value, size: pageSize.value }
  if (route.query.keyword) {
    params.keyword = route.query.keyword
    pageTitle.value = `搜索: ${route.query.keyword}`
  } else if (route.params.id) {
    params.categoryId = route.params.id
    pageTitle.value = '分类文章'
  } else if (route.query.tagId) {
    params.tagId = route.query.tagId
    pageTitle.value = '标签文章'
  } else {
    pageTitle.value = '全部文章'
  }
  const res = await articleApi.getArticles(params)
  articleList.value = res.data.records
  total.value = res.data.total
}

const handlePageChange = () => {
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => [route.query, route.params], () => { currentPage.value = 1; fetchArticles() }, { deep: true })
onMounted(() => fetchArticles())
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.pagination { display: flex; justify-content: center; margin-top: 24px; }
</style>