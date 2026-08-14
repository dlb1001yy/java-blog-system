<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--primary">
          <div class="stat-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">文章总数</p>
            <p class="stat-value">{{ stats.articleCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--secondary">
          <div class="stat-icon">
            <el-icon :size="28"><View /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">总浏览量</p>
            <p class="stat-value">{{ stats.totalViews || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--warning">
          <div class="stat-icon">
            <el-icon :size="28"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">评论总数</p>
            <p class="stat-value">{{ stats.commentCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--danger">
          <div class="stat-icon">
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
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">近7天文章发布趋势</span>
          </div>
          <div ref="trendChartRef" style="height: 320px;"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">文章类型分布</span>
          </div>
          <div ref="typeChartRef" style="height: 320px;"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">分类文章统计</span>
          </div>
          <div ref="categoryChartRef" style="height: 320px;"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">待处理事项</span>
          </div>
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
        </div>
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
      axisLine: { lineStyle: { color: '#E7E5E4' } },
      axisLabel: { color: '#A8A29E' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#A8A29E' },
      splitLine: { lineStyle: { color: '#F5F5F4' } }
    },
    series: [{
      name: '发布文章',
      type: 'line',
      smooth: true,
      data: res.data.map(item => item.count),
      itemStyle: { color: '#059669' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(5, 150, 105, 0.3)' },
          { offset: 1, color: 'rgba(5, 150, 105, 0.01)' }
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
        { value: res.data.original, name: '原创', itemStyle: { color: '#059669' } },
        { value: res.data.reproduced, name: '转载', itemStyle: { color: '#F59E0B' } },
        { value: res.data.translated, name: '翻译', itemStyle: { color: '#10B981' } }
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
      axisLabel: { color: '#A8A29E', rotate: 30 }
    },
    yAxis: { type: 'value', axisLabel: { color: '#A8A29E' } },
    series: [{
      type: 'bar',
      data: res.data.map(item => item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#059669' },
          { offset: 1, color: '#10B981' }
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
  margin-bottom: var(--space-5);
}

.chart-row {
  margin-top: var(--space-5);
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  color: #fff;
  overflow: hidden;
  transition: all var(--transition-base);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: -30px;
  right: -30px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-card--primary {
  background: linear-gradient(135deg, #059669, #10B981);
}

.stat-card--secondary {
  background: linear-gradient(135deg, #0D9488, #2DD4BF);
}

.stat-card--warning {
  background: linear-gradient(135deg, #F59E0B, #FBBF24);
}

.stat-card--danger {
  background: linear-gradient(135deg, #EC4899, #F472B6);
}

.stat-icon {
  position: relative;
  z-index: 1;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.stat-info {
  position: relative;
  z-index: 1;
  flex: 1;
}

.stat-label {
  font-size: var(--font-sm);
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: var(--space-1);
}

.stat-value {
  font-size: var(--font-3xl);
  font-weight: 700;
  color: #fff;
}

.chart-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: var(--space-5);
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.chart-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-color);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.chart-title {
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.todo-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.todo-item:hover {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-7);
  transform: translateX(4px);
}

.todo-item .el-icon {
  color: var(--color-primary);
  font-size: 20px;
}

.todo-text {
  flex: 1;
  font-size: var(--font-base);
  color: var(--text-regular);
  font-weight: 500;
}
</style>
