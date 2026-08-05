import request from './request'

export default {
  // 分页查询文章
  getPage(params) {
    return request.get('/admin/articles/page', { params })
  },
  
  // 获取文章详情
  getDetail(id) {
    return request.get(`/admin/articles/${id}`)
  },
  
  // 新增文章
  create(data) {
    return request.post('/admin/articles', data)
  },
  
  // 更新文章
  update(data) {
    return request.put('/admin/articles', data)
  },
  
  // 删除文章
  delete(id) {
    return request.delete(`/admin/articles/${id}`)
  },
  
  // 发布/取消发布
  togglePublish(id) {
    return request.put(`/admin/articles/${id}/publish`)
  },
  
  // 置顶/取消置顶
  toggleTop(id) {
    return request.put(`/admin/articles/${id}/top`)
  },
  
  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/articles/batch', { data: { ids } })
  }
}