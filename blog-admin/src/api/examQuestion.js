import request from './request'

export default {
  // 分页查询题目（type/category/difficulty/keyword/status）
  getPage(params) {
    return request.get('/admin/exam-questions', { params })
  },

  // 题型统计
  getStats() {
    return request.get('/admin/exam-questions/stats')
  },

  // 题目详情
  getDetail(id) {
    return request.get(`/admin/exam-questions/${id}`)
  },

  // 新增/更新题目
  save(data) {
    return request.post('/admin/exam-questions', data)
  },

  // 批量导入题目（xlsx 文件）
  importQuestions(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/admin/exam-questions/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    })
  },

  // 下载导入模板（xlsx）
  downloadTemplate() {
    return request.get('/admin/exam-questions/template', { responseType: 'blob' })
  },

  // 删除题目
  delete(id) {
    return request.delete(`/admin/exam-questions/${id}`)
  },

  // 批量删除
  batchDelete(ids) {
    return request.delete('/admin/exam-questions/batch', { data: { ids } })
  }
}
