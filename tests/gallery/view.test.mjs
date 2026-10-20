import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, RENDERS, failures, makeRoute, report, reviewSheetHtml, summarize } from '../../skills/android-design-explore/gallery/render.mjs';
import { DEVICES, PALETTES, STYLES, dataUri, inlineCss, inlineHtml, proposalTheme, runtimeAssets, screenName, spring, viewerHtml } from '../../skills/android-design-explore/gallery/view.mjs';
import { workspace } from './fixture.mjs';

const files = {
  '/runtime/runtime.css': ['text/css', '@font-face { src: url("fonts/F.woff2") format("woff2"); }'],
  '/runtime/fonts/F.woff2': ['font/woff2', 'FONT'],
  '/runtime/m3e.js': ['text/javascript', 'window.x = "</script><!--";'],
  '/proposal/style.css': ['text/css', '.hero { background: url(hero.png); } .remote { background: url(https://example.com/x.png); }'],
  '/proposal/hero.png': ['image/png', 'PNG'],
  '/proposal/app.js': ['text/javascript', 'console.log(1)'],
};
const fetchLocal = url => (url.origin === ORIGIN && files[url.pathname] ? { type: files[url.pathname][0], body: Buffer.from(files[url.pathname][1]) } : null);
const page = new URL(`${ORIGIN}/proposal/`);

test('inlineCss turns local url() into data URIs and leaves remote and missing ones', () => {
  const css = inlineCss(files['/proposal/style.css'][1] + ' .m { background: url(gone.png); }', new URL('style.css', page), fetchLocal);
  assert.match(css, new RegExp(`url\\("${dataUri('image/png', 'PNG').replace(/[+/]/g, '\\$&')}"\\)`));
  assert.match(css, /url\(https:\/\/example\.com\/x\.png\)/);
  assert.match(css, /url\(gone\.png\)/);
});

