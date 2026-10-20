import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, RENDERS, failures, makeRoute, report, reviewSheetHtml, summarize } from '../../skills/android-design-explore/gallery/render.mjs';
import { dataUri, inlineCss, inlineHtml, viewerHtml } from '../../skills/android-design-explore/gallery/view.mjs';
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

test('view.html has device, mode, color, and palette controls', () => {
  const out = viewerHtml({ title: 't', doc: '<p>' });
  for (const id of ['devices', 'schemes', 'seeds', 'variant', 'reset']) assert.match(out, new RegExp(`id="${id}"`));
});
