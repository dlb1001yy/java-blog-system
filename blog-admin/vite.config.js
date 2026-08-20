import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  base: '/admin/',
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // MinIO 上传文件（/uploads/ 相对前缀）开发环境代理到服务器 nginx 反代；
      // 若 8081 未对公网开放，备选直连：target 'http://gz.aeert.com:9000' + rewrite 去掉 /uploads 前缀换 /blog
      '/uploads': {
        target: 'http://gz.aeert.com:8081',
        changeOrigin: true
      }
    }
  }
})