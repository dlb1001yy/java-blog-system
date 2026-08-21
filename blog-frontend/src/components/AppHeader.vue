<template>
  <header class="app-header">
    <div class="header-container">
      <div class="logo" @click="$router.push('/')">
        <LogoIcon :size="32" />
        <span class="logo-text">Java码农笔记</span>
      </div>

      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/articles" class="nav-item">文章</router-link>
        <router-link to="/interview" class="nav-item">刷题</router-link>
        <router-link to="/exam" class="nav-item">考试</router-link>
        <router-link to="/scores" class="nav-item">成绩</router-link>
        <router-link to="/music" class="nav-item">音乐</router-link>
        <router-link to="/resume" class="nav-item">简历</router-link>
        <router-link to="/about-site" class="nav-item">关于站点</router-link>
        <router-link v-if="userStore.token" to="/profile/resume" class="nav-item">我的简历</router-link>
      </nav>

      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章..."
          :prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
        />
        <!-- 未登录：登录/注册入口 -->
        <template v-if="!userStore.token">
          <router-link to="/login" class="auth-link">登录</router-link>
          <router-link to="/register" class="auth-link auth-register">注册</router-link>
        </template>
        <!-- 已登录：用户名 + 退出 -->
        <template v-else>
          <span class="username" :title="displayName">{{ displayName }}</span>
          <el-button link type="danger" size="small" @click="handleLogout">退出</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import LogoIcon from './LogoIcon.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

const displayName = computed(() =>
  userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
)

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/articles', query: { keyword: searchKeyword.value } })
  }
}
</script>

<style scoped>
.app-header {
  background: var(--header-bg);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  backdrop-filter: blur(12px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 1000;
}

/* 不支持 backdrop-filter 的浏览器降级为不透明背景 */
@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  .app-header {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.header-container {
  width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  white-space: nowrap;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
}

.nav-menu {
  display: flex;
  gap: 8px;
  flex: 1;
  margin-left: 48px;
}

.nav-item {
  padding: 8px 16px;
  color: var(--text-regular);
  border-radius: var(--radius-sm);
  transition: all 0.3s;
  font-weight: 500;
  font-size: 15px;
  text-decoration: none;
}

.nav-item:hover {
  color: var(--primary-color);
  background: rgba(64, 158, 255, 0.08);
}

.nav-item.router-link-exact-active {
  color: var(--primary-color);
  background: rgba(64, 158, 255, 0.1);
}

.header-right {
  width: 320px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  flex: 1;
  min-width: 140px;
}

.auth-link {
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
  text-decoration: none;
  padding: 4px 8px;
}

.auth-link:hover {
  color: var(--primary-color);
}

.auth-register {
  color: var(--primary-color);
  font-weight: 600;
}

.username {
  white-space: nowrap;
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  color: var(--text-regular);
}

@media (max-width: 768px) {
  .header-container {
    flex-wrap: wrap;
    height: auto;
    padding: 12px 20px;
    width: 100%;
  }

  .nav-menu {
    overflow-x: auto;
    flex-wrap: nowrap;
    width: 100%;
    margin-left: 0;
    order: 3;
  }

  .nav-item {
    white-space: nowrap;
  }

  .header-right {
    width: 100%;
    order: 4;
  }
}
</style>
