import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import './styles/index.scss'

// 加载浏览器兼容性Polyfills
import './utils/polyfills'

// 检测浏览器兼容性
import { showBrowserWarning } from './utils/browserDetector'

// 在应用挂载前进行浏览器检测
showBrowserWarning()

// 注册PWA Service Worker
import { registerServiceWorker, requestNotificationPermission } from './utils/pwa'

const app = createApp(App)
const pinia = createPinia()

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')

// 注册Service Worker和请求通知权限
registerServiceWorker()
requestNotificationPermission()

const app = createApp(App)
const pinia = createPinia()

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')
