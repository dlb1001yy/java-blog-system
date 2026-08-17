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

  const toggleTheme = () => {
    applyTheme(theme.value === 'dark' ? 'light' : 'dark')
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