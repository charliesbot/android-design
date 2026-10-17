import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, mkdirSync, mkdtempSync, readFileSync, realpathSync, rmSync, symlinkSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { collect, main } from '../../skills/android-design-explore/assets/exploration-starter/render.mjs';

function temp(t) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'render-test-')));
  t.after(() => rmSync(root, { recursive: true, force: true }));
  return root;
}

test('missing images fail', t => {
  const root = temp(t);
  assert.throws(() => collect(join(root, 'missing'), join(root, 'renders')), /No screenshot previews/);
});

test('same filenames in different packages are preserved', t => {
  const root = temp(t);
  const source = join(root, 'reference');
  for (const name of ['a', 'b']) {
    mkdirSync(join(source, name), { recursive: true });
    writeFileSync(join(source, name, 'proposal.png'), name);
  }
  const output = join(root, 'renders');
  t.mock.method(console, 'log', () => {});
  collect(source, output);
  for (const name of ['a', 'b']) assert.equal(readFileSync(join(output, name, 'proposal.png'), 'utf8'), name);
});

test('symlink destination is rejected', t => {
  const root = temp(t);
  const output = join(root, 'renders');
  symlinkSync(root, output, 'dir');
  assert.throws(() => collect(join(root, 'reference'), output), /symlink/);
});

test('failed build removes stale outputs and reports unverified', t => {
  const root = temp(t);
  const output = join(root, 'renders/app');
  const references = join(root, 'app/src/screenshotTestDebug/reference');
  for (const folder of [output, references]) {
    mkdirSync(folder, { recursive: true });
    writeFileSync(join(folder, 'old.png'), 'stale');
  }
  const calls = [];
  const errors = [];
  t.mock.method(console, 'error', message => errors.push(message));
  const code = main(['app'], { root, run: (...call) => { calls.push(call); return { status: 1 }; } });
  assert.equal(code, 1);
  assert.equal(existsSync(output), false);
  assert.equal(existsSync(references), false);
  assert.equal(calls.length, 1);
  assert.equal(calls[0][0], join(root, 'gradlew'));
  assert.deepEqual(calls[0][1], ['--no-daemon', '--console=plain', ':app:updateDebugScreenshotTest']);
  assert.equal(calls[0][2].cwd, root);
  assert.equal(calls[0][2].stdio, 'inherit');
  assert.match(errors[0], /^Render failed; visual quality unverified: /);
});

test('successful build collects references into renders', t => {
  const root = temp(t);
  t.mock.method(console, 'log', () => {});
  const code = main(['wear'], {
    root,
    run: () => {
      const reference = join(root, 'wear/src/screenshotTestDebug/reference/pkg');
      mkdirSync(reference, { recursive: true });
      writeFileSync(join(reference, 'x.png'), 'png');
      return { status: 0 };
    },
  });
  assert.equal(code, 0);
  assert.equal(readFileSync(join(root, 'renders/wear/pkg/x.png'), 'utf8'), 'png');
});
