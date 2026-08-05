// 设计令牌系统：集中定义颜色、间距、圆角、阴影、字号
// 与 uni.scss 中的 SCSS 变量保持同步

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

export default { colors, spacing, radii, shadows, fontSize }
