import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端自己启动（默认 http://127.0.0.1:8080）。
// 开发时 vite 把 /api 代理到后端，前端无需关心跨域。
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist'
  }
})
