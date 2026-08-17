import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const tagsView = ref([])
  const cachedViews = ref([])
  const theme = ref('light')

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const applyTheme = (t, persist = true) => {
    theme.value = t
    document.documentElement.classList.toggle('dark', t === 'dark')
    if (persist) {
      localStorage.setItem('blog-admin-theme', t)
    }
  }

  const initTheme = () => {
    const saved = localStorage.getItem('blog-admin-theme')
    if (saved === 'light' || saved === 'dark') {
      applyTheme(saved)
    } else {
      // 首次访问跟随系统偏好；不持久化，用户手动选择前每次启动都跟随系统变化
      applyTheme(window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light', false)
    }
  }

  // 切换主题：支持 View Transitions 时以点击坐标为圆心做圆形扩散动画
  const toggleTheme = (event) => {
    const target = theme.value === 'dark' ? 'light' : 'dark'

    // 渐进增强：不支持 View Transitions 或用户开启"减少动态效果"时直接切换
    const supported = typeof document.startViewTransition === 'function' &&
      !window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (!supported) {
      applyTheme(target)
      return
    }

    // 圆心取点击坐标，无事件对象时兜底为视口中心
    const x = event?.clientX ?? window.innerWidth / 2
    const y = event?.clientY ?? window.innerHeight / 2

    // 点击位置到屏幕四个角的最远距离，保证扩散圆覆盖全屏
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    )

    // 用 View Transitions 包裹主题切换，浏览器对切换前后页面拍照生成快照
    const transition = document.startViewTransition(() => {
      applyTheme(target)
    })

    // 快照就绪后，对新快照伪元素执行 clip-path 圆形扩散 Keyframe 动画
    transition.ready.then(() => {
      document.documentElement.animate(
        {
          clipPath: [
            `circle(0px at ${x}px ${y}px)`,
            `circle(${endRadius}px at ${x}px ${y}px)`
          ]
        },
        {
          duration: 500,
          easing: 'ease-in-out',
          pseudoElement: '::view-transition-new(root)'
        }
      )
    }).catch(() => {})
  }

  const addTagView = (route) => {
    if (route.name && !tagsView.value.find(item => item.path === route.path)) {
      tagsView.value.push({
        name: route.name,
        path: route.path,
        title: route.meta?.title || '未命名',
        query: route.query
      })
    }
  }

  const removeTagView = (path) => {
    const index = tagsView.value.findIndex(item => item.path === path)
    if (index > -1) {
      tagsView.value.splice(index, 1)
    }
  }

  const removeOtherTagViews = (path) => {
    tagsView.value = tagsView.value.filter(item => item.path === path)
  }

  const removeAllTagViews = () => {
    tagsView.value = []
  }

  return {
    sidebarCollapsed,
    tagsView,
    cachedViews,
    theme,
    toggleSidebar,
    initTheme,
    toggleTheme,
    addTagView,
    removeTagView,
    removeOtherTagViews,
    removeAllTagViews
  }
})