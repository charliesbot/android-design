import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { RUNTIME_DIR, makeRoute, viewHtml } from '../../skills/android-design-explore/gallery/render.mjs';
import { workspace, writeProposal } from './fixture.mjs';
import { spring } from '../../skills/android-design-explore/gallery/view.mjs';

let chrome;
try { chrome = chromeExecutable(); } catch (error) { chrome = null; }
const skip = chrome ? false : 'Chrome not found; install it or set CHROME_PATH';
const opts = { skip, timeout: 60000 };

const PAGE = `<!doctype html><html><head><title>Fixture proposal</title><link rel="stylesheet" href="/runtime/runtime.css"><script type="module" src="/runtime/m3e.js"></script><script src="app.js"></script></head>
<body><m3e-theme color="#AA3355" variant="vibrant" scheme="light"><h1 class="t-headline-large">Hello</h1></m3e-theme></body></html>
`;

// The built runtime plus a one-page proposal whose own script marks the document, rendered into view.html like `render` does.
async function open(t, { width = 1440, height = 900 } = {}) {
  const root = workspace(t);
  const folder = writeProposal(join(root, 'learn-home'), { page: PAGE, files: { 'app.js': 'document.documentElement.dataset.ran = "yes";' } });
  const html = viewHtml(folder, makeRoute({ proposalDir: folder }));
  writeFileSync(join(root, 'view.html'), html);
  const browser = await Browser.launch(chrome);
  t.after(() => browser.close());
  const page = await browser.newPage();
  await page.setViewport(width, height);
  await page.goto(pathToFileURL(join(root, 'view.html')).href);
  await until(page, () => document.documentElement.classList.contains('ready'));
  return { page, html };
}

const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
async function until(page, fn, tries = 100) {
  for (let i = 0; i < tries; i++) {
    if (await page.evaluate(fn)) return;
    await wait(50);
  }
  throw new Error(`Timed out waiting for ${fn}`);
}
const click = (page, selector) => page.evaluate(sel => document.querySelector(sel).click(), selector);
// The state of both themes: the viewer's own and the proposal frame's.
const themes = page => page.evaluate(() => {
  const read = element => ({ color: element.getAttribute('color'), variant: element.getAttribute('variant'), scheme: element.getAttribute('scheme') });
  const frame = document.querySelector('iframe');
  return { chrome: read(document.getElementById('chrome')), frame: read(frame.contentDocument.querySelector('m3e-theme')) };
});
const selectedRows = (page, list = '.groups') => page.evaluate(selector => [...document.querySelectorAll(selector + ' m3e-list-option')].filter(option => option.selected).map(option => option.value), list);
const frameSize = page => page.evaluate(() => {
  const frame = document.querySelector('iframe');
  return { width: frame.style.width, height: frame.style.height, right: Math.round(document.getElementById('device').getBoundingClientRect().right), bottom: Math.round(document.getElementById('device').getBoundingClientRect().bottom) };
});

test('the viewer page defines the runtime elements from the shared copy', opts, async t => {
  const { page } = await open(t);
  const defined = await page.evaluate(() => ['m3e-theme', 'm3e-button', 'm3e-icon-button', 'm3e-selection-list', 'p-icon'].map(name => Boolean(customElements.get(name))));
  assert.deepEqual(defined, [true, true, true, true, true]);
  assert.equal(await page.evaluate(() => document.getElementById('chrome').parentElement === document.body), true, 'the top-level theme is a direct child of body');
});

test('the proposal frame renders with the runtime and its own script', opts, async t => {
  const { page } = await open(t);
  await until(page, () => document.querySelector('iframe').contentDocument?.documentElement?.dataset.ran === 'yes');
  const frame = await page.evaluate(() => {
    const win = document.querySelector('iframe').contentWindow;
    return { theme: Boolean(win.document.querySelector('m3e-theme')), defined: Boolean(win.customElements.get('m3e-theme')), checks: typeof win.runtimeChecks };
  });
  assert.deepEqual(frame, { theme: true, defined: true, checks: 'function' });
  assert.equal(await page.evaluate(() => document.querySelector('iframe').contentDocument.title), 'Fixture proposal');
  assert.equal(await page.evaluate(() => document.getElementById('name').textContent), 'Learn home \u00b7 411\u00d7923');
});

