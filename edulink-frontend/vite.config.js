import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import {fileURLToPath} from 'url'
import {visualizer} from 'rollup-plugin-visualizer'


const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig({
    build: {
        rollupOptions: {
            output: {
                manualChunks: {
                    elementPlus: ['element-plus'],
                    vueVendor: ['vue', 'vue-router', 'pinia']
                }
            }
        },
        minify: 'terser',
        terserOptions: {compress: {drop_console: true}}
    },
    plugins: [vue(),
        visualizer({open: true, filename: 'dist/stats.html'})
    ],
    define: {
        global: 'globalThis',      // 解决 "global is not defined" 错误
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
})