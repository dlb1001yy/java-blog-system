// #ifdef H5
export const BASE_URL = 'http://localhost:8080/api'
// #endif

// #ifdef APP-PLUS
export const BASE_URL = 'http://gz.aeert.com:19612/api'
// #endif

export const TOKEN_KEY = 'uni_app_token'
export const REFRESH_TOKEN_KEY = 'uni_app_refresh_token'

// 服务器 origin（去掉 /api 后缀），用于拼接相对路径的文件 URL
const SERVER_ORIGIN = BASE_URL.replace(/\/api$/, '')

/**
 * 解析文件 URL：相对路径拼接服务器 origin，已是完整 http(s) URL 的原样返回。
 * coverImage / avatar 等后端返回的路径统一走此函数。
 * @param {string} path
 * @returns {string}
 */
export function resolveFileUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path
  if (path.startsWith('/')) return SERVER_ORIGIN + path
  return SERVER_ORIGIN + '/' + path
}
