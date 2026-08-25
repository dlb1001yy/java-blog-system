import request from './request'

export default {
  // 分页获取题目列表
  getQuestions(params) {
    return request.get('/portal/interview/questions', { params })
  },

  // 题库方向列表（启用题目去重）
  getCategories() {
    return request.get('/portal/interview/categories')
  },

  // 获取题目详情（含参考答案）
  getQuestionAnswer(id) {
    return request.get(`/portal/interview/questions/${id}/answer`)
  },

  // 收藏/取消收藏（toggle）
  toggleFavorite(questionId) {
    return request.post(`/portal/interview/favorites/${questionId}`)
  },

  // 加入/移出错题本（toggle）
  toggleWrong(questionId) {
    return request.post(`/portal/interview/wrong/${questionId}`)
  },

  // 我的收藏列表
  getFavorites(params) {
    return request.get('/portal/interview/favorites', { params })
  },

  // 错题本列表
  getWrongList(params) {
    return request.get('/portal/interview/wrong', { params })
  }
}
