import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false, // Disable source maps
  },
  // Add source map configuration
  define: {
    __VUE_PROD_DEVTOOLS__: false,
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia'],
  },
})