test('the proposal row is selected first and both themes start on the proposal color, light', opts, async t => {
  const { page } = await open(t);
  await until(page, () => Boolean(document.querySelector('iframe').contentDocument?.querySelector('m3e-theme')));
  assert.deepEqual(await selectedRows(page), ['proposal']);
  const { chrome, frame } = await themes(page);
  assert.deepEqual(chrome, { color: '#aa3355', variant: 'vibrant', scheme: 'light' });
  assert.equal(frame.color, '#aa3355');
});

test('picking the Teal row recolors the viewer and the frame, and the Proposal row restores them', opts, async t => {
  const { page } = await open(t);
  await until(page, () => Boolean(document.querySelector('iframe').contentDocument?.querySelector('m3e-theme')));
  await click(page, 'm3e-list-option[value="teal"]');
  await until(page, () => document.querySelector('iframe').contentDocument.querySelector('m3e-theme').getAttribute('color') === '#006a6a');
  assert.deepEqual(await themes(page), {
    chrome: { color: '#006a6a', variant: 'vibrant', scheme: 'light' },
    frame: { color: '#006a6a', variant: 'vibrant', scheme: 'light' },
  });
  await click(page, 'm3e-list-option[value="proposal"]');
  await until(page, () => document.querySelector('iframe').contentDocument.querySelector('m3e-theme').getAttribute('color') === '#aa3355');
  assert.deepEqual((await themes(page)).chrome, { color: '#aa3355', variant: 'vibrant', scheme: 'light' });
});

test('the brightness group has two toggles, one pressed, and toggles the scheme on the viewer, the frame, and every palette row', opts, async t => {
  const { page } = await open(t);
  await until(page, () => Boolean(document.querySelector('iframe').contentDocument?.querySelector('m3e-theme')));
  const pressed = () => page.evaluate(() => ({
    group: document.getElementById('schemes').getAttribute('variant'),
    buttons: [...document.querySelectorAll('#schemes m3e-button')].map(button => [button.dataset.scheme, button.toggle, button.selected, button.textContent.trim()]),
  }));
  assert.deepEqual(await pressed(), { group: 'connected', buttons: [['light', true, true, 'light_modeLight'], ['dark', true, false, 'dark_modeDark']] });
  await click(page, '#schemes m3e-button[data-scheme="dark"]');
  await until(page, () => document.querySelector('iframe').contentDocument.querySelector('m3e-theme').getAttribute('scheme') === 'dark');
  const { chrome, frame } = await themes(page);
  assert.deepEqual([chrome.scheme, frame.scheme], ['dark', 'dark']);
  const rows = await page.evaluate(() => [...document.querySelectorAll('m3e-theme.pal')].map(row => row.getAttribute('scheme')));
  assert.equal(rows.length, 12);
  assert.ok(rows.every(scheme => scheme === 'dark'));
  assert.deepEqual((await pressed()).buttons.map(button => button[2]), [false, true]);
  await click(page, '#schemes m3e-button[data-scheme="dark"]');
  assert.deepEqual((await pressed()).buttons.map(button => button[2]), [false, true], 'pressing the selected one keeps it');
  await click(page, '#schemes m3e-button[data-scheme="light"]');
  await until(page, () => document.querySelector('iframe').contentDocument.querySelector('m3e-theme').getAttribute('scheme') === 'light');
  assert.equal((await themes(page)).chrome.scheme, 'light');
});

