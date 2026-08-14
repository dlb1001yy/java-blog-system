<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <template v-if="!appStore.sidebarCollapsed">
          <span class="logo-text">Java 博客</span>
          <span class="logo-dot"></span>
        </template>
        <span v-else class="logo-text">JB</span>
      </div>
      <Sidebar />
    </el-aside>

    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <Header />
      </el-header>

      <!-- 主内容 -->
      <el-main class="main-content">
        <TagsView />
        <div class="page-container">
          <router-view v-slot="{ Component }">
            <transition name="slide-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import TagsView from './TagsView.vue'

const appStore = useAppStore()
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background: var(--gradient-sidebar);
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: var(--font-xl);
  font-weight: 700;
  background: rgba(255, 255, 255, 0.05);
  letter-spacing: 1px;
  white-space: nowrap;
  overflow: hidden;
}

.logo-text {
  display: inline-block;
}

.logo-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-left: var(--space-2);
  border-radius: var(--radius-full);
  background: var(--gradient-primary);
}

.header {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: var(--shadow-sm);
  z-index: 1;
  padding: 0;
  border-bottom: 1px solid var(--border-color);
}

.main-content {
  position: relative;
  background: var(--bg-page);
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-content::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 20%, rgba(5, 150, 105, 0.06), transparent 50%),
              radial-gradient(circle at 80% 80%, rgba(20, 184, 166, 0.05), transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.page-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-5);
  position: relative;
  z-index: 1;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
