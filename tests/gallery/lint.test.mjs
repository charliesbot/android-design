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
  ['an unbundled font-family in a voice', '.voice-serif { font-family: Georgia, serif; }', /style\.css:1: .*font-family is not allowed: "Georgia" is not bundled/],
  ['font-family on a plain rule', 'body {\n  font-family: system-ui;\n}', /style\.css:2: .*font-family is not allowed outside a single-class \.voice-\* rule/],
  ['a bundled font-family on a plain rule', 'h1 { font-family: Fraunces; font-size: 40px; }', /style\.css:1: .*outside a single-class/],
  ['a bundled font-family in a compound voice selector', '.voice-a .x { font-family: "Fraunces"; font-size: 40px; }', /outside a single-class/],
  ['a font-family variable', '.voice-a { font-family: var(--face); font-size: 40px; }', /"var\(--face\)" is not bundled/],
  ['two bundled families in one voice', '.voice-a { font-family: "Fraunces", "Doto"; font-size: 40px; }', /exactly one bundled family/],
  ['a brand face below headline small', '\n.voice-a {\n  font-family: "Fraunces", serif;\n  font-size: 16px;\n}', /style\.css:4: .*Fraunces is only for headline small \(24px\) and up. This voice sets font-size 16px/],
  ['Bricolage Grotesque at 23px', '.voice-a { font-family: "Bricolage Grotesque"; font-size: 23px; }', /Bricolage Grotesque is only for headline small/],
  ['Doto at headline size', '.voice-a { font-family: Doto; font-size: 32px; }', /Doto is only for display small \(36px\) and up. This voice sets font-size 32px/],
  ['a brand face with no font-size in its rule', '.voice-a {\n  font-family: "Big Shoulders Display";\n}', /style\.css:2: .*needs a font-size of at least 24px in the same rule/],
  ['a brand face sized only outside its rule', '@media (min-width: 840px) { .voice-a { font-size: 40px; } }\n.voice-a { font-family: Fraunces; }', /needs a font-size of at least 24px in the same rule/],
  ['a brand face shrunk in a later media query', '.voice-a { font-family: Fraunces; font-size: 32px; }\n@media (max-width: 400px) { .voice-a { font-size: 20px; } }', /style\.css:2: .*Fraunces is only for headline small/],
  ['a brand face with a relative font-size', '.voice-a { font-family: Fraunces; font-size: 2rem; }', /Fraunces voices need a font-size in px/],
  ['the font shorthand in a voice', '.voice-a { font: 700 40px Georgia; }', /font shorthand is not allowed/],
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
  ['an unknown p-icon name', '<!doctype html>\n<p-icon\n  name="not_a_real_symbol"></p-icon>', /index\.html:2: <p-icon name="not_a_real_symbol"> is not a Material Symbols name/],
  ['a p-icon with no name', '<!doctype html>\n<p-icon filled></p-icon>', /index\.html:2: <p-icon> needs a quoted name/],
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

test('lint allows any Material Symbols name on p-icon, filled or not', t => {
  publish(t, { page: `${PAGE}<p-icon name="directions_car"></p-icon><p-icon name='hiking' filled label="Hike"></p-icon><p-icon name="cloud"></p-icon><p-icon name="3d_rotation"></p-icon>` })();
});

test('lint allows the bundled families in voices at their minimum sizes', t => {
  const style = [
    '.voice-flex { font-family: "Google Sans Flex", sans-serif; font-size: 14px; }',
    '.voice-code { font-family: "Google Sans Code", monospace; font-size: 12px; font-variation-settings: "wght" 500; }',
    '.voice-fraunces { font-family: Fraunces, serif; font-size: 24px; font-variation-settings: "opsz" 144, "SOFT" 100, "WONK" 1; }',
    '.voice-bricolage { font-family: "Bricolage Grotesque"; font-size: 28px; font-variation-settings: "wdth" 80; }',
    '.voice-shoulders { font-family: \'Big Shoulders Display\', sans-serif; font-size: 57px; }',
    '.voice-doto { font-family: Doto; font-size: 36px; font-variation-settings: "ROND" 100; }',
    '@media (min-width: 840px) { .voice-doto { font-size: 57px; } .voice-fraunces { font-size: 36px; } }',
    '/* .voice-bad { font-family: Georgia; } */',
  ].join('\n');
  publish(t, { style })();
});

test('a lint failure publishes nothing', t => {
  const { root, draft } = fixture(t);
  writeProposal(root, 'a', { style: `${STYLE}p { color: #000; }` });
  const store = new GalleryStore(root);
  assert.throws(() => store.publish(draft), /Raw color/);
  assert.equal(store.read().rounds.length, 0);
});
