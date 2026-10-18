import { mkdtempSync, mkdirSync, writeFileSync, rmSync, realpathSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

export const PAGE = '<!doctype html><html><head><link rel="stylesheet" href="/runtime/runtime.css"><link rel="stylesheet" href="style.css"><script type="module" src="/runtime/m3e.js"></script></head><body><h1 class="t-headline-large">Hello</h1></body></html>\n';
export const STYLE = '/* static colors */\n:root { --static-tomato: #D84315; }\nbody { color: var(--md-sys-color-on-surface); }\n';

// Writes a flat proposal folder: index.html, style.css, and any extra files.
export function writeProposal(folder, { page = PAGE, style = STYLE, files = {} } = {}) {
  mkdirSync(folder, { recursive: true });
  writeFileSync(join(folder, 'index.html'), page);
  writeFileSync(join(folder, 'style.css'), style);
  for (const [name, text] of Object.entries(files)) writeFileSync(join(folder, name), text);
  return folder;
}

export function workspace(t) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'gallery-test-')));
  t.after(() => rmSync(root, { recursive: true, force: true }));
  return root;
}
