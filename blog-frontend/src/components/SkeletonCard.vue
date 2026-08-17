<template>
  <div class="skeleton-card card">
    <div class="article-layout">
      <div class="skeleton-cover shimmer"></div>
      <div class="skeleton-info">
        <div class="skeleton-bar meta-row shimmer"></div>
        <div class="skeleton-bar title shimmer"></div>
        <div class="skeleton-bar summary shimmer"></div>
        <div class="skeleton-bar summary short shimmer"></div>
        <div class="skeleton-bar meta-row shimmer"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
/* shimmer 动画在 global.css 中定义（@keyframes shimmer） */
.skeleton-card {
  cursor: default;
}

.article-layout {
  display: flex;
  gap: 20px;
}

.skeleton-cover {
  width: 200px;
  aspect-ratio: 20 / 13;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-bar {
  border-radius: var(--radius-sm);
  height: 14px;
}

.skeleton-bar.title {
  height: 20px;
  width: 60%;
}

.skeleton-bar.summary {
  width: 100%;
}

.skeleton-bar.summary.short {
  width: 75%;
}

.skeleton-bar.meta-row {
  width: 35%;
  height: 12px;
}

/* 脉冲波浪扫过效果 */
.shimmer {
  position: relative;
  overflow: hidden;
  background-color: #e8ebf0;
}

.shimmer::after {
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  animation: shimmer 1.5s infinite;
  content: '';
}

@media (max-width: 768px) {
  .skeleton-cover {
    width: 100%;
    aspect-ratio: 16 / 9;
  }

  .article-layout {
    flex-direction: column;
  }
}
</style>