test('the panel has three section headings in one style, and the Proposal row is its own group', opts, async t => {
  const { page } = await open(t);
  const panel = await page.evaluate(() => ({
    headings: [...document.querySelectorAll('.panel h2')].map(h => [h.textContent, h.className, getComputedStyle(h).color, getComputedStyle(h).paddingLeft]),
    groups: [...document.querySelectorAll('.panel m3e-selection-list')].map(list => [...list.querySelectorAll('m3e-list-option')].map(option => option.value)),
    colors: [getComputedStyle(document.querySelector('.panel h2')).color, getComputedStyle(document.getElementById('chrome')).getPropertyValue('--md-sys-color-on-surface-variant')],
  }));
  assert.deepEqual(panel.headings.map(h => h[0]), ['Brightness', 'Color', 'Style']);
  assert.equal(new Set(panel.headings.map(h => h.slice(1).join('|'))).size, 1, 'one heading style');
  assert.equal(panel.headings[0][1], 't-title-small');
  assert.deepEqual(panel.groups, [['proposal'], ['purple', 'blue', 'green', 'coral', 'amber', 'teal'], ['tonal-spot', 'neutral', 'vibrant', 'expressive', 'monochrome']]);
  assert.equal(await page.evaluate(() => document.querySelector('.panel').textContent.includes('Colors')), false, 'no big Colors title');
});

test('color selection is exclusive across the two groups', opts, async t => {
  const { page } = await open(t);
  await click(page, 'm3e-list-option[value="teal"]');
  assert.deepEqual(await selectedRows(page), ['teal']);
  await click(page, 'm3e-list-option[value="amber"]');
  assert.deepEqual(await selectedRows(page), ['amber']);
  await click(page, 'm3e-list-option[value="proposal"]');
  assert.deepEqual(await selectedRows(page), ['proposal']);
});

test('the devices are one connected group of four toggles with exactly one pressed', opts, async t => {
  const { page } = await open(t);
  const state = () => page.evaluate(() => ({
    variant: document.getElementById('devices').getAttribute('variant'),
    buttons: [...document.querySelectorAll('#devices m3e-icon-button')].map(button => [button.toggle, button.selected, button.title]),
    parent: document.getElementById('devices').localName,
  }));
  const first = await state();
  assert.deepEqual([first.parent, first.variant, first.buttons.length], ['m3e-button-group', 'connected', 4]);
  assert.ok(first.buttons.every(([toggle]) => toggle === true));
  assert.deepEqual(first.buttons.map(([, selected]) => selected), [true, false, false, false]);
  assert.deepEqual(first.buttons.map(([, , title]) => title), ['Phone 411x923', 'Foldable 933x704', 'Tablet 1280x800', 'Desktop 1920x1080']);
  await click(page, '#devices m3e-icon-button:nth-of-type(3)');
  assert.deepEqual((await state()).buttons.map(([, selected]) => selected), [false, false, true, false]);
  await click(page, '#devices m3e-icon-button:nth-of-type(3)');
  assert.deepEqual((await state()).buttons.map(([, selected]) => selected), [false, false, true, false], 'pressing the selected device keeps it selected');
});

test('a device toggle changes the frame size', opts, async t => {
  const { page } = await open(t);
  assert.equal((await frameSize(page)).width, '411px');
  await click(page, '#devices m3e-icon-button:nth-of-type(3)');
  const size = await frameSize(page);
  assert.deepEqual([size.width, size.height], ['1280px', '800px']);
});

