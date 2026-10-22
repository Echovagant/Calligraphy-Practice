import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import App from './App.vue'
import './assets/main.css'
import router from './router'

// 创建Vue应用实例
const app = createApp(App)

// 创建并使用Pinia状态管理
const pinia = createPinia()

// 注册插件。Element Plus 默认是英文（分页会显示 Total / Go to），
// 站点其余文案都是中文，不配 locale 会出现中英夹杂。
app.use(ElementPlus, { locale: zhCn })
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')
