import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { existsSync, lstatSync, mkdirSync, mkdtempSync, readFileSync, readdirSync, realpathSync, rmSync, symlinkSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { create, cleanup, MARKER, TEMPLATE } from '../../skills/android-design/gallery/workspace.mjs';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';

const git = (root, ...args) => spawnSync('git', ['-C', root, ...args], { encoding: 'utf8' });

function repo(t) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'workspace-test-')));
  t.after(() => rmSync(root, { recursive: true, force: true }));
  assert.equal(git(root, 'init', '-q').status, 0);
  const template = join(root, 'starter');
  mkdirSync(template);
  writeFileSync(join(template, 'settings.gradle.kts'), 'starter');
  return { root, template };
}

test('unique copies leave starter unchanged', t => {
  const { root, template } = repo(t);
  const first = create({ template, root });
  const second = create({ template, root });
  assert.notEqual(first, second);
  writeFileSync(join(first, 'project/settings.gradle.kts'), 'proposal');
  assert.equal(readFileSync(join(second, 'project/settings.gradle.kts'), 'utf8'), 'starter');
  assert.equal(readFileSync(join(template, 'settings.gradle.kts'), 'utf8'), 'starter');
});

test('generated artifacts are not copied', t => {
  const { root, template } = repo(t);
  for (const name of ['build', '.gradle', '.git', 'renders']) {
    mkdirSync(join(template, name));
    writeFileSync(join(template, name, 'artifact'), 'cache');
  }
  writeFileSync(join(template, 'local.properties'), 'machine-specific');
  const result = create({ template, root });
  for (const name of ['build', '.gradle', '.git', 'renders', 'local.properties']) {
    assert.equal(existsSync(join(result, 'project', name)), false, name);
  }
});

test('created exploration is readable by the gallery store', t => {
  const { root, template } = repo(t);
  const result = create({ template, root, title: 'Reading' });
  const data = new GalleryStore(result).read();
  assert.equal(data.title, 'Reading');
  assert.deepEqual(data.rounds, []);
  assert.equal(data.feedback.revision, 0);
  assert.throws(() => create({ template, root, title: '   ' }), /Title/);
  assert.throws(() => create({ template, root, title: 'x'.repeat(201) }), /Title/);
});

test('cleanup only removes marked workspace', t => {
  const { root, template } = repo(t);
  const result = create({ template, root });
  cleanup(result, root);
  assert.equal(existsSync(result), false);
  assert.equal(existsSync(template), true);
});

test('cleanup refuses unmarked root and nested paths', t => {
  const { root, template } = repo(t);
  const result = create({ template, root });
  const nested = join(result, 'nested');
  mkdirSync(nested);
  for (const path of [root, template, nested]) {
    assert.throws(() => cleanup(path, root));
    assert.equal(existsSync(path), true);
  }
});

test('cleanup refuses symlink and copied marker', t => {
  const { root, template } = repo(t);
  const result = create({ template, root });
  const alias = join(root, 'design-explorations/android-design-explore-alias');
  symlinkSync(result, alias, 'dir');
  assert.throws(() => cleanup(alias, root));
  const other = join(root, 'design-explorations/android-design-explore-other');
  mkdirSync(other);
  writeFileSync(join(other, MARKER), readFileSync(join(result, MARKER), 'utf8'));
  assert.throws(() => cleanup(other, root), /does not match/);
  assert.equal(existsSync(result), true);
});

test('cleanup does not follow internal symlinks', t => {
  const { root, template } = repo(t);
  const result = create({ template, root });
  symlinkSync(template, join(result, 'outside'), 'dir');
  cleanup(result, root);
  assert.equal(existsSync(join(template, 'settings.gradle.kts')), true);
});

test('local git exclusion and initial gallery files', t => {
  const { root, template } = repo(t);
  const result = create({ template, root });
  create({ template, root });
  assert.equal(dirname(result), join(root, 'design-explorations'));
  assert.equal(lstatSync(join(result, 'exploration.json')).isFile(), true);
  assert.equal(lstatSync(join(result, 'feedback.json')).isFile(), true);
  assert.equal(git(root, 'check-ignore', result).status, 0);
  assert.equal(existsSync(join(root, '.gitignore')), false);
  const exclude = readFileSync(join(root, '.git/info/exclude'), 'utf8');
  assert.equal(exclude.split('\n').filter(line => line === '/design-explorations/').length, 1);
});

test('exclude rule is appended after a missing trailing newline', t => {
  const { root, template } = repo(t);
  mkdirSync(join(root, '.git/info'), { recursive: true });
  writeFileSync(join(root, '.git/info/exclude'), '*.log');
  create({ template, root });
  assert.equal(readFileSync(join(root, '.git/info/exclude'), 'utf8'), '*.log\n/design-explorations/\n');
});

test('refuse non-repository, symlink storage and tracked storage', t => {
  const { root, template } = repo(t);
  const plain = realpathSync(mkdtempSync(join(tmpdir(), 'workspace-plain-')));
  t.after(() => rmSync(plain, { recursive: true, force: true }));
  assert.throws(() => create({ template, root: plain }), /existing Git repository/);
  const storage = join(root, 'design-explorations');
  symlinkSync(template, storage, 'dir');
  assert.throws(() => create({ template, root }), /symlink/);
  rmSync(storage);
  mkdirSync(storage);
  writeFileSync(join(storage, 'tracked'), 'keep');
  assert.equal(git(root, 'add', 'design-explorations/tracked').status, 0);
  assert.throws(() => create({ template, root }), /tracked/);
});

test('installed starter contains only portable inputs', () => {
  const forbidden = new Set(['build', '.gradle', '.kotlin', '.git', 'local.properties', 'renders', 'screenshotTestDebug', '__pycache__']);
  for (const entry of readdirSync(TEMPLATE, { recursive: true, withFileTypes: true })) {
    const path = join(entry.parentPath, entry.name);
    assert.equal(forbidden.has(entry.name), false, path);
    assert.equal(entry.isSymbolicLink(), false, path);
  }
  for (const name of ['app', 'wear', 'core/model', 'core/domain', 'core/data', 'core/strings', 'core/designsystem/common']) {
    assert.equal(existsSync(join(TEMPLATE, name, 'build.gradle.kts')), true, name);
  }
  for (const name of ['google_sans_flex', 'roboto_flex']) {
    const font = join(TEMPLATE, 'core/designsystem/common/src/main/res/font', `${name}.ttf`);
    assert.ok(lstatSync(font).size > 1000, font);
  }
});

test('four explicit logical device canvases', () => {
  const source = readFileSync(join(TEMPLATE, 'app/src/main/kotlin/com/example/designsandbox/ProposalScreen.kt'), 'utf8');
  for (const [width, height, dpi] of [[411, 923, 420], [1280, 800, 320], [1440, 900, 320], [704, 933, 420]]) {
    assert.ok(source.includes(`spec:width=${width}dp,height=${height}dp,dpi=${dpi}`));
  }
  for (const name of ['Phone', 'Tablet', 'Desktop', 'Foldable']) assert.ok(source.includes(`fun Proposal${name}Preview()`));
});
