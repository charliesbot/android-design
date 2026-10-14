import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';
import { fixture, writeProposal, PAGE, STYLE } from './fixture.mjs';

const starter = name => readFileSync(fileURLToPath(new URL(`../../skills/android-design/assets/proposal-starter/proposals/a/${name}`, import.meta.url)), 'utf8');

function publish(t, files) {
  const { root, draft } = fixture(t);
  writeProposal(root, 'a', files);
  return () => new GalleryStore(root).publish(draft);
}

test('the shipped starter and the fixture pass the lint', t => {
  publish(t, { page: starter('index.html'), style: starter('style.css') })();
  publish(t, {})();
});

const cssRejections = [
  ['hex color', 'body {\n  color: #fff;\n}\n', /style\.css:2: .*Raw color/],
  ['rgb color', 'a { background: rgb(1, 2, 3); }', /style\.css:1: .*Raw color/],
  ['hsl color', 'a { border-color: hsla(0 0% 0% / 50%); }', /Raw color/],
  ['named color', '\n\na { color: tomato; }', /style\.css:3: .*Raw color/],
  ['raw color in a custom property outside the static block', ':root { --brand: #123456; }', /Raw color/],
  ['raw color in a second static colors block', '/* static colors */\n:root { --static-a: #111; }\n/* static colors */\n:root { --static-b: #222; }', /style\.css:4: .*Raw color/],
  ['non-static declaration in the static block', '/* static colors */\n:root { --static-a: #111; color: var(--md-sys-color-primary); }', /style\.css:2: .*--static-\*/],
  ['a dim color role with no Compose counterpart', 'a { color: var(--md-sys-color-primary-dim); }', /style\.css:1: .*primary-dim has no Compose counterpart/],
  ['font-family in a voice', '.voice-serif { font-family: Georgia, serif; }', /style\.css:1: .*font-family is not allowed/],
  ['font-family on a plain rule', 'body {\n  font-family: system-ui;\n}', /style\.css:2: .*font-family is not allowed/],
  ['font-size outside a voice', '.title { font-size: 20px; }', /font-size is only allowed/],
  ['font-weight in a compound voice selector', '.voice-a .x { font-weight: 700; }', /font-weight is only allowed/],
  ['line-height in a media query on a non-voice rule', '@media (min-width: 840px) {\n  h1 { line-height: 1.2; }\n}', /style\.css:2: .*line-height/],
  ['letter-spacing', 'p { letter-spacing: 0.5px; }', /letter-spacing/],
  ['font-variation-settings', 'p { font-variation-settings: "wght" 700; }', /font-variation-settings/],
];
for (const [name, style, message] of cssRejections) {
  test(`lint rejects ${name}`, t => {
    assert.throws(publish(t, { style }), message);
  });
}

test('lint allows surface-dim and the fixed-dim roles that Compose has', t => {
  const style = [
    'a { color: var(--md-sys-color-surface-dim); }',
    'b { color: var(--md-sys-color-primary-fixed-dim); }',
    'c { color: var(--md-sys-color-secondary-fixed-dim); }',
    'd { color: var(--md-sys-color-tertiary-fixed-dim); }',
  ].join('\n');
  publish(t, { style })();
});

test('lint allows voices, role variables, and neutral color keywords', t => {
  const style = [
    '.voice-hero { font-size: 40px; font-weight: 700; line-height: 48px; letter-spacing: 0; font-variation-settings: "wght" 700; }',
    '@media (min-width: 840px) { .voice-hero { font-size: 56px; } }',
    '.x { color: var(--md-sys-color-primary); background: transparent; border-color: currentColor; outline-color: inherit; fill: var(--static-red); }',
    '/* color: #fff; font-size: 99px; */',
  ].join('\n');
  publish(t, { style })();
});

const htmlRejections = [
  ['style attribute', '<!doctype html>\n<p\n  style="color: var(--md-sys-color-primary)">x</p>', /index\.html:3: .*style=/],
  ['inline style element', '<!doctype html>\n<style>p {}</style>', /index\.html:2: .*<style>/],
  ['inline script', '<!doctype html>\n\n<script>alert(1)</script>', /index\.html:3: .*<script>/],
];
for (const [name, page, message] of htmlRejections) {
  test(`lint rejects ${name}`, t => {
    assert.throws(publish(t, { page }), message);
  });
}

test('lint allows script with src and ignores commented markup', t => {
  publish(t, { page: `${PAGE}<!-- <p style="x"> --><script type="module" src="/runtime/m3e.js"></script>` })();
});

test('a lint failure publishes nothing', t => {
  const { root, draft } = fixture(t);
  writeProposal(root, 'a', { style: `${STYLE}p { color: #000; }` });
  const store = new GalleryStore(root);
  assert.throws(() => store.publish(draft), /Raw color/);
  assert.equal(store.read().rounds.length, 0);
});
