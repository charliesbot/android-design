import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { help, parse } from '../../skills/android-design/gallery/cli.mjs';
import { workspace, writeProposal, STYLE } from './fixture.mjs';

const CLI = fileURLToPath(new URL('../../skills/android-design/gallery/cli.mjs', import.meta.url));
const run = (...args) => spawnSync('node', [CLI, ...args], { encoding: 'utf8' });

test('parse splits the command from its arguments and options', () => {
  assert.deepEqual(parse(['render', '/x', 'a', 'b']).rest, ['/x', 'a', 'b']);
  const { command, values } = parse(['create', '--project', '/repo', '--title', 'T', '--starter', 'android']);
  assert.equal(command, 'create');
  assert.deepEqual({ ...values }, { project: '/repo', title: 'T', starter: 'android' });
  assert.equal(parse(['cleanup', '/x', '--yes']).values.yes, true);
});

test('the help lists create, render, and cleanup only', () => {
  for (const word of ['create', 'render', 'cleanup', 'view.html', 'contact-sheet.png']) assert.match(help, new RegExp(word));
  for (const word of ['publish', 'serve', 'status', 'wait']) assert.doesNotMatch(help, new RegExp(`cli\\.mjs ${word}\\b`));
});

test('removed and malformed commands fail', () => {
  for (const args of [['publish', 'x', 'y'], ['serve', 'x'], ['render'], ['cleanup', 'x']]) assert.notEqual(run(...args).status, 0, args.join(' '));
});

test('render stops on a lint error with file:line and exit code 2, before starting Chrome', t => {
  const root = workspace(t);
  writeProposal(root, 'a', { style: `${STYLE}p {\n  color: #000;\n}\n` });
  writeProposal(root, 'b');
  const done = run('render', root);
  assert.equal(done.status, 2);
  assert.match(done.stderr, /proposals\/a\/style\.css:5: .*Raw color/);
  assert.doesNotMatch(done.stderr, /proposals\/b\//);
});

test('render rejects an unknown proposal id', t => {
  const root = workspace(t);
  writeProposal(root, 'a');
  const done = run('render', root, 'zzz');
  assert.equal(done.status, 1);
  assert.match(done.stderr, /Unknown proposal "zzz"\. Available: a/);
});
