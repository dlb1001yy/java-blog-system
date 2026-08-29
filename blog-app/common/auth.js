import { TOKEN_KEY } from './config.js'

/**
 * 读取当前登录 token
 * @returns {string}
 */
export function getToken() {
  return uni.getStorageSync(TOKEN_KEY)
}

/**
 * 是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  return !!getToken()
}

/**
 * 构造当前页面完整路径（含查询串），供 requireLogin 的 redirect 参数使用
 * @returns {string} 形如 '/pages/index/index?tab=1'，取不到页面时返回 '/'
 */
export function buildLoginRedirect() {
  const pages = getCurrentPages()
  if (!pages || !pages.length) return '/'
  const current = pages[pages.length - 1]
  let url = '/' + (current.route || '')
  const options = current.options || {}
  const query = Object.keys(options)
    .map((key) => `${key}=${encodeURIComponent(options[key])}`)
    .join('&')
  if (query) url += '?' + query
  return url
}

/**
 * 登录校验：未登录时提示并跳转登录页
 * @param {string} redirectPath 登录成功后回跳的页面路径（建议用 buildLoginRedirect() 生成）
 * @returns {boolean} 是否已登录
 */
export function requireLogin(redirectPath) {
  if (isLoggedIn()) return true
  uni.showToast({ title: '请先登录', icon: 'none' })
  const redirect = redirectPath ? `?redirect=${encodeURIComponent(redirectPath)}` : ''
  setTimeout(() => {
    uni.reLaunch({ url: '/subpkg/pages/mine/login' + redirect })
  }, 1000)
  return false
}
