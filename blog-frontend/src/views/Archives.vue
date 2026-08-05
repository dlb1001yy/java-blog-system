<template>
  <div class="archives-page">
    <div class="container">
      <div class="card">
        <h2 class="page-title">文章归档</h2>
        <div class="timeline" v-for="group in archives" :key="group.month">
          <div class="timeline-header">
            <h3>{{ group.month }}</h3>
            <span class="count">{{ group.count }} 篇</span>
          </div>
          <ul class="archive-list">
            <li v-for="article in group.articles" :key="article.id" @click="$router.push(`/article/${article.id}`)">
              <span class="date">{{ formatDate(article.date) }}</span>
              <span class="title">{{ article.title }}</span>
            </li>
          </ul>
        </div>
        <el-empty v-if="archives.length === 0" description="暂无归档" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import articleApi from '@/api/article'

const archives = ref([])
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
onMounted(async () => {
  const res = await articleApi.getArchives()
  archives.value = res.data || []
})
</script>

<style scoped>
.page-title { margin-bottom: 32px; font-size: 20px; font-weight: 600; }
.timeline { margin-bottom: 32px; border-left: 2px solid var(--border-color); padding-left: 24px; }
.timeline-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.timeline-header h3 { font-size: 18px; font-weight: 600; }
.count { font-size: 12px; color: var(--text-secondary); background: #f5f7fa; padding: 2px 8px; border-radius: 4px; }
.archive-list { list-style: none; padding: 0; margin: 0; }
.archive-list li { display: flex; align-items: center; gap: 16px; padding: 8px 0; cursor: pointer; transition: color 0.3s; }
.archive-list li:hover { color: var(--primary-color); }
.date { font-size: 13px; color: var(--text-secondary); width: 80px; flex-shrink: 0; }
.title { font-size: 14px; color: var(--text-regular); }
</style>