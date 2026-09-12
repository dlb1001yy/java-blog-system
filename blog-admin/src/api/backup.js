import request from './request'

export default {
  // 分页查询备份记录
  pageBackups(params) {
    return request.get('/admin/backups/page', { params })
  },

  // 立即备份（同步执行，返回新记录）；大库备份耗时较长，单独放宽超时避免前端误报
  createBackup() {
    return request.post('/admin/backups', null, { timeout: 300000 })
  },

  // 删除备份
  deleteBackup(id) {
    return request.delete(`/admin/backups/${id}`)
  },

  // 还原备份（危险操作：覆盖当前全部数据）；还原耗时较长，单独放宽超时
  restoreBackup(id) {
    return request.post(`/admin/backups/${id}/restore`, null, { timeout: 300000 })
  }
}
