import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { lintFolder } from '../../skills/android-design-explore/gallery/lint.mjs';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { workspace, writeProposal, PAGE } from './fixture.mjs';

const starter = name => readFileSync(fileURLToPath(new URL(`../../skills/android-design-explore/assets/proposal-starter/${name}`, import.meta.url)), 'utf8');

// Returns a function that throws the lint error of a proposal folder, as `render` would report it.
function lint(t, files) {
  const folder = writeProposal(join(workspace(t), 'a'), files);
  return () => lintFolder(folder);
}

test('the shipped starter and the fixture pass the lint', t => {
  lint(t, { page: starter('index.html'), style: starter('style.css') })();
  lint(t, {})();
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
    assert.throws(lint(t, { style }), message);
  });
}

test('lint allows surface-dim and the fixed-dim roles that Compose has', t => {
  const style = [
    'a { color: var(--md-sys-color-surface-dim); }',
    'b { color: var(--md-sys-color-primary-fixed-dim); }',
    'c { color: var(--md-sys-color-secondary-fixed-dim); }',
    'd { color: var(--md-sys-color-tertiary-fixed-dim); }',
  ].join('\n');
  lint(t, { style })();
});

test('lint allows voices, role variables, and neutral color keywords', t => {
  const style = [
    '.voice-hero { font-size: 40px; font-weight: 700; line-height: 48px; letter-spacing: 0; font-variation-settings: "wght" 700; }',
    '@media (min-width: 840px) { .voice-hero { font-size: 56px; } }',
    '.x { color: var(--md-sys-color-primary); background: transparent; border-color: currentColor; outline-color: inherit; fill: var(--static-red); }',
    '/* color: #fff; font-size: 99px; */',
  ].join('\n');
  lint(t, { style })();
});

