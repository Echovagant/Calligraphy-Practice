import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { visualizer } from 'rollup-plugin-visualizer'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig(async ({ mode }) => ({
  plugins: [
    vue(),
    // Element Plus 按需引入：模板里用到的 el-* 组件自动注册，并各自带样式。
    // 不要再 app.use(ElementPlus) 全量注册 —— 那是首包 860KB JS + 346KB CSS 的根源。
    Components({
      // dts: false —— 不生成 components.d.ts。
      // 它默认落在项目根目录，而 tsconfig.json 的 include 只覆盖 src/**，
      // 生成的声明文件根本不会被类型检查引用，只是个没人维护的产物。
      dts: false,
      resolvers: [
        ElementPlusResolver({ importStyle: 'css' })
      ]
    }),
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
        // 注意：这里只列「整包都要」的依赖。
        // element-plus 与 @element-plus/icons-vue **不能**列进来 ——
        // manualChunks 的对象写法会把列进去的模块当成 chunk 的入口模块，
        // 它的导出被保留，摇树就失效了：整个 Element Plus（860KB）会原样进包。
        // 这两个包交给 Rollup 按使用处自动分块：
        // 入口用得到的（ElMessage / ElConfigProvider）留在首屏，
        // 其余随所属视图懒加载。
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          // fabric 只被 PracticeView 用（路由懒加载），放进 vendor 会让首页也要下载它
          vendor: ['@vueuse/core', 'three']
        },
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    },
    // 构建性能优化
    brotliSize: false,
    // 别为了消警告把它调大 —— 超标要么真去掉，要么就让它报出来
    chunkSizeWarningLimit: 500,
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
