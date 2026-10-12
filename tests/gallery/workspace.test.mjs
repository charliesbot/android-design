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
  mkdirSync(join(template, 'proposals/a'), { recursive: true });
  writeFileSync(join(template, 'proposals/a/index.html'), 'starter');
  return { root, template };
}

test('unique copies leave starter unchanged', t => {
  const { root, template } = repo(t);
  const first = create({ template, root });
  const second = create({ template, root });
  assert.notEqual(first, second);
  writeFileSync(join(first, 'project/proposals/a/index.html'), 'proposal');
  assert.equal(readFileSync(join(second, 'project/proposals/a/index.html'), 'utf8'), 'starter');
  assert.equal(readFileSync(join(template, 'proposals/a/index.html'), 'utf8'), 'starter');
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
  assert.equal(existsSync(join(template, 'proposals/a/index.html')), true);
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

test('installed starter is the HTML proposal starter and contains only portable inputs', () => {
  const forbidden = new Set(['build', '.gradle', '.kotlin', '.git', 'local.properties', 'renders', 'node_modules', '__pycache__']);
  for (const entry of readdirSync(TEMPLATE, { recursive: true, withFileTypes: true })) {
    const path = join(entry.parentPath, entry.name);
    assert.equal(forbidden.has(entry.name), false, path);
    assert.equal(entry.isSymbolicLink(), false, path);
  }
  for (const name of ['README.md', 'proposals/a/index.html', 'proposals/a/style.css']) {
    assert.equal(lstatSync(join(TEMPLATE, name)).isFile(), true, name);
  }
  const page = readFileSync(join(TEMPLATE, 'proposals/a/index.html'), 'utf8');
  for (const part of ['/runtime/runtime.css', '/runtime/m3e.js', '<m3e-theme', 'data-primary', '<p-floating-toolbar', '<p-icon', 'class="t-headline']) {
    assert.ok(page.includes(part), part);
  }
  assert.ok(readFileSync(join(TEMPLATE, 'proposals/a/style.css'), 'utf8').includes('min-width: 840px'));
});

test('create copies the starter without the Android project', t => {
  const { root } = repo(t);
  const result = create({ root });
  assert.equal(existsSync(join(result, 'project/proposals/a/index.html')), true);
  assert.equal(existsSync(join(result, 'project/settings.gradle.kts')), false);
  assert.equal(existsSync(join(result, 'project/app')), false);
});

const MAPPING = readFileSync(fileURLToPath(new URL('../../skills/android-design/references/compose-mapping.md', import.meta.url)), 'utf8');
const VARIANT_ATTRIBUTES = new Set(['variant', 'size', 'mode', 'shape', 'orientation', 'width', 'toggle', 'extended', 'vertical', 'multi', 'actionable']);
// m3e-toolbar is only the wrapper inside p-floating-toolbar, which the verified p-floating-toolbar row covers.
const COVERED_BY_PARENT = new Map([['m3e-toolbar', 'p-floating-toolbar']]);
const EXEMPT = new Set(['m3e-theme', 'p-icon']);

function attributesOf(text) {
  const attributes = new Map();
  for (const [, name, quoted] of text.matchAll(/([a-z][\w-]*)(?:="([^"]*)")?/g)) attributes.set(name, quoted ?? true);
  return attributes;
}

function verifiedRows() {
  const components = MAPPING.split('\n## ')[1];
  return components.split('\n').filter(line => line.startsWith('|')).map(line => line.split('|').slice(1, -1).map(cell => cell.trim()))
    .filter(cells => cells[2] === 'verified')
    .map(([html, , , notes]) => {
      const [self, context] = html.match(/`[^`]+`/g).map(span => span.slice(1, -1));
      const [tag, ...rest] = self.split(' ');
      const inside = context ? context.split(' ') : null;
      return { tag, attributes: attributesOf(rest.join(' ')), inside: inside && { tag: inside[0], attributes: attributesOf(inside.slice(1).join(' ')) }, notes };
    });
}

function elementsOf(html) {
  const stack = [];
  const elements = [];
  for (const [, closing, tag, rest] of html.matchAll(/<(\/?)((?:m3e|p)-[a-z0-9-]+)([^>]*)>/g)) {
    if (closing) { stack.pop(); continue; }
    elements.push({ tag, attributes: attributesOf(rest), ancestors: [...stack] });
    stack.push({ tag, attributes: attributesOf(rest) });
  }
  return elements;
}

function matches(row, element) {
  if (row.tag !== element.tag) return false;
  for (const [name, value] of row.attributes) if (element.attributes.get(name) !== value) return false;
  for (const [name, value] of element.attributes) {
    if (!VARIANT_ATTRIBUTES.has(name) || row.attributes.has(name)) continue;
    if (!new RegExp(`\\b${value === true ? name : value}\\b`).test(row.notes)) return false;
  }
  if (row.inside) {
    return element.ancestors.some(ancestor => ancestor.tag === row.inside.tag && [...row.inside.attributes].every(([name, value]) => ancestor.attributes.get(name) === value));
  }
  return true;
}

test('every component in the HTML starter matches a verified mapping row', () => {
  const rows = verifiedRows();
  assert.ok(rows.length >= 5);
  const unverified = [];
  for (const element of elementsOf(readFileSync(join(TEMPLATE, 'proposals/a/index.html'), 'utf8'))) {
    if (EXEMPT.has(element.tag)) continue;
    if (COVERED_BY_PARENT.has(element.tag) && element.ancestors.at(-1)?.tag === COVERED_BY_PARENT.get(element.tag)) continue;
    if (!rows.some(row => matches(row, element))) {
      unverified.push(`<${element.tag} ${[...element.attributes.keys()].filter(name => VARIANT_ATTRIBUTES.has(name)).map(name => `${name}=${element.attributes.get(name)}`).join(' ')}>`);
    }
  }
  assert.deepEqual(unverified, []);
});

test('create --starter android copies the Compose project with the same skip list', t => {
  const { root } = repo(t);
  const result = create({ root, starter: 'android' });
  assert.equal(existsSync(join(result, 'project/settings.gradle.kts')), true);
  assert.equal(existsSync(join(result, 'project/app/build.gradle.kts')), true);
  assert.equal(existsSync(join(result, 'project/proposals')), false);
  for (const name of ['build', '.gradle', '.git', 'renders', 'local.properties']) {
    assert.equal(existsSync(join(result, 'project', name)), false, name);
  }
  assert.equal(new GalleryStore(result).read().rounds.length, 0);
});

test('create rejects an unknown starter', t => {
  const { root } = repo(t);
  assert.throws(() => create({ root, starter: 'compose' }), /starter/);
  assert.equal(existsSync(join(root, 'design-explorations')), false);
});

test('the CLI selects the starter and validates the value', t => {
  const { root } = repo(t);
  const cli = fileURLToPath(new URL('../../skills/android-design/gallery/cli.mjs', import.meta.url));
  const run = (...args) => spawnSync('node', [cli, 'create', '--project', root, ...args], { encoding: 'utf8' });
  const html = run();
  assert.equal(html.status, 0, html.stderr);
  assert.equal(existsSync(join(html.stdout.trim(), 'project/proposals/a/index.html')), true);
  const android = run('--starter', 'android');
  assert.equal(android.status, 0, android.stderr);
  assert.equal(existsSync(join(android.stdout.trim(), 'project/settings.gradle.kts')), true);
  const bad = run('--starter', 'wear');
  assert.notEqual(bad.status, 0);
  assert.match(bad.stderr, /--starter/);
});
