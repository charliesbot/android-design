import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { existsSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';
import { help, openCommand, openView, parse } from '../../skills/android-design-explore/gallery/cli.mjs';
import { workspace, writeProposal, STYLE } from './fixture.mjs';

const CLI = fileURLToPath(new URL('../../skills/android-design-explore/gallery/cli.mjs', import.meta.url));
const run = (...args) => spawnSync('node', [CLI, ...args], { encoding: 'utf8' });

test('parse splits the command from its arguments and options', () => {
  assert.deepEqual(parse(['render', '/x']).rest, ['/x']);
  assert.equal(parse(['render', '/x']).command, 'render');
  assert.equal(parse(['render', '/x', '--open']).values.open, true);
  assert.equal(parse(['--help']).values.help, true);
});

test('the help lists the render command and its outputs only', () => {
  for (const word of ['render', 'view.html', 'review.png', 'light-full.png', 'checks.json', 'CHROME_PATH']) assert.match(help, new RegExp(word));
  for (const word of ['create', 'cleanup', 'publish', 'serve', 'status', 'wait']) assert.doesNotMatch(help, new RegExp(`cli\\.mjs ${word}\\b`));
  for (const word of ['contact-sheet', 'p-states', 'proposalState']) assert.doesNotMatch(help, new RegExp(word));
});

test('--help prints the help and removed or malformed commands fail', () => {
  const helped = run('--help');
  assert.equal(helped.status, 0);
  assert.match(helped.stdout, /cli\.mjs render <proposal-folder>/);
  for (const args of [['create', '--project', '/x'], ['cleanup', 'x', '--yes'], ['publish', 'x', 'y'], ['serve', 'x'], ['render'], ['render', 'a', 'b']]) assert.notEqual(run(...args).status, 0, args.join(' '));
});

test('render stops on a lint error with file:line and exit code 2, before starting Chrome', t => {
  const folder = writeProposal(join(workspace(t), 'a'), { style: `${STYLE}p {\n  color: #000;\n}\n` });
  const done = run('render', folder);
  assert.equal(done.status, 2);
  assert.match(done.stderr, /style\.css:5: .*Raw color/);
  assert.equal(existsSync(join(folder, 'renders')), false);
});

test('render needs an existing folder with an index.html', t => {
  const root = workspace(t);
  const missing = run('render', join(root, 'nope'));
  assert.equal(missing.status, 1);
  assert.match(missing.stderr, /Not a proposal folder/);
  mkdirSync(join(root, 'empty'));
  const empty = run('render', join(root, 'empty'));
  assert.equal(empty.status, 2);
  assert.match(empty.stderr, /index\.html: missing/);
});

test('openCommand picks the platform opener', () => {
  assert.deepEqual(openCommand('/r/view.html', 'darwin'), ['open', ['/r/view.html']]);
  assert.deepEqual(openCommand('/r/view.html', 'linux'), ['xdg-open', ['/r/view.html']]);
  assert.deepEqual(openCommand('C:\\r\\view.html', 'win32'), ['explorer.exe', ['C:\\r\\view.html']]);
});

test('openView opens the rendered view.html detached', () => {
  const calls = [];
  const spawn = (cmd, args, options) => { calls.push({ cmd, args, options }); return { unref() {}, on() {} }; };
  const opened = openView({ dir: '/p/renders' }, { platform: 'darwin', spawn });
  assert.equal(opened, join('/p/renders', 'view.html'));
  assert.deepEqual(calls.map(c => [c.cmd, c.args]), [['open', ['/p/renders/view.html']]]);
  assert.ok(calls.every(c => c.options.detached && c.options.stdio === 'ignore'));
});
