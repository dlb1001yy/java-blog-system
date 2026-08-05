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
  background: transparent;
  border-bottom: 1px solid var(--border-color);
  padding: var(--space-2) var(--space-4);
}

.tags-scrollbar {
  white-space: nowrap;
}

.tags-wrapper {
  display: flex;
  gap: var(--space-2);
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  font-size: var(--font-xs);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-base);
  background: var(--bg-subtle);
  color: var(--text-regular);
  font-weight: 500;
}

.tag-item:hover {
  color: var(--color-primary);
  background: var(--el-color-primary-light-9);
}

.tag-item.active {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: var(--shadow-primary);
}

.close-icon {
  font-size: 12px;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  width: 14px;
  height: 14px;
}

.close-icon:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.context-menu {
  position: fixed;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
  padding: var(--space-1) 0;
  font-size: var(--font-sm);
  overflow: hidden;
  z-index: 3000;
  list-style-type: none;
}

.context-menu li {
  padding: var(--space-2) var(--space-4);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-regular);
}

.context-menu li:hover {
  background: var(--bg-subtle);
  color: var(--color-primary);
}
</style>
