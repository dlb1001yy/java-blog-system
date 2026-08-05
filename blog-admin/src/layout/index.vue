<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!appStore.sidebarCollapsed">Java博客管理</span>
        <span v-else>JB</span>
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
            <transition name="fade" mode="out-in">
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
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  background-color: #2b3a4d;
  white-space: nowrap;
}
.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 1;
  padding: 0;
}
.main-content {
  background: #f0f2f5;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.page-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>