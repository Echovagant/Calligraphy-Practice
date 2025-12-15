import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import './assets/main.css'
import router from './router'

// 创建Vue应用实例
const app = createApp(App)

// 创建并使用Pinia状态管理
const pinia = createPinia()

// 注册插件
app.use(ElementPlus)
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')
