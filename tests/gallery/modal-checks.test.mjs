// What runtimeChecks() measures while a modal sheet or dialog is open: the page behind the scrim is inert, so its touch targets
// and contrast are not measured, while the modal's own surface and contents are.
import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { after, before, describe, test } from 'node:test';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, makeRoute } from '../../skills/android-design-explore/gallery/render.mjs';

let chrome;
try { chrome = chromeExecutable(); } catch { chrome = null; }

const PAGE = `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Modal</title>
<link rel="stylesheet" href="/runtime/runtime.css"><link rel="stylesheet" href="style.css"><script type="module" src="/runtime/m3e.js"></script></head>
<body><m3e-theme color="#6750A4" variant="tonal-spot" scheme="light">
  <main class="screen">
    <h1 class="t-headline-large-em">Page</h1>
    <button class="tiny" type="button">x</button>
    <p class="t-body-large faint">Faint text behind</p>
    <m3e-button variant="filled" size="extra-large" data-primary>Go</m3e-button>
  </main>
  <m3e-bottom-sheet id="sheet" modal handle>
    <p class="t-body-medium">Sheet text</p>
    <p class="t-body-medium sheet-faint">Faint sheet text</p>
  </m3e-bottom-sheet>
  <m3e-dialog id="dialog"><span slot="header">Title</span>Body text<div slot="actions" end><m3e-button variant="text"><m3e-dialog-action>Close</m3e-dialog-action></m3e-button></div></m3e-dialog>
</m3e-theme></body></html>`;
const STYLE = `.screen { padding: 24px 16px; color: var(--md-sys-color-on-surface); }
.tiny { width: 24px; height: 24px; }
.faint, .sheet-faint { color: var(--md-sys-color-surface-container-high); }
`;

describe('checks while a modal is open', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH' }, () => {
  let browser, page, root;
  before(async () => {
    root = realpathSync(mkdtempSync(join(tmpdir(), 'modal-checks-')));
    const dir = join(root, 'proposals/a');
    mkdirSync(dir, { recursive: true });
    writeFileSync(join(dir, 'index.html'), PAGE);
    writeFileSync(join(dir, 'style.css'), STYLE);
    browser = await Browser.launch(chrome);
    page = await browser.newPage();
    await page.serve(makeRoute({ proposalsDir: join(root, 'proposals') }));
    await page.setViewport(411, 923);
  });
  after(async () => {
    await browser?.close();
    if (root) rmSync(root, { recursive: true, force: true });
  });

  // Loads the page, optionally opens one modal, and returns the checks.
  async function checks(open) {
    await page.goto(`${ORIGIN}/draft/a/`);
    return page.evaluate(async id => {
      await document.fonts.ready;
      await window.runtimeSettle(); // a sheet runs a close animation when it first renders; opening it earlier would be undone
      if (id) await document.getElementById(id).show();
      await window.runtimeSettle();
      return window.runtimeChecks();
    }, open);
  }
  const targets = result => result.touchTargets.under48.map(item => item.el);
  const faint = result => result.contrast.fails.map(item => item.el).sort();

  test('with nothing open the page is measured, and a closed sheet is not', async () => {
    const result = await checks(null);
    assert.deepEqual(targets(result), ['button "x"']);
    assert.equal(faint(result).length, 1, JSON.stringify(result.contrast));
    assert.match(faint(result)[0], /Faint text behind/);
    assert.equal(result.primary.status, 'pass');
  });

  test('an open sheet exempts the page behind it and checks its own contents', async () => {
    const result = await checks('sheet');
    assert.deepEqual(targets(result), [], 'the 24px button behind the scrim is inert, and the drag handle has its 48px touch target');
    assert.equal(faint(result).length, 1, JSON.stringify(result.contrast));
    assert.match(faint(result)[0], /Faint sheet text/);
    assert.equal(result.overlaps.length, 0);
    assert.equal(result.primary.status, 'skipped');
    assert.equal(result.complete, true, JSON.stringify(result.unresolved));
  });

  test('an open dialog exempts the page behind it', async () => {
    const result = await checks('dialog');
    assert.deepEqual(targets(result), []);
    assert.deepEqual(faint(result), []);
    assert.equal(result.primary.status, 'skipped');
    assert.equal(result.complete, true, JSON.stringify(result.unresolved));
  });
});
