import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/stats': 'http://localhost:8080',
      '/task': 'http://localhost:8080',
      '/face-config': 'http://localhost:8080',
    },
  },
})
