import request from './request'

export default {
  // 获取统计数据
  getStats() {
    return request.get('/admin/dashboard/stats')
  },

  // 获取平台概览统计
  getOverview() {
    return request.get('/admin/dashboard/overview')
  },

  // 获取各模块内容数量分布
  getModuleStats() {
    return request.get('/admin/dashboard/module-stats')
  },
  
  // 获取文章发布趋势
  getArticleTrend() {
    return request.get('/admin/dashboard/article-trend')
  },
  
  // 获取分类统计
  getCategoryStats() {
    return request.get('/admin/dashboard/category-stats')
  },
  
  // 获取文章类型统计
  getTypeStats() {
    return request.get('/admin/dashboard/type-stats')
  },

  // 获取待办事项数量
  getTodo() {
    return request.get('/admin/dashboard/todo')
  },

  // 获取最近操作日志
  getActivities(params) {
    return request.get('/admin/dashboard/activities', { params })
  },

  // 获取系统运行状态
  getSystemStatus() {
    return request.get('/admin/dashboard/system-status')
  }
}