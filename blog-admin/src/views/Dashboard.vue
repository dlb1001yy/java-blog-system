<template>
  <div class="dashboard">
    <!-- 平台概览欢迎区块 -->
    <div class="hero-card">
      <div class="hero-main">
        <p class="hero-date">{{ todayText }}</p>
        <h2 class="hero-greeting">{{ greeting }}，{{ userStore.username || '管理员' }}</h2>
        <div class="hero-brand">
          <h1 class="hero-title">Java码农笔记</h1>
          <p class="hero-slogan">Java 技术学习分享一体化平台 · 集成 博客 · 面试题库 · 在线考试 · 音乐 · 简历 五大模块</p>
          <p class="hero-desc">记录 Java 学习之路，分享技术心得，提供在线刷题、组卷考试与简历制作能力</p>
        </div>
      </div>
      <div class="hero-chips">
        <div class="chip-group">
          <span class="chip-group-label">内容管理</span>
          <span class="chip" @click="router.push('/article')">文章管理</span>
          <span class="chip" @click="router.push('/interview-questions')">面试题管理</span>
          <span class="chip" @click="router.push('/music')">音乐管理</span>
        </div>
        <span class="chip-divider"></span>
        <div class="chip-group">
          <span class="chip-group-label">考试管理</span>
          <span class="chip" @click="router.push('/exam-questions')">题库</span>
          <span class="chip" @click="router.push('/exam-papers')">试卷</span>
          <span class="chip" @click="router.push('/marking')">阅卷中心</span>
        </div>
        <span class="chip-divider"></span>
        <div class="chip-group">
          <span class="chip-group-label">系统管理</span>
          <span class="chip" @click="router.push('/comment')">评论</span>
          <span class="chip" @click="router.push('/users')">用户</span>
          <span class="chip" @click="router.push('/operation-log')">操作日志</span>
        </div>
      </div>
    </div>

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
            <p class="stat-sub">已发布 {{ stats.publishedCount || 0 }} 篇</p>
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
            <el-icon :size="28"><Message /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">留言总数</p>
            <p class="stat-value">{{ stats.messageCount || 0 }}</p>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--info">
          <div class="stat-icon">
            <el-icon :size="28"><Notebook /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">面试题总数</p>
            <p class="stat-value">{{ stats.interviewQuestionCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--violet">
          <div class="stat-icon">
            <el-icon :size="28"><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">试题总数</p>
            <p class="stat-value">{{ stats.examQuestionCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--red">
          <div class="stat-icon">
            <el-icon :size="28"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">待阅卷数</p>
            <p class="stat-value">{{ stats.pendingMarkingCount || 0 }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-card--teal-dark">
          <div class="stat-icon">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">注册用户数</p>
            <p class="stat-value">{{ stats.userCount || 0 }}</p>
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
            <span class="chart-title">模块内容分布</span>
          </div>
          <div ref="moduleChartRef" style="height: 320px;"></div>
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
              <el-badge :value="todo.pendingCommentCount || 0" :max="99" />
            </div>
            <div class="todo-item" @click="$router.push('/message')">
              <el-icon><Message /></el-icon>
              <span class="todo-text">待审核留言</span>
              <el-badge :value="todo.pendingMessageCount || 0" :max="99" />
            </div>
            <div class="todo-item" @click="$router.push('/article')">
              <el-icon><Document /></el-icon>
              <span class="todo-text">草稿箱</span>
              <el-badge :value="stats.articleCount - stats.publishedCount" :max="99" />
            </div>
            <div class="todo-item" @click="$router.push('/marking')">
              <el-icon><EditPen /></el-icon>
              <span class="todo-text">待阅卷</span>
              <el-badge :value="todo.pendingMarkingCount || 0" :max="99" />
            </div>
            <div class="todo-item" @click="$router.push('/resumeManage')">
              <el-icon><Tickets /></el-icon>
              <span class="todo-text">待审核简历</span>
              <el-badge :value="todo.pendingResumeCount || 0" :max="99" />
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

    <!-- 最近活动 & 系统状态 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">最近活动</span>
          </div>
          <el-timeline v-if="activities.length">
            <el-timeline-item
              v-for="item in activities"
              :key="item.id"
              :timestamp="item.createTime"
              :type="item.status === 1 ? 'primary' : 'danger'"
            >
              <span class="activity-user">{{ item.username }}</span>
              <span class="activity-text">{{ item.operation }}</span>
              <span class="activity-uri">{{ item.method }} {{ item.uri }}</span>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无操作记录" :image-size="60" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">系统状态</span>
          </div>
          <div class="status-list">
            <div class="status-item">
              <span class="status-dot" :class="statusDotClass(systemStatus.redis?.status)"></span>
              <span class="status-name">Redis</span>
              <span class="status-value">{{ statusText(systemStatus.redis?.status) }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" :class="statusDotClass(systemStatus.database?.status)"></span>
              <span class="status-name">数据库</span>
              <span class="status-value">{{ statusText(systemStatus.database?.status) }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" :class="statusDotClass(systemStatus.disk?.status)"></span>
              <span class="status-name">磁盘</span>
              <span class="status-value">{{ diskText }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot" :class="jvmDotClass"></span>
              <span class="status-name">JVM 内存</span>
              <span class="status-value">{{ jvmText }}</span>
            </div>
            <div class="status-sub-title">服务信息</div>
            <div class="status-item">
              <span class="status-dot is-neutral"></span>
              <span class="status-name">运行时长</span>
              <span class="status-value">{{ uptimeText }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot is-neutral"></span>
              <span class="status-name">JDK 版本</span>
              <span class="status-value">{{ systemStatus.jdkVersion || '--' }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot is-neutral"></span>
              <span class="status-name">操作系统</span>
              <span class="status-value">{{ osText }}</span>
            </div>
            <div class="status-item">
              <span class="status-dot is-neutral"></span>
              <span class="status-name">后端版本</span>
              <span class="status-value">{{ appVersionText }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  Document, View, ChatDotRound, Message, Calendar, EditPen,
  Notebook, Collection, UserFilled, Tickets
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import dashboardApi from '@/api/dashboard'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({})
const trendChartRef = ref()
const moduleChartRef = ref()
const categoryChartRef = ref()

let trendChart = null
let moduleChart = null
let categoryChart = null

const appStore = useAppStore()

// 缓存图表数据，主题切换时直接用缓存重建 option，无需重新请求接口
const trendData = ref([])
const moduleData = ref([])
const categoryData = ref([])

// 从 CSS 变量读取主题感知颜色（html 上的 dark 类切换后变量值随之变化）
const getThemeColors = () => {
  const styles = getComputedStyle(document.documentElement)
  return {
    axisLine: styles.getPropertyValue('--border-color').trim() || '#E7E5E4',
    label: styles.getPropertyValue('--text-secondary').trim() || '#A8A29E',
    splitLine: (styles.getPropertyValue('--border-color').trim() || '#E7E5E4') + '55'
  }
}

// 欢迎区问候语与日期
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  const now = new Date()
  const date = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  const week = now.toLocaleDateString('zh-CN', { weekday: 'long' })
  return `${date} ${week}`
})

const fetchOverview = async () => {
  try {
    const res = await dashboardApi.getOverview()
    Object.assign(stats, res.data || {})
  } catch {
    // 概览拉取失败时保持空对象，卡片显示 0，不阻塞看板渲染
  }
}

// 待办事项（含待阅卷数、待审核简历数）
const todo = reactive({})

const fetchTodo = async () => {
  const res = await dashboardApi.getTodo()
  Object.assign(todo, res.data || {})
}

// 最近操作日志（前 8 条）
const activities = ref([])

const fetchActivities = async () => {
  const res = await dashboardApi.getActivities({ current: 1, size: 8 })
  activities.value = res.data?.records || []
}

// 系统状态
const systemStatus = reactive({})

const formatSize = (bytes) => {
  if (bytes == null) return '--'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let value = bytes
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit++
  }
  return `${value.toFixed(unit >= 2 ? 1 : 0)} ${units[unit]}`
}

const statusDotClass = (status) => status === 'up' ? 'is-up' : 'is-down'

const statusText = (status) => status === 'up' ? '正常' : '异常'

const diskText = computed(() => {
  const disk = systemStatus.disk
  if (!disk?.status) return '--'
  if (disk.status !== 'up') return '异常'
  const used = disk.totalSpace - disk.usableSpace
  const percent = disk.totalSpace ? Math.round((used / disk.totalSpace) * 100) : 0
  return `已用 ${formatSize(used)} / ${formatSize(disk.totalSpace)}（${percent}%）`
})

const jvmText = computed(() => {
  const jvm = systemStatus.jvm
  if (!jvm?.maxMemory) return '--'
  const used = jvm.totalMemory - jvm.freeMemory
  return `已用 ${formatSize(used)} / 最大 ${formatSize(jvm.maxMemory)}`
})

const jvmDotClass = computed(() => {
  const jvm = systemStatus.jvm
  if (!jvm?.maxMemory) return 'is-down'
  const used = jvm.totalMemory - jvm.freeMemory
  return used / jvm.maxMemory > 0.9 ? 'is-down' : 'is-up'
})

// 运行时长：毫秒格式化为 "X天 X小时 X分"
const uptimeText = computed(() => {
  const ms = Number(systemStatus.uptime)
  if (!ms || ms <= 0) return '--'
  const minutes = Math.floor(ms / 60000)
  const days = Math.floor(minutes / 1440)
  const hours = Math.floor((minutes % 1440) / 60)
  const mins = minutes % 60
  if (days > 0) return `${days}天 ${hours}小时 ${mins}分`
  if (hours > 0) return `${hours}小时 ${mins}分`
  return `${mins}分`
})

const osText = computed(() => {
  const { osName, osVersion } = systemStatus
  if (!osName && !osVersion) return '--'
  return `${osName || ''} ${osVersion || ''}`.trim()
})

const appVersionText = computed(() => {
  const { appVersion, buildTime } = systemStatus
  if (!appVersion && !buildTime) return '--'
  if (!buildTime) return appVersion
  return `${appVersion || '--'} · ${buildTime}`
})

const fetchSystemStatus = async () => {
  try {
    const res = await dashboardApi.getSystemStatus()
    Object.assign(systemStatus, res.data || {})
  } catch {
    // 状态拉取失败时展示 '--'，不让卡片报错
  }
}

// 构建趋势图配置：轴线/网格线/文字颜色跟随主题，品牌绿渐变保持不变
const buildTrendOption = (data) => {
  const colors = getThemeColors()
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date),
      axisLine: { lineStyle: { color: colors.axisLine } },
      axisLabel: { color: colors.label }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: colors.label },
      splitLine: { lineStyle: { color: colors.splitLine } }
    },
    series: [{
      name: '发布文章',
      type: 'line',
      smooth: true,
      data: data.map(item => item.count),
      itemStyle: { color: '#059669' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(5, 150, 105, 0.3)' },
          { offset: 1, color: 'rgba(5, 150, 105, 0.01)' }
        ])
      }
    }]
  }
}

const fetchTrend = async () => {
  const res = await dashboardApi.getArticleTrend()
  trendData.value = res.data || []

  await nextTick()
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption(buildTrendOption(trendData.value))
}

// 模块内容分布色板（品牌色系）
const MODULE_COLORS = ['#059669', '#10B981', '#F59E0B', '#0D9488', '#EC4899', '#8B5CF6', '#2DD4BF', '#F472B6', '#34D399']

// 构建模块分布环形饼图配置：图例文字颜色跟随主题，系列色固定为品牌色板
const buildModuleOption = (data) => {
  const colors = getThemeColors()
  const total = data.reduce((sum, item) => sum + (Number(item.value) || 0), 0)
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: colors.label } },
    title: {
      text: '内容总量',
      subtext: String(total),
      left: 'center',
      top: '38%',
      textStyle: { color: colors.label, fontSize: 14, fontWeight: 600 },
      subtextStyle: { color: colors.axisLine, fontSize: 18, fontWeight: 700 }
    },
    color: MODULE_COLORS,
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        scale: true,
        scaleSize: 6
      },
      data: data.map(item => ({ name: item.name, value: Number(item.value) || 0 }))
    }]
  }
}

