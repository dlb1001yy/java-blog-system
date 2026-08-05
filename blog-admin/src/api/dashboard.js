import request from './request'

export default {
  // 获取统计数据
  getStats() {
    return request.get('/admin/dashboard/stats')
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
  }
}