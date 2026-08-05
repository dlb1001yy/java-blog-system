<template>
  <div class="article-detail" v-if="article">
    <div class="container">
      <div class="layout">
        <div class="main-content">
          <!-- 文章内容 -->
          <div class="card">
            <div class="article-header">
              <el-tag :type="typeTag.type" size="small" effect="dark">
                {{ typeTag.label }}
              </el-tag>
              <h1 class="article-title">{{ article.title }}</h1>
              <div class="article-meta">
                <span><el-icon><Calendar /></el-icon> {{ formatDate(article.createTime) }}</span>
                <span><el-icon><View /></el-icon> {{ article.viewCount }} 阅读</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ article.commentCount }} 评论</span>
              </div>
            </div>
            
            <div 
              class="markdown-body article-content"
              v-html="renderedContent"
            ></div>
            
            <!-- 转载说明 -->
            <div v-if="article.type === 1 && article.sourceUrl" class="source-info">
              <el-alert type="warning" :closable="false">
                <template #title>
                  本文转载自：<a :href="article.sourceUrl" target="_blank">{{ article.sourceName || article.sourceUrl }}</a>
                </template>
              </el-alert>
            </div>
            
            <!-- 标签 -->
            <div class="article-tags" v-if="article.tags && article.tags.length">
              <el-tag 
                v-for="tag in article.tags" 
                :key="tag.id"
                type="info"
                effect="plain"
                @click="$router.push(`/tags?tagId=${tag.id}`)"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            
            <!-- 点赞按钮 -->
            <div class="like-section">
              <el-button 
                type="primary" 
                size="large" 
                round
                :icon="Star"
                @click="handleLike"
              >
                点赞 ({{ article.likeCount }})
              </el-button>
            </div>
          </div>
          
          <!-- 上一篇下一篇 -->
          <div class="card nav-article" v-if="prevNext.prev || prevNext.next">
            <div class="nav-item" v-if="prevNext.prev" @click="goArticle(prevNext.prev.id)">
              <span class="nav-label">上一篇</span>
              <span class="nav-title">{{ prevNext.prev.title }}</span>
            </div>
            <div class="nav-item right" v-if="prevNext.next" @click="goArticle(prevNext.next.id)">
              <span class="nav-label">下一篇</span>
              <span class="nav-title">{{ prevNext.next.title }}</span>
            </div>
          </div>
          
          <!-- 评论区 -->
          <CommentSection :articleId="article.id" />
        </div>
        
        <!-- 目录 -->
        <div class="sidebar">
          <div class="card toc-card" v-if="toc.length">
            <h4>目录</h4>
            <div class="toc-list">
              <a 
                v-for="item in toc" 
                :key="item.id"
                :href="`#${item.id}`"
                :class="['toc-item', `level-${item.level}`]"
              >
                {{ item.text }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, View, ChatDotRound, Star } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import articleApi from '@/api/article'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const router = useRouter()

const article = ref(null)
const toc = ref([])
const prevNext = ref({ prev: null, next: null })

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang }).value}</code></pre>`
      } catch (_) {}
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

const typeMap = {
  0: { label: '原创', type: 'primary' },
  1: { label: '转载', type: 'warning' },
  2: { label: '翻译', type: 'success' }
}

const typeTag = computed(() => article.value ? typeMap[article.value.type] : typeMap[0])

const renderedContent = computed(() => {
  if (!article.value) return ''
  return md.render(article.value.content)
})

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchArticle = async () => {
  const id = route.params.id
  const res = await articleApi.getArticleDetail(id)
  article.value = res.data
  
  // 提取目录
  await nextTick()
  extractToc()
}

const extractToc = () => {
  const content = document.querySelector('.article-content')
  if (!content) return
  const headers = content.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const items = []
  headers.forEach((header, index) => {
    const id = `heading-${index}`
    header.id = id
    items.push({
      id,
      text: header.textContent,
      level: parseInt(header.tagName.substring(1))
    })
  })
  toc.value = items
}

const handleLike = async () => {
  await articleApi.likeArticle(article.value.id)
  article.value.likeCount++
  ElMessage.success('点赞成功')
}

const goArticle = (id) => {
  router.push(`/article/${id}`)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchArticle()
    window.scrollTo({ top: 0 })
  }
})

onMounted(() => {
  fetchArticle()
})
</script>

<style scoped>
.article-header {
  margin-bottom: 24px;
}

.article-header .el-tag {
  margin-bottom: 12px;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: var(--text-secondary);
  font-size: 14px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-regular);
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin: 24px 0 16px;
  font-weight: 600;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(pre) {
  border-radius: var(--radius-sm);
  margin: 16px 0;
  overflow-x: auto;
}

.article-content :deep(code) {
  font-family: 'Fira Code', Consolas, Monaco, monospace;
  font-size: 14px;
}

.article-content :deep(blockquote) {
  border-left: 4px solid var(--primary-color);
  padding: 12px 20px;
  margin: 16px 0;
  background: #f5f7fa;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.article-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.article-content :deep(th),
.article-content :deep(td) {
  border: 1px solid var(--border-color);
  padding: 8px 12px;
  text-align: left;
}

.article-content :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.source-info {
  margin-top: 24px;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.article-tags .el-tag {
  cursor: pointer;
}

.like-section {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.nav-article {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.nav-item {
  flex: 1;
  cursor: pointer;
  transition: color 0.3s;
}

.nav-item:hover {
  color: var(--primary-color);
}

.nav-item.right {
  text-align: right;
}

.nav-label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.nav-title {
  font-size: 14px;
  font-weight: 500;
}

.toc-card {
  position: sticky;
  top: 84px;
}

.toc-card h4 {
  margin-bottom: 12px;
  font-size: 16px;
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toc-item {
  display: block;
  color: var(--text-regular);
  font-size: 13px;
  padding: 4px 0;
  border-left: 2px solid transparent;
  padding-left: 8px;
  transition: all 0.3s;
}

.toc-item:hover {
  color: var(--primary-color);
  border-left-color: var(--primary-color);
}

.toc-item.level-2 { padding-left: 20px; }
.toc-item.level-3 { padding-left: 32px; }
.toc-item.level-4 { padding-left: 44px; }

@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .toc-card {
    position: static;
    display: none;
  }
  
  .article-title {
    font-size: 22px;
  }
}
</style>