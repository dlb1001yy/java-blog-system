import request from './request'

export default {
  // 获取简历信息
  getResume() {
    return request.get('/admin/resume')
  },
  
  // 保存简历信息
  save(data) {
    return request.post('/admin/resume', data)
  },
  
  // 更新简历信息
  update(data) {
    return request.put('/admin/resume', data)
  }
}