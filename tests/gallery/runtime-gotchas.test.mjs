// Runtime CSS gotchas that eval agents kept working around: box sizing, nested themes, segmented list color, the sheet drag handle.
import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { after, before, describe, test } from 'node:test';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, makeRoute } from '../../skills/android-design-explore/gallery/render.mjs';

let chrome;
try { chrome = chromeExecutable(); } catch { chrome = null; }

const head = '<meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Fixture</title><link rel="stylesheet" href="/runtime/runtime.css"><link rel="stylesheet" href="style.css"><script type="module" src="/runtime/m3e.js"></script>';
const theme = (body, attrs = 'color="#6750A4" variant="tonal-spot" scheme="light"') => `<!doctype html><html lang="en"><head>${head}</head><body><m3e-theme ${attrs}>${body}</m3e-theme></body></html>`;

const FIXTURES = {
  // Compose sizes a Modifier.height(56.dp).padding(...) row as 56dp: the padding sits inside the size.
  padded: {
    page: theme('<div class="row" id="row"><span class="t-body-large">Row</span></div><div class="box" id="box"></div>'),
    style: '.row { height: 56px; padding: 12px 16px; } .box { width: 100px; height: 40px; padding: 8px; border: 2px solid var(--md-sys-color-outline); }',
  },
  // A preview card is a nested theme: it takes its content height, not the page height.
  nested: {
    page: theme('<main class="screen"><h1 class="t-headline-large">Page</h1><m3e-theme id="preview" scheme="dark" color="#B3261E"><div class="inner t-body-large">Preview</div></m3e-theme><p id="after" class="t-body-large">After</p></main>'),
    style: '.inner { height: 80px; padding: 16px; }',
  },
  // Three segmented lists: the documented token on the list, the item-level token on an ancestor (the list overrides it), none.
  segmented: {
    page: theme('<m3e-list variant="segmented" id="documented" class="documented"><m3e-list-item>A</m3e-list-item></m3e-list><div class="wrapper"><m3e-list variant="segmented" id="wrapped"><m3e-list-item>B</m3e-list-item></m3e-list></div><m3e-list variant="segmented" id="plain"><m3e-list-item>C</m3e-list-item></m3e-list>'),
    style: '.documented { --m3e-segmented-list-item-container-color: var(--md-sys-color-surface-container); } .wrapper { --m3e-list-item-container-color: var(--md-sys-color-surface-container); }',
  },
  sheet: {
    page: theme('<p class="t-body-large">Behind</p><m3e-bottom-sheet id="sheet" modal handle><p class="t-body-medium">Sheet</p></m3e-bottom-sheet>'),
    style: '',
  },
};

describe('runtime gotchas', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH' }, () => {
  let browser, page, root;
  before(async () => {
    root = realpathSync(mkdtempSync(join(tmpdir(), 'runtime-gotchas-')));
    for (const [name, { page: html, style }] of Object.entries(FIXTURES)) {
      mkdirSync(join(root, 'proposals', name), { recursive: true });
      writeFileSync(join(root, 'proposals', name, 'index.html'), html);
      writeFileSync(join(root, 'proposals', name, 'style.css'), style);
    }
    browser = await Browser.launch(chrome);
    page = await browser.newPage();
    await page.serve(makeRoute({ proposalsDir: join(root, 'proposals') }));
    await page.setViewport(411, 923);
  });
  after(async () => {
    await browser?.close();
    if (root) rmSync(root, { recursive: true, force: true });
  });

  async function load(name, run) {
    await page.goto(`${ORIGIN}/draft/${name}/`);
    return page.evaluate(async fn => {
      await document.fonts.ready;
      await window.runtimeSettle();
      return eval(`(${fn})()`);
    }, run.toString());
  }

  test('padding and borders sit inside a declared size', async () => {
    const sizes = await load('padded', () => ['row', 'box'].map(id => { const r = document.getElementById(id).getBoundingClientRect(); return [r.width, r.height]; }));
    assert.deepEqual(sizes, [[411, 56], [100, 40]]);
  });

  test('a nested m3e-theme takes its content height, the top-level theme still fills the page', async () => {
    const sizes = await load('nested', () => {
      const top = document.querySelector('m3e-theme').getBoundingClientRect();
      const nested = document.getElementById('preview').getBoundingClientRect();
      return { top: top.height, nested: nested.height, afterTop: document.getElementById('after').getBoundingClientRect().top, nestedBottom: nested.bottom };
    });
    assert.ok(sizes.top >= 923, `top-level theme is ${sizes.top}px`);
    assert.equal(sizes.nested, 80, `nested theme is ${sizes.nested}px`);
    assert.ok(sizes.afterTop >= sizes.nestedBottom && sizes.afterTop < sizes.nestedBottom + 40, 'the paragraph follows the preview');
  });

  test('only --m3e-segmented-list-item-container-color on the list sets the segmented item container', async () => {
    const colors = await load('segmented', () => {
      const painted = id => getComputedStyle(document.getElementById(id).querySelector('m3e-list-item').shadowRoot.querySelector('.base')).backgroundColor;
      const probe = document.createElement('div');
      document.body.append(probe);
      const resolve = name => { probe.style.backgroundColor = `var(${name})`; return getComputedStyle(probe).backgroundColor; };
      const result = { documented: painted('documented'), wrapped: painted('wrapped'), plain: painted('plain'), surface: resolve('--md-sys-color-surface'), container: resolve('--md-sys-color-surface-container') };
      probe.remove();
      return result;
    });
    assert.equal(colors.plain, colors.surface, 'the default container is the surface color');
    assert.equal(colors.documented, colors.container, JSON.stringify(colors));
    assert.equal(colors.wrapped, colors.surface, 'the list resets --m3e-list-item-container-color, so an ancestor cannot set it');
  });

  test('an open modal sheet draws no browser focus outline on its drag handle', async () => {
    const handle = await load('sheet', async () => {
      const sheet = document.getElementById('sheet');
      await sheet.show();
      await window.runtimeSettle();
      await new Promise(resolve => setTimeout(resolve, 300));
      const el = sheet.shadowRoot.querySelector('.handle');
      const style = getComputedStyle(el);
      return { focused: sheet.shadowRoot.activeElement === el, outline: style.outlineStyle, background: style.backgroundColor };
    });
    assert.equal(handle.focused, true, 'the library moves focus to the handle on open');
    assert.equal(handle.outline, 'none', JSON.stringify(handle));
    assert.equal(handle.background, 'rgb(97, 93, 104)', 'the handle stays the on-surface-variant role');
  });
});
