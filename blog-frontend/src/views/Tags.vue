<template>
  <div class="tags-page">
    <div class="container">
      <div class="card">
        <h2 class="page-title">标签云</h2>
        <div class="tag-cloud-container">
          <el-tag v-for="tag in tags" :key="tag.id" :style="{ fontSize: getTagSize(tag) }" class="tag-cloud-item" @click="$router.push(`/tags?tagId=${tag.id}`)">
            {{ tag.name }}
          </el-tag>
        </div>
        <el-empty v-if="tags.length === 0" description="暂无标签" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import articleApi from '@/api/article'

const tags = ref([])
const getTagSize = (tag) => {
  const sizes = ['14px', '16px', '18px', '20px', '22px', '24px']
  return sizes[tag.id % sizes.length]
}
onMounted(async () => {
  const res = await articleApi.getTags()
  tags.value = res.data || []
})
</script>

<style scoped>
.page-title { margin-bottom: 24px; font-size: 20px; font-weight: 600; }
.tag-cloud-container { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.tag-cloud-item { cursor: pointer; transition: transform 0.3s; }
.tag-cloud-item:hover { transform: scale(1.1); }
</style>