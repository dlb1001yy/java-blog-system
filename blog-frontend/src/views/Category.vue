<template>
  <div class="category-page">
    <div class="container">
      <div class="card">
        <h2 class="page-title">文章分类</h2>
        <div class="category-grid">
          <div class="category-item" v-for="cat in categories" :key="cat.id" @click="$router.push(`/category/${cat.id}`)">
            <el-icon :size="32"><Folder /></el-icon>
            <span class="cat-name">{{ cat.name }}</span>
          </div>
        </div>
        <el-empty v-if="categories.length === 0" description="暂无分类" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Folder } from '@element-plus/icons-vue'
import articleApi from '@/api/article'

const categories = ref([])
onMounted(async () => {
  const res = await articleApi.getCategories()
  categories.value = res.data || []
})
</script>

<style scoped>
.page-title { margin-bottom: 24px; font-size: 20px; font-weight: 600; }
.category-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 16px; }
.category-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 24px; background: #f5f7fa; border-radius: 8px; cursor: pointer; transition: all 0.3s; color: var(--text-regular); }
.category-item:hover { background: #ecf5ff; color: var(--primary-color); transform: translateY(-4px); }
.cat-name { font-size: 14px; font-weight: 500; }
</style>