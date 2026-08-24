import request from './request'

export default {
  // 分页查询
  getPage(params) {
    return request.get('/admin/interview-questions', { params })
  },

  // 详情
  getDetail(id) {
    return request.get(`/admin/interview-questions/${id}`)
  },

  // 新增/更新
  save(data) {
    return request.post('/admin/interview-questions', data)
  },

  // 批量导入
  importQuestions(data) {
    return request.post('/admin/interview-questions/import', data)
  },

  // 删除
  delete(id) {
    return request.delete(`/admin/interview-questions/${id}`)
  }
}
