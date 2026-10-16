import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, realpathSync, rmSync, statSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { chromeExecutable } from '../../skills/android-design/gallery/cdp.mjs';
import { RENDERS } from '../../skills/android-design/gallery/render.mjs';

const CLI = fileURLToPath(new URL('../../skills/android-design/gallery/cli.mjs', import.meta.url));
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
