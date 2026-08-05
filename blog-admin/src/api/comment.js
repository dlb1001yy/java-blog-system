import request from './request'

export default {
  // 分页查询评论
  getPage(params) {
    return request.get('/admin/comments/page', { params })
  },
  
  // 审核 - 通过
  approve(id) {
    return request.put(`/admin/comments/${id}/approve`)
  },
  
  // 审核 - 拒绝
  reject(id) {
    return request.put(`/admin/comments/${id}/reject`)
  },
  
  // 删除评论
  delete(id) {
    return request.delete(`/admin/comments/${id}`)
  },
  
  // 批量审核通过
  batchApprove(ids) {
    return request.put('/admin/comments/batch-approve', { ids })
  },
  
  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/comments/batch', { data: { ids } })
  }
}