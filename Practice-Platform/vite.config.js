import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { visualizer } from 'rollup-plugin-visualizer'

// https://vite.dev/config/
export default defineConfig(async ({ mode }) => ({
  plugins: [
    vue(),
    // 开发工具插件只在开发模式下启用
    mode === 'development' ? (await import('vite-plugin-vue-devtools')).default() : null,
    // 构建分析插件
    mode === 'analyze' ? visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true,
      filename: 'dist/stats.html'
    }) : null
  ].filter(Boolean),
  server: {
    host: '0.0.0.0',
    port: 5174,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true
      }
    }
  },
  build: {
    // 构建优化配置
    target: 'es2015',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info', 'console.warn']
      },
      format: {
        comments: false
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          elementPlus: ['element-plus', '@element-plus/icons-vue'],
          vendor: ['@vueuse/core', 'three', 'fabric']
        },
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    },
    // 构建性能优化
    brotliSize: false,
    chunkSizeWarningLimit: 1000,
    reportCompressedSize: true,
    cssTarget: 'chrome61',
    sourcemap: mode === 'development'
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: ['vue', 'element-plus', '@element-plus/icons-vue', 'three', 'fabric'],
    exclude: ['@vueuse/core']
  },
  esbuild: {
    drop: mode === 'production' ? ['console', 'debugger'] : [],
    pure: mode === 'production' ? ['console.log', 'console.info', 'console.warn'] : []
  }
}))
