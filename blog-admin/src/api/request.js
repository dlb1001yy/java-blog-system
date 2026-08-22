import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { signRequest } from './signing'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加 access token + 请求签名
request.interceptors.request.use(
  async config => {
    // 请求锁：刷新进行中时，业务请求等待刷新完成再用新 token 发出
    if (refreshing && !config._isRefresh) {
      try {
        const newToken = await refreshing
        if (newToken) {
          localStorage.setItem('admin_token', newToken)
        }
      } catch {
        // 刷新失败，静默放行，由响应拦截器处理 401
      }
    }
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      const { timestamp, nonce, signature } = signRequest(config.method?.toUpperCase() || 'GET', config.url)
      config.headers['X-Timestamp'] = timestamp
      config.headers['X-Nonce'] = nonce
      config.headers['X-Signature'] = signature
    }
    return config
  },
  error => Promise.reject(error)
)

// 刷新令牌的并发去重：同一时刻多个 401 只触发一次刷新请求
let refreshing = null
function refreshTokenOnce() {
  if (!refreshing) {
    refreshing = request({
        url: '/auth/refresh',
        method: 'post',
        _isRefresh: true
      })
      .then(res => {
        const newToken = res.data.accessToken
        localStorage.setItem('admin_token', newToken)
        return newToken
      })
      .finally(() => {
        // 刷新结束后清空，便于下次失败重新发起
        refreshing = null
      })
  }
  return refreshing
}

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 滑动续期：后端在响应头下发新 AccessToken 时静默替换
    const newToken = response.headers['x-new-token']
    if (newToken) localStorage.setItem('admin_token', newToken)
    // 文件流响应（如模板下载）直接透传
    if (response.config.responseType === 'blob') return response
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  async error => {
    const status = error.response?.status
    const originalConfig = error.config

    if (status === 401 && !originalConfig._retry) {
      // 登录接口 401 直接失败，避免死循环
      if (originalConfig.url?.includes('/auth/login')) {
        ElMessage.error('用户名或密码错误')
        return Promise.reject(error)
      }
      originalConfig._retry = true
      try {
        const newToken = await refreshTokenOnce()
        originalConfig.headers['Authorization'] = `Bearer ${newToken}`
        // 用新令牌重试原请求
        return request(originalConfig)
      } catch (refreshErr) {
        // 刷新失败，清除登录态并跳转登录页
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_username')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
        return Promise.reject(refreshErr)
      }
    }

    if (error.response) {
      switch (error.response.status) {
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 423:
          ElMessage.error(error.response.data?.message || '账户已被锁定')
          break
        case 429:
          ElMessage.error(error.response.data?.message || '请求过于频繁，请稍后再试')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
