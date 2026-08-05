<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapse"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    router
    class="sidebar-menu"
  >
    <template v-for="route in menuRoutes" :key="route.path">
      <el-menu-item :index="route.path">
        <el-icon v-if="route.meta?.icon">
          <component :is="route.meta.icon" />
        </el-icon>
        <template #title>{{ route.meta?.title }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import router from '@/router'

const route = useRoute()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.sidebarCollapsed)

const menuRoutes = computed(() => {
  return router.options.routes
    .find(r => r.path === '/')
    .children
    .filter(r => !r.meta?.hidden)
    .map(r => ({
      ...r,
      path: '/' + r.path
    }))
})

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}
</style>