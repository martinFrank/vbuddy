import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/vbuddy/',
  server: {
    port: 5173,
    proxy: {
      '/vbuddy/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
