import request from './request'

export default {
  getPage(params) {
    return request.get('/admin/links/page', { params })
  },
  getAll() {
    return request.get('/admin/links')
  },
  create(data) {
    return request.post('/admin/links', data)
  },
  update(data) {
    return request.put('/admin/links', data)
  },
  delete(id) {
    return request.delete(`/admin/links/${id}`)
  },

  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/links/batch', { data: { ids } })
  }
}