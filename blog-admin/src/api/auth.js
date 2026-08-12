import request from './request'

export default {
  // 登录
  login(data) {
    return request.post('/auth/login', data)
  },
  
  // 退出登录
  logout() {
    return request.post('/auth/logout')
  },

  // 刷新访问令牌（refresh token 由 HTTP-only Cookie 自动携带）
  refresh() {
    return request.post('/auth/refresh')
  },
  
  // 获取用户信息
  getUserInfo() {
    return request.get('/auth/info')
  },
  
  // 修改密码
  changePassword(data) {
    return request.put('/auth/password', data)
  },
  
  // 更新个人信息
  updateProfile(data) {
    return request.put('/auth/profile', data)
  }
}