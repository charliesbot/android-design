import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, realpathSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { RENDERS } from '../../skills/android-design-explore/gallery/render.mjs';

const CLI = fileURLToPath(new URL('../../skills/android-design-explore/gallery/cli.mjs', import.meta.url));
let chrome;
try { chrome = chromeExecutable(); } catch (error) { chrome = null; }

test('render writes images, checks, a contact sheet, and a self-contained view.html', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, t => {
  const repo = realpathSync(mkdtempSync(join(tmpdir(), 'render-chrome-')));
  t.after(() => rmSync(repo, { recursive: true, force: true }));
  assert.equal(spawnSync('git', ['init', '-q', repo]).status, 0);
  const created = spawnSync('node', [CLI, 'create', '--project', repo], { encoding: 'utf8' });
  assert.equal(created.status, 0, created.stderr);
  const exploration = created.stdout.trim();
  const done = spawnSync('node', [CLI, 'render', exploration], { encoding: 'utf8' });
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /^a: pass$/m);
  const dir = join(exploration, 'renders/a');
  for (const { name } of RENDERS) assert.ok(statSync(join(dir, `${name}.png`)).size > 1000, name);
  assert.ok(statSync(join(exploration, 'renders/contact-sheet.png')).size > 1000);
  const checks = JSON.parse(readFileSync(join(dir, 'checks.json'), 'utf8'));
  assert.deepEqual([checks.pass, checks.complete, checks.renders.length], [true, true, RENDERS.length]);
  const view = readFileSync(join(dir, 'view.html'), 'utf8');
  assert.equal(existsSync(join(dir, 'view.html')), true);
  assert.doesNotMatch(view, /src=\\?"\/runtime|href=\\?"\/runtime|url\(\\?"?fonts\//);
  assert.match(view, /font\/woff2;base64/);
});

// A proposal that declares open states: a modal bottom sheet (queue) and a dialog (credits) over the starter screen.
function exploration(t) {
  const repo = realpathSync(mkdtempSync(join(tmpdir(), 'render-states-')));
  t.after(() => rmSync(repo, { recursive: true, force: true }));
  assert.equal(spawnSync('git', ['init', '-q', repo]).status, 0);
  const created = spawnSync('node', [CLI, 'create', '--project', repo], { encoding: 'utf8' });
  assert.equal(created.status, 0, created.stderr);
  return created.stdout.trim();
}

function declareStates(root, script) {
  const dir = join(root, 'project/proposals/a');
  const page = readFileSync(join(dir, 'index.html'), 'utf8')
    .replace('</head>', '  <meta name="p-states" content="queue credits">\n  <script type="module" src="app.js"></script>\n</head>')
    .replace('</m3e-theme>', `<m3e-bottom-sheet id="queue" modal handle><div class="t-title-large">Up next</div><p class="t-body-medium">Golden Hour</p></m3e-bottom-sheet>
    <m3e-dialog id="credits"><span slot="header">Credits</span>Written by Mara Vale.<div slot="actions" end><m3e-button variant="text"><m3e-dialog-action>Close</m3e-dialog-action></m3e-button></div></m3e-dialog>
  </m3e-theme>`);
  writeFileSync(join(dir, 'index.html'), page);
  writeFileSync(join(dir, 'app.js'), script);
}

test('render opens each declared state and writes its renders and checks', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, t => {
  const root = exploration(t);
  declareStates(root, 'window.proposalState = name => document.getElementById(name).show();\n');
  const done = spawnSync('node', [CLI, 'render', root], { encoding: 'utf8' });
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /^a: pass$/m);
  assert.match(done.stdout, /queue-light,queue-tablet,credits-light,credits-tablet\}\.png/);
  const dir = join(root, 'renders/a');
  for (const name of ['queue-light', 'queue-tablet', 'credits-light', 'credits-tablet']) assert.ok(statSync(join(dir, `${name}.png`)).size > 1000, name);
  const checks = JSON.parse(readFileSync(join(dir, 'checks.json'), 'utf8'));
  assert.deepEqual(checks.renders.map(r => r.name), [...RENDERS.map(r => r.name), 'queue-light', 'queue-tablet', 'credits-light', 'credits-tablet']);
  assert.deepEqual(checks.renders.slice(RENDERS.length).map(r => [r.result.pass, r.result.complete]), Array(4).fill([true, true]));
  assert.equal(checks.renders.find(r => r.name === 'queue-light').viewport.join('x'), '411x923');
  // Each state view really has its overlay open (a regression to the open-then-hidden race would leave it closed).
  for (const render of checks.renders.slice(RENDERS.length)) assert.deepEqual([render.name, render.result.modal.open, render.result.primary.status], [render.name, 1, 'skipped']);
  for (const render of checks.renders.slice(0, RENDERS.length)) assert.equal(render.result.modal.open, 0, render.name);
});

