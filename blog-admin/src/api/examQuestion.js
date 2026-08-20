import request from './request'

export default {
  // 分页查询题目（type/category/difficulty/keyword/status）
  getPage(params) {
    return request.get('/admin/exam-questions', { params })
  },

  // 题型统计
  getStats() {
    return request.get('/admin/exam-questions/stats')
  },

  // 题目详情
  getDetail(id) {
    return request.get(`/admin/exam-questions/${id}`)
  },

  // 新增/更新题目
  save(data) {
    return request.post('/admin/exam-questions', data)
  },

  // 删除题目
  delete(id) {
    return request.delete(`/admin/exam-questions/${id}`)
  }
}
