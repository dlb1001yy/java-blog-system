import request from './request'

export default {
  // 分页查询试卷（keyword/status）
  getPage(params) {
    return request.get('/admin/exam-papers', { params })
  },

  // 试卷详情（返回 { paper, questionIds }）
  getDetail(id) {
    return request.get(`/admin/exam-papers/${id}`)
  },

  // 新增/更新试卷
  save(data) {
    return request.post('/admin/exam-papers', data)
  },

  // 组卷（全量替换试卷题目）
  compose(id, questionIds) {
    return request.post(`/admin/exam-papers/${id}/compose`, { questionIds })
  },

  // 发布/停用试卷
  publish(id, enable) {
    return request.post(`/admin/exam-papers/${id}/publish`, null, { params: { enable } })
  },

  // 删除试卷
  delete(id) {
    return request.delete(`/admin/exam-papers/${id}`)
  },

  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/exam-papers/batch', { data: { ids } })
  }
}
