import { defineConfig } from 'vite';
import { applyStartupTheme, themeFromHTML } from './skills/android-design/gallery/startup-theme.mjs';

// The gallery frontend builds into the skill folder so the installed skill serves it with no npm step.
const target = 'http://127.0.0.1:8765';
export default defineConfig({
  root: 'skills/android-design/gallery/src',
  plugins: [{
    name: 'gallery-startup-theme',
    apply: 'serve',
    async transformIndexHtml(html) {
      try {
        const response = await fetch(`${target}/`, { signal: AbortSignal.timeout(3000) });
        if (!response.ok) throw new Error('Gallery unavailable');
        return applyStartupTheme(html, themeFromHTML(await response.text()));
      } catch {
        // An unavailable gallery keeps the ordinary loading shell, not a blocked page.
        return html;
      }
    },
  }],
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
      '/runtime': { target, changeOrigin: true },
      '/rounds': { target, changeOrigin: true },
      '/draft': { target, changeOrigin: true },
    },
  },
});
