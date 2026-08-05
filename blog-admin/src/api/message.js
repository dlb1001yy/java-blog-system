import request from './request'

export default {
  // 分页查询留言
  getPage(params) {
    return request.get('/admin/messages/page', { params })
  },
  
  // 审核 - 通过
  approve(id) {
    return request.put(`/admin/messages/${id}/approve`)
  },
  
  // 审核 - 拒绝
  reject(id) {
    return request.put(`/admin/messages/${id}/reject`)
  },
  
  // 删除留言
  delete(id) {
    return request.delete(`/admin/messages/${id}`)
  },
  
  // 批量审核通过
  batchApprove(ids) {
    return request.put('/admin/messages/batch-approve', { ids })
  },
  
  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/messages/batch', { data: { ids } })
  }
}