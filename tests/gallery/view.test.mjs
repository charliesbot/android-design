import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, RENDERS, SHEET_COLUMNS, contactSheetHtml, failures, makeRoute, report, stateRenders, summarize } from '../../skills/android-design-explore/gallery/render.mjs';
import { dataUri, inlineCss, inlineHtml, viewerHtml } from '../../skills/android-design-explore/gallery/view.mjs';
import { workspace } from './fixture.mjs';

const files = {
  '/runtime/runtime.css': ['text/css', '@font-face { src: url("fonts/F.woff2") format("woff2"); }'],
  '/runtime/fonts/F.woff2': ['font/woff2', 'FONT'],
  '/runtime/m3e.js': ['text/javascript', 'window.x = "</script><!--";'],
  '/draft/a/style.css': ['text/css', '.hero { background: url(hero.png); } .remote { background: url(https://example.com/x.png); }'],
  '/draft/a/hero.png': ['image/png', 'PNG'],
  '/draft/a/app.js': ['text/javascript', 'console.log(1)'],
};
const fetchLocal = url => (url.origin === ORIGIN && files[url.pathname] ? { type: files[url.pathname][0], body: Buffer.from(files[url.pathname][1]) } : null);
const page = new URL(`${ORIGIN}/draft/a/`);

test('inlineCss turns local url() into data URIs and leaves remote and missing ones', () => {
  const css = inlineCss(files['/draft/a/style.css'][1] + ' .m { background: url(gone.png); }', new URL('style.css', page), fetchLocal);
  assert.match(css, new RegExp(`url\\("${dataUri('image/png', 'PNG').replace(/[+/]/g, '\\$&')}"\\)`));
  assert.match(css, /url\(https:\/\/example\.com\/x\.png\)/);
  assert.match(css, /url\(gone\.png\)/);
});

