import request from './request'

export default {
  // 获取我的简历
  getMyResume() {
    return request.get('/portal/resume/mine')
  },

  // 保存我的简历
  saveMyResume(data) {
    return request.put('/portal/resume/mine', data)
  },

  // 通过分享 token 获取简历（匿名）
  getResumeByToken(token) {
    return request.get(`/portal/resume/share/${token}`)
  },

  // 创建分享链接
  createShare(data) {
    return request.post('/portal/resume/mine/share', data)
  },

  // 获取我的分享列表
  getMyShares() {
    return request.get('/portal/resume/mine/shares')
  },

  // 撤销分享
  revokeShare(id) {
    return request.delete(`/portal/resume/mine/share/${id}`)
  }
}
