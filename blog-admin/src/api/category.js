import request from './request'

export default {
  // 获取所有分类
  getAll() {
    return request.get('/admin/categories')
  },
  
  // 分页查询
  getPage(params) {
    return request.get('/admin/categories/page', { params })
  },
  
  // 获取详情
  getDetail(id) {
    return request.get(`/admin/categories/${id}`)
  },
  
  // 新增
  create(data) {
    return request.post('/admin/categories', data)
  },
  
  // 更新
  update(data) {
    return request.put('/admin/categories', data)
  },
  
  // 删除
  delete(id) {
    return request.delete(`/admin/categories/${id}`)
  }
}