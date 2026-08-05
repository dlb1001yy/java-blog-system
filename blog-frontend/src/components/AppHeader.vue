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
        <router-link to="/category" class="nav-item">分类</router-link>
        <router-link to="/tags" class="nav-item">标签</router-link>
        <router-link to="/archives" class="nav-item">归档</router-link>
        <router-link to="/resume" class="nav-item">简历</router-link>
        <router-link to="/messages" class="nav-item">留言</router-link>
        <router-link to="/about" class="nav-item">关于</router-link>
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
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import LogoIcon from './LogoIcon.vue'

const router = useRouter()
const searchKeyword = ref('')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/articles', query: { keyword: searchKeyword.value } })
  }
}
</script>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
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
  width: 220px;
}

.search-input {
  width: 100%;
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