test('render fails clearly when a declared state has no window.proposalState', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, t => {
  const root = exploration(t);
  declareStates(root, 'window.somethingElse = () => {};\n');
  const done = spawnSync('node', [CLI, 'render', root], { encoding: 'utf8' });
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /^a: FAIL \(queue-light: proposalState is missing/m);
  const checks = JSON.parse(readFileSync(join(root, 'renders/a/checks.json'), 'utf8'));
  assert.equal(checks.pass, false);
});

test('render reports a state that throws instead of crashing', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, t => {
  const root = exploration(t);
  declareStates(root, 'window.proposalState = name => { throw new Error("no such " + name); };\n');
  const done = spawnSync('node', [CLI, 'render', root], { encoding: 'utf8' });
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /queue-light: proposalState\("queue"\) threw: no such queue/);
});

test('view.html opens a declared state from its picker and returns to Default', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, async t => {
  const root = exploration(t);
  declareStates(root, 'window.proposalState = name => document.getElementById(name).show();\n');
  assert.equal(spawnSync('node', [CLI, 'render', root], { encoding: 'utf8' }).status, 0);
  const browser = await Browser.launch(chrome);
  t.after(() => browser.close());
  const page = await browser.newPage();
  await page.setViewport(1400, 1000);
  await page.goto(pathToFileURL(join(root, 'renders/a/view.html')).href);
  const pressed = () => page.evaluate(() => [...document.querySelectorAll('#states button')].map(b => `${b.textContent}:${b.getAttribute('aria-pressed')}`));
  const sheetOpen = () => page.evaluate(async () => {
    await new Promise(resolve => setTimeout(resolve, 1500));
    return document.querySelector('iframe').contentDocument.getElementById('queue').matches(':popover-open');
  });
  assert.deepEqual(await pressed(), ['Default:true', 'queue:false', 'credits:false']);
  assert.equal(await sheetOpen(), false);
  await page.evaluate(() => document.querySelector('#states [data-state="queue"]').click());
  assert.deepEqual(await pressed(), ['Default:false', 'queue:true', 'credits:false']);
  assert.equal(await sheetOpen(), true);
  await page.evaluate(() => document.querySelector('#states [data-state=""]').click());
  assert.equal(await sheetOpen(), false);
});

// Width and height from a PNG's IHDR chunk.
const pngSize = file => { const head = readFileSync(file); return [head.readUInt32BE(16), head.readUInt32BE(20)]; };

test('render also writes light-full.png at the full document height, outside checks and views', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, t => {
  const root = exploration(t);
  const dir = join(root, 'project/proposals/a');
  writeFileSync(join(dir, 'style.css'), `${readFileSync(join(dir, 'style.css'), 'utf8')}\n.screen { min-height: 2000px; }\n`);
  const done = spawnSync('node', [CLI, 'render', root], { encoding: 'utf8' });
  assert.equal(done.status, 0, done.stderr);
  assert.match(done.stdout, /light-full\.png/);
  const out = join(root, 'renders/a');
  const [width, height] = pngSize(join(out, 'light-full.png'));
  assert.equal(width, 411);
  assert.ok(height >= 2000, `light-full.png is ${height}px tall`);
  // The viewport shots keep their size, and the extra capture is not a checked view.
  assert.deepEqual(pngSize(join(out, 'light.png')), [411, 923]);
  const checks = JSON.parse(readFileSync(join(out, 'checks.json'), 'utf8'));
  assert.deepEqual(checks.renders.map(r => r.name), RENDERS.map(r => r.name));
  assert.equal(checks.pass, true);
  assert.doesNotMatch(readFileSync(join(out, 'view.html'), 'utf8'), /light-full/);
});

test('light-full.png of a page shorter than the viewport is the viewport size', { skip: chrome ? false : 'Chrome not found; install it or set CHROME_PATH', timeout: 180000 }, t => {
  const root = exploration(t);
  assert.equal(spawnSync('node', [CLI, 'render', root], { encoding: 'utf8' }).status, 0);
  const [width, height] = pngSize(join(root, 'renders/a/light-full.png'));
  assert.deepEqual([width, height >= 923], [411, true]);
});
