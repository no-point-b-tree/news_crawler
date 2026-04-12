import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: "0.0.0.0",
    port: 4444,
    proxy: {
      '/api': {
        target: 'http://localhost:9009',
        changeOrigin: true
      }
    }
  }
});