import { defineConfig } from 'vite';
import { runtimeAssets } from './skills/android-design/gallery/runtime-src/assets.mjs';

// The proposal runtime builds into gallery/public/runtime/ with unhashed names so proposals can link it.
export default defineConfig({
  root: 'skills/android-design/gallery/runtime-src',
  publicDir: false,
  plugins: [runtimeAssets()],
  build: {
    outDir: '../public/runtime',
    emptyOutDir: true,
    assetsInlineLimit: 0,
    copyPublicDir: false,
    lib: { entry: 'm3e.js', formats: ['es'], fileName: () => 'm3e.js' },
  },
});
