<template>
  <div id="app">
    <template v-if="hideLayout">
      <router-view />
    </template>
    <template v-else>
      <AppHeader />
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <AppFooter />
      <PlayerBar />
      <BackToTop />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import BackToTop from '@/components/BackToTop.vue'
import PlayerBar from '@/components/PlayerBar.vue'

const route = useRoute()
const hideLayout = computed(() => !!route.meta.hideLayout)
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 24px;
  padding-bottom: 40px;
}

/* 全局播放条显示时为主内容留出空间（播放条 72px + 间距） */
body:has(.player-bar) .main-content {
  padding-bottom: 88px;
}
</style>
