import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  // 获取网关地址，优先使用环境变量，否则使用默认值
  const gatewayUrl = env.VITE_GATEWAY_URL || 'http://localhost:30010'
  
  console.log('🌐 Gateway URL:', gatewayUrl)
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 3000,
      open: true,
      proxy: {
        '/api': {
          target: gatewayUrl,
          changeOrigin: true,
          // 不重写路径，保留 /api 前缀以匹配网关路由
          // rewrite: (path) => path.replace(/^\/api/, ''),
          configure: (proxy, options) => {
            proxy.on('error', (err, req, res) => {
              console.log('proxy error', err)
            })
            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log('Sending Request to the Target:', req.method, gatewayUrl + req.url)
            })
            proxy.on('proxyRes', (proxyRes, req, res) => {
              console.log('Received Response from the Target:', proxyRes.statusCode, gatewayUrl + req.url)
            })
          }
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: false
    }
  }
})
