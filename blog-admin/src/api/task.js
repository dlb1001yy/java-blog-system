import request from './request'

export default {
  // 分页查询定时任务
  pageTasks(params) {
    return request.get('/admin/tasks/page', { params })
  },

  // 更新任务 cron 表达式
  updateTaskCron(id, data) {
    return request.put(`/admin/tasks/${id}/cron`, data)
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
