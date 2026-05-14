import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 修复 Vue 3 wheel 事件被动监听器警告的插件
const passiveEventsPlugin = () => {
  return {
    name: 'vite-plugin-passive-events',
    transformIndexHtml: (html: string) => {
      return html.replace(
        '<head>',
        `<head>
        <script>
          // 全局修复被动事件监听器警告
          (function() {
            const addEventListener = EventTarget.prototype.addEventListener;
            EventTarget.prototype.addEventListener = function(type, listener, options) {
              const blockingEvents = ['wheel', 'mousewheel', 'touchstart', 'touchmove', 'touchend', 'scroll', 'DOMMouseScroll'];
              if (blockingEvents.includes(type)) {
                if (typeof options === 'object' && options !== null && !options.passive && !options.capture) {
                  options = { ...options, passive: true };
                } else if (options === undefined || options === false) {
                  options = { passive: true };
                }
              }
              return addEventListener.call(this, type, listener, options);
            };
          })();
        </script>`
      )
    }
  }
}

export default defineConfig({
  plugins: [
    vue(),
    passiveEventsPlugin(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  optimizeDeps: {
    include: ['axios', 'vue', 'vue-router', 'pinia', 'tdesign-icons-view'],
    exclude: ['tdesign-vue-next'],
  },
  server: {
    port: 8082,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})
