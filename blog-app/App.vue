<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    // 检查更新逻辑可在此处添加
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
/* 全局变量 */
@import '@/uni.scss';

page {
  background-color: #f5f7fa;
  font-size: 14px;
  color: #303133;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 全局样式重置 */
view, text, image, button, input, textarea {
  box-sizing: border-box;
}

button {
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  &::after {
    border: none;
  }
}
</style>
