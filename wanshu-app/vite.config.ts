import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiBase = env.VUE_APP_API_BASE || 'http://127.0.0.1:8080/api'
  return {
    plugins: [uni()],
    define: {
      'process.env.VUE_APP_API_BASE': JSON.stringify(apiBase),
    },
  }
})
