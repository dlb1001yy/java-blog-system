import request from './request'

export default {
  // 待批改答卷分页
  pending(params) {
    return request.get('/admin/marking/pending', { params })
  },

  // 批改详情
  detail(recordId) {
    return request.get(`/admin/marking/records/${recordId}`)
  },

  // 保存批改（submit=false 存草稿，true 提交并发布成绩）
  save(recordId, items, submit) {
    return request.post(`/admin/marking/records/${recordId}/save`, { items, submit })
  }
}
