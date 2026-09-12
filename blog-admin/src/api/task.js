import request from './request'

export default {
  // 可用任务 SPI 列表（新增任务时选择类型）
  spiOptions() {
    return request.get('/admin/tasks/spis')
  },

  // 新增任务
  createTask(data) {
    return request.post('/admin/tasks', data)
  },

  // 更新任务（任务名/描述/cron）
  updateTask(id, data) {
    return request.put(`/admin/tasks/${id}`, data)
  },

  // 分页查询定时任务
  pageTasks(params) {
    return request.get('/admin/tasks/page', { params })
  },

  // 暂停任务
  pauseTask(id) {
    return request.put(`/admin/tasks/${id}/pause`)
  },

  // 恢复（启用）任务
  resumeTask(id) {
    return request.put(`/admin/tasks/${id}/resume`)
  },

  // 立即执行一次（异步触发，立即返回）
  runTask(id) {
    return request.post(`/admin/tasks/${id}/run`)
  }
}
