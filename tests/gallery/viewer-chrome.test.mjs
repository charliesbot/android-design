import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { viewerHtml } from '../../skills/android-design-explore/gallery/view.mjs';

let chrome;
try { chrome = chromeExecutable(); } catch (error) { chrome = null; }
const skip = chrome ? false : 'Chrome not found; install it or set CHROME_PATH';
const opts = { skip, timeout: 60000 };

// A stand-in proposal: it records the theme and state it was sent, and, like theme-bridge.js, reports
// {type: "state-ready"} to its parent once the state is applied (after `delay` ms, never when `silent`).
const fakeDoc = ({ delay = 300, silent = false } = {}) => `<!doctype html><body data-screen="default"><script>
addEventListener('message', e => {
  const d = e.data;
  if (d.type === 'theme') document.body.dataset.scheme = d.scheme ?? '';
  if (d.type === 'state') setTimeout(() => {
    document.body.dataset.screen = d.name;
    if (!${silent}) parent.postMessage({ type: 'state-ready', name: d.name }, '*');
  }, ${delay});
});
</script></body>`;

async function open(t, { states = ['queue', 'lyrics', 'credits', 'filters'], width = 1400, doc = fakeDoc(), revealMs } = {}) {
  const dir = mkdtempSync(join(tmpdir(), 'viewer-'));
  t.after(() => rmSync(dir, { recursive: true, force: true }));
  writeFileSync(join(dir, 'view.html'), viewerHtml({ title: 't', doc, states, revealMs }));
  const browser = await Browser.launch(chrome);
  t.after(() => browser.close());
  const page = await browser.newPage();
  await page.setViewport(width, 1000);
  await page.goto(pathToFileURL(join(dir, 'view.html')).href);
  return page;
}

const frames = page => page.evaluate(() => [...document.querySelectorAll('iframe')].map(f => ({
  visible: getComputedStyle(f).visibility === 'visible', screen: f.contentDocument?.body?.dataset.screen, scheme: f.contentDocument?.body?.dataset.scheme,
  width: f.style.width, right: Math.round(f.getBoundingClientRect().right), caption: f.parentElement.querySelector('figcaption')?.textContent ?? null,
})));
const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
const click = (page, selector) => page.evaluate(sel => document.querySelector(sel).click(), selector);

test('a proposal with states opens on All screens: one captioned live frame per screen, fitted to the window', opts, async t => {
  const page = await open(t);
  await wait(900);
  const shown = await frames(page);
  assert.deepEqual(shown.map(f => f.caption), ['Default', 'queue', 'lyrics', 'credits', 'filters']);
  assert.deepEqual(shown.map(f => f.screen), ['default', 'queue', 'lyrics', 'credits', 'filters']);
  assert.ok(shown.every(f => f.visible), 'every frame is revealed when its own state is ready');
  assert.ok(shown.every(f => f.right <= 1400), 'frames fit the window width');
  const pressed = await page.evaluate(() => [...document.querySelectorAll('#states button')].map(b => `${b.textContent}:${b.getAttribute('aria-pressed')}`));
  assert.deepEqual(pressed, ['All screens:true', 'Default:false', 'queue:false', 'lyrics:false', 'credits:false', 'filters:false']);
});

test('All screens wraps into rows on a narrow window', opts, async t => {
  const page = await open(t, { width: 700 });
  const rows = await page.evaluate(() => new Set([...document.querySelectorAll('iframe')].map(f => Math.round(f.getBoundingClientRect().top))).size);
  assert.ok(rows > 1, `frames sit in ${rows} row(s)`);
});

test('changing device, mode, or color keeps All screens, one frame per screen, and reaches every frame', opts, async t => {
  const page = await open(t);
  await wait(900);
  await click(page, '#devices button:nth-of-type(3)');
  let shown = await frames(page);
  assert.equal(shown.length, 5);
  assert.ok(shown.every(f => f.width === '1280px'), 'every frame is tablet-sized');
  await click(page, '#schemes [data-value="dark"]');
  await click(page, '#seeds .swatch:nth-of-type(2)');
  await wait(100);
  shown = await frames(page);
  assert.equal(shown.length, 5);
  assert.ok(shown.every(f => f.scheme === 'dark'), 'the theme message reached every frame');
  assert.equal(await page.evaluate(() => document.querySelector('#states [aria-pressed="true"]').textContent), 'All screens');
});

test('a picked state keeps its frame hidden until state-ready, so Default never flashes', opts, async t => {
  const page = await open(t);
  await wait(900);
  await click(page, '#states [data-state="queue"]');
  const seen = await page.evaluate(async () => {
    const log = [];
    const started = performance.now();
    while (performance.now() - started < 1200) {
      const f = document.querySelector('iframe');
      log.push([getComputedStyle(f).visibility === 'visible', f.contentDocument?.body?.dataset.screen]);
      await new Promise(resolve => setTimeout(resolve, 10));
    }
    return log;
  });
  assert.ok(seen.some(([visible]) => !visible), 'hidden while the state is pending');
  assert.ok(seen.every(([visible, screen]) => !visible || screen === 'queue'), 'never visible on the default screen');
  assert.equal(seen.at(-1)[0], true, 'revealed once ready');
  assert.deepEqual((await frames(page)).map(f => f.caption), [null], 'single-frame mode has no caption');
});

test('Default reveals as soon as its frame loads', opts, async t => {
  const page = await open(t);
  await click(page, '#states [data-state=""]');
  await wait(150);
  const shown = await frames(page);
  assert.deepEqual(shown.map(f => [f.visible, f.screen]), [[true, 'default']]);
});

test('a frame whose state never reports ready is revealed after the fallback timeout', opts, async t => {
  const page = await open(t, { doc: fakeDoc({ silent: true }), revealMs: 500 });
  await click(page, '#states [data-state="queue"]');
  await wait(150);
  assert.equal((await frames(page))[0].visible, false);
  await wait(800);
  assert.equal((await frames(page))[0].visible, true);
});

test('a proposal without states keeps one plain frame and no state picker', opts, async t => {
  const page = await open(t, { states: [] });
  await wait(200);
  const shown = await frames(page);
  assert.deepEqual(shown.map(f => [f.visible, f.caption]), [[true, null]]);
  assert.equal(await page.evaluate(() => !!document.getElementById('states')), false);
});
