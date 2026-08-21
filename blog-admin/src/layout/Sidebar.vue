<template>
  <el-scrollbar class="menu-scrollbar">
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      background-color="transparent"
      text-color="rgba(255,255,255,0.65)"
      active-text-color="#FFFFFF"
      router
      class="sidebar-menu"
      :unique-opened="false"
    >
      <template v-for="group in menuGroups" :key="group.label">
        <!-- 无子菜单的顶层项（概览） -->
        <el-menu-item v-if="!group.items || group.items.length <= 1" :index="group.items[0].path">
          <el-icon v-if="group.items[0].icon">
            <component :is="group.items[0].icon" />
          </el-icon>
          <template #title>
            <span>{{ group.items[0].title }}</span>
          </template>
        </el-menu-item>
        <!-- 可折叠分组 -->
        <el-sub-menu v-else :index="group.label">
          <template #title>
            <el-icon v-if="group.icon">
              <component :is="group.icon" />
            </el-icon>
            <span>{{ group.label }}</span>
          </template>
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
        </el-sub-menu>
      </template>
    </el-menu>
  </el-scrollbar>
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
    icon: 'Files',
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
    icon: 'Tickets',
    items: [
      getRoute('/exam-questions'),
      getRoute('/exam-papers'),
      getRoute('/marking')
    ]
  },
  {
    label: '系统管理',
    icon: 'Setting',
    items: [
      getRoute('/comment'),
      getRoute('/message'),
      getRoute('/link'),
      getRoute('/users'),
      // 简历管理暂时屏蔽
      // getRoute('/resume'),
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
.menu-scrollbar {
  flex: 1;
  min-height: 0;
}

.sidebar-menu {
  border-right: none;
  padding: var(--space-2);
  width: 100%;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  border-radius: var(--radius-md);
  margin-bottom: var(--space-1);
  height: 44px;
  line-height: 44px;
  color: rgba(255, 255, 255, 0.65);
  transition: all var(--transition-base);
}

:deep(.el-sub-menu .el-menu) {
  background: transparent;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
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
