import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { cpSync, existsSync, mkdtempSync, readFileSync, readdirSync, realpathSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { RENDERS } from '../../skills/android-design-explore/gallery/render.mjs';

const CLI = fileURLToPath(new URL('../../skills/android-design-explore/gallery/cli.mjs', import.meta.url));
const STARTER = fileURLToPath(new URL('../../skills/android-design-explore/assets/proposal-starter', import.meta.url));
let chrome;
try { chrome = chromeExecutable(); } catch (error) { chrome = null; }
const skip = chrome ? false : 'Chrome not found; install it or set CHROME_PATH';
const opts = { skip, timeout: 180000 };

// A proposal folder copied from the starter, as the agent does it.
function proposal(t) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'render-chrome-')));
  t.after(() => rmSync(root, { recursive: true, force: true }));
  const folder = join(root, 'design-explorations', 'music');
  cpSync(STARTER, folder, { recursive: true });
  return folder;
}

const render = (folder, ...flags) => spawnSync('node', [CLI, 'render', folder, ...flags], { encoding: 'utf8' });

test('the starter ignores itself and is a flat proposal folder', () => {
  assert.equal(readFileSync(join(STARTER, '.gitignore'), 'utf8'), '*\n');
  assert.deepEqual(readdirSync(STARTER).sort(), ['.gitignore', 'index.html', 'style.css']);
});

