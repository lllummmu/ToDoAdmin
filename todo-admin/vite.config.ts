import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端部署在 Tomcat 的 /TodoApp 上下文，JSESSIONID 默认带 Path=/TodoApp。
// 经代理后浏览器不会把它发回 /api，因此统一改写为 Path=/，保证会话在 5173 下可用。
function rewriteSessionCookie(proxyRes: { headers: Record<string, string[] | undefined> }) {
  const cookies = proxyRes.headers['set-cookie']
  if (Array.isArray(cookies)) {
    proxyRes.headers['set-cookie'] = cookies.map((c) => c.replace(/Path=\/TodoApp\/?/i, 'Path=/'))
  }
}

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081/TodoApp',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', rewriteSessionCookie)
        }
      },
      '/uploads': {
        target: 'http://localhost:8081/TodoApp',
        changeOrigin: true
      }
    }
  }
})
