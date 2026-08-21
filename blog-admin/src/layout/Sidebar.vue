<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapse"
    background-color="transparent"
    text-color="rgba(255,255,255,0.65)"
    active-text-color="#FFFFFF"
    router
    class="sidebar-menu"
  >
    <template v-for="group in menuGroups" :key="group.label">
      <div v-if="!isCollapse" class="menu-group-title">{{ group.label }}</div>
      <template v-for="item in group.items" :key="item.path">
        <el-menu-item :index="item.path">
          <el-icon v-if="item.icon">
            <component :is="item.icon" />
          </el-icon>
          <template #title>
            <span>{{ item.title }}</span>
            <el-badge
              v-if="item.path === '/marking' && pendingMarkingCount > 0"
              :value="pendingMarkingCount"
              :max="99"
              class="menu-badge"
            />
          </template>
        </el-menu-item>
      </template>
    </template>
  </el-menu>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import router from '@/router'
import dashboardApi from '@/api/dashboard'

const route = useRoute()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.sidebarCollapsed)

// 从路由表构建 path -> meta 索引
const routeMetaMap = computed(() => {
  const map = {}
  router.options.routes
    .find(r => r.path === '/')
    .children
    .forEach(r => {
      map['/' + r.path] = r.meta || {}
    })
  return map
})

const getRoute = (path) => ({
  path,
  title: routeMetaMap.value[path]?.title || path,
  icon: routeMetaMap.value[path]?.icon
})

// 分组定义：概览 / 内容管理 / 考试管理 / 系统管理
const menuGroups = computed(() => ([
  { label: '概览', items: [getRoute('/dashboard')] },
  {
    label: '内容管理',
    items: [
      getRoute('/article'),
      getRoute('/category'),
      getRoute('/tag'),
      getRoute('/interview-questions'),
      getRoute('/music')
    ]
  },
  {
    label: '考试管理',
    items: [
      getRoute('/exam-questions'),
      getRoute('/exam-papers'),
      getRoute('/marking')
    ]
  },
  {
    label: '系统管理',
    items: [
      getRoute('/comment'),
      getRoute('/message'),
      getRoute('/link'),
      getRoute('/users'),
      getRoute('/resume'),
      getRoute('/resumeManage'),
      getRoute('/operation-log'),
      getRoute('/settings')
    ]
  }
]))

const activeMenu = computed(() => route.path)

// 待阅卷数 badge
const pendingMarkingCount = ref(0)

onMounted(async () => {
  try {
    const res = await dashboardApi.getTodo()
    pendingMarkingCount.value = res.data?.pendingMarkingCount || 0
  } catch {
    // 静默失败，badge 不展示
  }
})
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
  padding: var(--space-2);
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.menu-group-title {
  padding: var(--space-3) var(--space-3) var(--space-1);
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 0.05em;
}

.menu-group-title:not(:first-child) {
  margin-top: var(--space-3);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: var(--space-3);
}

:deep(.el-menu-item) {
  border-radius: var(--radius-md);
  margin-bottom: var(--space-1);
  height: 44px;
  line-height: 44px;
  color: rgba(255, 255, 255, 0.65);
  transition: all var(--transition-base);
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: var(--shadow-primary);
  font-weight: 600;
}

:deep(.el-menu--collapse) .el-menu-item {
  width: 100%;
  border-radius: var(--radius-md);
}

.menu-badge {
  margin-left: var(--space-2);
}

:deep(.el-menu--collapse) .menu-badge {
  display: none;
}
</style>
