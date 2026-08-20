import request from './request'

export default {
  // 分页获取已发布试卷
  getPapers(params) {
    return request.get('/portal/exam/papers', { params })
  },

  // 获取试卷题目
  getPaperQuestions(paperId) {
    return request.get(`/portal/exam/papers/${paperId}`)
  },

  // 提交试卷
  submitPaper(paperId, data) {
    return request.post(`/portal/exam/papers/${paperId}/submit`, data)
  },

  // 我的成绩
  getMyRecords(params) {
    return request.get('/portal/exam/records', { params })
  },

  // 成绩详情
  getRecordDetail(id) {
    return request.get(`/portal/exam/records/${id}`)
  }
}
