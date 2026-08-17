import { IMG_CDN_HOSTS } from './config.js'

/**
 * 图片 URL 优化：命中 CDN host 白名单的图片追加 OSS 图片处理参数（缩放 + webp 转码）。
 * - url 为空、非 http(s) 开头（相对路径走本地服务）或未命中白名单时，原样返回
 * - width > 0 时追加 resize 参数，否则仅做 webp 格式转换
 * @param {string} url 图片地址
 * @param {number} width 目标宽度（px）
 * @returns {string} 处理后的图片地址
 */
export function optimizeImageUrl(url, width = 0) {
  // 空地址或非 http(s) 开头（相对路径走本地服务）：原样返回
  if (!url || !/^https?:\/\//i.test(url)) return url

  // 用正则解析 host（避免依赖 new URL，兼容各端运行时），失败则原样返回
  const matched = url.match(/^https?:\/\/([^/?#]+)/i)
  if (!matched) return url
  // 去掉端口部分，统一转小写便于不区分大小写匹配
  const host = matched[1].split('@').pop().split(':')[0].toLowerCase()

  // 仅当白名单中任一项为 url host 的子串（不区分大小写）时才追加处理参数
  const hit = (IMG_CDN_HOSTS || []).some(
    (item) => item && host.includes(String(item).toLowerCase())
  )
  if (!hit) return url

  // OSS 图片处理参数：width>0 时缩放到指定宽度，统一转 webp
  const process =
    width > 0
      ? `x-oss-process=image/resize,w_${width}/format,webp`
      : 'x-oss-process=image/format,webp'

  // 已有 query 用 & 拼接，否则用 ? 开启
  return url + (url.includes('?') ? '&' : '?') + process
}
