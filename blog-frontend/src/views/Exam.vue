<template>
  <div class="exam-page">
    <div class="page-header">
      <h1 class="page-title">在线考试</h1>
      <p class="page-desc">选择一份试卷开始你的测验，切屏超限将被强制交卷，请诚信作答</p>
    </div>

    <div v-loading="loading" class="paper-list">
      <template v-if="papers.length">
        <el-card v-for="paper in papers" :key="paper.id" class="paper-card" shadow="hover">
          <div class="paper-body">
            <div class="paper-info">
              <h3 class="paper-title">{{ paper.title }}</h3>
              <p class="paper-desc">{{ paper.description || '暂无描述' }}</p>
              <div class="paper-meta">
                <el-tag size="small" type="warning">总分 {{ paper.totalScore }} 分</el-tag>
                <el-tag size="small" type="success">时长 {{ paper.duration }} 分钟</el-tag>
                <el-tag size="small" type="info">{{ paper.questionCount }} 道题</el-tag>
              </div>
            </div>
            <div class="paper-action">
              <el-button type="primary" @click="startExam(paper)">开始考试</el-button>
            </div>
          </div>
        </el-card>
      </template>
      <el-empty v-else-if="!loading" description="暂无已发布的试卷" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import examApi from '@/api/exam'

const router = useRouter()
const loading = ref(false)
const papers = ref([])

const loadPapers = async () => {
  loading.value = true
  try {
    const res = await examApi.getPapers({ page: 1, size: 50 })
    papers.value = res.data?.records || res.data || []
  } finally {
    loading.value = false
  }
}

const startExam = (paper) => {
  router.push({ path: `/exam/${paper.id}`, query: { title: paper.title, duration: paper.duration } })
}

onMounted(loadPapers)
</script>

<style scoped>
.exam-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary, #303133);
  margin-bottom: 8px;
}

.page-desc {
  color: var(--text-secondary, #909399);
  font-size: 14px;
}

.paper-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.paper-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.paper-info {
  flex: 1;
  min-width: 0;
}

.paper-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  margin-bottom: 8px;
}

.paper-desc {
  color: var(--text-regular, #606266);
  font-size: 14px;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.paper-meta {
  display: flex;
  gap: 8px;
}

.paper-action {
  flex-shrink: 0;
}
</style>
