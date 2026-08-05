import { BASE_URL, TOKEN_KEY } from './config.js'

const pendingRequest = new Map()

// 生成请求Key
const generateRequestKey = (config) => {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

const request = (options) => {
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

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || options.params,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
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
        } else if (res.statusCode === 401) {
          // Token过期，跳转登录
          uni.removeStorageSync(TOKEN_KEY)
          uni.showToast({ title: '登录已过期', icon: 'none' })
          setTimeout(() => uni.reLaunch({ url: '/pages/mine/login' }), 1500)
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
