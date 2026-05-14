import { createApp } from 'vue'
import { createPinia } from 'pinia'
import TDesign from 'tdesign-vue-next'
import App from './App.vue'
import router from './router'
import 'tdesign-vue-next/dist/tdesign.css'
import './styles/main.css'

// 修复滚动事件被动监听器警告 - 在 Vue 初始化前执行
const fixPassiveEvents = () => {
  const blockingEvents = ['wheel', 'mousewheel', 'touchstart', 'touchmove', 'touchend', 'scroll', 'DOMMouseScroll']
  const originalAddEventListener = EventTarget.prototype.addEventListener
  
  EventTarget.prototype.addEventListener = function(type, listener, options) {
    if (blockingEvents.includes(type)) {
      if (typeof options === 'object' && options !== null && !options.passive && !options.capture) {
        options = { ...options, passive: true }
      } else if (options === undefined || options === false) {
        options = { passive: true }
      }
    }
    return originalAddEventListener.call(this, type, listener, options)
  }
}

// 立即执行，不等待 DOM
fixPassiveEvents()

const app = createApp(App)
app.use(createPinia())
app.use(TDesign)
app.use(router)
app.mount('#app')
