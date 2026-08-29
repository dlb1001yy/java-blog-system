import request from './request.js'

// 获取图形验证码
export const getCaptcha = () => request({ url: '/auth/captcha', method: 'GET' })

export default {
  // 文章
  getArticles(params, opts) {
    return request({ url: '/portal/articles/page', method: 'GET', data: params, ...opts })
  },
  getArticleDetail(id) {
    return request({ url: `/portal/articles/${id}`, method: 'GET' })
  },
  likeArticle(id) {
    return request({ url: `/portal/articles/${id}/like`, method: 'POST' })
  },
  // 搜索文章
  searchArticles(params, opts) {
    return request({ url: '/portal/articles/search', method: 'GET', data: params, ...opts })
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
  getCategories(opts) {
    return request({ url: '/portal/categories', method: 'GET', ...opts })
  },
  // 标签
  getTags(opts) {
    return request({ url: '/portal/tags', method: 'GET', ...opts })
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
  },

  // 注册
  register(data) {
    return request({ url: '/auth/register', method: 'POST', data })
  },

  // 文章（热门 / 最新 / 归档）
  getHotArticles() {
    return request({ url: '/portal/articles/hot', method: 'GET' })
  },
  getLatestArticles() {
    return request({ url: '/portal/articles/latest', method: 'GET' })
  },
  getArchives(opts) {
    return request({ url: '/portal/articles/archives', method: 'GET', ...opts })
  },

  // 留言
  sendMessage(data) {
    return request({ url: '/portal/messages', method: 'POST', data })
  },

  // 面试题
  getInterviewQuestions(params) {
    return request({ url: '/portal/interview/questions', method: 'GET', data: params })
  },
  getInterviewCategories() {
    return request({ url: '/portal/interview/categories', method: 'GET' })
  },
  getInterviewAnswer(id) {
    return request({ url: `/portal/interview/questions/${id}/answer`, method: 'GET' })
  },
  toggleInterviewFavorite(qid) {
    return request({ url: `/portal/interview/favorites/${qid}`, method: 'POST' })
  },
  toggleInterviewWrong(qid) {
    return request({ url: `/portal/interview/wrong/${qid}`, method: 'POST' })
  },
  getInterviewFavorites(params) {
    return request({ url: '/portal/interview/favorites', method: 'GET', data: params })
  },
  getInterviewWrong(params) {
    return request({ url: '/portal/interview/wrong', method: 'GET', data: params })
  },

  // 在线考试
  getExamPapers(params) {
    return request({ url: '/portal/exam/papers', method: 'GET', data: params })
  },
  getExamPaper(id) {
    return request({ url: `/portal/exam/papers/${id}`, method: 'GET' })
  },
  submitExam(id, data) {
    return request({ url: `/portal/exam/papers/${id}/submit`, method: 'POST', data })
  },
  getExamRecords(params) {
    return request({ url: '/portal/exam/records', method: 'GET', data: params })
  },
  getExamRecord(id) {
    return request({ url: `/portal/exam/records/${id}`, method: 'GET' })
  },

  // 音乐
  getSongs(params) {
    return request({ url: '/portal/music/songs', method: 'GET', data: params })
  },
  getPlaylists() {
    return request({ url: '/portal/music/playlists', method: 'GET' })
  },
  getPlaylist(id) {
    return request({ url: `/portal/music/playlists/${id}`, method: 'GET' })
  },
  reportPlay(id) {
    return request({ url: `/portal/music/songs/${id}/play`, method: 'POST', loading: false })
  },

  // 我的简历
  getMyResume() {
    return request({ url: '/portal/resume/mine', method: 'GET' })
  },
  saveMyResume(data) {
    return request({ url: '/portal/resume/mine', method: 'PUT', data })
  },
  createResumeShare(data) {
    return request({ url: '/portal/resume/mine/share', method: 'POST', data })
  },
  getMyResumeShares() {
    return request({ url: '/portal/resume/mine/shares', method: 'GET' })
  },
  revokeResumeShare(id) {
    return request({ url: `/portal/resume/mine/share/${id}`, method: 'DELETE' })
  }
}
