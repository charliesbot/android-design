import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const SRC = new URL('../../skills/android-design-explore/gallery/runtime-src/', import.meta.url);
const sizes = JSON.parse(readFileSync(new URL('icon-sizes.json', SRC), 'utf8')).sizes;
const css = readFileSync(new URL('runtime.css', SRC), 'utf8');

test('runtime.css sizes p-icon for every Compose icon size the lint allows', () => {
  for (const size of sizes) {
    assert.match(css, new RegExp(`p-icon\\[size="${size}"\\]\\s*\\{\\s*font-size:\\s*${size}px;?\\s*\\}`), `size ${size}`);
  }
});

test('the runtime draws icons and text at the font default optical size, as Compose does', () => {
  assert.match(css, /font-optical-sizing:\s*none/);
});
