<template>
  <div class="tags-view-container">
    <el-scrollbar class="tags-scrollbar">
      <div class="tags-wrapper">
        <div
          v-for="tag in appStore.tagsView"
          :key="tag.path"
          :class="['tag-item', { active: isActive(tag.path) }]"
          @click="handleClick(tag)"
          @contextmenu.prevent="openMenu(tag, $event)"
        >
          <span>{{ tag.title }}</span>
          <el-icon 
            v-if="tag.path !== '/dashboard'"
            class="close-icon"
            @click.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </div>
      </div>
    </el-scrollbar>
    
    <!-- 右键菜单 -->
    <ul v-show="menuVisible" 
        :style="{ left: menuLeft + 'px', top: menuTop + 'px' }"
        class="context-menu">
      <li @click="refreshSelectedTag(selectedTag)">刷新</li>
      <li v-if="selectedTag.path !== '/dashboard'" @click="closeTag(selectedTag)">关闭</li>
      <li @click="closeOtherTags(selectedTag)">关闭其他</li>
      <li @click="closeAllTags">关闭全部</li>
    </ul>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const menuVisible = ref(false)
const menuLeft = ref(0)
const menuTop = ref(0)
const selectedTag = ref({})

const isActive = (path) => route.path === path

const handleClick = (tag) => {
  router.push({ path: tag.path, query: tag.query })
}

const closeTag = (tag) => {
  appStore.removeTagView(tag.path)
  if (isActive(tag.path)) {
    const tags = appStore.tagsView
    if (tags.length > 0) {
      router.push(tags[tags.length - 1].path)
    } else {
      router.push('/dashboard')
    }
  }
}

const closeOtherTags = (tag) => {
  appStore.removeOtherTagViews(tag.path)
  router.push(tag.path)
  menuVisible.value = false
}

const closeAllTags = () => {
  appStore.removeAllTagViews()
  router.push('/dashboard')
  menuVisible.value = false
}

const refreshSelectedTag = (tag) => {
  router.replace({ path: '/redirect' + tag.path })
  menuVisible.value = false
}

const openMenu = (tag, e) => {
  selectedTag.value = tag
  const menuMinWidth = 105
  const offsetLeft = e.clientX
  const offsetWidth = e.target.offsetWidth
  const maxLeft = offsetWidth - menuMinWidth
  menuLeft.value = offsetLeft > maxLeft ? maxLeft : offsetLeft
  menuTop.value = e.clientY
  menuVisible.value = true
}

const closeMenu = () => {
  menuVisible.value = false
}

watch(() => route.path, () => {
  appStore.addTagView(route)
}, { immediate: true })

onMounted(() => {
  appStore.addTagView(route)
  document.body.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  document.body.removeEventListener('click', closeMenu)
})
</script>

<style scoped>
.tags-view-container {
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.04);
  padding: 4px 8px;
}

.tags-scrollbar {
  white-space: nowrap;
}

.tags-wrapper {
  display: flex;
  gap: 4px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  font-size: 12px;
  border: 1px solid #d8dce5;
  border-radius: 3px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.tag-item:hover {
  color: #409eff;
}

.tag-item.active {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}

.close-icon {
  font-size: 12px;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-icon:hover {
  background: rgba(255, 255, 255, 0.3);
}

.context-menu {
  position: fixed;
  background: #fff;
  z-index: 3000;
  list-style-type: none;
  padding: 5px 0;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #333;
  box-shadow: 2px 2px 12px rgba(0, 0, 0, 0.1);
}

.context-menu li {
  margin: 0;
  padding: 7px 16px;
  cursor: pointer;
}

.context-menu li:hover {
  background: #f0f0f0;
}
</style>