test('the device group sits above the frame and the screen name below it, both as wide as the frame', opts, async t => {
  const { page } = await open(t);
  await click(page, '#devices m3e-icon-button:nth-of-type(2)');
  await wait(800);
  const box = await page.evaluate(() => {
    const rect = selector => {
      const r = document.querySelector(selector).getBoundingClientRect();
      return { left: r.left, right: r.right, top: r.top, bottom: r.bottom };
    };
    return { row: rect('#row'), frame: rect('#device'), group: rect('#devices'), name: rect('#name') };
  });
  const near = (a, b) => Math.abs(a - b) <= 1.5;
  assert.ok(near(box.row.left, box.frame.left) && near(box.row.right, box.frame.right), 'the row spans the frame');
  assert.ok(box.row.bottom <= box.frame.top + 1.5, 'the row is above the frame');
  assert.ok(near(box.group.left, box.frame.left), 'the group starts at the frame corner');
  assert.ok(box.name.top >= box.frame.bottom - 1.5 && box.name.top - box.frame.bottom < 14, 'the label sits just below the frame');
  assert.ok(near(box.name.left, box.frame.left), 'the label starts at the frame outer edge');
});

test('the device buttons sit 2px apart and the selected one is a different shape', opts, async t => {
  const { page } = await open(t);
  const boxes = await page.evaluate(() => [...document.querySelectorAll('#devices m3e-icon-button')].map(button => {
    const r = (button.shadowRoot.querySelector('.base') ?? button).getBoundingClientRect();
    return { left: r.left, right: r.right, height: r.height, radius: getComputedStyle(button.shadowRoot.querySelector('.base') ?? button).borderTopLeftRadius };
  }));
  for (let i = 1; i < boxes.length; i++) assert.ok(Math.abs(boxes[i].left - boxes[i - 1].right - 2) <= 0.5, `gap ${i}: ${boxes[i].left - boxes[i - 1].right}`);
  assert.equal(new Set(boxes.map(b => b.height)).size, 1, 'one height');
  assert.notEqual(boxes[0].radius, boxes[1].radius, 'the selected button has its own shape');
});

test('the style rows are one single-select list that starts on the proposal style, in the same form as the color rows', opts, async t => {
  const { page } = await open(t);
  assert.deepEqual(await selectedRows(page, '#styles'), ['vibrant']);
  assert.equal(await page.evaluate(() => document.querySelectorAll('#styles m3e-list-option').length), 5);
  const forms = await page.evaluate(() => {
    const read = selector => {
      const option = document.querySelector(selector);
      return [Math.round(option.getBoundingClientRect().height), option.querySelectorAll('.dots i').length, Boolean(option.querySelector('.split')), option.querySelector('.name').className];
    };
    return [read('#preset-list m3e-list-option'), read('#styles m3e-list-option')];
  });
  assert.deepEqual(forms[0], forms[1], 'a style row has the height, dots, swatch, and type of a color row');
});

test('picking the Neutral style sets the variant on the viewer, the frame, and every color row', opts, async t => {
  const { page } = await open(t);
  await until(page, () => Boolean(document.querySelector('iframe').contentDocument?.querySelector('m3e-theme')));
  await click(page, '#styles m3e-list-option[value="neutral"]');
  await until(page, () => document.querySelector('iframe').contentDocument.querySelector('m3e-theme').getAttribute('variant') === 'neutral');
  const { chrome, frame } = await themes(page);
  assert.deepEqual([chrome.variant, frame.variant], ['neutral', 'neutral']);
  assert.deepEqual(await page.evaluate(() => [...new Set([...document.querySelectorAll('m3e-theme.color')].map(row => row.getAttribute('variant')))]), ['neutral']);
  assert.equal(await page.evaluate(() => document.querySelectorAll('m3e-theme.color').length), 7);
  assert.deepEqual(await selectedRows(page, '#styles'), ['neutral']);
  assert.equal(chrome.color, '#aa3355', 'the color is unchanged');
  await click(page, '#styles m3e-list-option[value="neutral"]');
  assert.deepEqual(await selectedRows(page, '#styles'), ['neutral'], 'pressing the selected row keeps it');
  assert.deepEqual(await selectedRows(page), ['proposal'], 'the color selection is unchanged');
});

