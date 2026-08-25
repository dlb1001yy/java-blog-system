import request from './request'

export default {
  // 获取所有标签
  getAll() {
    return request.get('/admin/tags')
  },
  
  // 分页查询
  getPage(params) {
    return request.get('/admin/tags/page', { params })
  },
  
  // 获取详情
  getDetail(id) {
    return request.get(`/admin/tags/${id}`)
  },
  
  // 新增
  create(data) {
    return request.post('/admin/tags', data)
  },
  
  // 更新
  update(data) {
    return request.put('/admin/tags', data)
  },
  
  // 删除
  delete(id) {
    return request.delete(`/admin/tags/${id}`)
  },

  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/tags/batch', { data: { ids } })
  }
}