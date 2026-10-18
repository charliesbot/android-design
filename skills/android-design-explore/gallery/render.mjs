// `render`: lint a proposal folder, then render it headlessly in the system Chrome and write the images, the rule-check
// results, a review sheet, and a self-contained view.html to <folder>/renders/. No server and no content port: Chrome is
// driven over a pipe (cdp.mjs) and its requests for https://proposal.invalid/ are answered from disk.
import { existsSync, mkdirSync, readFileSync, realpathSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { basename, dirname, extname, join, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { Browser } from './cdp.mjs';
import { LintError, lintProposal, proposalFiles } from './lint.mjs';
import { dataUri, inlineHtml, viewerHtml } from './view.mjs';

const HERE = dirname(fileURLToPath(import.meta.url));
export const RUNTIME_DIR = join(HERE, 'public/runtime');
export const ORIGIN = 'https://proposal.invalid';
const PHONE = [411, 923];
const FULL_PAGE_VIEW = 'light';

// One row per render. `color` stands in for a red dynamic-color wallpaper.
export const RENDERS = [
  { name: 'light', viewport: PHONE, scheme: 'light' },
  { name: 'dark', viewport: PHONE, scheme: 'dark' },
  { name: 'wallpaper', viewport: PHONE, scheme: 'light', color: '#b3261e' },
  { name: 'foldable', viewport: [933, 704], scheme: 'light' },
  { name: 'tablet', viewport: [1280, 800], scheme: 'light' },
  { name: 'desktop', viewport: [1920, 1080], scheme: 'light' },
];
const TYPES = {
  '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8',
  '.json': 'application/json', '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.gif': 'image/gif',
  '.webp': 'image/webp', '.avif': 'image/avif', '.ico': 'image/x-icon', '.woff2': 'font/woff2', '.woff': 'font/woff', '.ttf': 'font/ttf', '.txt': 'text/plain; charset=utf-8',
};

export function summarize(url, renders) {
  return {
    url,
    renders,
    pass: renders.every(r => r.result.pass === true),
    complete: renders.every(r => r.result.complete === true),
  };
}

// URL -> { type, body } | null. /runtime/* comes from the built runtime, /proposal/* from the proposal folder.
export function makeRoute({ runtimeDir = RUNTIME_DIR, proposalDir }) {
  return url => {
    if (url.origin !== ORIGIN) return null;
    const path = decodeURIComponent(url.pathname);
    let base, rest;
    if (path.startsWith('/runtime/')) [base, rest] = [runtimeDir, path.slice('/runtime/'.length)];
    else if (path.startsWith('/proposal/')) [base, rest] = [proposalDir, path.slice('/proposal/'.length)];
    else return null;
    if (!rest || rest.endsWith('/')) rest += 'index.html';
    const file = resolve(base, rest);
    if (!file.startsWith(base + sep)) return null;
    try {
      if (!statSync(file).isFile()) return null;
      return { type: TYPES[extname(file).toLowerCase()] ?? 'application/octet-stream', body: readFileSync(file) };
    } catch { return null; }
  };
}

// Runs in the page: apply the theme, wait for fonts, two frames, a stable primary color, and no running transitions,
// then run the checks.
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
  const result = window.runtimeChecks();
  // An open modal exempts the page behind it; opened on load it would hide failures.
  if (result.modal?.open > 0) {
    return { ...result, pass: false, error: "a modal is open on load; open it from a control in the page's script instead" };
  }
  return result;
}

// Renders `url` once per RENDERS row on an open page. `onShot(name, png)` receives each screenshot, plus `light-full`: the
// phone light view captured at the full document height, for the agent's own review. It is not a checked view, so it is not
// in the returned renders.
export async function renderViews(page, url, onShot = () => {}) {
  const renders = [];
  for (const spec of RENDERS) {
    await page.setViewport(...spec.viewport);
    await page.goto(url);
    const result = await page.evaluate(prepare, spec);
    onShot(spec.name, await page.screenshot());
    if (spec.name === FULL_PAGE_VIEW) onShot(`${spec.name}-full`, await page.screenshot({ fullPage: true }));
    renders.push({ name: spec.name, viewport: spec.viewport, result });
  }
  return renders;
}

// One line per failing render: which check keys failed, or the page error.
export function failures(renders) {
  const flagged = value => (Array.isArray(value) ? value.length > 0 : Boolean(value && typeof value === 'object'
    && ((Array.isArray(value.fails) ? value.fails.length : value.fails) || value.under48?.length || value.offenders?.length || value.status === 'fail' || value.ok === false)));
  const bad = result => Object.entries(result).filter(([key, value]) => !['pass', 'complete', 'unresolved', 'viewport'].includes(key) && flagged(value)).map(([key]) => key);
  return renders.filter(r => r.result.pass !== true || r.result.complete !== true).map(r => {
    const keys = bad(r.result);
    return `${r.name}: ${r.result.error ?? (keys.length ? keys.join(', ') : r.result.pass === true ? 'checks incomplete' : 'failed')}`;
  });
}

const escape = text => String(text).replace(/[&<>"]/g, c => `&#${c.charCodeAt(0)};`);

// The review sheet is the proposal's checked views on one image, for the agent's own review: a phone row (every phone-width
// view) over a large row (the rest), wrapped so the image is never wider than REVIEW_SHEET_WIDTH. Pure.
export const REVIEW_SHEET_WIDTH = 1600;
const REVIEW_ROW_HEIGHTS = { phone: 560, large: 360 };
export function reviewSheetHtml(title, images) {
  const views = RENDERS;
  const row = (label, height, list) => `<section><h2>${label}</h2>
<div>${list.map(view => `<figure><img src="${images[view.name]}" style="height: ${height}px"><figcaption>${view.name}</figcaption></figure>`).join('')}</div></section>`;
  const isPhone = view => view.viewport[0] === PHONE[0];
  return `<!doctype html><meta charset="utf-8"><title>${escape(title)}</title>
<style>
  body { box-sizing: border-box; margin: 0; padding: 16px; max-width: ${REVIEW_SHEET_WIDTH}px; font: 14px system-ui, sans-serif; background: #eee; color: #222; }
  h1 { margin: 0 0 8px; font-size: 20px; }
  h2 { margin: 12px 0 6px; font-size: 14px; color: #666; }
  section > div { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-start; }
  figure { margin: 0; }
  img { display: block; border: 1px solid #bbb; background: #fff; }
  figcaption { margin-top: 4px; color: #666; }
</style>
<h1>${escape(title)}</h1>
${row('phone', REVIEW_ROW_HEIGHTS.phone, views.filter(isPhone))}
${row('large', REVIEW_ROW_HEIGHTS.large, views.filter(view => !isPhone(view)))}`;
}

// Serves `html`, sizes the viewport to the laid-out document (at least `width` wide), and screenshots it to `file`.
async function writeSheet(browser, file, html, width = 1200) {
  const page = await browser.newPage();
  try {
    await page.serve(url => (url.href === `${ORIGIN}/sheet.html` ? { type: 'text/html; charset=utf-8', body: Buffer.from(html) } : null));
    await page.setViewport(width, 800);
    await page.goto(`${ORIGIN}/sheet.html`);
    const size = await page.evaluate(async () => {
      await Promise.all([...document.images].map(image => image.decode()));
      return [document.documentElement.scrollWidth, document.documentElement.scrollHeight];
    });
    await page.setViewport(...size);
    writeFileSync(file, await page.screenshot());
  } finally {
    await page.close();
  }
}

// Lints the folder and returns the errors as "file:line: message" lines; nothing is rendered when there are any.
export function lintFolder(folder) {
  try {
    const files = proposalFiles(folder);
    if (!files.some(file => file.path === 'index.html')) throw new LintError('index.html: missing');
    lintProposal(files);
    return [];
  } catch (error) {
    if (!(error instanceof LintError)) throw error;
    return [error.message];
  }
}

export function viewHtml(folder, route) {
  const pageUrl = new URL(`${ORIGIN}/proposal/`);
  const source = route(new URL('index.html', pageUrl)).body.toString('utf8');
  return viewerHtml({ title: `${basename(folder)} - Design proposal`, doc: inlineHtml(source, pageUrl, route) });
}

// Returns { folder, dir: <folder>/renders, summary, problems, missing }. Throws a LintError first when the lint fails.
export async function renderProposal(folder) {
  const given = resolve(folder);
  if (!existsSync(given) || !statSync(given).isDirectory()) throw new Error(`Not a proposal folder: ${given}`);
  folder = realpathSync(given);
  const errors = lintFolder(folder);
  if (errors.length) throw new LintError(errors.join('\n'));

  const route = makeRoute({ proposalDir: folder });
  const dir = join(folder, 'renders');
  rmSync(dir, { recursive: true, force: true });
  mkdirSync(dir, { recursive: true });
  const missing = new Set();
  const browser = await Browser.launch();
  try {
    const page = await browser.newPage();
    await page.serve(route, url => { if (!url.endsWith('/favicon.ico')) missing.add(url); });
    const images = {};
    const url = `${ORIGIN}/proposal/`;
    const renders = await renderViews(page, url, (name, png) => { writeFileSync(join(dir, `${name}.png`), png); images[name] = dataUri('image/png', png); });
    await page.close();
    await writeSheet(browser, join(dir, 'review.png'), reviewSheetHtml(basename(folder), images), REVIEW_SHEET_WIDTH);
    const summary = summarize(url, renders);
    writeFileSync(join(dir, 'checks.json'), `${JSON.stringify(summary, null, 2)}\n`);
    writeFileSync(join(dir, 'view.html'), viewHtml(folder, route));
    return { folder, dir, summary, problems: failures(renders), missing: [...missing] };
  } finally {
    await browser.close();
  }
}

// The text `cli.mjs render` prints.
export function report({ folder, dir, summary, problems, missing }) {
  const lines = [`${basename(folder)}: ${summary.pass && summary.complete ? 'pass' : `FAIL (${problems.join('; ')})`}`];
  lines.push(`  ${dir}/{${RENDERS.map(r => r.name).join(',')}}.png, checks.json, view.html`);
  lines.push(`  ${dir}/${FULL_PAGE_VIEW}-full.png: the phone light view at the full document height (your own review; a page whose inner scroller holds the content shows only what the document shows)`);
  lines.push(`  ${dir}/review.png: every checked view on one image (your own review)`);
  if (missing.length) lines.push(`  not found: ${missing.join(', ')}`);
  return lines.join('\n');
}
