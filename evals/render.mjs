// Renders one published HTML proposal into the fixed eval screenshots and runs the in-page rule checks, with
// no Android SDK. Usage: node evals/render.mjs --url http://127.0.0.1:8790/rounds/001/a/ --out <dir>
// Writes <name>.png per render plus review.json. Exits 0 when rendering worked, whether or not the checks pass.

import { existsSync } from 'node:fs';
import { mkdir, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { parseArgs } from 'node:util';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';

const PHONE = [411, 923];
// One row per render. `color` stands in for a red dynamic-color wallpaper.
export const RENDERS = [
  { name: 'light', viewport: PHONE, scheme: 'light' },
  { name: 'dark', viewport: PHONE, scheme: 'dark' },
  { name: 'wallpaper', viewport: PHONE, scheme: 'light', color: '#b3261e' },
  { name: 'foldable', viewport: [933, 704], scheme: 'light' },
  { name: 'tablet', viewport: [1280, 800], scheme: 'light' },
  { name: 'desktop', viewport: [1920, 1080], scheme: 'light' },
];

const DEFAULT_CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';

export function summarize(url, renders) {
  return {
    url,
    renders,
    pass: renders.every(r => r.result.pass === true),
    complete: renders.every(r => r.result.complete === true),
  };
}

// Runs in the page: apply the theme, wait for fonts, two frames, a stable primary color, and no running transitions, then run the checks.
async function prepare({ scheme, color }) {
  const theme = document.querySelector('m3e-theme');
  if (theme) {
    theme.setAttribute('scheme', scheme);
    if (color) theme.setAttribute('color', color);
  }
  const frames = () => new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)));
  const primary = () => getComputedStyle(document.documentElement).getPropertyValue('--md-sys-color-primary');
  await document.fonts.ready;
  await frames();
  // The theme recomputes its tokens asynchronously: wait until the value holds for three frames (max ~2s).
  let last = primary();
  let stable = 0;
  for (let i = 0; i < 120 && stable < 3; i++) {
    await frames();
    const now = primary();
    stable = now === last ? stable + 1 : 0;
    last = now;
  }
  if (typeof window.runtimeChecks !== 'function') {
    return { pass: false, complete: false, error: 'runtimeChecks is missing: the page does not load /runtime/m3e.js' };
  }
  await window.runtimeSettle();
  return window.runtimeChecks();
}

export function chromeExecutable() {
  return process.env.CHROME_PATH || DEFAULT_CHROME;
}

// Renders `url` once per RENDERS row. Pass an open `browser` to reuse it (the caller closes it); without one a
// browser is launched and closed here.
export async function renderAll(url, out, chromePath, browser) {
  const owned = !browser;
  browser ??= await puppeteer.launch({ executablePath: chromePath, headless: true });
  try {
    const page = await browser.newPage();
    const renders = [];
    for (const spec of RENDERS) {
      const [width, height] = spec.viewport;
      await page.setViewport({ width, height, deviceScaleFactor: 1 });
      await page.goto(url, { waitUntil: 'load' });
      const result = await page.evaluate(prepare, spec);
      await page.screenshot({ path: join(out, `${spec.name}.png`), fullPage: false });
      renders.push({ name: spec.name, viewport: spec.viewport, result });
    }
    await page.close();
    return renders;
  } finally {
    if (owned) await browser.close();
  }
}

async function main() {
  const { values } = parseArgs({ options: { url: { type: 'string' }, out: { type: 'string' } } });
  if (!values.url || !values.out) throw new Error('usage: node evals/render.mjs --url <proposal page URL> --out <dir>');
  const chromePath = chromeExecutable();
  if (!existsSync(chromePath)) throw new Error(`Chrome not found at ${chromePath}. Install Google Chrome or set CHROME_PATH.`);
  await mkdir(values.out, { recursive: true });
  const review = summarize(values.url, await renderAll(values.url, values.out, chromePath));
  await writeFile(join(values.out, 'review.json'), `${JSON.stringify(review, null, 2)}\n`);
  console.log(`rendered ${review.renders.length} screenshots to ${values.out} (pass: ${review.pass}, complete: ${review.complete})`);
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main().catch(error => {
    console.error(`render failed: ${error.message}`);
    process.exit(1);
  });
}
