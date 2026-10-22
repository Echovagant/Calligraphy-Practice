import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './assets/main.css'
import router from './router'

// ElMessage / ElMessageBox 是 JS API，组件 resolver 不会给它们带样式，
// 这里显式引一次。各视图里保留显式的 import { ElMessage } from 'element-plus'，
// 摇树后只保留这两个 API。
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'

// 创建Vue应用实例
const app = createApp(App)

// 创建并使用Pinia状态管理
const pinia = createPinia()

// 注册插件。
// Element Plus 不再全量注册 —— 组件由 unplugin-vue-components 按需引入；
// 中文 locale 由 App.vue 根部的 <el-config-provider :locale="zhCn"> 提供，
// 否则分页器会显示 Element Plus 默认的英文（Total / Go to）。
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')