test('inlineHtml inlines the runtime script and CSS, fonts, proposal files, and images', () => {
  const html = [
    '<link rel="stylesheet" href="/runtime/runtime.css">', '<link rel="stylesheet" href="style.css">',
    '<script type="module" src="/runtime/m3e.js"></script>', '<script src="app.js"></script>',
    '<img alt="" src="hero.png">', '<img src="https://example.com/r.png">',
  ].join('\n');
  const out = inlineHtml(html, page, fetchLocal);
  assert.doesNotMatch(out, /href="|src="(?!data:|https:\/\/example)/);
  assert.match(out, /<style>@font-face \{ src: url\("data:font\/woff2;base64,/);
  assert.match(out, /<script type="module">window\.x = "<\\\/script><\\!--";<\/script>/);
  assert.match(out, /<script>console\.log\(1\)<\/script>/);
  assert.match(out, /<img alt="" src="data:image\/png;base64,/);
  assert.match(out, /src="https:\/\/example\.com\/r\.png"/);
});

test('viewerHtml embeds the document so it cannot close the script tag', () => {
  const out = viewerHtml({ title: 'a <b>', doc: '<p>hi</p></script><script>alert(1)</script>' });
  assert.match(out, /<title>a &#60;b&#62;<\/title>/);
  assert.equal(out.split('</script>').length, 2);
  assert.match(out, /\\u003cp>hi\\u003c\/p>/);
});

test('makeRoute serves the runtime and proposal folders and refuses to leave them', t => {
  const root = workspace(t);
  mkdirSync(join(root, 'p/a'), { recursive: true });
  mkdirSync(join(root, 'rt'));
  writeFileSync(join(root, 'p/a/index.html'), 'home');
  writeFileSync(join(root, 'p/secret.txt'), 'no');
  writeFileSync(join(root, 'rt/m3e.js'), 'js');
  const route = makeRoute({ runtimeDir: join(root, 'rt'), proposalsDir: join(root, 'p') });
  assert.equal(route(new URL(`${ORIGIN}/draft/a/`)).body.toString(), 'home');
  assert.equal(route(new URL(`${ORIGIN}/draft/a/index.html`)).type, 'text/html; charset=utf-8');
  assert.equal(route(new URL(`${ORIGIN}/runtime/m3e.js`)).body.toString(), 'js');
  for (const bad of ['/draft/a/..%2F..%2Fsecret.txt', '/draft/a/../secret.txt', '/runtime/../p/a/index.html', '/draft/b/', '/other', '/runtime/missing.js']) assert.equal(route(new URL(ORIGIN + bad)), null, bad);
  assert.equal(route(new URL('https://example.com/runtime/m3e.js')), null);
});

test('the render table has the fixed names and sizes', () => {
  assert.deepEqual(Object.fromEntries(RENDERS.map(r => [r.name, r.viewport])), {
    light: [411, 923], dark: [411, 923], wallpaper: [411, 923], foldable: [933, 704], tablet: [1280, 800], desktop: [1920, 1080],
  });
  for (const name of SHEET_COLUMNS) assert.ok(RENDERS.some(r => r.name === name), name);
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
  assert.deepEqual(failures([{ name: 'queue-light', result }]), ['queue-light: touchTargets']);
});

test('contactSheetHtml has one row per proposal and a pass or fail label', () => {
  const images = Object.fromEntries(SHEET_COLUMNS.map(name => [name, `data:image/png;base64,${name}`]));
  const out = contactSheetHtml('T', [{ id: 'a', pass: true, images }, { id: 'b', pass: false, images }]);
  assert.equal(out.split('<section>').length - 1, 2);
  assert.match(out, /<h2>b <small class="bad">fail/);
  assert.equal(out.split('<img').length - 1, 2 * SHEET_COLUMNS.length);
});

test('chromeExecutable honors CHROME_PATH and explains a missing Chrome', () => {
  assert.equal(chromeExecutable({ env: { CHROME_PATH: '/x/chrome' }, exists: () => true }), '/x/chrome');
  assert.throws(() => chromeExecutable({ env: { CHROME_PATH: '/x/chrome' }, exists: () => false }), /CHROME_PATH does not exist/);
  assert.throws(() => chromeExecutable({ env: { PATH: '/bin' }, exists: () => false }), /Chrome not found/);
  assert.equal(chromeExecutable({ env: { PATH: '/a:/b' }, exists: p => p === '/b/chromium' }), '/b/chromium');
});

test('each declared state renders at phone and tablet size in light', () => {
  const specs = stateRenders(['queue', 'lyrics']);
  assert.deepEqual(specs.map(r => [r.name, r.state, r.viewport, r.scheme]), [
    ['queue-light', 'queue', [411, 923], 'light'], ['queue-tablet', 'queue', [1280, 800], 'light'],
    ['lyrics-light', 'lyrics', [411, 923], 'light'], ['lyrics-tablet', 'lyrics', [1280, 800], 'light'],
  ]);
  assert.deepEqual(stateRenders([]), []);
});

test('the render report lists a proposal\'s state renders after the default views', () => {
  const summary = { pass: true, complete: true };
  const text = report({ sheet: '/s.png', proposals: [{ id: 'a', dir: '/r/a', summary, problems: [], missing: [], states: ['queue'] }, { id: 'b', dir: '/r/b', summary, problems: [], missing: [], states: [] }] });
  assert.match(text, /\/r\/a\/\{light,dark,wallpaper,foldable,tablet,desktop,queue-light,queue-tablet\}\.png/);
  assert.match(text, /\/r\/b\/\{light,dark,wallpaper,foldable,tablet,desktop\}\.png/);
});

test('view.html gets a state picker only for a proposal that declares states', () => {
  const data = html => JSON.parse(/const \{ doc, devices, seeds, variants, states \} = (.*);\n/.exec(html)[1].replace(/\\u003c/g, '<'));
  assert.deepEqual(data(viewerHtml({ title: 't', doc: '<p>', states: ['queue', 'lyrics'] })).states, ['queue', 'lyrics']);
  assert.deepEqual(data(viewerHtml({ title: 't', doc: '<p>' })).states, []);
  assert.match(viewerHtml({ title: 't', doc: '', states: ['queue'] }), /id="states"/);
});
