<template>
  <div class="latest-card card" @click="$router.push(`/articles/${article.id}`)">
    <div class="cover-wrap">
      <img v-if="article.coverImage" :src="article.coverImage" class="cover" alt="cover" />
      <div v-else class="cover cover-placeholder"></div>
    </div>
    <div class="body">
      <div class="meta-row">
        <el-tag size="small" type="primary" effect="light">
          {{ article.categoryName || article.tags?.[0]?.name || '未分类' }}
        </el-tag>
        <span class="date">{{ formatDate(article.createTime) }}</span>
      </div>
      <h3 class="title">{{ article.title }}</h3>
      <p class="summary">{{ article.summary }}</p>
      <div class="footer-row">
        <span class="stat"><el-icon :size="14"><View /></el-icon>{{ article.viewCount || 0 }}</span>
        <span class="stat"><el-icon :size="14"><Star /></el-icon>{{ article.likeCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { View, Star } from '@element-plus/icons-vue'

defineProps({
  article: { type: Object, required: true }
})

const formatDate = (d) => {
  if (!d) return ''
  return String(d).slice(0, 10)
}
</script>

<style scoped>
.latest-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.latest-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.cover-wrap {
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  background: linear-gradient(135deg, var(--primary-color), #9b59b6);
}

.body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date {
  font-size: 12px;
  color: var(--text-secondary);
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.latest-card:hover .title {
  color: var(--primary-color);
}

.summary {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.footer-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
