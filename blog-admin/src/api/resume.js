import request from './request'

export default {
  // 获取简历信息
  getResume() {
    return request.get('/admin/resume')
  },
  
  // 保存简历信息
  save(data) {
    return request.post('/admin/resume', data)
  },
  
  // 更新简历信息
  update(data) {
    return request.put('/admin/resume', data)
  },

  // 分页查询用户简历
  getPage(params) {
    return request.get('/admin/resume/page', { params })
  },

  // 简历详情
  getDetail(id) {
    return request.get(`/admin/resume/detail/${id}`)
  },

  // 审核简历（status 1通过 2拒绝）
  audit(id, status, remark) {
    return request.put(`/admin/resume/audit/${id}`, null, { params: { status, remark } })
  }
}