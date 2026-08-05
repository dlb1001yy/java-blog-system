<template>
  <div class="header-container">
    <div class="header-left">
      <el-icon
        class="collapse-btn"
        @click="appStore.toggleSidebar"
      >
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <el-tooltip content="刷新页面" placement="bottom">
        <el-icon class="header-icon" @click="handleRefresh">
          <Refresh />
        </el-icon>
      </el-tooltip>

      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="header-icon" @click="toggleFullscreen">
          <FullScreen />
        </el-icon>
      </el-tooltip>

      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" :src="userStore.userInfo.avatar">
            {{ userStore.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <span class="username">{{ userStore.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold, Expand, ArrowDown, Refresh, FullScreen,
  User, Setting, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const currentTitle = computed(() => route.meta?.title || '')

const handleRefresh = () => {
  window.location.reload()
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      }).catch(() => {})
      break
    case 'settings':
      router.push('/settings')
      break
    case 'profile':
      ElMessage.info('个人中心功能开发中...')
      break
  }
}
</script>

<style scoped>
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 var(--space-6);
  background: transparent;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-regular);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.collapse-btn:hover {
  background: var(--bg-subtle);
  color: var(--color-primary);
}

:deep(.el-breadcrumb__item) {
  font-size: var(--font-base);
}

:deep(.el-breadcrumb__inner.is-link) {
  color: var(--text-regular);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.header-icon {
  font-size: 18px;
  cursor: pointer;
  color: var(--text-regular);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.header-icon:hover {
  background: var(--bg-subtle);
  color: var(--color-primary);
  transform: translateY(-1px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  transition: all var(--transition-base);
}

.user-info:hover {
  background: var(--bg-subtle);
}

.username {
  font-size: var(--font-base);
  color: var(--text-primary);
  font-weight: 500;
}

:deep(.el-avatar) {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.3);
}
</style>
