import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './styles/tokens.css'
import './styles/app.css'
import App from './App.vue'

// 暗色模式：html.dark 由 tokens.css 的 :root, html.dark 选择器接管全部变量
document.documentElement.classList.add('dark')

createApp(App).use(ElementPlus, { locale: zhCn }).mount('#app')
