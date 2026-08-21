import request from './request'

export default {
  // 用户登录
  login(data) {
    return request.post('/auth/login', data)
  },

  // 获取图形验证码
  getCaptcha() {
    return request.get('/auth/captcha')
  },

  // 用户注册
  register(data) {
    return request.post('/auth/register', data)
  }
}