test('inlineHtml inlines proposal files and images but leaves the shared runtime as a bare path', () => {
  const html = [
    '<link rel="stylesheet" href="/runtime/runtime.css">', '<link rel="stylesheet" href="style.css">',
    '<script type="module" src="https://proposal.invalid/runtime/m3e.js"></script>', '<script src="app.js"></script>',
    '<img alt="" src="hero.png">', '<img src="https://example.com/r.png">',
  ].join('\n');
  const out = inlineHtml(html, page, fetchLocal);
  assert.match(out, /<link rel="stylesheet" href="\/runtime\/runtime\.css">/);
  assert.match(out, /<script type="module" src="\/runtime\/m3e\.js"><\/script>/);
  assert.doesNotMatch(out, /data:font|font\/woff2/);
  assert.match(out, /<style>\.hero \{ background: url\("data:image\/png;base64,/);
  assert.match(out, /<script>console\.log\(1\)<\/script>/);
  assert.match(out, /<img alt="" src="data:image\/png;base64,/);
  assert.match(out, /src="https:\/\/example\.com\/r\.png"/);
});

test('inlineHtml still inlines a script from another path and keeps source text from closing its tag', () => {
  const out = inlineHtml('<script type="module" src="/runtime/other.js"></script>', page, url => (url.pathname === '/runtime/other.js' ? { type: 'text/javascript', body: Buffer.from('x = "</script><!--";') } : null));
  assert.match(out, /^<script type="module">x = "<\\\/script><\\!--";<\/script>$/);
});

test('runtimeAssets reads runtime.css, m3e.js, and each font the css names', () => {
  const runtime = runtimeAssets(fetchLocal, ORIGIN);
  assert.equal(runtime.css, files['/runtime/runtime.css'][1]);
  assert.equal(runtime.js, files['/runtime/m3e.js'][1]);
  assert.deepEqual(runtime.fonts, { 'fonts/F.woff2': Buffer.from('FONT').toString('base64') });
  assert.throws(() => runtimeAssets(() => null, ORIGIN), /runtime\/runtime\.css is missing/);
});

test('proposalTheme reads color and variant from the first m3e-theme and defaults the rest', () => {
  assert.deepEqual(proposalTheme('<body><m3e-theme color="#AA3355" variant="vibrant" scheme="dark">'), { color: '#aa3355', variant: 'vibrant' });
  assert.deepEqual(proposalTheme("<m3e-theme variant='neutral'>"), { color: '#6750a4', variant: 'neutral' });
  assert.deepEqual(proposalTheme('<m3e-theme color="red">'), { color: '#6750a4', variant: 'tonal-spot' });
  assert.deepEqual(proposalTheme('<p>no theme</p>'), { color: '#6750a4', variant: 'tonal-spot' });
});

test('viewerHtml embeds the document so it cannot close the script tag', () => {
  const out = viewerHtml({ title: 'a <b>', doc: '<p>hi</p></script><script>alert(1)</script>', runtime: { css: '', js: '', fonts: {} } });
  assert.match(out, /<title>a &#60;b&#62;<\/title>/);
  assert.equal(out.split('</script>').length, 3, 'the data block and the viewer script only');
  assert.match(out, /\\u003cp>hi\\u003c\/p>/);
});

test('makeRoute serves the runtime and the proposal folder and refuses to leave them', t => {
  const root = workspace(t);
  mkdirSync(join(root, 'p'), { recursive: true });
  mkdirSync(join(root, 'rt'));
  writeFileSync(join(root, 'p/index.html'), 'home');
  writeFileSync(join(root, 'secret.txt'), 'no');
  writeFileSync(join(root, 'rt/m3e.js'), 'js');
  const route = makeRoute({ runtimeDir: join(root, 'rt'), proposalDir: join(root, 'p') });
  assert.equal(route(new URL(`${ORIGIN}/proposal/`)).body.toString(), 'home');
  assert.equal(route(new URL(`${ORIGIN}/proposal/index.html`)).type, 'text/html; charset=utf-8');
  assert.equal(route(new URL(`${ORIGIN}/runtime/m3e.js`)).body.toString(), 'js');
  for (const bad of ['/proposal/..%2Fsecret.txt', '/proposal/../secret.txt', '/runtime/../p/index.html', '/draft/a/', '/other', '/runtime/missing.js']) assert.equal(route(new URL(ORIGIN + bad)), null, bad);
  assert.equal(route(new URL('https://example.com/runtime/m3e.js')), null);
});

test('the render table has the fixed names and sizes', () => {
  assert.deepEqual(Object.fromEntries(RENDERS.map(r => [r.name, r.viewport])), {
    light: [411, 923], dark: [411, 923], foldable: [933, 704], tablet: [1280, 800], desktop: [1920, 1080],
  });
});

test('summarize passes only when every render passes and completes', () => {
  const ok = { pass: true, complete: true };
  const render = (name, result) => ({ name, viewport: [1, 1], result });
  assert.equal(summarize('u', [render('a', ok), render('b', ok)]).pass, true);
  assert.equal(summarize('u', [render('a', ok), render('b', { pass: false, complete: true })]).pass, false);
  const partial = summarize('u', [render('a', ok), render('b', { pass: true, complete: false })]);
  assert.deepEqual([partial.pass, partial.complete], [true, false]);
});

test('failures names the failing check keys of each render', () => {
  const bad = { pass: false, complete: true, pageOverflow: { fails: true }, touchTargets: { under48: [{}] }, fonts: { offenders: [] }, overflow: [] };
  assert.deepEqual(failures([{ name: 'light', result: { pass: true, complete: true } }, { name: 'tablet', result: bad }]), ['tablet: pageOverflow, touchTargets']);
  assert.deepEqual(failures([{ name: 'x', result: { pass: false, complete: false, error: 'boom' } }]), ['x: boom']);
});

test('failures does not name the viewport or a check with an empty list of failures', () => {
  const result = { pass: false, complete: true, viewport: [411, 923], contrast: { fails: [], passed: 3 }, touchTargets: { under48: [{}] }, overlaps: [], primary: { status: 'skipped' } };
  assert.deepEqual(failures([{ name: 'home-light', result }]), ['home-light: touchTargets']);
});

test('reviewSheetHtml has a phone row over a large row and no light-full', () => {
  const names = RENDERS.map(r => r.name);
  const images = Object.fromEntries([...names, 'light-full'].map(name => [name, `data:image/png;base64,${name}`]));
  const out = reviewSheetHtml('a', images);
  const [phone, large] = out.split('<h2>').slice(1);
  assert.match(phone, /^phone/);
  assert.match(large, /^large/);
  const captions = part => [...part.matchAll(/<figcaption>([^<]+)</g)].map(m => m[1]);
  assert.deepEqual(captions(phone), ['light', 'dark']);
  assert.deepEqual(captions(large), ['foldable', 'tablet', 'desktop']);
  assert.equal(out.split('<img').length - 1, names.length);
  assert.doesNotMatch(out, /light-full/);
  assert.match(out, /<title>a<\/title>/);
  assert.match(out, /max-width: 1600px/);
  assert.match(out, /flex-wrap: wrap/);
});

test('chromeExecutable honors CHROME_PATH and explains a missing Chrome', () => {
  assert.equal(chromeExecutable({ env: { CHROME_PATH: '/x/chrome' }, exists: () => true }), '/x/chrome');
  assert.throws(() => chromeExecutable({ env: { CHROME_PATH: '/x/chrome' }, exists: () => false }), /CHROME_PATH does not exist/);
  assert.throws(() => chromeExecutable({ env: { PATH: '/bin' }, exists: () => false }), /Chrome not found/);
  assert.equal(chromeExecutable({ env: { PATH: '/a:/b' }, exists: p => p === '/b/chromium' }), '/b/chromium');
});

test('the render report lists the folder\'s outputs', () => {
  const summary = { pass: true, complete: true };
  const text = report({ folder: '/work/music', dir: '/work/music/renders', summary, problems: [], missing: [] });
  assert.match(text, /^music: pass$/m);
  assert.match(text, /\/work\/music\/renders\/\{light,dark,foldable,tablet,desktop\}\.png, checks\.json, view\.html/);
  assert.match(text, /renders\/light-full\.png/);
  assert.match(text, /renders\/review\.png: every checked view/);
  const failed = report({ folder: '/work/music', dir: '/work/music/renders', summary: { pass: false, complete: true }, problems: ['light: touchTargets'], missing: ['https://proposal.invalid/x.png'] });
  assert.match(failed, /^music: FAIL \(light: touchTargets\)$/m);
  assert.match(failed, /not found: https:\/\/proposal\.invalid\/x\.png/);
});

test('screenName humanizes a folder name', () => {
  assert.equal(screenName('learn-home'), 'Learn home');
  assert.equal(screenName('music_player--v2'), 'Music player v2');
  assert.equal(screenName('home'), 'Home');
});

test('view.html has the section headings, the brightness group, the two color groups, the device group, and the label', () => {
  const out = viewerHtml({ title: 't', name: 'Learn home', doc: '<p>', runtime: { css: '', js: '', fonts: {} }, theme: { color: '#aa3355', variant: 'vibrant' } });
  assert.match(out, /<m3e-list-option value="proposal" data-color="#aa3355" selected>/);
  for (const { id, color } of PALETTES) assert.match(out, new RegExp(`<m3e-list-option value="${id}" data-color="${color}">`));
  assert.doesNotMatch(out, /value="mono"/);
  assert.equal(out.match(/<m3e-theme class="pal color" color="#[0-9a-f]{6}" variant="vibrant"/g).length, PALETTES.length + 1, 'every row previews the proposal style');
  assert.deepEqual([...out.matchAll(/<m3e-list-option value="([^"]+)" data-variant="[^"]+"( selected)?>/g)].map(m => [m[1], Boolean(m[2])]), STYLES.map(([value]) => [value, value === 'vibrant']));
  assert.equal(out.match(/<m3e-theme class="pal variant" color="#aa3355"/g).length, STYLES.length, 'every style row previews the proposal color');
  assert.match(out, /<m3e-button-group id="devices" variant="connected">/);
  for (const { size } of DEVICES) assert.match(out, new RegExp(`data-w="${size[0]}" data-h="${size[1]}"`));
  const unstyled = viewerHtml({ title: 't', doc: '<p>', runtime: { css: '', js: '', fonts: {} }, theme: { color: '#aa3355', variant: 'fidelity' } });
  assert.doesNotMatch(unstyled, /<m3e-list-option value="[^"]+" data-variant="[^"]+" selected>/, 'no style row is selected for a variant that is not one of the styles');
  assert.doesNotMatch(out, /filter-chip/);
  for (const id of ['schemes', 'proposal-list', 'preset-list', 'styles']) assert.match(out, new RegExp(`id="${id}"`));
  assert.deepEqual([...out.matchAll(/<h2 class="t-title-small">([^<]+)<\/h2>/g)].map(m => m[1]), ['Brightness', 'Color', 'Style']);
  assert.match(out, /<h1 class="t-title-small" id="name">Learn home \u00b7 <span class="mono" id="size">411\u00d7923<\/span><\/h1>/);
  assert.deepEqual([...out.matchAll(/<m3e-icon-button[^>]* data-bezel="(\w+)"/g)].map(m => m[1]), ['true', 'true', 'false', 'false']);
  assert.doesNotMatch(out, /<select|id="reset"|id="seed"|id="tabs"|id="variants"|contrast|dynamic/i);
});

test('spring turns a Material spring into a CSS duration and linear() easing', () => {
  const spatial = spring(0.8, 380);
  assert.ok(spatial.ms > 300 && spatial.ms < 500, `default spatial settles in ${spatial.ms}ms`);
  const points = spatial.easing.slice('linear('.length, -1).split(', ').map(Number);
  assert.equal(points[0], 0);
  assert.equal(points.at(-1), 1);
  assert.ok(Math.max(...points) > 1, 'an underdamped spatial spring overshoots');
  const effects = spring(1, 1600);
  assert.ok(effects.ms < spatial.ms);
  assert.ok(Math.max(...effects.easing.slice('linear('.length, -1).split(', ').map(Number)) <= 1, 'a critically damped effects spring never overshoots');
});
