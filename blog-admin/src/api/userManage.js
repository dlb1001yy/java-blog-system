import request from './request'

export default {
  // 用户分页列表
  getList(params) {
    return request.get('/admin/users/list', { params })
  },

  // 用户统计
  getStats() {
    return request.get('/admin/users/stats')
  },

  // 编辑用户
  update(id, data) {
    return request.put(`/admin/users/${id}`, data)
  },

  // 重置密码（newPassword 为空时后端生成并返回）
  resetPassword(id, newPassword) {
    return request.post(`/admin/users/${id}/reset-password`, { newPassword })
  },

  // 启用/禁用用户
  enable(id, enable) {
    return request.post(`/admin/users/${id}/enable`, null, { params: { enable } })
  }
}
