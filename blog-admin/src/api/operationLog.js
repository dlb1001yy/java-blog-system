import request from './request'

export default {
  // 分页查询操作日志
  getPage(params) {
    return request.get('/admin/operation-logs/page', { params })
  }
}
