// 离线缓存与稍后阅读：网络状态监听、文章列表/详情离线缓存、稍后阅读列表
// storage 读写均使用 uni.setStorageSync/getStorageSync，异常静默处理
import { ref } from 'vue'

// ========== 网络状态 ==========
// 是否处于离线模式（无网络连接时为 true）
export const offlineMode = ref(false)

// onNetworkStatusChange 是否已注册（防止重复注册）
let networkWatchRegistered = false

// 初始化网络监听：先读当前网络类型，再注册状态变化监听（幂等，可多处调用）
export function initNetworkWatch() {
  // 读取初始网络状态（none → 离线）
  try {
    uni.getNetworkType({
      success: (res) => {
        offlineMode.value = (res && res.networkType) === 'none'
      }
    })
  } catch (e) {
    // 读取失败保持在线假设，由后续状态变化纠正
  }

  // 监听网络状态变化（用标志位防止重复注册）
  if (!networkWatchRegistered && typeof uni.onNetworkStatusChange === 'function') {
    uni.onNetworkStatusChange((res) => {
      offlineMode.value = !res.isConnected
    })
    networkWatchRegistered = true
  }
}

// ========== 文章列表离线缓存 ==========
// 存储键与常量
const LIST_CACHE_KEY = 'app_cached_article_list'
const LIST_CACHE_EXPIRE = 7 * 24 * 60 * 60 * 1000 // 7 天过期

// 缓存文章列表：仅缓存第一页（最多 10 条），深拷贝避免与页面状态互相影响
export function cacheArticleList(records) {
  try {
    const copy = JSON.parse(JSON.stringify((records || []).slice(0, 10)))
    uni.setStorageSync(LIST_CACHE_KEY, { ts: Date.now(), records: copy })
  } catch (e) {
    // 写入失败静默，离线时降级为空态
  }
}

// 读取缓存的文章列表：无缓存或已过期（7 天）返回 null（过期同时清理 key）
export function getCachedArticleList() {
  try {
    const cached = uni.getStorageSync(LIST_CACHE_KEY)
    if (!cached || !Array.isArray(cached.records)) return null
    if (Date.now() - cached.ts > LIST_CACHE_EXPIRE) {
      uni.removeStorageSync(LIST_CACHE_KEY)
      return null
    }
    return cached.records
  } catch (e) {
    // 读取失败视为无缓存
    return null
  }
}

// ========== 文章详情离线缓存 ==========
// 存储键与常量
const DETAIL_CACHE_KEY = 'app_cached_article_details'
const DETAIL_CACHE_EXPIRE = 30 * 24 * 60 * 60 * 1000 // 30 天过期
const DETAIL_CACHE_MAX = 30 // 最多保留 30 篇

// 缓存文章详情：写入/更新指定 id，超过 30 篇按 ts 淘汰最旧
export function cacheArticleDetail(article) {
  if (!article || article.id == null) return
  try {
    const map = uni.getStorageSync(DETAIL_CACHE_KEY) || {}
    map[article.id] = { ts: Date.now(), article: JSON.parse(JSON.stringify(article)) }
    // 超出上限时按时间戳淘汰最旧
    const ids = Object.keys(map)
    if (ids.length > DETAIL_CACHE_MAX) {
      ids.sort((a, b) => map[a].ts - map[b].ts)
      ids.slice(0, ids.length - DETAIL_CACHE_MAX).forEach((id) => delete map[id])
    }
    uni.setStorageSync(DETAIL_CACHE_KEY, map)
  } catch (e) {
    // 写入失败静默
  }
}

// 读取缓存的文章详情：无缓存或已过期（30 天）返回 null
export function getCachedArticleDetail(id) {
  if (id == null) return null
  try {
    const map = uni.getStorageSync(DETAIL_CACHE_KEY) || {}
    const item = map[id]
    if (!item || !item.article) return null
    if (Date.now() - item.ts > DETAIL_CACHE_EXPIRE) return null
    return item.article
  } catch (e) {
    // 读取失败视为无缓存
    return null
  }
}

// ========== 稍后阅读 ==========
// 存储键与常量
const READ_LATER_KEY = 'app_readlater'
const READ_LATER_MAX = 50 // 最多 50 篇

// 稍后阅读 id 集合（响应式，toggle/remove 后同步刷新，供详情页按钮状态绑定）
export const readLaterIds = ref([])

// 从 storage 读取稍后阅读列表（元素为文章完整对象，含 content）
export function getReadLaterList() {
  try {
    return uni.getStorageSync(READ_LATER_KEY) || []
  } catch (e) {
    // 读取失败返回空列表
    return []
  }
}

// 刷新响应式 id 集合
export function refreshReadLaterIds() {
  readLaterIds.value = getReadLaterList().map((item) => item && item.id)
}

// 判断文章是否已加入稍后阅读
export function isReadLater(id) {
  return readLaterIds.value.includes(id)
}

// 切换稍后阅读状态：已存在则移除返回 false，否则头部插入返回 true（最多 50 篇）
export function toggleReadLater(article) {
  if (!article || article.id == null) return false
  try {
    const list = getReadLaterList()
    const idx = list.findIndex((item) => item.id === article.id)
    if (idx > -1) {
      list.splice(idx, 1)
      uni.setStorageSync(READ_LATER_KEY, list)
      refreshReadLaterIds()
      return false
    }
    // 头部插入完整文章对象（含 content，支持离线阅读）
    list.unshift(JSON.parse(JSON.stringify(article)))
    // 限制最多 50 篇
    if (list.length > READ_LATER_MAX) list.length = READ_LATER_MAX
    uni.setStorageSync(READ_LATER_KEY, list)
    refreshReadLaterIds()
    return true
  } catch (e) {
    // 写入失败返回 false，按钮状态不变
    return false
  }
}

// 移除指定文章的稍后阅读记录
export function removeReadLater(id) {
  try {
    const list = getReadLaterList().filter((item) => item.id !== id)
    uni.setStorageSync(READ_LATER_KEY, list)
    refreshReadLaterIds()
  } catch (e) {
    // 移除失败静默
  }
}

// 模块加载时初始化一次 id 集合，保证详情页按钮初始状态正确
refreshReadLaterIds()
