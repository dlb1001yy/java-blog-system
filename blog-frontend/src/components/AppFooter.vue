<template>
  <footer class="app-footer">
    <div class="footer-container">
      <p>© {{ new Date().getFullYear() }} Java码农笔记. All Rights Reserved.</p>
      <p>Powered by Vue3 & Spring Boot</p>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.app-footer {
  background: #fff;
  border-top: 1px solid var(--border-color);
  padding: 24px 0;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

.footer-container {
  width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.app-footer p {
  margin-bottom: 4px;
}
</style>
