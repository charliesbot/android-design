import { defineConfig } from 'vite';

// The gallery frontend builds into the skill folder so the installed skill serves it with no npm step.
const target = 'http://127.0.0.1:8765';
export default defineConfig({
  root: 'skills/android-design/gallery/src',
  publicDir: 'static',
  build: {
    outDir: '../public',
    emptyOutDir: true,
    assetsInlineLimit: 0,
    modulePreload: { polyfill: false },
  },
  server: {
    proxy: {
      '/api': { target, changeOrigin: true },
      '/images': { target, changeOrigin: true },
    },
  },
});