const htmlRejections = [
  ['style attribute', '<!doctype html>\n<p\n  style="color: var(--md-sys-color-primary)">x</p>', /index\.html:3: .*style=/],
  ['inline style element', '<!doctype html>\n<style>p {}</style>', /index\.html:2: .*<style>/],
  ['an unknown p-icon name', '<!doctype html>\n<p-icon\n  name="not_a_real_symbol"></p-icon>', /index\.html:2: <p-icon name="not_a_real_symbol"> is not a Material Symbols name/],
  ['a p-icon with no name', '<!doctype html>\n<p-icon filled></p-icon>', /index\.html:2: <p-icon> needs a quoted name/],
  ['a p-icon size that is not a Compose icon size', '<!doctype html>\n<p-icon\n  name="search" size="30"></p-icon>', /index\.html:2: <p-icon size="30"> is not a Compose icon size/],
  ['a p-icon size without quotes', '<!doctype html>\n<p-icon name="search" size=24></p-icon>', /index\.html:2: <p-icon size> needs a quoted value/],
  ['a p-icon size inside an icon button', '<!doctype html>\n<m3e-icon-button><p-icon\n  name="search" size="40"></p-icon></m3e-icon-button>', /index\.html:2: <p-icon size> is ignored inside <m3e-icon-button>: the component sets this icon's size; port with its default icon size/],
  ['a p-icon size inside a nested slot of a fab', '<!doctype html>\n<m3e-fab><div><p-icon name="add" size="32"></p-icon></div></m3e-fab>', /index\.html:2: <p-icon size> is ignored inside <m3e-fab>/],
  ['a p-icon size inside a nav item', '<!doctype html>\n<m3e-nav-bar><m3e-nav-item><p-icon slot="icon" name="home" size="32"></p-icon>Home</m3e-nav-item></m3e-nav-bar>', /index\.html:2: <p-icon size> is ignored inside <m3e-nav-item>/],
  ['a p-icon size inside a button', '<!doctype html>\n<m3e-theme><m3e-button><p-icon slot="icon" name="add" size="32"></p-icon>Add</m3e-button></m3e-theme>', /index\.html:2: <p-icon size> is ignored inside <m3e-button>/],
  ['a p-icon size inside a filter chip', '<!doctype html>\n<m3e-filter-chip><p-icon slot="icon" name="add" size="20"></p-icon>Add</m3e-filter-chip>', /index\.html:2: <p-icon size> is ignored inside <m3e-filter-chip>/],
  ['a p-icon size inside a fab menu item', '<!doctype html>\n<m3e-fab-menu><m3e-fab-menu-item><p-icon slot="icon" name="add" size="20"></p-icon>Add</m3e-fab-menu-item></m3e-fab-menu>', /index\.html:2: <p-icon size> is ignored inside <m3e-fab-menu-item>/],
  ['a self-closed p-icon size inside an icon button', '<!doctype html>\n<m3e-icon-button><p-icon name="add" size="40"/></m3e-icon-button>', /index\.html:2: <p-icon size> is ignored inside <m3e-icon-button>/],
  ['a p-icon size inside a menu item', '<!doctype html>\n<m3e-menu-item><p-icon slot="icon" name="add" size="24"></p-icon>Add</m3e-menu-item>', /index\.html:2: <p-icon size> is ignored inside <m3e-menu-item>/],
  ['a p-icon size inside a radio menu item', '<!doctype html>\n<m3e-menu-item-radio><p-icon slot="icon" name="add" size="24"></p-icon>Add</m3e-menu-item-radio>', /index\.html:2: <p-icon size> is ignored inside <m3e-menu-item-radio>/],
  ['inline script', '<!doctype html>\n\n<script>alert(1)</script>', /index\.html:3: .*<script>/],
];
for (const [name, page, message] of htmlRejections) {
  test(`lint rejects ${name}`, t => {
    assert.throws(lint(t, { page }), message);
  });
}

test('lint allows script with src and ignores commented markup', t => {
  lint(t, { page: `${PAGE}<!-- <p style="x"> --><script type="module" src="/runtime/m3e.js"></script>` })();
});

test('lint allows any Material Symbols name on p-icon, filled or not', t => {
  lint(t, { page: `${PAGE}<p-icon name="directions_car"></p-icon><p-icon name='hiking' filled label="Hike"></p-icon><p-icon name="cloud"></p-icon><p-icon name="3d_rotation"></p-icon>` })();
});

test('lint allows p-icon size inside m3e-theme, a card, and layout elements', t => {
  lint(t, { page: '<!doctype html>\n<m3e-theme color="#6750A4"><p-icon name="add" size="48"></p-icon><m3e-card><div slot="content"><p-icon name="add" size="32"></p-icon></div></m3e-card><m3e-app-bar><p-icon name="add" size="20"></p-icon></m3e-app-bar></m3e-theme>' })();
});

test('lint allows p-icon size in the body of components that force only a named icon slot', t => {
  lint(t, { page: '<!doctype html>\n<m3e-theme><m3e-expansion-panel><div><p-icon name="add" size="32"></p-icon></div></m3e-expansion-panel><m3e-dialog><div><p-icon name="add" size="40"></p-icon></div></m3e-dialog><m3e-tree-item><p-icon name="add" size="24"></p-icon></m3e-tree-item></m3e-theme>' })();
});

test('lint allows each Compose icon size on p-icon', t => {
  const sizes = [18, 20, 24, 28, 32, 36, 40, 48];
  lint(t, { page: PAGE + sizes.map(size => `<p-icon name="search" size="${size}"></p-icon>`).join('') })();
});

test('lint allows p-icon size inside a list item and outside components, also after a component closes', t => {
  lint(t, { page: PAGE + '<m3e-icon-button><p-icon name="search"></p-icon></m3e-icon-button><m3e-list><m3e-list-item><p-icon slot="leading" name="person" size="40"></p-icon>Name</m3e-list-item></m3e-list><div><p-icon name="add" size="48"></p-icon></div>' })();
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
  lint(t, { style })();
});

const scriptRejections = [
  ['a relative import', "import { x } from './data.js';\n", /app\.js:1: Relative import "\.\/data\.js"/],
  ['a parent import', "const a = 1;\nimport '../util.mjs';\n", /app\.js:2: Relative import/],
  ['a root-relative import', "import x from \"/shared/x.js\";", /Relative import "\/shared\/x\.js"/],
  ['a re-export', "export * from './data.js';", /Relative import/],
  ['a multi-line import', "import {\n  a,\n  b,\n} from './data.js';", /app\.js:1: Relative import/],
  ['a dynamic import', "const m = await import('./lazy.js');", /app\.js:1: Dynamic import\(\)/],
  ['import.meta', "const u = new URL('a.png', import.meta.url);", /app\.js:1: import\.meta/],
];
for (const [name, script, message] of scriptRejections) {
  test(`lint rejects ${name} in a proposal script`, t => {
    assert.throws(lint(t, { files: { 'app.js': script } }), message);
  });
}

test('lint allows a self-contained script and ignores commented imports', t => {
  lint(t, { files: { 'app.js': "// import './x.js';\n/* import('./y.js') */\nconst important = 'import from ./z';\ndocument.title = important;\n" } })();
});

test('lint rejects a CSS @import', t => {
  assert.throws(lint(t, { style: '\n@import url("other.css");\nbody { color: var(--md-sys-color-on-surface); }' }), /style\.css:2: @import is not allowed/);
});

test('lint skips renders/ and dotfiles, so a rendered folder and its .gitignore pass', t => {
  const folder = writeProposal(join(workspace(t), 'a'), { files: { '.gitignore': '*\n' } });
  mkdirSync(join(folder, 'renders'));
  writeFileSync(join(folder, 'renders/view.html'), '<div style="color: #fff"></div>');
  assert.doesNotThrow(() => lintFolder(folder));
});

test('lint reports a folder without index.html', t => {
  const folder = join(workspace(t), 'a');
  mkdirSync(folder);
  assert.throws(() => lintFolder(folder), /index\.html: missing/);
});
