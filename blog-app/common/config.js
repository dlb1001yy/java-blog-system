// #ifdef H5
export const BASE_URL = 'http://localhost:8080/api'
// #endif

// #ifdef APP-PLUS
export const BASE_URL = 'http://gz.aeert.com:19612/api'
// #endif

export const TOKEN_KEY = 'uni_app_token'
export const REFRESH_TOKEN_KEY = 'uni_app_refresh_token'

/**
 * 图片 CDN host 白名单：仅当图片 URL 的 host 命中列表中任一项（子串匹配、不区分大小写）时，
 * 才追加 OSS 图片处理参数（见 common/imageUrl.js 的 optimizeImageUrl）；
 * 相对路径与未命中的 URL 一律原样返回。配置示例：'img.example.com'
 */
export const IMG_CDN_HOSTS = []

// 站点文章页地址前缀，用于分享海报二维码（如 'https://example.com/article/'）；
// 为空时 H5 用当前站点路由（origin + /#/pages/article/detail?id=），App/小程序回退服务器地址（BASE_URL 去 /api）
export const SITE_URL = ''

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
