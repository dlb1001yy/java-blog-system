<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">文章总数</p>
            <p class="stat-value">{{ stats.articleCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="28"><View /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">总浏览量</p>
            <p class="stat-value">{{ stats.totalViews || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="28"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">评论总数</p>
            <p class="stat-value">{{ stats.commentCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="28"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">总点赞数</p>
            <p class="stat-value">{{ stats.totalLikes || 0 }}</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover">
          <template #header>
            <span>近7天文章发布趋势</span>
          </template>
          <div ref="trendChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <span>文章类型分布</span>
          </template>
          <div ref="typeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span>分类文章统计</span>
          </template>
          <div ref="categoryChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>待处理事项</span>
            </div>
          </template>
          <div class="todo-list">
            <div class="todo-item" @click="$router.push('/comment')">
              <el-icon><ChatDotRound /></el-icon>
              <span class="todo-text">待审核评论</span>
              <el-badge :value="stats.pendingCommentCount || 0" :max="99" />
            </div>
            <div class="todo-item" @click="$router.push('/message')">
              <el-icon><Message /></el-icon>
              <span class="todo-text">待审核留言</span>
              <el-badge :value="stats.pendingMessageCount || 0" :max="99" />
            </div>
            <div class="todo-item" @click="$router.push('/article')">
              <el-icon><Document /></el-icon>
              <span class="todo-text">草稿箱</span>
              <el-badge :value="stats.articleCount - stats.publishedCount" :max="99" />
            </div>
            <div class="todo-item">
              <el-icon><Calendar /></el-icon>
              <span class="todo-text">今日新增</span>
              <el-badge :value="stats.todayArticleCount || 0" :max="99" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { 
  Document, View, ChatDotRound, Star, Message, Calendar 
} from '@element-plus/icons-vue'
import dashboardApi from '@/api/dashboard'

const stats = reactive({})
const trendChartRef = ref()
const typeChartRef = ref()
const categoryChartRef = ref()

let trendChart = null
let typeChart = null
let categoryChart = null

const fetchStats = async () => {
  const res = await dashboardApi.getStats()
  Object.assign(stats, res.data)
}

const fetchTrend = async () => {
  const res = await dashboardApi.getArticleTrend()
  
  await nextTick()
  trendChart = echarts.init(trendChartRef.value)
  
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: res.data.map(item => item.date),
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#606266' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      name: '发布文章',
      type: 'line',
      smooth: true,
      data: res.data.map(item => item.count),
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.01)' }
        ])
      }
    }]
  })
}

const fetchTypeStats = async () => {
  const res = await dashboardApi.getTypeStats()
  
  await nextTick()
  typeChart = echarts.init(typeChartRef.value)
  
  typeChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
      data: [
        { value: res.data.original, name: '原创', itemStyle: { color: '#409eff' } },
        { value: res.data.reproduced, name: '转载', itemStyle: { color: '#e6a23c' } },
        { value: res.data.translated, name: '翻译', itemStyle: { color: '#67c23a' } }
      ]
    }]
  })
}

const fetchCategoryStats = async () => {
  const res = await dashboardApi.getCategoryStats()
  
  await nextTick()
  categoryChart = echarts.init(categoryChartRef.value)
  
  categoryChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: res.data.map(item => item.name),
      axisLabel: { color: '#606266', rotate: 30 }
    },
    yAxis: { type: 'value', axisLabel: { color: '#606266' } },
    series: [{
      type: 'bar',
      data: res.data.map(item => item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  typeChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  fetchTypeStats()
  fetchCategoryStats()
  
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.todo-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.todo-text {
  flex: 1;
  font-size: 14px;
  color: #606266;
}
</style>