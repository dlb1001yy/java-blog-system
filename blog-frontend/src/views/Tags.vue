<template>
  <div class="tags-page">
    <div class="container">
      <div class="card">
        <h2 class="page-title">标签云</h2>
        <div class="tag-cloud-container">
          <el-tag v-for="tag in visibleTags" :key="tag.id" :style="{ fontSize: getTagSize(tag) }" class="tag-cloud-item" @click="$router.push(`/tags?tagId=${tag.id}`)">
            {{ tag.name }}
          </el-tag>
        </div>
        <div class="load-more" v-if="tags.length > visibleCount">
          <el-button text type="primary" @click="showMore">
            加载更多 ({{ visibleCount }}/{{ tags.length }})
          </el-button>
        </div>
        <el-empty v-if="tags.length === 0" description="暂无标签" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import articleApi from '@/api/article'

const tags = ref([])
// 分步显示：初始 8 个，点"加载更多"每次追加 8 个
const STEP = 8
const visibleCount = ref(STEP)
const visibleTags = computed(() => tags.value.slice(0, visibleCount.value))
const showMore = () => { visibleCount.value += STEP }

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
.load-more { display: flex; justify-content: center; margin-top: 16px; }
</style>