test('picking the Teal color sets the color on every style row', opts, async t => {
  const { page } = await open(t);
  await click(page, 'm3e-list-option[value="teal"]');
  await until(page, () => [...document.querySelectorAll('m3e-theme.variant')].every(row => row.getAttribute('color') === '#006a6a'));
  assert.equal(await page.evaluate(() => document.querySelectorAll('m3e-theme.variant').length), 5);
  assert.deepEqual(await page.evaluate(() => [...document.querySelectorAll('m3e-theme.variant')].map(row => row.getAttribute('variant'))), ['tonal-spot', 'neutral', 'vibrant', 'expressive', 'monochrome']);
});

test('a style picked before a palette carries over to the palette color', opts, async t => {
  const { page } = await open(t);
  await until(page, () => Boolean(document.querySelector('iframe').contentDocument?.querySelector('m3e-theme')));
  await click(page, '#styles m3e-list-option[value="monochrome"]');
  await click(page, 'm3e-list-option[value="teal"]');
  await until(page, () => document.querySelector('iframe').contentDocument.querySelector('m3e-theme').getAttribute('color') === '#006a6a');
  assert.deepEqual((await themes(page)).frame, { color: '#006a6a', variant: 'monochrome', scheme: 'light' });
});

test('the label shows the device size in Google Sans Code and follows a device change', opts, async t => {
  const { page } = await open(t);
  const label = () => page.evaluate(() => ({ text: document.getElementById('name').textContent, size: getComputedStyle(document.getElementById('size')).fontFamily, sizePx: getComputedStyle(document.getElementById('size')).fontSize, namePx: getComputedStyle(document.getElementById('name')).fontSize, cls: document.getElementById('name').className }));
  const first = await label();
  assert.equal(first.text, 'Learn home \u00b7 411\u00d7923');
  assert.match(first.size, /^"Google Sans Code"/);
  assert.equal(first.sizePx, first.namePx);
  assert.equal(first.cls, 't-title-small');
  await click(page, '#devices m3e-icon-button:nth-of-type(4)');
  assert.equal((await label()).text, 'Learn home \u00b7 1920\u00d71080');
});

test('a phone and a foldable have a bezel ring and concentric radii, a tablet and a desktop do not', opts, async t => {
  const { page } = await open(t);
  const geometry = () => page.evaluate(() => {
    const device = getComputedStyle(document.getElementById('device')), frame = getComputedStyle(document.querySelector('iframe'));
    return { ring: device.paddingTop, outer: device.borderTopLeftRadius, inner: frame.borderTopLeftRadius, background: device.backgroundColor, outline: device.outlineStyle + ' ' + device.outlineWidth };
  });
  const phone = await geometry();
  assert.deepEqual([phone.ring, phone.outer, phone.inner, phone.outline], ['10px', '38px', '28px', 'solid 1px']);
  assert.notEqual(phone.background, 'rgba(0, 0, 0, 0)');
  await click(page, '#devices m3e-icon-button:nth-of-type(2)');
  await until(page, () => !document.documentElement.classList.contains('animating'));
  assert.equal((await geometry()).ring, '10px');
  for (const nth of [3, 4]) {
    await click(page, `#devices m3e-icon-button:nth-of-type(${nth})`);
    await until(page, () => !document.documentElement.classList.contains('animating'));
    const window = await geometry();
    assert.deepEqual([window.ring, window.outer, window.inner, window.background, window.outline], ['0px', '12px', '12px', 'rgba(0, 0, 0, 0)', 'solid 1px']);
  }
});

