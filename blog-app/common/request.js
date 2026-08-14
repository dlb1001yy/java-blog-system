import { BASE_URL, TOKEN_KEY, REFRESH_TOKEN_KEY } from './config.js'
import { signRequest } from './signing.js'

const pendingRequest = new Map()

// 生成请求Key
const generateRequestKey = (config) => {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

// 刷新令牌的并发去重：同一时刻多个 401 只触发一次刷新请求
let refreshing = null
function refreshTokenOnce() {
  if (!refreshing) {
    const refreshToken = uni.getStorageSync(REFRESH_TOKEN_KEY)
    if (!refreshToken) {
      return Promise.reject(new Error('no refresh token'))
    }
    refreshing = new Promise((resolve, reject) => {
      uni.request({
        url: BASE_URL + '/auth/refresh',
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'X-Refresh-Token': refreshToken
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 200) {
            uni.setStorageSync(TOKEN_KEY, res.data.data.accessToken)
            uni.setStorageSync(REFRESH_TOKEN_KEY, res.data.data.refreshToken)
            resolve(res.data.data.accessToken)
          } else {
            reject(new Error('refresh failed'))
          }
        },
        fail: (err) => reject(err)
      })
    }).finally(() => {
      refreshing = null
    })
  }
  return refreshing
}

// 清除登录态并跳转登录页
function clearAuthAndRedirect() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
  uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
  setTimeout(() => uni.reLaunch({ url: '/pages/mine/login' }), 1500)
}

const request = async (options) => {
  // 请求锁：刷新进行中时，等待刷新完成再用新 token 发出
  if (refreshing) {
    try {
      await refreshing
    } catch {
      // 刷新失败，静默放行，由 401 处理逻辑跳转登录
    }
  }

  // 防止重复提交
  const requestKey = generateRequestKey(options)
  if (pendingRequest.has(requestKey)) {
    return Promise.reject(new Error('请求重复'))
  }
  pendingRequest.set(requestKey, true)

  // 显示 Loading
  if (options.loading !== false) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync(TOKEN_KEY)

    // Generate request signature for authenticated requests
    let signHeaders = {}
    if (token) {
      const sign = signRequest(options.method || 'GET', options.url)
      signHeaders = {
        'X-Timestamp': sign.timestamp,
        'X-Nonce': sign.nonce,
        'X-Signature': sign.signature
      }
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || options.params,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...signHeaders,
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data)
          } else {
            // 业务错误
            uni.showToast({ title: res.data.message || '请求失败', icon: 'none' })
            reject(res.data)
          }
        } else if (res.statusCode === 401 && !options._retry) {
          // Token 过期，尝试静默刷新
          options._retry = true
          refreshTokenOnce()
            .then((newToken) => {
              // 用新令牌重试原请求
              pendingRequest.delete(requestKey)
              options.header = options.header || {}
              options.header['Authorization'] = `Bearer ${newToken}`
              request(options).then(resolve).catch(reject)
            })
            .catch(() => {
              // 刷新失败，清除登录态并跳转登录页
              clearAuthAndRedirect()
              reject(res)
            })
        } else if (res.statusCode === 401) {
          // 已重试过仍 401，直接跳转登录
          clearAuthAndRedirect()
          reject(res)
        } else {
          uni.showToast({ title: '网络异常', icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      },
      complete: () => {
        uni.hideLoading()
        pendingRequest.delete(requestKey)
      }
    })
  })
}

export default request