const fetchModuleStats = async () => {
  try {
    const res = await dashboardApi.getModuleStats()
    moduleData.value = res.data || []
  } catch {
    moduleData.value = []
  }

  await nextTick()
  if (moduleChartRef.value && !moduleChart) moduleChart = echarts.init(moduleChartRef.value)
  moduleChart?.setOption(buildModuleOption(moduleData.value))
}

// 构建分类柱状图配置：轴标签/网格线颜色跟随主题，绿色渐变保持不变
const buildCategoryOption = (data) => {
  const colors = getThemeColors()
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLine: { lineStyle: { color: colors.axisLine } },
      axisLabel: { color: colors.label, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: colors.label },
      splitLine: { lineStyle: { color: colors.splitLine } }
    },
    series: [{
      type: 'bar',
      data: data.map(item => item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#059669' },
          { offset: 1, color: '#10B981' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  }
}

const fetchCategoryStats = async () => {
  const res = await dashboardApi.getCategoryStats()
  categoryData.value = res.data || []

  await nextTick()
  if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption(buildCategoryOption(categoryData.value))
}

const handleResize = () => {
  trendChart?.resize()
  moduleChart?.resize()
  categoryChart?.resize()
}

// 主题切换后用缓存数据 + 新颜色重建 option，并触发各图表 resize
watch(() => appStore.theme, () => {
  nextTick(() => {
    if (trendChart) trendChart.setOption(buildTrendOption(trendData.value))
    if (moduleChart) moduleChart.setOption(buildModuleOption(moduleData.value))
    if (categoryChart) categoryChart.setOption(buildCategoryOption(categoryData.value))
    handleResize()
  })
})

onMounted(() => {
  fetchOverview()
  fetchTodo()
  fetchActivities()
  fetchSystemStatus()
  fetchTrend()
  fetchModuleStats()
  fetchCategoryStats()

  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.hero-card {
  position: relative;
  padding: var(--space-6) var(--space-6) var(--space-5);
  margin-bottom: var(--space-5);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #059669 0%, #10B981 100%);
  box-shadow: var(--shadow-primary);
  color: #fff;
  overflow: hidden;
}

/* 渐变区右上装饰光斑 */
.hero-card::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -40px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  pointer-events: none;
}

.hero-card::after {
  content: '';
  position: absolute;
  bottom: -80px;
  right: 120px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}

.hero-date {
  position: relative;
  z-index: 1;
  font-size: var(--font-sm);
  color: rgba(255, 255, 255, 0.85);
}

.hero-greeting {
  position: relative;
  z-index: 1;
  margin-top: var(--space-1);
  font-size: var(--font-xl);
  font-weight: 700;
  color: #fff;
}

.hero-brand {
  position: relative;
  z-index: 1;
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid rgba(255, 255, 255, 0.25);
}

.hero-title {
  font-size: var(--font-2xl);
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.hero-slogan {
  margin-top: var(--space-2);
  font-size: var(--font-base);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
}

.hero-desc {
  margin-top: var(--space-1);
  font-size: var(--font-sm);
  color: rgba(255, 255, 255, 0.8);
}

.hero-chips {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-5);
}

.chip-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.chip-group-label {
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.75);
  margin-right: var(--space-1);
}

.chip-divider {
  width: 1px;
  height: 18px;
  background: rgba(255, 255, 255, 0.3);
}

.chip {
  padding: var(--space-1) var(--space-4);
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.18);
  font-size: var(--font-sm);
  color: #fff;
  cursor: pointer;
  transition: all var(--transition-fast);
  backdrop-filter: blur(4px);
}

.chip:hover {
  background: rgba(255, 255, 255, 0.32);
  transform: translateY(-1px);
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

.stat-card--info {
  background: linear-gradient(135deg, #2563EB, #60A5FA);
}

.stat-card--violet {
  background: linear-gradient(135deg, #6D28D9, #A78BFA);
}

.stat-card--red {
  background: linear-gradient(135deg, #DC2626, #F87171);
}

.stat-card--teal-dark {
  background: linear-gradient(135deg, #134E4A, #0F766E);
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

.stat-sub {
  margin-top: var(--space-1);
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.8);
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

.activity-user {
  font-weight: 600;
  color: var(--text-primary);
  margin-right: var(--space-2);
}

.activity-text {
  color: var(--text-regular);
  margin-right: var(--space-2);
}

.activity-uri {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.status-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.is-up {
  background: #10B981;
  box-shadow: 0 0 4px rgba(16, 185, 129, 0.6);
}

.status-dot.is-down {
  background: #EF4444;
  box-shadow: 0 0 4px rgba(239, 68, 68, 0.6);
}

.status-dot.is-neutral {
  background: var(--text-secondary);
  box-shadow: none;
}

.status-sub-title {
  margin-top: var(--space-2);
  font-size: var(--font-xs);
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.status-name {
  font-size: var(--font-base);
  font-weight: 600;
  color: var(--text-primary);
  min-width: 72px;
}

.status-value {
  flex: 1;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  text-align: right;
}
</style>
