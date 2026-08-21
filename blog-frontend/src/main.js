import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { install as VueMonacoEditorPlugin, loader } from '@guolao/vue-monaco-editor'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'highlight.js/styles/github-dark.css'
import 'github-markdown-css'

import App from './App.vue'
import router from './router'
import './assets/styles/global.css'
import './assets/styles/variables.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Monaco Editor 通过 CDN 加载，避免打包体积过大
loader.config({
  paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.45.0/min/vs' }
})

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(VueMonacoEditorPlugin)

app.mount('#app')
