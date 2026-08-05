<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <div class="title-bar"></div>
        <div class="header-text">
          <h2 class="page-title">{{ title }}</h2>
          <p v-if="description" class="page-desc">{{ description }}</p>
        </div>
      </div>
      <div v-if="$slots.action" class="header-action">
        <slot name="action" />
      </div>
    </div>
    <div class="page-body">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.title-bar {
  width: 4px;
  height: 32px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-size: var(--font-2xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  margin: 0;
}

.page-desc {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin: 0;
}

.header-action {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
</style>
