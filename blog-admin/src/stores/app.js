import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const tagsView = ref([])
  const cachedViews = ref([])

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
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
    toggleSidebar,
    addTagView,
    removeTagView,
    removeOtherTagViews,
    removeAllTagViews
  }
})