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

// A stand-in proposal: it records the theme it was sent.
const fakeDoc = () => `<!doctype html><body><script>
addEventListener('message', e => {
  const d = e.data;
  if (d.type === 'theme') Object.assign(document.body.dataset, { scheme: d.scheme ?? '', color: d.color ?? '' });
});
</script></body>`;

async function open(t, { width = 1400 } = {}) {
  const dir = mkdtempSync(join(tmpdir(), 'viewer-'));
  t.after(() => rmSync(dir, { recursive: true, force: true }));
  writeFileSync(join(dir, 'view.html'), viewerHtml({ title: 't', doc: fakeDoc() }));
  const browser = await Browser.launch(chrome);
  t.after(() => browser.close());
  const page = await browser.newPage();
  await page.setViewport(width, 1000);
  await page.goto(pathToFileURL(join(dir, 'view.html')).href);
  return page;
}

const frames = page => page.evaluate(() => [...document.querySelectorAll('iframe')].map(f => ({
  scheme: f.contentDocument?.body?.dataset.scheme, color: f.contentDocument?.body?.dataset.color,
  width: f.style.width, right: Math.round(f.getBoundingClientRect().right),
})));
const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
const click = (page, selector) => page.evaluate(sel => document.querySelector(sel).click(), selector);

test('view.html shows one frame fitted to the window, with no state picker', opts, async t => {
  const page = await open(t, { width: 700 });
  await wait(200);
  const shown = await frames(page);
  assert.equal(shown.length, 1);
  assert.ok(shown[0].right <= 700, 'the frame fits the window width');
  assert.equal(await page.evaluate(() => !!document.getElementById('states')), false);
});

test('changing device, mode, or color reaches the frame', opts, async t => {
  const page = await open(t);
  await wait(200);
  await click(page, '#devices button:nth-of-type(3)');
  assert.equal((await frames(page))[0].width, '1280px');
  await click(page, '#schemes [data-value="dark"]');
  await click(page, '#seeds .swatch:nth-of-type(2)');
  await wait(100);
  const [frame] = await frames(page);
  assert.deepEqual([frame.scheme, frame.color], ['dark', '#0061a4']);
  assert.equal(await page.evaluate(() => document.querySelector('#schemes [aria-pressed="true"]').textContent), 'dark');
});

test('reset reloads the frame without the chosen theme', opts, async t => {
  const page = await open(t);
  await click(page, '#schemes [data-value="dark"]');
  await wait(100);
  assert.equal((await frames(page))[0].scheme, 'dark');
  await click(page, '#reset');
  await wait(200);
  assert.equal((await frames(page))[0].scheme, '');
});
