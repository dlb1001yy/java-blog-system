import request from './request'

export default {
  // 分页获取文章
  getArticles(params) {
    return request.get('/portal/articles/page', { params })
  },

  // 搜索文章（Elasticsearch 全文检索）
  searchArticles(params) {
    return request.get('/portal/articles/search', { params })
  },

  // 获取站点统计信息
  getStats() {
    return request.get('/portal/stats')
  },

  // 获取文章详情
  getArticleDetail(id) {
    return request.get(`/portal/articles/${id}`)
  },
  
  // 获取热门文章
  getHotArticles() {
    return request.get('/portal/articles/hot')
  },
  
  // 获取最新文章
  getLatestArticles() {
    return request.get('/portal/articles/latest')
  },
  
  // 获取归档
  getArchives() {
    return request.get('/portal/articles/archives')
  },
  
  // 点赞文章
  likeArticle(id) {
    return request.post(`/portal/articles/${id}/like`)
  },
  
  // 获取分类
  getCategories() {
    return request.get('/portal/categories')
  },
  
  // 获取标签
  getTags() {
    return request.get('/portal/tags')
  },
  
  // 获取标签下的文章
  getArticlesByTag(tagId, params) {
    return request.get('/portal/articles/page', { 
      params: { ...params, tagId } 
    })
  },
  
  // 获取评论
  getComments(articleId) {
    return request.get(`/portal/comments/${articleId}`)
  },
  
  // 发表评论
  addComment(data) {
    return request.post('/portal/comments', data)
  },
  
  // 获取简历信息
  getResume() {
    return request.get('/portal/resume')
  },

  // 获取指定用户简历
  getResumeByUserId(userId) {
    return request.get(`/portal/resume/${userId}`)
  },

  // 获取我的简历
  getMyResume() {
    return request.get('/portal/resume/mine')
  },

  // 保存我的简历
  saveMyResume(data) {
    return request.put('/portal/resume/mine', data)
  },
  
  // 获取友情链接
  getLinks() {
    return request.get('/portal/links')
  },
  
  // 提交留言
  addMessage(data) {
    return request.post('/portal/messages', data)
  }
}