test('a device change animates on the Expressive default spatial spring and a window resize does not', opts, async t => {
  const { page } = await open(t);
  const transition = () => page.evaluate(() => {
    const style = getComputedStyle(document.getElementById('wrap'));
    return { animating: document.documentElement.classList.contains('animating'), duration: style.transitionDuration, easing: style.transitionTimingFunction };
  });
  assert.equal((await transition()).duration, '0s');
  await click(page, '#devices m3e-icon-button:nth-of-type(3)');
  const during = await transition();
  assert.equal(during.animating, true);
  assert.equal(during.duration, `${spring(0.8, 380).ms / 1000}s`);
  assert.match(during.easing, /^linear\(0[ ,]/);
  const props = await page.evaluate(() => ({ device: getComputedStyle(document.getElementById('device')).transitionProperty, frame: getComputedStyle(document.querySelector('iframe')).transitionProperty }));
  assert.match(props.device, /padding/);
  assert.match(props.device, /border-radius/);
  assert.match(props.frame, /border-radius/);
  await until(page, () => !document.documentElement.classList.contains('animating'));
  await page.setViewport(1200, 800);
  assert.equal((await transition()).duration, '0s', 'a resize does not set the animating class');
});

test('reduced motion turns the device-change transition off', opts, async t => {
  const { page } = await open(t);
  await page.send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-reduced-motion', value: 'reduce' }] });
  await click(page, '#devices m3e-icon-button:nth-of-type(3)');
  const style = await page.evaluate(() => ({ wrap: getComputedStyle(document.getElementById('wrap')).transitionDuration, frame: getComputedStyle(document.querySelector('iframe')).transitionDuration, device: getComputedStyle(document.getElementById('device')).transitionDuration }));
  assert.deepEqual(style, { wrap: '0s', frame: '0s', device: '0s' });
});

test('every palette dot has a 1px ring', opts, async t => {
  const { page } = await open(t);
  const rings = await page.evaluate(() => [...document.querySelectorAll('.dots i')].map(dot => getComputedStyle(dot).borderTopWidth));
  assert.equal(rings.length, 60);
  assert.ok(rings.every(width => width === '1px'));
});

test('the frame is scaled to fit and centered in the stage, and refits when the window grows and shrinks', opts, async t => {
  const { page } = await open(t, { width: 700, height: 700 });
  await click(page, '#devices m3e-icon-button:nth-of-type(4)');
  await until(page, () => !document.documentElement.classList.contains('animating'));
  const placement = () => page.evaluate(() => {
    const device = document.getElementById('device').getBoundingClientRect(), wrap = document.getElementById('wrap').getBoundingClientRect(), stage = document.getElementById('stage').getBoundingClientRect();
    return {
      inside: device.left >= stage.left && device.right <= stage.right && device.top >= stage.top && device.bottom <= stage.bottom,
      width: device.width,
      margins: [wrap.left - stage.left, stage.right - wrap.right, wrap.top - stage.top, stage.bottom - wrap.bottom],
    };
  });
  const small = await placement();
  assert.ok(small.inside, 'the desktop device fits inside the stage');
  const [left, right, top, bottom] = small.margins;
  assert.ok(Math.abs(left - right) <= 2 && Math.abs(top - bottom) <= 2, `centered: ${small.margins}`);
  await page.setViewport(1440, 900);
  // Recentering happens on layout, refitting on the resize event after it: wait for the device itself to grow.
  await until(page, () => document.getElementById('device').getBoundingClientRect().width > 400);
  const large = await placement();
  assert.ok(large.inside && large.width > small.width, 'the device grows with the window and still fits');
  await page.setViewport(700, 700);
  await until(page, () => document.getElementById('device').getBoundingClientRect().width < 400);
  assert.ok((await placement()).inside, 'the device shrinks back inside the stage');
});

test('view.html carries the runtime once, and the proposal document has none of it', opts, async t => {
  const { html } = await open(t);
  const count = (text, part) => text.split(part).length - 1;
  const font = readFileSync(join(RUNTIME_DIR, 'fonts/GoogleSansFlex.woff2')).toString('base64');
  assert.equal(count(html, font), 1, 'the Google Sans Flex font');
  const script = JSON.stringify(readFileSync(join(RUNTIME_DIR, 'm3e.js'), 'utf8')).replace(/</g, '\\u003c').slice(1, -1);
  assert.equal(count(html, script.slice(20000, 20300)), 1, 'm3e.js');
  assert.doesNotMatch(html, /data:font\//);
});
