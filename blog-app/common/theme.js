// 设计令牌系统：集中定义颜色、间距、圆角、阴影、字号
// 与 uni.scss 中的 SCSS 变量保持同步
import { ref } from 'vue'

// 颜色
export const colors = {
  primary: '#4F46E5',        // 靛蓝主色
  primaryLight: '#6366F1',
  primaryDark: '#4338CA',
  secondary: '#06B6D4',      // 青色辅色
  secondaryLight: '#22D3EE',
  accent: '#8B5CF6',         // 紫色强调（用于技能标签等）
  bg: '#F1F5F9',             // 页面背景
  bgCard: '#FFFFFF',
  text: '#0F172A',           // 主文字
  textSecondary: '#64748B',  // 次要文字
  textTertiary: '#94A3B8',   // 三级文字
  border: '#E2E8F0',
  divider: '#F1F5F9',
  success: '#10B981',
  warning: '#F59E0B',
  danger: '#EF4444',
  // 文章类型徽章配色
  typeOriginal: '#4F46E5',
  typeReproduced: '#F59E0B',
  typeTranslated: '#10B981',
  // 渐变
  gradientPrimary: 'linear-gradient(135deg, #4F46E5 0%, #6366F1 100%)',
  gradientHero: 'linear-gradient(135deg, #4F46E5 0%, #06B6D4 100%)',
  gradientMesh: 'radial-gradient(at 0% 0%, #4F46E5 0%, transparent 50%), radial-gradient(at 100% 0%, #06B6D4 0%, transparent 50%), radial-gradient(at 50% 100%, #8B5CF6 0%, transparent 50%)',
}

// 暗黑模式颜色（与 App.vue 中 .theme-dark 的 CSS 变量保持一致）
export const darkColors = {
  primary: '#818CF8',
  primaryLight: '#6366F1',
  primaryDark: '#A5B4FC',
  secondary: '#22D3EE',
  secondaryLight: '#67E8F9',
  accent: '#A78BFA',
  bg: '#0B1220',
  bgCard: '#1E293B',
  text: '#E2E8F0',
  textSecondary: '#94A3B8',
  textTertiary: '#64748B',
  border: '#334155',
  divider: '#1E293B',
  success: '#34D399',
  warning: '#FBBF24',
  danger: '#F87171',
  gradientPrimary: 'linear-gradient(135deg,#4F46E5 0%,#6366F1 100%)',
  gradientHero: 'linear-gradient(135deg,#3730A3 0%,#0E7490 100%)',
}

// 间距（数字，单位 px）
export const spacing = { xs: 4, sm: 8, md: 12, lg: 16, xl: 20, xxl: 24, xxxl: 32 }

// 圆角
export const radii = { sm: 4, md: 8, lg: 12, xl: 16, xxl: 20, full: 999 }

// 阴影
export const shadows = {
  card: '0 2px 8px rgba(15, 23, 42, 0.06)',
  cardHover: '0 4px 16px rgba(15, 23, 42, 0.1)',
  floating: '0 8px 24px rgba(15, 23, 42, 0.12)',
  tabbar: '0 -1px 8px rgba(15, 23, 42, 0.04)',
}

// 字号（数字，单位 px）
export const fontSize = { xs: 11, sm: 12, base: 13, md: 14, lg: 16, xl: 18, xxl: 22, xxxl: 28 }

// ========== 暗黑模式基础设施 ==========
// 主题模式存储键
const THEME_MODE_KEY = 'app_theme_mode'

// 主题模式：'system'（跟随系统）| 'light'（强制亮色）| 'dark'（强制暗色）
export const themeMode = ref('system')
// 系统当前主题：'light' | 'dark'
export const systemTheme = ref('light')
// 是否处于暗黑模式（手动同步的 ref，供模板绑定使用）
export const isDark = ref(false)

// 根据当前模式与系统主题统一计算 isDark，并同步平台根节点主题类
function updateIsDark() {
  isDark.value = themeMode.value === 'dark' ||
    (themeMode.value === 'system' && systemTheme.value === 'dark')
  syncRootThemeClass()
}

// 同步主题类到平台根节点：
// 页面内容区的切换由各页面根节点 :class="isDark ? 'theme-dark' : ''" 响应式负责；
// H5 端 page 元素由框架渲染、无法通过模板绑类，需在 body 上同步，
// 配合 App.vue 中 body.theme-dark page 规则使 page 自身背景（overscroll 露出的底色）也切换
function syncRootThemeClass() {
  // #ifdef H5
  try {
    if (typeof document !== 'undefined' && document.body) {
      document.body.classList.toggle('theme-dark', isDark.value)
    }
  } catch (e) {
    // DOM 不可用时静默（不影响页面内容区主题）
  }
  // #endif
}

// 读取系统主题（优先 getAppBaseInfo，回退 getSystemInfoSync，默认 light）
function readSystemTheme() {
  try {
    if (typeof uni.getAppBaseInfo === 'function') {
      const baseInfo = uni.getAppBaseInfo()
      if (baseInfo && baseInfo.theme) return baseInfo.theme
    }
    const sysInfo = uni.getSystemInfoSync()
    if (sysInfo && sysInfo.theme) return sysInfo.theme
  } catch (e) {
    // 读取失败时静默回退为亮色
  }
  return 'light'
}

// onThemeChange 是否已注册（防止重复注册）
let themeChangeRegistered = false

// 初始化主题：App.onLaunch 时调用一次
export function initTheme() {
  // 读取本地保存的主题模式（非法值回退 'system'）
  try {
    const saved = uni.getStorageSync(THEME_MODE_KEY)
    if (saved === 'light' || saved === 'dark' || saved === 'system') {
      themeMode.value = saved
    }
  } catch (e) {
    // 读取失败保持默认 'system'
  }

  systemTheme.value = readSystemTheme()
  updateIsDark()

  // 监听系统主题变化（用标志位防止重复注册）
  if (!themeChangeRegistered && typeof uni.onThemeChange === 'function') {
    uni.onThemeChange((res) => {
      systemTheme.value = (res && res.theme) || 'light'
      updateIsDark()
    })
    themeChangeRegistered = true
  }
}

// 设置主题模式（'system' | 'light' | 'dark'）并持久化
export function setThemeMode(mode) {
  if (mode !== 'light' && mode !== 'dark' && mode !== 'system') return
  themeMode.value = mode
  try {
    uni.setStorageSync(THEME_MODE_KEY, mode)
  } catch (e) {
    // 写入失败不影响本次会话生效
  }
  updateIsDark()
}

// 在亮/暗之间切换：system 模式下按当前实际效果取反，并固化为显式模式
export function toggleTheme() {
  const next = isDark.value ? 'light' : 'dark'
  setThemeMode(next)
}

// 将原生导航栏前景/背景色同步为当前主题（H5 等平台不支持时静默失败）
export function applyNavBarTheme() {
  try {
    uni.setNavigationBarColor({
      frontColor: '#ffffff',
      backgroundColor: isDark.value ? '#1E293B' : '#4F46E5',
      fail: () => {}
    })
  } catch (e) {
    // 平台不支持 setNavigationBarColor 时静默
  }
}

export default { colors, spacing, radii, shadows, fontSize }