test('rendering a folder copied from the starter writes the images, checks, review sheet, and a self-contained view.html', opts, t => {
  const folder = proposal(t);
  assert.equal(readFileSync(join(folder, '.gitignore'), 'utf8'), '*\n');
  const done = render(folder);
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /^music: pass$/m);
  const dir = join(folder, 'renders');
  for (const { name } of RENDERS) assert.ok(statSync(join(dir, `${name}.png`)).size > 1000, name);
  for (const name of ['light-full.png', 'review.png']) assert.ok(statSync(join(dir, name)).size > 1000, name);
  assert.deepEqual(readdirSync(dir).sort(), [...RENDERS.map(r => `${r.name}.png`), 'checks.json', 'light-full.png', 'review.png', 'view.html'].sort());
  const checks = JSON.parse(readFileSync(join(dir, 'checks.json'), 'utf8'));
  assert.deepEqual([checks.pass, checks.complete, checks.renders.length], [true, true, RENDERS.length]);
  const view = readFileSync(join(dir, 'view.html'), 'utf8');
  assert.doesNotMatch(view, /src=\\?"\/runtime|href=\\?"\/runtime|url\(\\?"?fonts\//);
  assert.match(view, /font\/woff2;base64/);
  assert.match(view, /id="devices"/);
});

test('rendering again replaces the renders folder and does not lint it', opts, t => {
  const folder = proposal(t);
  assert.equal(render(folder).status, 0);
  writeFileSync(join(folder, 'renders/stale.png'), 'old');
  const done = render(folder);
  assert.equal(done.status, 0, done.stderr);
  assert.equal(existsSync(join(folder, 'renders/stale.png')), false);
});

test('render fails a page that opens a modal on load', opts, t => {
  const folder = proposal(t);
  const page = readFileSync(join(folder, 'index.html'), 'utf8')
    .replace('</head>', '  <script type="module" src="app.js"></script>\n</head>')
    .replace('</m3e-theme>', '<m3e-dialog id="credits"><span slot="header">Credits</span>Written by Mara Vale.<div slot="actions" end><m3e-button variant="text"><m3e-dialog-action>Close</m3e-dialog-action></m3e-button></div></m3e-dialog>\n</m3e-theme>');
  writeFileSync(join(folder, 'index.html'), page);
  writeFileSync(join(folder, 'app.js'), "customElements.whenDefined('m3e-dialog').then(() => requestAnimationFrame(() => document.getElementById('credits').show()));\n");
  const done = render(folder);
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /^music: FAIL \(light: a modal is open on load; open it from a control in the page's script instead/m);
  assert.equal(JSON.parse(readFileSync(join(folder, 'renders/checks.json'), 'utf8')).pass, false);
});

test('view.html opens a modal from a page script control and still checks its overlay', opts, async t => {
  const folder = proposal(t);
  const page = readFileSync(join(folder, 'index.html'), 'utf8')
    .replace('</head>', '  <script type="module" src="app.js"></script>\n</head>')
    .replace('</m3e-theme>', '<m3e-dialog id="credits"><span slot="header">Credits</span>Written by Mara Vale.<div slot="actions" end><m3e-button variant="text"><m3e-dialog-action>Close</m3e-dialog-action></m3e-button></div></m3e-dialog>\n</m3e-theme>');
  writeFileSync(join(folder, 'index.html'), page);
  writeFileSync(join(folder, 'app.js'), "window.openCredits = () => document.getElementById('credits').show();\n");
  assert.equal(render(folder).status, 0);
  const browser = await Browser.launch(chrome);
  t.after(() => browser.close());
  const tab = await browser.newPage();
  await tab.setViewport(1400, 1000);
  await tab.goto(pathToFileURL(join(folder, 'renders/view.html')).href);
  const open = await tab.evaluate(async () => {
    await new Promise(resolve => setTimeout(resolve, 1500));
    const doc = document.querySelector('iframe').contentDocument;
    const before = doc.getElementById('credits').matches(':popover-open, [open]');
    await document.querySelector('iframe').contentWindow.openCredits();
    await document.querySelector('iframe').contentWindow.runtimeSettle();
    return { before, modal: document.querySelector('iframe').contentWindow.runtimeChecks().modal.open };
  });
  assert.deepEqual(open, { before: false, modal: 1 });
});

// Width and height from a PNG's IHDR chunk.
const pngSize = file => { const head = readFileSync(file); return [head.readUInt32BE(16), head.readUInt32BE(20)]; };

test('render also writes light-full.png at the full document height, outside checks and views', opts, t => {
  const root = proposal(t);
  const dir = root;
  writeFileSync(join(dir, 'style.css'), `${readFileSync(join(dir, 'style.css'), 'utf8')}\n.screen { min-height: 2000px; }\n`);
  const done = render(root);
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /light-full\.png/);
  const out = join(root, 'renders');
  const [width, height] = pngSize(join(out, 'light-full.png'));
  assert.equal(width, 411);
  assert.ok(height >= 2000, `light-full.png is ${height}px tall`);
  // The viewport shots keep their size, and the extra capture is not a checked view.
  assert.deepEqual(pngSize(join(out, 'light.png')), [411, 923]);
  const checks = JSON.parse(readFileSync(join(out, 'checks.json'), 'utf8'));
  assert.deepEqual(checks.renders.map(r => r.name), RENDERS.map(r => r.name));
  assert.equal(checks.pass, true);
  assert.doesNotMatch(readFileSync(join(out, 'view.html'), 'utf8'), /light-full/);
  // review.png holds every checked view on one image no wider than 1600px, and is likewise outside checks and views.
  assert.match(done.stdout, /review\.png/);
  const [reviewWidth, reviewHeight] = pngSize(join(out, 'review.png'));
  assert.ok(reviewWidth <= 1600, `review.png is ${reviewWidth}px wide`);
  assert.ok(reviewHeight > 560, `review.png is ${reviewHeight}px tall`);
  assert.ok(checks.renders.every(r => !r.name.startsWith('review')));
  assert.doesNotMatch(readFileSync(join(out, 'view.html'), 'utf8'), /review\.png/);
});

test('light-full.png of a page shorter than the viewport is the viewport size', opts, t => {
  const root = proposal(t);
  assert.equal(render(root).status, 0);
  const [width, height] = pngSize(join(root, 'renders/light-full.png'));
  assert.deepEqual([width, height >= 923], [411, true]);
});
