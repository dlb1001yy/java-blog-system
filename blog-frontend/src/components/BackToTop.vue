<template>
  <transition name="fade">
    <div v-if="visible" class="back-to-top" @click="scrollToTop">
      <el-icon :size="20"><Top /></el-icon>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Top } from '@element-plus/icons-vue'

const visible = ref(false)

const handleScroll = () => { visible.value = window.scrollY > 300 }
const scrollToTop = () => { window.scrollTo({ top: 0, behavior: 'smooth' }) }

onMounted(() => window.addEventListener('scroll', handleScroll))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.back-to-top {
  position: fixed; bottom: 40px; right: 40px;
  width: 44px; height: 44px;
  background: var(--primary-color); color: #fff;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  z-index: 999; transition: transform 0.3s;
}
.back-to-top:hover { transform: scale(1.1); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>