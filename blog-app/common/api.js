import request from './request.js'

export default {
  // 文章
  getArticles(params) {
    return request({ url: '/portal/articles/page', method: 'GET', data: params })
  },
  getArticleDetail(id) {
    return request({ url: `/portal/articles/${id}`, method: 'GET' })
  },
  likeArticle(id) {
    return request({ url: `/portal/articles/${id}/like`, method: 'POST' })
  },
  // 搜索文章
  searchArticles(params) {
    return request({ url: '/portal/articles/search', method: 'GET', data: params })
  },
  // 相关文章
  getRelatedArticles(id) {
    return request({ url: `/portal/articles/${id}/related`, method: 'GET' })
  },

  // 评论
  getComments(articleId) {
    return request({ url: `/portal/comments/${articleId}`, method: 'GET' })
  },
  addComment(data) {
    return request({ url: '/portal/comments', method: 'POST', data })
  },

  // 简历
  getResume() {
    return request({ url: '/portal/resume', method: 'GET' })
  },

  // 分类
  getCategories() {
    return request({ url: '/portal/categories', method: 'GET' })
  },
  // 标签
  getTags() {
    return request({ url: '/portal/tags', method: 'GET' })
  },
  // 站点统计
  getStats() {
    return request({ url: '/portal/stats', method: 'GET' })
  },
  // 公开留言
  getMessages() {
    return request({ url: '/portal/messages', method: 'GET' })
  },

  // 用户 (新增)
  login(data) {
    return request({ url: '/auth/login', method: 'POST', data })
  },
  // 当前用户信息
  getUserInfo() {
    return request({ url: '/user/info', method: 'GET' })
  }
}
