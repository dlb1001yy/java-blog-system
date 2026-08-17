<template>
  <div class="article-card card" @click="goDetail">
    <div class="article-layout">
      <div class="article-cover" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />
      </div>
      <div class="article-info">
        <div class="article-header">
          <el-tag 
            :type="typeTag.type" 
            size="small" 
            effect="dark"
          >
            {{ typeTag.label }}
          </el-tag>
          <span class="article-date">{{ formatDate(article.createTime) }}</span>
        </div>
        
        <h3 class="article-title">{{ article.title }}</h3>
        
        <p class="article-summary">{{ article.summary }}</p>
        
        <div class="article-footer">
          <div class="article-meta">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ article.viewCount }}
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              {{ article.likeCount }}
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ article.commentCount }}
            </span>
          </div>
          <div class="article-category" v-if="article.categoryName">
            <el-icon><Folder /></el-icon>
            {{ article.categoryName }}
          </div>
        </div>
        
        <div class="article-tags" v-if="article.tags && article.tags.length">
          <el-tag 
            v-for="tag in article.tags" 
            :key="tag.id"
            size="small"
            type="info"
            effect="plain"
          >
            {{ tag.name }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { View, Star, ChatDotRound, Folder } from '@element-plus/icons-vue'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const typeMap = {
  0: { label: '原创', type: 'primary' },
  1: { label: '转载', type: 'warning' },
  2: { label: '翻译', type: 'success' }
}

const typeTag = computed(() => typeMap[props.article.type] || typeMap[0])

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const goDetail = () => {
  router.push(`/article/${props.article.id}`)
}
</script>

<style scoped>
.article-card {
  cursor: pointer;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.article-layout {
  display: flex;
  gap: 20px;
}

.article-cover {
  width: 200px;
  aspect-ratio: 20 / 13;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.article-date {
  font-size: 13px;
  color: var(--text-secondary);
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s;
}

.article-card:hover .article-title {
  color: var(--primary-color);
}

.article-summary {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
  flex: 1;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.article-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.article-category {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.article-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .article-cover {
    width: 100%;
    aspect-ratio: 16 / 9;
  }
  
  .article-layout {
    flex-direction: column;
  }
}
</style>